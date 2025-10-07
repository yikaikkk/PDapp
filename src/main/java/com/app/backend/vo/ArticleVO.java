package com.app.backend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ArticleVO {
    private String title;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String type;
    private String description;
    private String tips;
    private String author_id;
    private String notice;
    private String tools;
    private String address;
    private List<String> accessUrl;
    private Long userId;
}
