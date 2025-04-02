CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    role varchar(25),
    age INT,
    region varchar(255));
CREATE TABLE admin( admin_id INT PRIMARY KEY AUTO_INCREMENT,
                    adminname VARCHAR(50) UNIQUE,
                    password VARCHAR(100),
                    role varchar(25));
CREATE TABLE recommendation_history ( id INT PRIMARY KEY AUTO_INCREMENT,
                                      user_id INT,
                                      car_id INT,
                                      score FLOAT,
                                      timestamp DATETIME,
                                      name varchar(255));
CREATE TABLE default_recommendations (
                                         id INT PRIMARY KEY AUTO_INCREMENT,
                                         car_id INT,
                                         brand_name VARCHAR(255),
                                         full_name VARCHAR(255),
                                         price_range VARCHAR(255),
                                         popularity INT
);