package com.popstars.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 启动检查接口。
 * 浏览器或前端访问 /api/health，能返回文字就说明后端已经跑起来了。
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("message", "消灭星星后端已启动");
        return result;
    }
}
