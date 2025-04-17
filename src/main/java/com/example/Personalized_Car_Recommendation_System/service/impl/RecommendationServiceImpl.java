package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.ImCarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.CarRecommendationDto;
import com.example.Personalized_Car_Recommendation_System.entity.*;
import com.example.Personalized_Car_Recommendation_System.repository.*;
import org.apache.hadoop.shaded.org.apache.commons.math3.linear.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;


import org.apache.spark.rdd.RDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    @Autowired
    private RecommendationHistoryRepository recommendationHistoryRepository;
    @Autowired
    private CarInfoRepository carInfoRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private CarBrandRepository carBrandRepository;
    @Autowired
    private DefaultRecommendationRepository defaultRecommendationRepository;
    private static final String RECOMMENDATION_SQL = "SELECT ci.car_id, b.name, ci.full_name AS fullName, " +
            "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
            "AVG(rh.score) AS avgScore " +
            "FROM recommendation_history rh " +
            "JOIN car_info ci ON rh.car_id = ci.car_id " +
            "JOIN car_brand b ON ci.brand_id = b.brand_id " +
            "WHERE rh.user_id = ? " +
            "AND (b.name LIKE ? OR ci.full_name LIKE ?) " +
            "GROUP BY ci.car_id, b.name, ci.full_name, ci.minprice, ci.maxprice " +
            "ORDER BY avgScore DESC " +
            "LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(DISTINCT ci.car_id) " +
            "FROM recommendation_history rh " +
            "JOIN car_info ci ON rh.car_id = ci.car_id " +
            "JOIN car_brand b ON ci.brand_id = b.brand_id " +
            "WHERE rh.user_id = ? " +
            "AND (b.name LIKE ? OR ci.full_name LIKE ?)";

    private static final String ALL_RECOMMENDATION_SQL = "SELECT ci.car_id, b.name, ci.full_name AS fullName, " +
            "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
            "AVG(rh.score) AS avgScore " +
            "FROM recommendation_history rh " +
            "JOIN car_info ci ON rh.car_id = ci.car_id " +
            "JOIN car_brand b ON ci.brand_id = b.brand_id " +
            "WHERE rh.user_id = ? " +
            "GROUP BY ci.car_id, b.name, ci.full_name, ci.minprice, ci.maxprice " +
            "ORDER BY avgScore DESC";

    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private final CarRepository carRepository;

    @Value("${hadoop.home.dir}")
    private String hadoopHomeDir;
    @Value("${als.rank:10}")
    private int alsRank;
    @Value("${als.numIterations:10}")
    private int alsNumIterations;

    @Autowired
    public RecommendationServiceImpl(ChatClient chatClient, JdbcTemplate jdbcTemplate,UserRepository userRepository,
                                     RecommendationHistoryRepository recommendationHistoryRepository,
                                     CarRepository carRepository) {
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.recommendationHistoryRepository = recommendationHistoryRepository;
        this.carRepository = carRepository;
    }

    /**
     * 从令牌中获取用户ID
     * @param token 用户的令牌
     * @return 用户ID
     */
    @Override
    public int getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            logger.error("Token 为空，无法解析");
            throw new IllegalArgumentException("Token 为空，无法解析");
        }
        token = token.trim().replace("Bearer ", "");
        logger.info("开始解析 Token: {}", token);
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                logger.error("Token 解析结果为空，无法获取用户 ID");
                throw new IllegalArgumentException("Token 解析失败，无法获取用户 ID");
            }
            logger.debug("成功解析 Token，用户 ID: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Token 解析异常: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token 解析失败", e);
        }
    }

    /**
     * 异步调用AI接口
     * @param message 提示信息
     * @return 异步的AI响应
     */
    @Async
    @Override
    public CompletableFuture<String> callAI(String message) {
        try {
            Prompt prompt = new Prompt(new UserMessage(message));
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            logger.info("AI响应内容: {}", response);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            logger.error("AI 调用异常: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture("AI服务异常，请稍后重试");
        }
    }

    /**
     * 根据用户ID获取推荐信息
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 推荐信息列表
     */
    @Override
    public Map<String, Object> getRecommendations(int userId, int page, int size, String keyword) {
        String searchKeyword = keyword == null ? "%" : "%" + keyword + "%";
        int offset = (page - 1) * size;

        try {
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(RECOMMENDATION_SQL, userId, searchKeyword, searchKeyword, size, offset);
            List<CarRecommendationDto> carRecommendations = resultList.stream()
                    .map(row -> {
                        Integer carId = (Integer) row.get("car_id");
                        String brandName = (String) row.get("name");
                        String fullName = (String) row.get("fullName");
                        String priceRange = (String) row.get("priceRange");
                        Float avgScore = ((Number) row.get("avgScore")).floatValue();
                        return new CarRecommendationDto(carId, brandName, fullName, priceRange, avgScore);
                    })
                    .collect(Collectors.toList());

            int total = jdbcTemplate.queryForObject(COUNT_SQL, Integer.class, userId, searchKeyword, searchKeyword);

            Map<String, Object> response = new HashMap<>();
            response.put("data", carRecommendations);
            response.put("total", total);
            return response;
        } catch (Exception e) {
            logger.error("查询推荐汽车列表时出错: {}", e.getMessage(), e);
            throw new RuntimeException("查询推荐汽车列表时出错", e);
        }
    }

    @Override
    public Map<String, Object> getAllRecommendations(int userId) {
        try {
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(ALL_RECOMMENDATION_SQL, userId);
            List<CarRecommendationDto> carRecommendations = resultList.stream()
                    .map(row -> {
                        Integer carId = (Integer) row.get("car_id");
                        String brandName = (String) row.get("name");
                        String fullName = (String) row.get("fullName");
                        String priceRange = (String) row.get("priceRange");
                        Float avgScore = ((Number) row.get("avgScore")).floatValue();
                        return new CarRecommendationDto(carId, brandName, fullName, priceRange, avgScore);
                    })
                    .collect(Collectors.toList());

            int total = jdbcTemplate.queryForObject(COUNT_SQL, Integer.class, userId, "%", "%");

            Map<String, Object> response = new HashMap<>();
            response.put("data", carRecommendations);
            response.put("total", total);
            return response;
        } catch (Exception e) {
            logger.error("查询全部推荐汽车列表时出错: {}", e.getMessage(), e);
            throw new RuntimeException("查询全部推荐汽车列表时出错", e);
        }
    }



    /**
     * 混合推荐方法
     * @param userId 用户ID
     * @param data 调用 /api/ai/recommend 接口返回的数据
     * @param maxPrice 用户输入的最高价格
     * @return 过滤后的数据
     */
    @Override
    public List<Map<String, Object>> getAlsRecommendations(int userId, List<Map<String, Object>> data, int maxPrice) {
        JavaSparkContext sc = null;
        try {
            sc = createSparkContext();

            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            logger.info("推荐历史记录{}",allHistory);
            if (allHistory.isEmpty()) {
                logger.info("没有推荐历史记录，返回默认推荐{}",getDefaultRecommendations());
                return getDefaultRecommendations();
            }
            logger.info("所有用户的推荐历史记录: {}", allHistory);

            JavaRDD<Rating> ratingsRDD = convertToRatingsRDD(sc, allHistory);
            Map<Integer, Long> carIdCounts = ratingsRDD.mapToPair(r -> new Tuple2<>(r.product(), 1L))
                    .reduceByKey(Long::sum)
                    .collectAsMap();
            logger.info("CarID 分布: {}", carIdCounts);

            JavaRDD<Rating>[] splits = ratingsRDD.randomSplit(new double[]{0.8, 0.2}, 12345L);
            JavaRDD<Rating> training = splits[0];
            JavaRDD<Rating> test = splits[1];

            MatrixFactorizationModel model = trainALSModel(training);
            if (model == null) {
                logger.info("无法训练 ALS 模型，返回默认推荐");
                return getDefaultRecommendations();
            }

            double mse = calculateMSE(model, test);
            logger.info("模型的均方误差 (MSE): {}", mse);

            double mseThreshold = 1.0;
            if (mse > mseThreshold) {
                logger.info("模型性能不佳，MSE 超过阈值，返回默认推荐");
                return getDefaultRecommendations();
            }

            List<Integer> carIds = extractCarIds(data);
            if (carIds.isEmpty()) {
                logger.info("没有有效的汽车 ID，返回默认推荐");
                return getDefaultRecommendations();
            }
            logger.info("所有汽车的 ID: {}", carIds);

            JavaRDD<Tuple2<Object, Object>> inputRDD = createInputRDD(sc, userId, carIds);
            logger.info("预测的 (用户 ID, 物品 ID) 元组列表: {}", inputRDD);

            JavaRDD<Rating> userCarRatingsRDD = predictRatings(model, inputRDD);
            logger.info("模型预测结果: {}", userCarRatingsRDD);

            List<Rating> userCarRatings = userCarRatingsRDD.collect();
            logger.info("预测结果列表: {}", userCarRatings);

            userCarRatings = new ArrayList<>(userCarRatings);
            userCarRatings.sort((r1, r2) -> Double.compare(r2.rating(), r1.rating()));
            logger.info("排序预测结果: {}", userCarRatings);

            List<Map<String, Object>> recommendedCars = filterRecommendedCars(data, userCarRatings, maxPrice);
            if (recommendedCars.isEmpty()) {
                logger.info("没有推荐的汽车，返回默认推荐");
                return getDefaultRecommendations();
            }
            logger.info("筛选预测结果: {}", recommendedCars);
            logger.info("最终推荐结果: {}", recommendedCars);

            return recommendedCars;
        } catch (Exception e) {
            logger.error("ALS 推荐过程中出现异常: {}", e.getMessage(), e);
            return getDefaultRecommendations();
        } finally {
            if (sc != null) {
                sc.stop();
            }
        }
    }

    // 计算均方误差（MSE）的方法
    private double calculateMSE(MatrixFactorizationModel model, JavaRDD<Rating> testData) {
        JavaRDD<Tuple2<Object, Object>> userProducts = testData.map(rating -> new Tuple2<>(rating.user(), rating.product()));
        JavaRDD<Rating> predictions = predictRatings(model, userProducts);

        JavaRDD<Tuple2<Double, Double>> ratesAndPreds = testData.mapToPair(rating -> new Tuple2<>(new Tuple2<>(rating.user(), rating.product()), rating.rating()))
                .join(predictions.mapToPair(rating -> new Tuple2<>(new Tuple2<>(rating.user(), rating.product()), rating.rating())))
                .values();

        double mse = ratesAndPreds.mapToDouble(pair -> {
            double err = pair._1() - pair._2();
            return err * err;
        }).mean();

        logger.info("模型在测试集上计算得到的均方误差 (MSE) 为: {}", mse);
        return mse;
    }

    private JavaSparkContext createSparkContext() {
        System.setProperty("hadoop.home.dir", hadoopHomeDir);
        String jvmOptions = String.join(" ",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.net=ALL-UNNAMED",
                "--add-opens=java.base/java.nio=ALL-UNNAMED",
                "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
                "-Xss4m"
        );

        SparkConf conf = new SparkConf()
                .setAppName("CarRecommendationALS")
                .setMaster("local[*]")
                .set("spark.driver.extraJavaOptions", jvmOptions)
                .set("spark.executor.extraJavaOptions", jvmOptions)
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.kryo.registrationRequired", "false")
                .registerKryoClasses(new Class<?>[] {
                        Rating.class,
                        Tuple2.class,
                        CarInfo.class,
                        RecommendationHistory.class,
                        CarRecommendationDto.class,
                        ImCarDetailsDto.class
                });

        return new JavaSparkContext(conf);
    }

    private JavaRDD<Rating> convertToRatingsRDD(JavaSparkContext sc, List<RecommendationHistory> allHistory) {
        return sc.parallelize(allHistory.stream()
                .map(history -> new Rating(history.getUserId(), history.getCarId(), history.getScore()))
                .collect(Collectors.toList()));
    }

    private MatrixFactorizationModel trainALSModel(JavaRDD<Rating> ratingsRDD) {
        if (ratingsRDD.isEmpty()) {
            logger.info("Ratings RDD 为空，无法训练 ALS 模型");
            return null;
        }
        return ALS.train(ratingsRDD.rdd(), alsRank, alsNumIterations, 0.01);
    }

    private List<Integer> extractCarIds(List<Map<String, Object>> data) {
        return data.stream()
                .limit(1000)
                .map(carMap -> (Integer) carMap.get("carId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private JavaRDD<Tuple2<Object, Object>> createInputRDD(JavaSparkContext sc, int userId, List<Integer> carIds) {
        List<Tuple2<Integer, Integer>> userCarPairs = carIds.stream()
                .map(singleCarId -> new Tuple2<>(userId, singleCarId))
                .collect(Collectors.toList());
        JavaPairRDD<Integer, Integer> userCarPairsRDD = sc.parallelizePairs(userCarPairs);
        return userCarPairsRDD.map(pair -> new Tuple2<>(pair._1(), pair._2()));
    }

    private JavaRDD<Rating> predictRatings(MatrixFactorizationModel model, JavaRDD<Tuple2<Object, Object>> inputRDD) {
        RDD<Rating> predictedRatingsRDD = model.predict(inputRDD.rdd());
        ClassTag<Rating> ratingClassTag = ClassTag$.MODULE$.apply(Rating.class);
        return JavaRDD.fromRDD(predictedRatingsRDD, ratingClassTag);
    }

    private List<Map<String, Object>> filterRecommendedCars(List<Map<String, Object>> data, List<Rating> userRatings, int userMaxPrice) {
        Map<Integer, Map<String, Object>> carMap = data.stream()
                .filter(car -> car.get("carId") != null)
                .collect(Collectors.toMap(
                        car -> (Integer) car.get("carId"),
                        Function.identity()
                ));

        List<Integer> carIds = userRatings.stream()
                .map(Rating::product)
                .toList();
        String carInfoSql = "SELECT car_id, minprice, maxprice FROM car_info WHERE car_id IN (" +
                carIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Map<String, Object>> carInfoList = jdbcTemplate.queryForList(carInfoSql);

        return userRatings.stream()
                .sorted((r1, r2) -> Double.compare(r2.rating(), r1.rating()))
                .map(Rating::product)
                .distinct()
                .filter(carId -> {
                    Map<String, Object> carInfo = carInfoList.stream()
                            .filter(info -> carId.equals(info.get("car_id")))
                            .findFirst()
                            .orElse(null);
                    return carInfo != null &&
                            carInfo.get("maxprice") != null &&
                            (Integer) carInfo.get("maxprice") <= userMaxPrice;
                })
                .map(carMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 按热度排序，处理空指针
    private List<Map<String, Object>> sortByPopularity(List<Map<String, Object>> cars) {
        return cars.stream()
                .sorted((a, b) -> {
                    Integer popularityA = a.get("popularity") != null ? (Integer) a.get("popularity") : 0;
                    Integer popularityB = b.get("popularity") != null ? (Integer) b.get("popularity") : 0;
                    return popularityB.compareTo(popularityA);
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成显式推荐
     * @param rank 显式推荐的秩
     * @param iterations 迭代次数
     * @param lambda 正则化参数
     * @param maxPrice 用户输入的最高价格
     * @return 显式推荐结果
     */
    private static final int MAX_RECOMMENDATIONS = 10;
    @Cacheable(value = "recommendations", key = "#rank+#iterations+#lambda+#maxPrice+#userId")
    public List<CarDetailsDto> generateExplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            Integer userId) {

        // 获取当前用户信息
        User currentUser = userRepository.findByUserId(userId);
        if (currentUser == null) {
            logger.error("未找到用户 ID 为 " + userId + " 的用户信息");
            return Collections.emptyList();
        }
        logger.info("当前用户信息：年龄={}, 地区={}", currentUser.getAge(), currentUser.getRegion());

        // 先尝试按地区和年龄匹配相似用户
        List<Long> similarUserIds = userRepository.findUserIdsByRegionAndAgeBetween(
                currentUser.getRegion(),
                currentUser.getAge() - 5,
                currentUser.getAge() + 5
        );
        similarUserIds.remove((long) userId);

        if (similarUserIds.isEmpty()) {
            // 如果没有相似地区的用户，按年龄匹配
            logger.info("未找到相似地区的用户，尝试按年龄匹配");
            similarUserIds = userRepository.findUserIdsByAgeBetween(
                    currentUser.getAge() - 5,
                    currentUser.getAge() + 5
            );
            similarUserIds.remove((long) userId);
        }

        logger.info("相似用户 ID 列表：{}", similarUserIds);

        // 根据相似用户的历史记录查找汽车信息
        List<CarDetailsDto> recommendedCars = new ArrayList<>();
        if (!similarUserIds.isEmpty()) {
            List<CarDetailsDto> carsFromHistory = carRepository.findCarsHistoryByUsers(similarUserIds, maxPrice).stream()
                    .map(carInfo -> {
                        String brandName = carRepository.getBrandNameByBrandId(carInfo.getBrandId());
                        CarDetailsDto dto = new CarDetailsDto(
                                carInfo.getId(),
                                brandName,
                                carInfo.getFullName(),
                                carInfo.getMinPrice(),
                                carInfo.getMaxPrice()
                        );
                        // 计算推荐分数，这里简单结合 rank、iterations 和 lambda
                        double rawScore = (double) rank * iterations / (carInfo.getMaxPrice() + lambda);
                        double normalizedScore = normalizeScore(rawScore);
                        dto.setRecommendationScore(normalizedScore);
                        return dto;
                    })
                    .collect(Collectors.toList());
            recommendedCars.addAll(carsFromHistory);
        } else {
            logger.info("未找到相似用户，无法生成推荐");
        }

        // 按推荐分数排序
        recommendedCars.sort((dto1, dto2) -> Double.compare(dto2.getRecommendationScore(), dto1.getRecommendationScore()));

        // 去除重复品牌
        Set<String> uniqueBrands = new HashSet<>();
        List<CarDetailsDto> Recommendations = new ArrayList<>();
        for (CarDetailsDto dto : recommendedCars) {
            if (uniqueBrands.add(dto.getName())) {
                Recommendations.add(dto);
                if (Recommendations.size() >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        }
        logger.info("最终推荐结果数量：{}", Recommendations.size());
        logger.info("最终推荐结果：{}", Recommendations);
        return Recommendations;
    }
    private double normalizeScore(double rawScore) {
         rawScore = rawScore * 3000;
        return rawScore;
    }
    /**
     * 生成隐式推荐
     * @param rank 隐式推荐的秩
     * @param iterations 迭代次数
     * @param lambda 正则化参数
     * @param maxPrice 用户输入的最高价格
     * @return 隐式推荐结果
     */

    @Cacheable(value = "implicitRecommendations", key = "#rank+#iterations+#lambda+#maxPrice+#userId")
    public List<ImCarDetailsDto> generateImplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            int userId
    ) {

        logger.info("开始生成隐式推荐，用户ID: {}, 最大价格: {}", userId, maxPrice);

        try {
            // 1. 加载数据
            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            List<CarInfo> carInfos = carInfoRepository.findAll();

            if (allHistory.isEmpty() || carInfos.isEmpty()) {
                logger.warn("数据不足，返回默认推荐");
                return ImgetDefaultRecommendations();
            }

            // 2. 构建正确的评分矩阵
            RatingMatrixBuilder.RatingMatrix matrix = new RatingMatrixBuilder()
                    .setHistories(allHistory)
                    .setCarInfos(carInfos)
                    .build();

            // 3. 检查目标用户是否存在
            if (!matrix.userIndexMap.containsKey(userId)) {
                logger.warn("用户ID {} 不存在于历史数据中", userId);
                return ImgetDefaultRecommendations();
            }

            // 4. 执行矩阵分解
            MatrixFactorizationResult result = performALS(
                    matrix.matrix,
                    rank,
                    iterations,
                    lambda
            );

            // 5. 获取目标用户的预测评分
            int userIndex = matrix.userIndexMap.get(userId);
            double[] userPredictions = result.getUserFeatures().getRow(userIndex);

            // 6. 生成推荐结果
            return generateRecommendations(
                    userPredictions,
                    carInfos,
                    matrix.carIndexMap,
                    maxPrice
            );

        } catch (Exception e) {
            logger.error("生成隐式推荐时发生错误", e);
            return ImgetDefaultRecommendations();
        }
    }

    // 评分矩阵构建器
    private static class RatingMatrixBuilder {
        private List<RecommendationHistory> histories;
        private List<CarInfo> carInfos;

        public RatingMatrixBuilder setHistories(List<RecommendationHistory> histories) {
            this.histories = histories;
            return this;
        }

        public RatingMatrixBuilder setCarInfos(List<CarInfo> carInfos) {
            this.carInfos = carInfos;
            return this;
        }

        public RatingMatrix build() {
            // 创建用户ID到矩阵索引的映射
            Map<Integer, Integer> userIndexMap = new HashMap<>();
            int userIndex = 0;
            for (RecommendationHistory history : histories) {
                if (!userIndexMap.containsKey(history.getUserId())) {
                    userIndexMap.put(history.getUserId(), userIndex++);
                }
            }

            // 创建汽车ID到矩阵索引的映射
            Map<Integer, Integer> carIndexMap = new HashMap<>();
            int carIndex = 0;
            for (CarInfo car : carInfos) {
                if (!carIndexMap.containsKey(car.getId())) {
                    carIndexMap.put(car.getId(), carIndex++);
                }
            }

            // 初始化评分矩阵
            RealMatrix matrix = MatrixUtils.createRealMatrix(
                    userIndexMap.size(),
                    carIndexMap.size()
            );

            // 填充评分矩阵
            for (RecommendationHistory history : histories) {
                Integer uIdx = userIndexMap.get(history.getUserId());
                Integer cIdx = carIndexMap.get(history.getCarId());
                if (uIdx != null && cIdx != null) {
                    matrix.setEntry(uIdx, cIdx, history.getScore());
                }
            }

            return new RatingMatrix(matrix, userIndexMap, carIndexMap);
        }

        static class RatingMatrix {
            final RealMatrix matrix;
            final Map<Integer, Integer> userIndexMap;
            final Map<Integer, Integer> carIndexMap;

            RatingMatrix(RealMatrix matrix,
                         Map<Integer, Integer> userIndexMap,
                         Map<Integer, Integer> carIndexMap) {
                this.matrix = matrix;
                this.userIndexMap = userIndexMap;
                this.carIndexMap = carIndexMap;
            }
        }
    }

    // 生成推荐结果
    private List<ImCarDetailsDto> generateRecommendations(
            double[] userPredictions,
            List<CarInfo> carInfos,
            Map<Integer, Integer> carIndexMap,
            int maxPrice) {

        // 创建汽车ID到预测评分的映射
        Map<Integer, Double> predictions = new HashMap<>();
        for (CarInfo car : carInfos) {
            Integer index = carIndexMap.get(car.getId());
            if (index != null && index < userPredictions.length) {
                predictions.put(car.getId(), userPredictions[index]);
            }
        }
        Map<String, ImCarDetailsDto> bestCarPerBrand = new LinkedHashMap<>();

        carInfos.stream()
                .filter(car -> car.getMaxPrice() != null && car.getMaxPrice() <= maxPrice)
                .forEach(car -> {
                    String brandName = carBrandRepository.findById(car.getBrandId())
                            .map(CarBrand::getName)
                            .orElse("未知品牌");

                    double rating = predictions.getOrDefault(car.getId(), 0.0);

                    ImCarDetailsDto currentDto = new ImCarDetailsDto(
                            car.getId(),
                            brandName,
                            car.getFullName(),
                            car.getMinPrice(),
                            car.getMaxPrice(),
                            rating
                    );

                    // Keep only the highest-rated car for each brand
                    bestCarPerBrand.merge(brandName, currentDto,
                            (existing, newDto) ->
                                    newDto.getPredictedRating() > existing.getPredictedRating() ? newDto : existing);
                });

        // Convert to list and sort by rating
        return bestCarPerBrand.values().stream()
                .sorted((c1, c2) -> Double.compare(c2.getPredictedRating(), c1.getPredictedRating()))
                .collect(Collectors.toList());
    }

    private List<ImCarDetailsDto> ImgetDefaultRecommendations() {
        List<DefaultRecommendation> defaultRecs = defaultRecommendationRepository.findAll();
        return convertDefaultToDto(defaultRecs);
    }

    private List<ImCarDetailsDto> convertDefaultToDto(List<DefaultRecommendation> defaultRecs) {
        return defaultRecs.stream()
                .map(dr -> new ImCarDetailsDto(
                        dr.getCarId(),
                        dr.getBrandName(),
                        dr.getFullName(),
                        dr.getPrice(),
                        dr.getPrice(),
                        0.0 // 默认评分
                ))
                .collect(Collectors.toList());
    }

    // ALS矩阵分解实现
    private MatrixFactorizationResult performALS(RealMatrix ratings, int rank, int iterations, double lambda) {
        int userCount = ratings.getRowDimension();
        int itemCount = ratings.getColumnDimension();

        // 初始化用户和物品特征矩阵
        RealMatrix userFeatures = MatrixUtils.createRealMatrix(userCount, rank);
        RealMatrix itemFeatures = MatrixUtils.createRealMatrix(itemCount, rank);

        // 随机初始化
        Random random = new Random();
        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < rank; j++) {
                userFeatures.setEntry(i, j, random.nextDouble());
            }
        }

        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < rank; j++) {
                itemFeatures.setEntry(i, j, random.nextDouble());
            }
        }

        // ALS迭代
        for (int iter = 0; iter < iterations; iter++) {
            // 固定物品特征，优化用户特征
            for (int u = 0; u < userCount; u++) {
                RealMatrix A = MatrixUtils.createRealMatrix(rank, rank);
                RealVector b = MatrixUtils.createRealVector(new double[rank]);

                for (int i = 0; i < itemCount; i++) {
                    double rating = ratings.getEntry(u, i);
                    if (rating > 0) {
                        RealVector itemVector = itemFeatures.getRowVector(i);
                        A = A.add(itemVector.outerProduct(itemVector));
                        b = b.add(itemVector.mapMultiply(rating));
                    }
                }

                // 添加正则化项
                RealMatrix lambdaI = MatrixUtils.createRealIdentityMatrix(rank).scalarMultiply(lambda);
                A = A.add(lambdaI);

                // 解线性方程组
                DecompositionSolver solver = new LUDecomposition(A).getSolver();
                RealVector userVector = solver.solve(b);
                userFeatures.setRow(u, userVector.toArray());
            }

            // 固定用户特征，优化物品特征
            for (int i = 0; i < itemCount; i++) {
                RealMatrix A = MatrixUtils.createRealMatrix(rank, rank);
                RealVector b = MatrixUtils.createRealVector(new double[rank]);

                for (int u = 0; u < userCount; u++) {
                    double rating = ratings.getEntry(u, i);
                    if (rating > 0) {
                        RealVector userVector = userFeatures.getRowVector(u);
                        A = A.add(userVector.outerProduct(userVector));
                        b = b.add(userVector.mapMultiply(rating));
                    }
                }

                // 添加正则化项
                RealMatrix lambdaI = MatrixUtils.createRealIdentityMatrix(rank).scalarMultiply(lambda);
                A = A.add(lambdaI);

                // 解线性方程组
                DecompositionSolver solver = new LUDecomposition(A).getSolver();
                RealVector itemVector = solver.solve(b);
                itemFeatures.setRow(i, itemVector.toArray());
            }
        }

        return new MatrixFactorizationResult(userFeatures, itemFeatures);
    }

    // 矩阵分解结果容器类
    private static class MatrixFactorizationResult {
        private final RealMatrix userFeatures;
        private final RealMatrix itemFeatures;

        public MatrixFactorizationResult(RealMatrix userFeatures, RealMatrix itemFeatures) {
            this.userFeatures = userFeatures;
            this.itemFeatures = itemFeatures;
        }

        public RealMatrix getUserFeatures() {
            return userFeatures;
        }

        public RealMatrix getItemFeatures() {
            return itemFeatures;
        }

        public RealMatrix getPredictions() {
            return userFeatures.multiply(itemFeatures.transpose());
        }
    }


    private List<Map<String, Object>> getDefaultRecommendations() {
        return defaultRecommendationRepository.findAll().stream()
                .map(recommendation -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("carId", recommendation.getCarId());
                    map.put("brandName", recommendation.getBrandName());
                    map.put("fullName", recommendation.getFullName());
                    map.put("price", recommendation.getPrice());
                    return map;
                })
                .collect(Collectors.toList());
    }
}