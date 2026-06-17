package com.studyhub.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传头像
     * @param file 头像文件
     * @return 可访问的头像 URL
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 上传笔记图片
     * @param file 图片文件
     * @return 可访问的图片 URL
     */
    String uploadImage(MultipartFile file);
}
