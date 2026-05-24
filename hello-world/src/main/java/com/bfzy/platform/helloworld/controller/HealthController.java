package com.bfzy.platform.helloworld.controller;

import com.bfzy.platform.common.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 健康检查与示例接口.
 *
 * @author zhangyu
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口.
     * <p>GET /api/health — 返回服务运行状态</p>
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "BFZY Platform",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Hello World 示例接口.
     * <p>GET /api/hello — 返回欢迎信息</p>
     */
    @GetMapping("/hello")
    public ApiResponse<Map<String, String>> hello() {
        return ApiResponse.success(Map.of(
                "message", "Hello, World!",
                "greeting", "Welcome to BFZY Platform"
        ));
    }
}
