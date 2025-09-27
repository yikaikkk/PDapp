package com.app.backend.config;

import com.app.backend.task.LikeStatisticsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz定时任务配置类
 */
@Configuration
public class QuartzConfig {

    /**
     * 点赞统计任务JobDetail
     */
    @Bean
    public JobDetail likeStatisticsJobDetail() {
        return JobBuilder.newJob(LikeStatisticsJob.class)
                .withIdentity("likeStatisticsJob", "statisticsGroup")
                .withDescription("点赞数据统计任务")
                .storeDurably()
                .build();
    }

    /**
     * 点赞统计任务触发器 - 每10分钟执行一次
     */
    @Bean
    public Trigger likeStatisticsTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(likeStatisticsJobDetail())
                .withIdentity("likeStatisticsTrigger", "statisticsGroup")
                .withDescription("点赞统计任务触发器")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 */1 * * * ?")) // 每10分钟执行一次
                .build();
    }

    /**
     * 也可以配置为每小时执行一次的触发器
     */
    // @Bean
    // public Trigger likeStatisticsHourlyTrigger() {
    //     return TriggerBuilder.newTrigger()
    //             .forJob(likeStatisticsJobDetail())
    //             .withIdentity("likeStatisticsHourlyTrigger", "statisticsGroup")
    //             .withDescription("点赞统计任务触发器(每小时)")
    //             .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")) // 每小时执行一次
    //             .build();
    // }

    /**
     * 也可以配置为每天凌晨执行一次的触发器
     */
    // @Bean
    // public Trigger likeStatisticsDailyTrigger() {
    //     return TriggerBuilder.newTrigger()
    //             .forJob(likeStatisticsJobDetail())
    //             .withIdentity("likeStatisticsDailyTrigger", "statisticsGroup")
    //             .withDescription("点赞统计任务触发器(每日)")
    //             .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")) // 每天凌晨2点执行
    //             .build();
    // }
}