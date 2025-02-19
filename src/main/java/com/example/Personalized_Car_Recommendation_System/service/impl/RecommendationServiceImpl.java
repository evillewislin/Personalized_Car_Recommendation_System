package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    @Autowired
    public RecommendationServiceImpl(JwtUtil jwtUtil, ChatClient chatClient, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getUserIdFromToken(String token) {
        System.out.println("Received token: " + token);
        try {
            logger.info("开始解析 Token: {}", token);
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                logger.error("Token 解析失败，未获取到用户 ID");
                throw new IllegalArgumentException("Token 解析失败");
            }
            logger.info("成功解析 Token，用户 ID: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Token 解析异常: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token 解析失败", e);
        }
    }

    @Async
    public CompletableFuture<String> callAI(Prompt prompt) {
        try {
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            // 处理异常
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public List<Map<String, Object>> getALSRecommendations(int userId) {
        System.setProperty("org.apache.spark.internal.Logging", "org.apache.spark.internal.Slf4jLogging");
        // 初始化 SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("ALSRecommendation")
                .master("local[*]")
                .getOrCreate();

        try {
            log.info("Getting ALS recommendations for user: {}", userId);
            // 从数据库中获取用户对汽车的评分数据
            String sql = "SELECT user_id, car_id, score FROM recommendation_history";
            List<Map<String, Object>> ratingData = jdbcTemplate.queryForList(sql);

            // 将数据转换为 Spark DataFrame
            List<Map<String, Object>> filteredData = new ArrayList<>();
            for (Map<String, Object> data : ratingData) {
                if (data.containsKey("user_id") && data.containsKey("car_id") && data.containsKey("score")) {
                    filteredData.add(data);
                }
            }
            Dataset<Row> df = spark.createDataFrame(filteredData, Map.class);

            // 配置 ALS 算法
            ALS als = new ALS()
                    .setMaxIter(10)
                    .setRegParam(0.01)
                    .setUserCol("user_id")
                    .setItemCol("car_id")
                    .setRatingCol("score");

            // 训练 ALS 模型
            ALSModel model = als.fit(df);

            // 为指定用户生成推荐结果
            Dataset<Row> userRecs = model.recommendForUserSubset(spark.createDataFrame(Collections.singletonList(Map.of("user_id", userId)), Map.class), 10);

            // 提取推荐的汽车 ID
            List<Integer> recommendedCarIds = new ArrayList<>();
            for (Row row : userRecs.collectAsList()) {
                List<Row> recs = (List<Row>) row.getAs("recommendations");
                for (Row rec : recs) {
                    recommendedCarIds.add(rec.getInt(0));
                }
            }

            // 根据推荐的汽车 ID 获取汽车详细信息
            List<Map<String, Object>> recommendedCars = new ArrayList<>();
            if (!recommendedCarIds.isEmpty()) {
                String inClause = String.join(",", Collections.nCopies(recommendedCarIds.size(), "?"));
                String carSql = "SELECT b.name, ci.full_name AS fullName, " +
                        "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
                        "rh.score " +
                        "FROM car_info ci " +
                        "JOIN car_brand b ON ci.brand_id = b.brand_id " +
                        "JOIN recommendation_history rh ON ci.car_id = rh.car_id " +
                        "WHERE ci.car_id IN (" + inClause + ")";
                recommendedCars = jdbcTemplate.queryForList(carSql, recommendedCarIds.toArray());
            }

            return recommendedCars;
        } catch (Exception e) {
            logger.error("ALS 推荐出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            // 关闭 SparkSession
            spark.stop();
        }
    }
}