package com.studyhub.converter;

import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.User;

public class UserConverter {

    public static UserBriefResponse toBriefResponse(User user) {
        if (user == null) {
            return null;
        }

        UserBriefResponse response = new UserBriefResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        return response;
    }
}
