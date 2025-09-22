package com.app.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePathEnum {

    AVATAR("pdapp/avatar/", "头像路径"),

    ARTICLE("pdapp/articles/", "文章图片路径"),

    VOICE("pdapp/voice/", "音频路径"),

    PHOTO("pdapp/photos/", "相册路径"),

    CONFIG("pdapp/config/", "配置图片路径"),

    TALK("pdapp/talks/", "配置图片路径"),

    MD("pdapp/markdown/", "md文件路径");

    private final String path;

    private final String desc;

}

