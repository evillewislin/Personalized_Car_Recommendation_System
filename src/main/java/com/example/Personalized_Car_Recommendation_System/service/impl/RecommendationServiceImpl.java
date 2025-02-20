package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
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
    private static SparkSession sparkSession;

    @Autowired
    public RecommendationServiceImpl(JwtUtil jwtUtil, ChatClient chatClient, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
        // 初始化 SparkSession
        initSparkSession();
    }

    private void initSparkSession() {
        if (sparkSession == null) {
            sparkSession = SparkSession.builder()
                    .appName("ALSRecommendation")
                    .master("local[*]")
                    .config("spark.ui.enabled", "false")
                    .getOrCreate();
        }
    }

    @Override
    public int getUserIdFromToken(String token) {
        logger.info("开始解析 Token: {}", token);
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);
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
            logger.error("AI 调用异常: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public List<Map<String, Object>> getALSRecommendations(int userId) {
        System.setProperty("hadoop.home.dir", "C:\\Users\\ZL15772\\Downloads\\hadoop-3.0.0");
        try {
            logger.info("Getting ALS recommendations for user: {}", userId);
            // 从数据库中获取用户对汽车的评分数据
            List<Map<String, Object>> ratingData = getRatingDataFromDB();
            logger.info("获取到的评分数据数量: {}", ratingData.size());

            // 将数据转换为 Spark DataFrame
            Dataset<Row> df = convertToDataFrame(ratingData);

            // 配置 ALS 算法
            ALS als = configureALS();

            // 训练 ALS 模型
            ALSModel model = als.fit(df);

            // 为指定用户生成推荐结果
            Dataset<Row> userRecs = generateRecommendations(model, userId);

            // 提取推荐的汽车 ID
            List<Integer> recommendedCarIds = extractRecommendedCarIds(userRecs);

            // 根据推荐的汽车 ID 获取汽车详细信息

            return getCarDetails(recommendedCarIds);
        } catch (Exception e) {
            logger.error("ALS 推荐出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> getRatingDataFromDB() {
        String sql = "SELECT user_id, car_id, score FROM recommendation_history";
        List<Map<String, Object>> ratingData = jdbcTemplate.queryForList(sql);
        // 输出查询结果，检查数据是否正确
        for (Map<String, Object> data : ratingData) {
            System.out.println("DB data: " + data);
        }
        return ratingData;
    }

    private Dataset<Row> convertToDataFrame(List<Map<String, Object>> ratingData) {
        List<Row> rows = new ArrayList<>();
        for (Map<String, Object> data : ratingData) {
            if (data.containsKey("user_id") && data.containsKey("car_id") && data.containsKey("score")) {
                Integer userId = (Integer) data.get("user_id");
                Integer carId = (Integer) data.get("car_id");
                Float score = (Float) data.get("score");
                rows.add(RowFactory.create(userId, carId, score));
            }
        }
        List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("user_id", DataTypes.IntegerType, true));
        fields.add(DataTypes.createStructField("car_id", DataTypes.IntegerType, true));
        fields.add(DataTypes.createStructField("score", DataTypes.FloatType, true));
        StructType schema = DataTypes.createStructType(fields);
        // 输出过滤后的数据，检查键是否正确
        for (Row data : rows) {
            System.out.println("Data keys: " + data);
        }
        // 使用定义好的 schema 创建 DataFrame
        return sparkSession.createDataFrame(rows, schema);
    }

    private ALS configureALS() {
        return new ALS()
                .setMaxIter(10)
                .setRegParam(0.01)
                .setUserCol("user_id")
                .setItemCol("car_id")
                .setRatingCol("score");
    }

    private Dataset<Row> generateRecommendations(ALSModel model, int userId) {
        return model.recommendForUserSubset(sparkSession.createDataFrame(Collections.singletonList(Map.of("user_id", userId)), Map.class), 10);
    }

    private List<Integer> extractRecommendedCarIds(Dataset<Row> userRecs) {
        List<Integer> recommendedCarIds = new ArrayList<>();
        for (Row row : userRecs.collectAsList()) {
            List<Row> recs = row.getAs("recommendations");
            for (Row rec : recs) {
                recommendedCarIds.add(rec.getInt(0));
            }
        }
        return recommendedCarIds;
    }

    private List<Map<String, Object>> getCarDetails(List<Integer> recommendedCarIds) {
        if (recommendedCarIds.isEmpty()) {
            return Collections.emptyList();
        }
        String inClause = String.join(",", Collections.nCopies(recommendedCarIds.size(), "?"));
        String carSql = "SELECT b.name, ci.full_name AS fullName, " +
                "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
                "rh.score " +
                "FROM car_info ci " +
                "JOIN car_brand b ON ci.brand_id = b.brand_id " +
                "JOIN recommendation_history rh ON ci.car_id = rh.car_id " +
                "WHERE ci.car_id IN (" + inClause + ")";
        List<Map<String, Object>> recommendedCars = jdbcTemplate.queryForList(carSql, recommendedCarIds.toArray());
        logger.info("获取到的推荐汽车详细信息数量: {}", recommendedCars.size());
        return recommendedCars;
    }

    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }
}