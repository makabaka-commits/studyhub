package com.studyhub.controller;

import com.studyhub.common.ErrorCode;
import com.studyhub.common.Result;
import com.studyhub.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "基础测试接口")
@RestController
public class HelloController {

    @Operation(summary = "Hello 测试")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello StudyHub");
    }

    @Operation(summary = "异常测试")
    @GetMapping("/error-test")
    public Result<String> errorTest() {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
