package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pd_article")
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 博文标题
     */
    private String title;
    
    /**
     * 地点名称
     */
    private String name;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 类型（architecture/nature/portrait/street/night）
     */
    private String type;
    
    /**
     * 地点描述
     */
    private String description;
    
    /**
     * 摄影提示
     */
    private String tips;
    
    /**
     * 作者ID
     */
    private String authorId;

    private String notice;

    private String tools;

    private String address;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}