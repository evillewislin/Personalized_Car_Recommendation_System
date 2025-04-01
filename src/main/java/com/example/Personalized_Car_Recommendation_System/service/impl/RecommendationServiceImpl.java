package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.ImCarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.CarRecommendationDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarRepository;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.repository.RecommendationHistoryRepository;
import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;


import org.apache.spark.rdd.RDD;
import org.apache.spark.storage.StorageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private CarBrandRepository carBrandRepository;
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
    public RecommendationServiceImpl(ChatClient chatClient, JdbcTemplate jdbcTemplate,
                                     RecommendationHistoryRepository recommendationHistoryRepository,
                                     CarRepository carRepository) {
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
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
     * 新增：基于历史记录的推荐方法
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
            if (allHistory.isEmpty()) {
                logger.info("没有推荐历史记录，返回默认推荐");
                return getDefaultRecommendations(data);
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
                return getDefaultRecommendations(data);
            }

            double mse = calculateMSE(model, test);
            logger.info("模型的均方误差 (MSE): {}", mse);

            double mseThreshold = 1.0;
            if (mse > mseThreshold) {
                logger.info("模型性能不佳，MSE 超过阈值，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            List<Integer> carIds = extractCarIds(data);
            if (carIds.isEmpty()) {
                logger.info("没有有效的汽车 ID，返回默认推荐");
                return getDefaultRecommendations(data);
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
                return getDefaultRecommendations(data);
            }
            logger.info("筛选预测结果: {}", recommendedCars);
            logger.info("最终推荐结果: {}", recommendedCars);

            return recommendedCars;
        } catch (Exception e) {
            logger.error("ALS 推荐过程中出现异常: {}", e.getMessage(), e);
            return getDefaultRecommendations(data);
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

        // 核心修复点：扩展模块开放配置
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
                // 设置主节点 URL，这里使用 local[*] 表示在本地使用所有可用的核心
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
     * @param rank 隐式推荐的秩
     * @param iterations 迭代次数
     * @param lambda 正则化参数
     * @param maxPrice 用户输入的最高价格
     * @param page 页码
     * @param size 每页数量
     * @return 显式推荐结果
     */
    @Override
    @Cacheable(value = "recommendations", key = "#rank+#iterations+#lambda+#maxPrice+#page+#size")
    public Page<CarDetailsDto> generateExplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            int page,
            int size) {
        if (page < 1 || size < 1) {
            logger.error("无效的分页参数: page={}, size={}", page, size);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
        }

        List<CarInfo> carInfos = carRepository.findAll();
        if (carInfos.isEmpty()) {
            logger.warn("没有找到任何汽车信息");
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
        }

        List<CarDetailsDto> dtos = carInfos.stream()
                .filter(carInfo ->
                        carInfo.getMaxPrice() != null &&
                                carInfo.getMaxPrice() <= maxPrice
                )
                .map(carInfo -> {
                    String brandName = Optional.ofNullable(carInfo.getBrandId())
                            .flatMap(brandId -> Optional.ofNullable(carRepository.getBrandNameByBrandId(brandId)))
                            .orElse("未知品牌");
                    return new CarDetailsDto(
                            carInfo.getId(),
                            brandName,
                            carInfo.getFullName(),
                            carInfo.getMinPrice(),
                            carInfo.getMaxPrice()
                    );
                })
                .collect(Collectors.toList());

        dtos = dtos.stream()
                .sorted((dto1, dto2) -> {
                    Integer price1 = dto1.getMaxPrice() != null ? dto1.getMaxPrice() : 0;
                    Integer price2 = dto2.getMaxPrice() != null ? dto2.getMaxPrice() : 0;
                    return price2.compareTo(price1);
                })
                .collect(Collectors.toList());

        int totalElements = dtos.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        if (page > totalPages) {
            logger.warn("请求的页码超出范围: page={}, totalPages={}，自动调整为最后一页", page, totalPages);
            page = totalPages;
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, totalElements);
        if (start >= totalElements) {
            logger.warn("起始索引超出数据范围: start={}, totalElements={}", start, totalElements);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), totalElements);
        }

        List<CarDetailsDto> subList = dtos.subList(start, end);
        return new PageImpl<>(subList, PageRequest.of(page - 1, size), totalElements);
    }



    @Override
    @Cacheable(value = "implicitRecommendations", key = "#rank+#iterations+#lambda+#maxPrice+#userId")
    public Page<ImCarDetailsDto> generateImplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int page,
            int size,
            int maxPrice,
            int userId
    ) {
        long startTime = System.currentTimeMillis();
        logger.info("开始生成隐式推荐，用户ID: {}, 最大价格: {}", userId, maxPrice);

        try {
            // 1. 加载推荐历史数据
            long loadHistoryStartTime = System.currentTimeMillis();
            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            long loadHistoryEndTime = System.currentTimeMillis();
            long loadHistoryTime = loadHistoryEndTime - loadHistoryStartTime;
            if (allHistory.isEmpty()) {
                logger.warn("没有找到推荐历史记录，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            }
            logger.info("加载推荐历史数据耗时: {}ms", loadHistoryTime);

            // 2. 构建用户-汽车评分矩阵
            long buildRatingsStartTime = System.currentTimeMillis();
            Map<Integer, Map<Integer, Double>> userCarRatings = buildUserCarRatings(allHistory);
            long buildRatingsEndTime = System.currentTimeMillis();
            long buildRatingsTime = buildRatingsEndTime - buildRatingsStartTime;
            logger.info("构建用户-汽车评分矩阵耗时: {}ms", buildRatingsTime);

            // 3. 计算用户相似度
            long similarityStartTime = System.currentTimeMillis();
            Map<Integer, Double> userSimilarities = calculateUserSimilarities(userId, userCarRatings);
            long similarityEndTime = System.currentTimeMillis();
            long similarityTime = similarityEndTime - similarityStartTime;
            logger.info("计算用户相似度耗时: {}ms", similarityTime);

            // 4. 加载汽车数据
            long loadCarsStartTime = System.currentTimeMillis();
            List<CarInfo> carInfos = carInfoRepository.findAll();
            long loadCarsEndTime = System.currentTimeMillis();
            long loadCarsTime = loadCarsEndTime - loadCarsStartTime;
            if (carInfos.isEmpty()) {
                logger.warn("没有找到汽车信息");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            }
            logger.info("加载汽车数据耗时: {}ms", loadCarsTime);

            // 5. 生成推荐
            long generateRecommendationsStartTime = System.currentTimeMillis();
            List<ImCarDetailsDto> recommendedCars = generateRecommendations(userId, userSimilarities, carInfos, maxPrice, userCarRatings);
            long generateRecommendationsEndTime = System.currentTimeMillis();
            long generateRecommendationsTime = generateRecommendationsEndTime - generateRecommendationsStartTime;
            logger.info("生成推荐耗时: {}ms", generateRecommendationsTime);

            // 6. 分页处理结果，固定每页大小为10，页码为第1页
            long paginateStartTime = System.currentTimeMillis();
            int fixedPage = 1;
            int fixedSize = 10;
            Page<ImCarDetailsDto> result = paginateResults(recommendedCars, fixedPage, fixedSize);
            long paginateEndTime = System.currentTimeMillis();
            long paginateTime = paginateEndTime - paginateStartTime;
            logger.info("分页处理结果耗时: {}ms", paginateTime);

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("隐式推荐生成完成，总耗时: {}ms", totalTime);
            logger.info("各步骤耗时详情 - 加载历史数据: {}ms, 构建评分矩阵: {}ms, 计算相似度: {}ms, 加载汽车数据: {}ms, 生成推荐: {}ms, 分页处理: {}ms",
                    loadHistoryTime, buildRatingsTime, similarityTime, loadCarsTime, generateRecommendationsTime, paginateTime);
            return result;

        } catch (Exception e) {
            logger.error("生成隐式推荐时发生错误", e);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        }
    }

    // 构建用户-汽车评分矩阵
    private Map<Integer, Map<Integer, Double>> buildUserCarRatings(List<RecommendationHistory> allHistory) {
        Map<Integer, Map<Integer, Double>> userCarRatings = new HashMap<>();
        for (RecommendationHistory history : allHistory) {
            int userId = history.getUserId();
            int carId = history.getCarId();
            double score = history.getScore();

            userCarRatings.computeIfAbsent(userId, k -> new HashMap<>()).put(carId, score);
        }
        return userCarRatings;
    }

    // 计算用户相似度（这里使用简单的余弦相似度）
    private Map<Integer, Double> calculateUserSimilarities(int targetUserId, Map<Integer, Map<Integer, Double>> userCarRatings) {
        Map<Integer, Double> userSimilarities = new HashMap<>();
        Map<Integer, Double> targetUserRatings = userCarRatings.get(targetUserId);
        if (targetUserRatings == null) {
            return userSimilarities;
        }

        for (Map.Entry<Integer, Map<Integer, Double>> entry : userCarRatings.entrySet()) {
            int otherUserId = entry.getKey();
            if (otherUserId == targetUserId) {
                continue;
            }

            Map<Integer, Double> otherUserRatings = entry.getValue();
            double similarity = cosineSimilarity(targetUserRatings, otherUserRatings);
            userSimilarities.put(otherUserId, similarity);
        }
        return userSimilarities;
    }

    // 计算余弦相似度
    private double cosineSimilarity(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<Integer, Double> entry : vector1.entrySet()) {
            int key = entry.getKey();
            double value1 = entry.getValue();
            normA += value1 * value1;
            if (vector2.containsKey(key)) {
                double value2 = vector2.get(key);
                dotProduct += value1 * value2;
            }
        }

        for (double value : vector2.values()) {
            normB += value * value;
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 生成推荐结果
    private List<ImCarDetailsDto> generateRecommendations(
            int userId,
            Map<Integer, Double> userSimilarities,
            List<CarInfo> carInfos,
            int maxPrice,
            Map<Integer, Map<Integer, Double>> userCarRatings) {

        Map<Integer, Double> predictedRatings = new HashMap<>();
        Map<Integer, Integer> ratingCount = new HashMap<>();

        // 根据用户相似度计算预测评分
        for (Map.Entry<Integer, Double> entry : userSimilarities.entrySet()) {
            int otherUserId = entry.getKey();
            double similarity = entry.getValue();
            Map<Integer, Double> otherUserRatings = userCarRatings.get(otherUserId);

            for (Map.Entry<Integer, Double> carRatingEntry : otherUserRatings.entrySet()) {
                int carId = carRatingEntry.getKey();
                double rating = carRatingEntry.getValue();

                predictedRatings.compute(carId, (k, v) -> (v == null ? 0.0 : v) + similarity * rating);
                ratingCount.compute(carId, (k, v) -> (v == null ? 0 : v) + 1);
            }
        }

        // 计算最终预测评分
        for (Map.Entry<Integer, Integer> countEntry : ratingCount.entrySet()) {
            int carId = countEntry.getKey();
            int count = countEntry.getValue();
            predictedRatings.put(carId, predictedRatings.get(carId) / count);
        }

        // 筛选符合价格条件的汽车
        List<ImCarDetailsDto> recommendedCars = new ArrayList<>();
        for (CarInfo carInfo : carInfos) {
            if (carInfo.getMaxPrice() != null && carInfo.getMaxPrice() <= maxPrice) {
                int carId = carInfo.getId();
                // 这里去掉了预测评分相关的逻辑，因为新的 ImCarDetailsDto 构造函数不包含预测评分

                Optional<CarBrand> carBrandOptional = carBrandRepository.findById(carInfo.getBrandId());
                String brandName = carBrandOptional.map(CarBrand::getName).orElse("未知品牌");

                recommendedCars.add(new ImCarDetailsDto(
                        carId,
                        brandName,
                        carInfo.getFullName(),
                        carInfo.getMinPrice(),
                        carInfo.getMaxPrice()
                ));
            }
        }

        // 由于新的 ImCarDetailsDto 没有预测评分，这里的排序逻辑需要调整或移除
        // 如果需要排序，可以根据其他字段进行排序，例如价格
        recommendedCars.sort((c1, c2) -> {
            if (c1.getMaxPrice() != null && c2.getMaxPrice() != null) {
                return c2.getMaxPrice().compareTo(c1.getMaxPrice());
            }
            return 0;
        });

        logger.info("生成 {} 条符合条件的推荐", recommendedCars.size());
        return recommendedCars;
    }

    // 分页处理结果
    private Page<ImCarDetailsDto> paginateResults(List<ImCarDetailsDto> recommendedCars, int page, int size) {
        int start = (page - 1) * size;
        if (start >= recommendedCars.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), recommendedCars.size());
        }

        int end = Math.min(start + size, recommendedCars.size());
        List<ImCarDetailsDto> pagedResults = recommendedCars.subList(start, end);

        return new PageImpl<>(pagedResults, PageRequest.of(page - 1, size), recommendedCars.size());
    }

    // 新用户默认推荐逻辑
    private List<Map<String, Object>> getDefaultRecommendations(List<Map<String, Object>> data) {
        return sortByPopularity(data);
    }
}