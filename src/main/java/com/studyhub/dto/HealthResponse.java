package com.studyhub.dto;

public class HealthResponse {

    private String status;
    private String database;
    private String redis;
    private String rabbitmq;
    private Long timestamp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRedis() {
        return redis;
    }

    public void setRedis(String redis) {
        this.redis = redis;
    }

    public String getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(String rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
