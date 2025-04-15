package com.example.Personalized_Car_Recommendation_System.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImgController {
    private static final Logger log = LogManager.getLogger(CarController.class);
    private static final String IMAGE_DIR = "src/main/resources/assets";

    /**
     * 获取所有图片的文件名列表
     * @return 图片文件名列表
     */
    @GetMapping
    public List<String> getAllImages() {
        // 这里简单模拟获取图片文件名列表，实际应用中需要遍历目录
        List<String> imageNames = new ArrayList<>();
        imageNames.add("car1.jpg");
        imageNames.add("car2.jpg");
        imageNames.add("car3.jpg");
        imageNames.add("car4.jpg");
        imageNames.add("car5.jpg");
        imageNames.add("car6.jpg");
        imageNames.add("car7.jpg");
        imageNames.add("car8.jpg");
        return imageNames;
    }

    /**
     * 根据图片名获取图片
     * @param imageName 图片名
     * @return 图片资源
     */
    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            // 构建图片文件的路径
            Path imagePath = Paths.get(IMAGE_DIR).resolve(imageName);
            log.info("Image path: " + imagePath.toString());
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}