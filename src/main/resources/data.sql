CREATE TABLE user ( id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50) UNIQUE, password VARCHAR(100));
CREATE TABLE admin( id INT PRIMARY KEY AUTO_INCREMENT, adminname VARCHAR(50) UNIQUE, password VARCHAR(100));
CREATE TABLE car ( id INT PRIMARY KEY AUTO_INCREMENT, brand VARCHAR(50), price DECIMAL(10,2),  fuel_type VARCHAR(20), image_url VARCHAR(200) );
CREATE TABLE user_preference ( user_id INT, min_price DECIMAL(10,2), max_price DECIMAL(10,2), preferred_type VARCHAR(20), preferred_fuel VARCHAR(20), FOREIGN KEY (user_id) REFERENCES user(id) );
CREATE TABLE recommendation_history ( id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, car_id INT, score FLOAT, timestamp DATETIME, FOREIGN KEY (user_id) REFERENCES user(id), FOREIGN KEY (car_id) REFERENCES car(id) );