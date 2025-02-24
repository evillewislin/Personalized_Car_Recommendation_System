package com.example.Personalized_Car_Recommendation_System.service.impl;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.repository.*;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    @Value("${user.max.price:300000}")
    private int userMaxPrice;

    @Autowired
    public RecommendationServiceImpl(ChatClient chatClient, JdbcTemplate jdbcTemplate,
                                     RecommendationHistoryRepository recommendationHistoryRepository) {
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
        this.recommendationHistoryRepository = recommendationHistoryRepository;
    }

    /**
     * 从令牌中获取用户ID
     * @param token 用户的令牌
     * @return 用户ID
     */
    @Override
    public int getUserIdFromToken(String token) {
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
     * @return 推荐信息列表
     */
    @Override
    public List<Map<String, Object>> getRecommendationsByUserId(int userId) {
        String sql = "SELECT ci.car_id, b.name, ci.full_name AS fullName, " +
                "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
                "AVG(rh.score) AS avgScore " +
                "FROM recommendation_history rh " +
                "JOIN car_info ci ON rh.car_id = ci.car_id " +
                "JOIN car_brand b ON ci.brand_id = b.brand_id " +
                "WHERE rh.user_id = ? " +
                "GROUP BY ci.car_id, b.name, ci.full_name, ci.minprice, ci.maxprice " +
                "ORDER BY avgScore DESC " +
                "LIMIT 10";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        logger.info("查询结果数量: {}", result.size());
        return result;
    }

    /**
     * 新增：基于历史记录的推荐方法
     * @param userId 用户ID
     * @param data 调用 /api/ai/recommend 接口返回的数据
     * @return 过滤后的数据
     */
    @Override
    public List<Map<String, Object>> getAlsRecommendations(int userId, List<Map<String, Object>> data) {
        System.setProperty("hadoop.home.dir", "D:\\hadoop-3.0.0");
        JavaSparkContext sc = null;
        try {
            sc = createSparkContext();

            // 获取所有用户的推荐历史记录
            List<RecommendationHistory> allHistory = recommendationHistoryRepository.findAll();
            if (allHistory.isEmpty()) {
                logger.info("没有推荐历史记录，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 将推荐历史记录转换为 Spark 的 Rating 对象
            JavaRDD<Rating> ratingsRDD = convertToRatingsRDD(sc, allHistory);

            // 训练 ALS 模型
            MatrixFactorizationModel model = trainALSModel(ratingsRDD);

            // 获取所有汽车的 ID
            List<Integer> carIds = extractCarIds(data);
            if (carIds.isEmpty()) {
                logger.info("没有有效的汽车 ID，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 创建待预测的 (用户 ID, 物品 ID) 元组列表
            JavaRDD<Tuple2<Object, Object>> inputRDD = createInputRDD(sc, userId, carIds);

            // 使用模型进行预测
            JavaRDD<Rating> userCarRatingsRDD = predictRatings(model, inputRDD);

            // 将预测结果转换为列表
            List<Rating> userCarRatings = userCarRatingsRDD.collect();

            // 根据预测评分对汽车进行排序
            userCarRatings.sort((r1, r2) -> Double.compare(r2.rating(), r1.rating()));

            // 根据排序后的评分筛选出对应的汽车信息
            List<Map<String, Object>> recommendedCars = filterRecommendedCars(data, userCarRatings);

            if (recommendedCars.isEmpty()) {
                logger.info("没有推荐的汽车，返回默认推荐");
                return getDefaultRecommendations(data);
            }

            // 可选：结合业务规则过滤（如价格区间）
            return filterByPrice(recommendedCars);
        } catch (Exception e) {
            logger.error("ALS 推荐过程中出现异常: {}", e.getMessage(), e);
            return getDefaultRecommendations(data);
        } finally {
            if (sc != null) {
                sc.stop();
            }
        }
    }

    private JavaSparkContext createSparkContext() {
        SparkConf conf = new SparkConf().setAppName("CarRecommendationALS").setMaster("local");
        return new JavaSparkContext(conf);
    }

    private JavaRDD<Rating> convertToRatingsRDD(JavaSparkContext sc, List<RecommendationHistory> allHistory) {
        return sc.parallelize(allHistory.stream()
                .map(history -> new Rating(history.getUserId(), history.getCarId(), history.getScore()))
                .collect(Collectors.toList()));
    }

    private MatrixFactorizationModel trainALSModel(JavaRDD<Rating> ratingsRDD) {
        int rank = 10;
        int numIterations = 10;
        return ALS.train(ratingsRDD.rdd(), rank, numIterations, 0.01);
    }

    private List<Integer> extractCarIds(List<Map<String, Object>> data) {
        return data.stream()
                .map(carMap -> (Integer) carMap.get("carId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private JavaRDD<Tuple2<Object, Object>> createInputRDD(JavaSparkContext sc, int userId, List<Integer> carIds) {
        List<Tuple2<Integer, Integer>> userCarPairs = carIds.stream()
                .map(carId -> new Tuple2<>(userId, carId))
                .collect(Collectors.toList());
        JavaPairRDD<Integer, Integer> userCarPairsRDD = sc.parallelizePairs(userCarPairs);
        return userCarPairsRDD.map(pair -> new Tuple2<>(pair._1(), pair._2()));
    }

    private JavaRDD<Rating> predictRatings(MatrixFactorizationModel model, JavaRDD<Tuple2<Object, Object>> inputRDD) {
        RDD<Rating> predictedRatingsRDD = model.predict(inputRDD.rdd());
        ClassTag<Rating> ratingClassTag = ClassTag$.MODULE$.apply(Rating.class);
        return JavaRDD.fromRDD(predictedRatingsRDD, ratingClassTag);
    }

    private List<Map<String, Object>> filterRecommendedCars(List<Map<String, Object>> data, List<Rating> userCarRatings) {
        List<Map<String, Object>> recommendedCars = new ArrayList<>();
        for (Rating rating : userCarRatings) {
            for (Map<String, Object> carMap : data) {
                Integer carId = (Integer) carMap.get("carId");
                if (carId != null && carId == rating.product()) {
                    recommendedCars.add(carMap);
                    break;
                }
            }
        }
        return recommendedCars;
    }

    // 按热度排序
    private List<Map<String, Object>> sortByPopularity(List<Map<String, Object>> cars) {
        return cars.stream()
                .sorted((a, b) -> {
                    Integer popularityA = (Integer) a.get("popularity");
                    Integer popularityB = (Integer) b.get("popularity");

                    if (popularityA == null && popularityB == null) {
                        return 0;
                    } else if (popularityA == null) {
                        return 1; // 将 null 值放到后面
                    } else if (popularityB == null) {
                        return -1; // 将 null 值放到后面
                    }
                    return popularityB.compareTo(popularityA);
                })
                .collect(Collectors.toList());
    }

    // 示例：根据价格区间过滤
    private List<Map<String, Object>> filterByPrice(List<Map<String, Object>> cars) {
        return cars.stream()
                .filter(car -> {
                    Integer minPrice = (Integer) car.get("minPrice");
                    Integer maxPrice = (Integer) car.get("maxPrice");
                    return maxPrice != null && maxPrice <= userMaxPrice;
                })
                .collect(Collectors.toList());
    }

    // 新用户默认推荐逻辑
    private List<Map<String, Object>> getDefaultRecommendations(List<Map<String, Object>> data) {
        return sortByPopularity(data);
    }
}