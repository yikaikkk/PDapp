package com.app.backend.consumer;

import com.alibaba.fastjson2.JSON;
import com.app.backend.service.LikeService;
import com.app.backend.vo.LikeVO;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.app.backend.constant.RabbitMQConstant.LIKE_QUEUE;

@Component
@RabbitListener(queues = LIKE_QUEUE)
public class LikeConsumer {

    @Autowired
    private LikeService likeService;

    @RabbitHandler
    public void process(byte[] data){
        LikeVO likeVO= JSON.parseObject(new String(data),LikeVO.class);
        likeService.submitLikeMessage(likeVO);
    }

}
