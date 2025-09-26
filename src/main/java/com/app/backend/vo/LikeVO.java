package com.app.backend.vo;

import lombok.Data;

@Data
public class LikeVO {
    private Long uerId;
    private Integer articleId;

    //1 为点赞 0为取消
    private Integer operationType;
}
