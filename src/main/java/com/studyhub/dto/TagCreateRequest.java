package com.studyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagCreateRequest {

    @NotBlank(message = "tag name cannot be blank")
    @Size(max = 50, message = "tag name length cannot exceed 50")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}