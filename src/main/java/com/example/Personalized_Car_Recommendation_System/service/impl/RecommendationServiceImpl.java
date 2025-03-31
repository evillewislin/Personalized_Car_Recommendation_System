package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.ImCarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.CarRecommendationDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.entity.User;
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

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

import org.apache.spark.rdd.RDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
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
    private final RecommendationHistoryRepository recommendationHistoryRepository;
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
        // 去除可能存在的空格和多余字符
        token = token.trim().replace("Bearer ", ""); 
        logger.info("开始解析 Token: {}", token);
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);
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
            // 可以添加重试机制或者其他处理逻辑
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
            // 修正查询参数传递
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(RECOMMENDATION_SQL, userId, searchKeyword, searchKeyword, size, offset);
            List<CarRecommendationDto> carRecommendations = new ArrayList<>();
            for (Map<String, Object> row : resultList) {
                Integer carId = (Integer) row.get("car_id");
                String brandName = (String) row.get("name");
                String fullName = (String) row.get("fullName");
                String priceRange = (String) row.get("priceRange");
                Float avgScore = ((Number) row.get("avgScore")).floatValue();
                carRecommendations.add(new CarRecommendationDto(carId, brandName, fullName, priceRange, avgScore));
            }

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
            // 查询数据
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(ALL_RECOMMENDATION_SQL, userId);
            List<CarRecommendationDto> carRecommendations = new ArrayList<>();
            for (Map<String, Object> row : resultList) {
                Integer carId = (Integer) row.get("car_id");
                String brandName = (String) row.get("name");
                String fullName = (String) row.get("fullName");
                String priceRange = (String) row.get("priceRange");
                Float avgScore = ((Number) row.get("avgScore")).floatValue();
                carRecommendations.add(new CarRecommendationDto(carId, brandName, fullName, priceRange, avgScore));
            }

            // 查询总数
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

            // 获取所有用户的推荐历史记录
            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            logger.info("所有用户的推荐历史记录: {}", allHistory);
            if (allHistory.isEmpty()) {
                logger.info("没有推荐历史记录，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 将推荐历史记录转换为 Spark 的 Rating 对象
            JavaRDD<Rating> ratingsRDD = convertToRatingsRDD(sc, allHistory);
            if (ratingsRDD.isEmpty()) {
                logger.info("转换后的 Ratings RDD 为空，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 划分训练集和测试集，这里使用 80% 作为训练集，20% 作为测试集
            JavaRDD<Rating>[] splits = ratingsRDD.randomSplit(new double[]{0.8, 0.2}, 12345L);
            JavaRDD<Rating> training = splits[0];
            JavaRDD<Rating> test = splits[1];

            // 训练 ALS 模型
            MatrixFactorizationModel model = trainALSModel(training);
            if (model == null) {
                logger.info("无法训练 ALS 模型，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 模型性能检查：计算均方误差（MSE）
            double mse = calculateMSE(model, test);
            logger.info("模型的均方误差 (MSE): {}", mse);

            // 可以设置一个 MSE 阈值，如果 MSE 过高，认为模型性能不佳，返回默认推荐
            double mseThreshold = 1.0;
            if (mse > mseThreshold) {
                logger.info("模型性能不佳，MSE 超过阈值，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 获取所有汽车的 ID
            List<Integer> carIds = extractCarIds(data);
            logger.info("所有汽车的 ID: {}", carIds);
            if (carIds.isEmpty()) {
                logger.info("没有有效的汽车 ID，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 创建待预测的 (用户 ID, 物品 ID) 元组列表
            JavaRDD<Tuple2<Object, Object>> inputRDD = createInputRDD(sc, userId, carIds);
            logger.info("预测的 (用户 ID, 物品 ID) 元组列表: {}", inputRDD);

            // 使用模型进行预测
            JavaRDD<Rating> userCarRatingsRDD = predictRatings(model, inputRDD);
            logger.info("模型预测结果: {}", userCarRatingsRDD);

            // 将预测结果转换为列表
            List<Rating> userCarRatings = userCarRatingsRDD.collect();
            logger.info("预测结果列表: {}", userCarRatings);

            // 将列表转换为支持修改操作的 ArrayList
            userCarRatings = new ArrayList<>(userCarRatings);

            // 根据预测评分对汽车进行排序
            userCarRatings.sort((r1, r2) -> Double.compare(r2.rating(), r1.rating()));
            logger.info("排序预测结果: {}", userCarRatings);

            // 根据排序后的评分筛选出对应的汽车信息
            List<Map<String, Object>> recommendedCars = filterRecommendedCars(data, userCarRatings, maxPrice);
            logger.info("筛选预测结果: {}", recommendedCars);

            if (recommendedCars.isEmpty()) {
                logger.info("没有推荐的汽车，返回默认推荐");
                return getDefaultRecommendations(data);
            }
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

        // 记录 MSE 结果到日志
        logger.info("模型在测试集上计算得到的均方误差 (MSE) 为: {}", mse);

        return mse;
    }

    private JavaSparkContext createSparkContext() {
        System.setProperty("hadoop.home.dir", hadoopHomeDir);
        SparkConf conf = new SparkConf().setAppName("CarRecommendationALS").setMaster("local");
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
        logger.debug("传入的汽车数据列表: {}", data);
        return data.stream()
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
        List<Map<String, Object>> recommendedCars = new ArrayList<>();

        // 从 carinfo 表中查询所有汽车信息
        String carInfoSql = "SELECT car_id, minprice, maxprice FROM car_info";
        List<Map<String, Object>> carInfoList = jdbcTemplate.queryForList(carInfoSql);

        // 将 carinfo 表的数据转换为以 car_id 为键的 Map，方便查找
        Map<Integer, Map<String, Object>> carInfoMap = new HashMap<>();
        for (Map<String, Object> carInfo : carInfoList) {
            Integer carId = (Integer) carInfo.get("car_id");
            carInfoMap.put(carId, carInfo);
        }

        for (Rating rating : userRatings) {
            logger.info("当前预测的汽车 ID: {}", rating.product());
            for (Map<String, Object> carMap : data) {
                Integer carId = (Integer) carMap.get("carId");
                if (carId != null) {
                    if (carId == rating.product()) {
                        Map<String, Object> carInfo = carInfoMap.get(carId);
                        if (carInfo != null) {
                            Integer minPrice = (Integer) carInfo.get("minprice");
                            Integer maxPrice = (Integer) carInfo.get("maxprice");
                            if (maxPrice != null && maxPrice <= userMaxPrice) {
                                recommendedCars.add(carMap);
                                logger.info("添加符合条件的汽车，car_id: {}", carId);
                                break;
                            }
                        }
                    }
                }
            }
        }
        logger.info("最终推荐的汽车列表: {}", recommendedCars);
        return recommendedCars;
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
        try {
            // 分页参数校验
            if (page < 1 || size < 1) {
                logger.error("无效的分页参数: page={}, size={}", page, size);
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }

            // 获取原始数据
            List<CarInfo> carInfos = carRepository.findAll();
            if (carInfos.isEmpty()) {
                logger.warn("没有找到任何汽车信息");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }

            // 价格过滤
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

            // 排序
            dtos = dtos.stream()
                    .sorted((dto1, dto2) -> {
                        Integer price1 = dto1.getMaxPrice() != null ? dto1.getMaxPrice() : 0;
                        Integer price2 = dto2.getMaxPrice() != null ? dto2.getMaxPrice() : 0;
                        return price2.compareTo(price1);
                    })
                    .collect(Collectors.toList());

            // 分页参数校验
            int totalElements = dtos.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // 页码矫正
            if (page > totalPages) {
                logger.warn("请求的页码超出范围: page={}, totalPages={}，自动调整为最后一页", page, totalPages);
                page = totalPages;
            }

            // 计算分页索引
            int start = (page - 1) * size;
            int end = Math.min(start + size, totalElements);
            if (start >= totalElements) {
                logger.warn("起始索引超出数据范围: start={}, totalElements={}", start, totalElements);
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), totalElements);
            }

            // 获取分页数据
            List<CarDetailsDto> subList = dtos.subList(start, end);
            return new PageImpl<>(subList, PageRequest.of(page - 1, size), totalElements);
        } catch (Exception e) {
            logger.error("显式推荐生成失败", e);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
        }
    }
 /**
     * 生成隐式推荐
     * @param rank 隐式推荐的秩
     * @param iterations 迭代次数
     * @param lambda 正则化参数
     * @param maxPrice 用户输入的最高价格
     * @param page 页码
     * @param size 每页数量
     * @param userId 用户ID
     * @return 隐式推荐结果
     */
    @Override
    @Cacheable(value = "implicitRecommendations", key = "#rank+#iterations+#lambda+#maxPrice+#page+#size+#userId")
   
    public Page<ImCarDetailsDto> generateImplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            int page,
            int size,
            int userId
    ) {
        JavaSparkContext sc = null;
        try {
            // 分页参数校验
            if (page < 1 || size < 1) {
                logger.error("无效的分页参数: page={}, size={}", page, size);
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }

            sc = createSparkContext();

            // 获取所有用户的推荐历史记录
            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            if (allHistory.isEmpty()) {
                logger.info("没有推荐历史记录，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }
            logger.info("成功获取所有用户的推荐历史记录，记录数量: {}", allHistory.size());

            // 将推荐历史记录转换为 Spark 的 Rating 对象
            JavaRDD<Rating> ratingsRDD = convertToRatingsRDD(sc, allHistory);
            if (ratingsRDD.isEmpty()) {
                logger.info("转换后的 Ratings RDD 为空，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }
            logger.info("成功将推荐历史记录转换为 Ratings RDD，RDD 数量: {}", ratingsRDD.count());

            // 划分训练集和测试集，这里使用 80% 作为训练集，20% 作为测试集
            JavaRDD<Rating>[] splits = ratingsRDD.randomSplit(new double[]{0.8, 0.2}, 12345L);
            JavaRDD<Rating> training = splits[0];
            JavaRDD<Rating> test = splits[1];
            logger.info("训练集数量: {}, 测试集数量: {}", training.count(), test.count());

            // 训练 ALS 模型
            MatrixFactorizationModel model = ALS.train(training.rdd(), rank, iterations, lambda);
            if (model == null) {
                logger.info("无法训练 ALS 模型，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }
            logger.info("成功训练 ALS 模型");

            // 模型性能检查：计算均方误差（MSE）
            double mse = calculateMSE(model, test);
            logger.info("模型的均方误差 (MSE): {}", mse);

            // 可以设置一个 MSE 阈值，如果 MSE 过高，认为模型性能不佳，返回空推荐结果
            double mseThreshold = 1.0;
            if (mse > mseThreshold) {
                logger.info("模型性能不佳，MSE 超过阈值，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }

            // 获取所有汽车的 ID
            List<CarInfo> carInfos = carRepository.findAll();
            List<Integer> carIds = carInfos.stream()
                   .map(CarInfo::getId)
                   .collect(Collectors.toList());
            if (carIds.isEmpty()) {
                logger.info("没有有效的汽车 ID，返回空推荐结果");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
            }
            logger.info("成功获取所有汽车的 ID，ID 数量: {}", carIds.size());

            // 创建待预测的 (用户 ID, 物品 ID) 元组列表
            JavaRDD<Tuple2<Object, Object>> inputRDD = createInputRDD(sc, userId, carIds);
            logger.info("成功创建待预测的 (用户 ID, 物品 ID) 元组列表，列表数量: {}", inputRDD.count());

            // 使用模型进行预测
            JavaRDD<Rating> userCarRatingsRDD = predictRatings(model, inputRDD);
            logger.info("成功使用模型进行预测，预测结果数量: {}", userCarRatingsRDD.count());

            // 将预测结果转换为列表
            List<Rating> userCarRatings = userCarRatingsRDD.collect();
            logger.info("成功将预测结果转换为列表，列表数量: {}", userCarRatings.size());

            // 根据预测评分对汽车进行排序
            userCarRatings.sort((r1, r2) -> Double.compare(r2.rating(), r1.rating()));
            logger.info("成功根据预测评分对汽车进行排序");

            // 根据排序后的评分筛选出对应的汽车信息
            List<ImCarDetailsDto> recommendedCars = filterImplicitRecommendedCars(carInfos, userCarRatings, userId, maxPrice);
            logger.info("成功筛选出对应的汽车信息，推荐汽车数量: {}", recommendedCars.size());

            // 分页处理
            int totalElements = recommendedCars.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // 页码矫正
            if (page > totalPages) {
                logger.warn("请求的页码超出范围: page={}, totalPages={}，自动调整为最后一页", page, totalPages);
                page = totalPages;
            }

            // 计算分页索引
            int start = (page - 1) * size;
            int end = Math.min(start + size, totalElements);
            if (start >= totalElements) {
                logger.warn("起始索引超出数据范围: start={}, totalElements={}", start, totalElements);
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), totalElements);
            }

            // 获取分页数据
            List<ImCarDetailsDto> subList = recommendedCars.subList(start, end);
            return new PageImpl<>(subList, PageRequest.of(page - 1, size), totalElements);

        } catch (Exception e) {
            logger.error("隐式推荐生成失败", e);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);
        } finally {
            if (sc != null) {
                sc.stop();
            }
        }
    }

    private List<ImCarDetailsDto> filterImplicitRecommendedCars(List<CarInfo> carInfos, List<Rating> userRatings, int userId, int userMaxPrice) {
        List<ImCarDetailsDto> recommendedCars = new ArrayList<>();
        Map<Integer, CarInfo> carInfoMap = carInfos.stream()
               .collect(Collectors.toMap(CarInfo::getId, Function.identity()));

        for (Rating rating : userRatings) {
            Integer carId = rating.product();
            CarInfo carInfo = carInfoMap.get(carId);
            if (carInfo != null && carInfo.getMaxPrice() != null && carInfo.getMaxPrice() <= userMaxPrice) {
                // 获取品牌名称
                String brandName = Optional.ofNullable(carInfo.getBrandId())
                       .flatMap(brandId -> Optional.ofNullable(carRepository.getBrandNameByBrandId(brandId)))
                       .orElse("未知品牌");

                recommendedCars.add(new ImCarDetailsDto(
                        carId,
                        userId,
                        brandName,
                        carInfo.getFullName(),
                        carInfo.getMinPrice(),
                        carInfo.getMaxPrice(),
                        (float) rating.rating(),
                        0
                ));
            }
        }
        return recommendedCars;
    }

    // 新用户默认推荐逻辑
    private List<Map<String, Object>> getDefaultRecommendations(List<Map<String, Object>> data) {
        return sortByPopularity(data);
    }
}    