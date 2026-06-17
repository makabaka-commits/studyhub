package com.studyhub.service.impl;

import com.studyhub.dto.AiSummaryRequest;
import com.studyhub.dto.AiSummaryResponse;
import com.studyhub.service.AiSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AiSummaryServiceImpl implements AiSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryServiceImpl.class);

    private final RestClient aiRestClient;

    @Value("${ai.deepseek.model}")
    private String model;

    public AiSummaryServiceImpl(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }

    @Override
    public String generateSummary(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        // 1. 构建请求消息
        AiSummaryRequest request = new AiSummaryRequest();
        request.setModel(model);
        request.setTemperature(0.3);

        AiSummaryRequest.Message systemMsg = new AiSummaryRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent("你是一个笔记摘要助手。请用简洁的语言总结以下笔记内容，控制在100字以内。直接返回摘要内容，不要加任何前缀。");

        AiSummaryRequest.Message userMsg = new AiSummaryRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(content);

        request.setMessages(List.of(systemMsg, userMsg));

        // 2. 调用 API
        try {
            AiSummaryResponse response = aiRestClient.post()
                    .uri("/v1/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(AiSummaryResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "";
        } catch (Exception e) {
            log.error("AI summary generation failed", e);
            String clean = content.replaceAll("\\s+", " ").trim();
            return clean.length() <= 100 ? clean : clean.substring(0, 100) + "...";
        }
    }
}