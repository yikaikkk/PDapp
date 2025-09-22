# 摄影地点分享后端服务

一个基于Spring Boot的摄影地点分享平台后端服务，支持用户注册登录、博文管理、图片上传等功能。

## 📋 目录

- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
- [接口文档](#接口文档)
- [核心功能实现](#核心功能实现)
- [数据库设计](#数据库设计)
- [配置说明](#配置说明)
- [部署指南](#部署指南)

## 🛠 技术栈

### 后端框架
- **Spring Boot 3.4.9** - 主框架
- **Spring Web** - Web开发
- **Spring AOP** - 面向切面编程
- **Spring Security** - 安全框架（可选）

### 数据持久化
- **MyBatis-Plus 3.5.7** - ORM框架
- **MySQL 8.0+** - 关系型数据库
- **HikariCP** - 数据库连接池

### 缓存与消息
- **Redis** - 缓存数据库
- **RabbitMQ** - 消息队列
- **Elasticsearch** - 全文搜索引擎

### 安全认证
- **JWT (JSON Web Token)** - 身份认证
- **MD5** - 密码加密（支持BCrypt升级）

### 文件存储
- **MinIO** - 对象存储服务
- **阿里云OSS** - 云存储（可选）

### 工具库
- **Lombok** - 简化代码
- **Hutool** - Java工具类库
- **Knife4j** - API文档生成
- **Apache Commons** - 通用工具库

### 其他服务
- **Spring Mail** - 邮件发送
- **Quartz** - 定时任务调度
- **Thymeleaf** - 模板引擎

## ✨ 功能特性

### 🔐 用户管理
- 用户注册/登录
- JWT Token认证
- 密码加密存储
- 用户信息管理

### 📝 博文管理
- 摄影地点博文发布
- 地理位置定位
- 图片批量上传
- 博文分类管理
- 分页查询支持

### 🖼 图片服务
- 多图片上传
- 图片顺序排序
- MinIO对象存储
- 图片访问URL生成

### 🔍 搜索功能
- 按类型搜索博文
- 地理位置范围查询
- 作者博文查询
- Elasticsearch全文搜索

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- MinIO服务

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE pd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行SQL脚本：
```bash
mysql -u root -p pd < src/main/resources/sql/create_pd_user_table.sql
mysql -u root -p pd < src/main/resources/sql/create_pd_article_table.sql
```

### 配置文件
复制配置模板并修改数据库连接信息：
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pd?serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 启动服务
```bash
# 使用Maven启动
./mvnw spring-boot:run

# 或者打包后启动
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

服务启动后访问：http://localhost:8080

## 📚 接口文档

### 用户认证接口

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123"
}
```

### 博文管理接口

#### 创建博文
```http
POST /api/articles/create
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "北京故宫建筑之美",
  "name": "故宫博物院",
  "latitude": 39.9163447,
  "longitude": 116.3972282,
  "type": "architecture",
  "description": "紫禁城，明清两朝的皇家宫殿",
  "tips": "早晨或傍晚光线最佳",
  "address": "北京市东城区景山前街4号",
  "notice": "需要提前预约",
  "tools": "单反相机，三脚架"
}
```

#### 查询博文列表
```http
GET /api/articles/type/architecture/page?page=1&size=10
```

#### 图片上传
```http
POST /api/articles/images
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

articleId: 123
images: [file1, file2, file3]
```

## 🔧 核心功能实现

### JWT认证实现

#### 1. Token生成
```java
@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    private final long expiration = TimeUnit.HOURS.toMillis(24);
    
    public String generateToken(String username) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
}
```

#### 2. 拦截器验证
```java
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            request.setAttribute("username", username);
        }
        return true;
    }
}
```

#### 3. 白名单配置
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }
}
```

### 密码加密实现

```java
public class PasswordUtils {
    
    public static String encrypt(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
    
    public static boolean matches(String rawPassword, String encryptedPassword) {
        return encrypt(rawPassword).equals(encryptedPassword);
    }
}
```

### 文件上传实现

#### 1. MinIO配置
```yaml
upload:
  mode: minio
  minio:
    url: http://localhost:9000
    endpoint: http://localhost:9000
    accesskey: minioadmin
    secretKey: minioadmin
    bucketName: photos
```

#### 2. 上传Service
```java
@Service
public class FileUploadService {
    
    @Value("${upload.minio.endpoint}")
    private String endpoint;
    
    public String uploadFile(MultipartFile file) {
        // MinIO上传逻辑
        String fileName = generateFileName(file.getOriginalFilename());
        minioClient.putObject(bucketName, fileName, file.getInputStream());
        return getFileUrl(fileName);
    }
}
```

#### 3. 图片排序存储
```java
@Service
public class ArticleImageService {
    
    public void saveArticleImages(Integer articleId, List<MultipartFile> images) {
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = fileUploadService.uploadFile(images.get(i));
            
            ArticleImage articleImage = new ArticleImage();
            articleImage.setArticleId(articleId);
            articleImage.setImageUrl(imageUrl);
            articleImage.setOrderIndex(i + 1); // 设置排序索引
            
            articleImageMapper.insert(articleImage);
        }
    }
}
```

## 🗄 数据库设计

### 用户表 (pd_user)
```sql
CREATE TABLE `pd_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);
```

### 博文表 (pd_article)
```sql
CREATE TABLE `pd_article` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL COMMENT '博文标题',
  `name` VARCHAR(100) NOT NULL COMMENT '地点名称',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `type` VARCHAR(50) NOT NULL COMMENT '类型',
  `description` TEXT COMMENT '地点描述',
  `tips` TEXT COMMENT '摄影提示',
  `author_id` VARCHAR(50) COMMENT '作者ID',
  `address` VARCHAR(255) COMMENT '详细地址',
  `notice` TEXT COMMENT '注意事项',
  `tools` TEXT COMMENT '推荐器材',
  `create_time` DATETIME NOT NULL,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 图片表 (pd_article_image)
```sql
CREATE TABLE `pd_article_image` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `article_id` INT NOT NULL COMMENT '博文ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `order_index` INT NOT NULL COMMENT '排序索引',
  INDEX `idx_article_id` (`article_id`)
);
```

## ⚙️ 配置说明

### 数据库连接池优化
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 2
      idle-timeout: 120000
      maximum-pool-size: 8
      max-lifetime: 240000
      connection-timeout: 10000
      keepalive-time: 60000
```

### Redis缓存配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    lettuce:
      pool:
        max-active: 100
        max-idle: 100
        min-idle: 10
```

### JWT安全配置
```yaml
jwt:
  secret: pdJWTSecretKey2025ForSpringBootBackendProject
  # 密钥长度至少32字符，确保安全性
```

## 🚀 部署指南

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - minio
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/pd
      - SPRING_REDIS_HOST=redis
      - UPLOAD_MINIO_ENDPOINT=http://minio:9000

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: pd
    ports:
      - "3306:3306"

  redis:
    image: redis:6.2
    ports:
      - "6379:6379"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
```

## 📖 开发规范

### 代码规范
- 使用Lombok简化代码
- 统一异常处理
- JWT拦截器统一认证
- 密码加密存储

### 安全规范
- 敏感接口需要JWT认证
- 密码使用MD5加密（建议升级BCrypt）
- 输入参数验证
- SQL注入防护

### 性能优化
- 数据库连接池优化
- Redis缓存策略
- 分页查询优化