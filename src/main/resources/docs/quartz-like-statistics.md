# 点赞统计定时任务系统

## 📋 概述

本系统基于Quartz实现定时任务，从MySQL的like表中汇集点赞信息并存储到Redis中，提供高性能的点赞数据查询服务。

## 🏗️ 系统架构

### 核心组件

1. **定时任务调度**
   - `QuartzConfig` - Quartz配置类
   - `LikeStatisticsJob` - 点赞统计任务Job
   - 使用内存模式，无需额外数据库表

2. **缓存服务**
   - `LikeCacheService` - 缓存服务接口
   - `LikeCacheServiceImpl` - 缓存服务实现
   - `RedisConfig` - Redis配置（FastJson2序列化）

3. **数据访问**
   - `LikeMapper` - 增强的点赞数据访问层
   - `LikeServiceImpl` - 集成缓存的业务逻辑层

4. **管理接口**
   - `TaskController` - 定时任务管理控制器
   - 提供手动触发、状态查询、缓存管理等功能

## ⚙️ 配置说明

### 定时任务配置 (application.yml)
```yaml
spring:
  quartz:
    job-store-type: memory  # 使用内存模式
    properties:
      org:
        quartz:
          scheduler:
            instanceName: LikeStatisticsScheduler
            instanceId: AUTO
          threadPool:
            class: org.quartz.simpl.SimpleThreadPool
            threadCount: 5
            threadPriority: 5
```

### Redis缓存配置
- **用户点赞缓存**: `like:user:count:{userId}`
- **博文点赞缓存**: `like:article:count:{articleId}`
- **缓存过期时间**: 24小时
- **序列化方式**: FastJson2

## 🔄 执行流程

### 定时任务执行流程
1. **触发时机**: 每10分钟执行一次 (`0 */10 * * * ?`)
2. **数据统计**: 
   - 统计每个用户的点赞总数
   - 统计每个博文的被点赞数
3. **缓存更新**: 将统计结果批量存储到Redis
4. **日志记录**: 记录执行时间、数据量等信息

### 缓存策略
- **读取优先级**: 缓存 > 数据库
- **写入策略**: 数据库更新后同步更新缓存
- **失效策略**: 24小时自动过期 + 手动清理

## 🔗 API接口

### 任务管理接口 (`/api/admin/tasks`)

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| `POST` | `/like-statistics/trigger` | 手动触发统计任务 | 立即执行一次统计 |
| `GET` | `/like-statistics/status` | 获取任务状态 | 查看任务运行状态 |
| `POST` | `/like-statistics/pause` | 暂停定时任务 | 停止自动执行 |
| `POST` | `/like-statistics/resume` | 恢复定时任务 | 重新开始自动执行 |

### 缓存管理接口

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| `GET` | `/cache/statistics` | 获取缓存统计 | 缓存数量、更新时间等 |
| `DELETE` | `/cache/clear` | 清空所有缓存 | 清理所有点赞缓存 |
| `GET` | `/cache/user/{userId}/likes` | 查询用户点赞数 | 从缓存获取 |
| `GET` | `/cache/article/{articleId}/likes` | 查询博文点赞数 | 从缓存获取 |

## 🚀 部署说明

### 1. 数据库准备
```sql
-- 执行点赞表创建脚本
source /src/main/resources/sql/like_table.sql

-- 如果需要使用JDBC模式的Quartz，执行以下脚本
-- source /src/main/resources/sql/quartz_tables.sql
```

### 2. 启动应用
系统启动后会自动：
- 注册定时任务
- 开始按计划执行点赞统计
- 初始化Redis连接

### 3. 验证运行
```bash
# 检查定时任务状态
curl -X GET http://localhost:8080/api/admin/tasks/like-statistics/status

# 手动触发一次统计
curl -X POST http://localhost:8080/api/admin/tasks/like-statistics/trigger

# 查看缓存统计
curl -X GET http://localhost:8080/api/admin/tasks/cache/statistics
```

## 📊 性能优化

### 缓存优势
- **查询性能**: Redis查询比MySQL快10-100倍
- **减少数据库压力**: 大部分查询走缓存
- **高并发支持**: Redis支持更高的并发访问

### 定时任务优化
- **批量操作**: 一次性处理所有数据
- **内存模式**: 避免数据库存储任务信息
- **异步执行**: 不阻塞主业务流程

### 容错机制
- **缓存降级**: 缓存失败时自动查询数据库
- **异常处理**: 完善的错误日志和异常捕获
- **事务保证**: 数据库操作的事务一致性

## 🔧 监控与维护

### 日志监控
- 任务执行日志: 执行时间、处理数据量
- 缓存操作日志: 缓存命中率、失败原因
- 错误日志: 异常堆栈、错误原因

### 性能指标
- 任务执行时间
- 缓存命中率
- 数据库查询次数
- Redis连接状态

### 运维建议
1. **监控任务执行**: 关注任务是否正常执行
2. **缓存预热**: 系统启动后手动触发一次统计
3. **定期清理**: 根据需要清理过期缓存
4. **性能调优**: 根据数据量调整执行频率

## 📝 扩展说明

### 自定义执行频率
修改 `QuartzConfig.java` 中的 cron 表达式：
```java
// 每小时执行
.withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))

// 每天凌晨2点执行  
.withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
```

### 添加新的统计任务
1. 创建新的Job类
2. 在QuartzConfig中注册JobDetail和Trigger
3. 实现相应的缓存服务

### 缓存策略调整
- 修改过期时间: `CACHE_EXPIRE_HOURS`
- 更改Redis key前缀: 修改对应常量
- 调整序列化方式: 更新RedisConfig

## ⚠️ 注意事项

1. **Redis连接**: 确保Redis服务正常运行
2. **内存使用**: 监控Redis内存使用情况
3. **数据一致性**: 定时任务执行期间的数据变更
4. **并发控制**: 避免任务重复执行
5. **网络延迟**: 考虑Redis和MySQL的网络延迟

---

## 🎯 使用建议

- **开发环境**: 使用较短的执行间隔进行测试
- **生产环境**: 根据数据量和业务需求调整执行频率
- **高峰期**: 避免在业务高峰期执行大量统计任务
- **监控告警**: 设置任务执行失败的告警机制