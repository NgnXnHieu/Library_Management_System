package com.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * API test trả về chuỗi text và in ra console:
     * URL: GET http://localhost:8080/api/public/hello hoặc http://localhost:8080/api/hello
     * Có thể truyền tham số: GET http://localhost:8080/api/public/hello?name=Antigravity
     */
    @GetMapping({"/public/hello", "/hello"})
    public String sayHello(@RequestParam(defaultValue = "World") String name) {
        String message = "Hello, " + name + "!";

        // In thông điệp ra console / log terminal
        System.out.println(">>> [CONSOLE OUTPUT] " + message);
        log.info(">>> [LOG OUTPUT] API /api/public/hello được gọi: {}", message);

        return message;
    }

    /**
     * API test trả về định dạng JSON:
     * URL: GET http://localhost:8080/api/public/hello/json
     */
    @GetMapping({"/public/hello/json", "/hello/json"})
    public Map<String, Object> sayHelloJson(@RequestParam(defaultValue = "World") String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Hello, " + name + "!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
