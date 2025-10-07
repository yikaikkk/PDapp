package com.app.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.backend.entity.Collect;
import com.app.backend.mapper.CollectMapper;
import com.app.backend.service.CollectService;
import org.springframework.stereotype.Service;

/**
 * 收藏服务实现类
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {
    // 可以添加自定义的业务实现
}