package com.app.backend.vo;


import lombok.Data;

@Data
public class PagedArticleVO {
    private Integer page;
    private Integer size;
    private String address;
    private String title;
    private String type;
}
