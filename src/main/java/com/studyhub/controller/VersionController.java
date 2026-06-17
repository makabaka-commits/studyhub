package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.VersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@Tag(name = "版本信息接口")
@RestController
@RequestMapping("/version")
public class VersionController {

    private final Environment environment;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${project.version:0.0.1-SNAPSHOT}")
    private String version;

    public VersionController(Environment environment) {
        this.environment = environment;
    }

    @Operation(summary = "获取版本信息")
    @GetMapping
    public Result<VersionResponse> getVersion() {
        VersionResponse response = new VersionResponse();
        response.setAppName(appName);
        response.setVersion(version);
        response.setJavaVersion(System.getProperty("java.version"));

        String[] profiles = environment.getActiveProfiles();
        response.setActiveProfile(profiles.length == 0 ? "default" : String.join(",", profiles));

        return Result.success(response);
    }
}