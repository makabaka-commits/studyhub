package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.HealthResponse;
import com.studyhub.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康检查接口")
@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @Operation(summary = "检查服务健康状态")
    @GetMapping
    public Result<HealthResponse> checkHealth() {
        return Result.success(healthService.checkHealth());
    }
}