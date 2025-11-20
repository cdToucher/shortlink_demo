# 短链接生成服务

## 项目概述
这是一个高性能的短链接生成服务，支持QPS达到1000+。使用Spring Boot框架，结合Redis缓存和MySQL/PostgreSQL数据库，实现高效的长短链接转换。

## 架构设计

### 技术栈
- Spring Boot 3.2.0
- Redis (缓存层，提高读取性能)
- MySQL/PostgreSQL (持久化存储)
- JPA (数据访问层)

### 数据库设计
```sql
CREATE TABLE short_urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL UNIQUE,  -- 短码
    original_url VARCHAR(2048) NOT NULL,     -- 原始链接
    business_id VARCHAR(64) NOT NULL,        -- 业务ID（用于业务分段）
    click_count BIGINT DEFAULT 0,           -- 点击次数
    is_active BOOLEAN DEFAULT TRUE,          -- 是否激活
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_short_code (short_code),       -- 短码索引
    INDEX idx_business_id (business_id),     -- 业务ID索引
    INDEX idx_created_at (created_at)        -- 创建时间索引
);
```

### 性能优化策略

1. **Redis缓存**：短链接到长链接的映射缓存在Redis中，提高查询性能
2. **Redis ID生成器**：使用Redis的原子操作生成唯一ID，避免数据库瓶颈
3. **异步计数同步**：点击计数先存储在Redis，定期批量同步到数据库
4. **索引优化**：在关键字段上建立索引，加速查询
5. **连接池配置**：优化数据库和Redis连接池参数

## 核心功能

### 1. 短链接生成
- 使用64进制编码，生成短码
- 支持业务分段（business_id）
- 从随机数开始编码，确保短码随机性

### 2. 链接重定向
- 直接访问短链接实现302重定向
- 统计点击次数

### 3. API接口
- 创建短链接
- 查询原始链接

## 接口文档

### 创建短链接
```
POST /api/short-url/create
参数:
- originalUrl: 原始链接 (必填)
- businessId: 业务ID (可选，默认为"default")
```

### 查询原始链接
```
GET /api/short-url/get/{shortCode}
参数:
- shortCode: 短码
```

### 重定向
```
GET /{shortCode}
直接访问短码，自动重定向到原始链接
```

## 部署配置

### 环境要求
- Java 17+
- Redis 6+
- MySQL 8+ 或 PostgreSQL 12+

### 配置文件
在 `application.yml` 中配置数据库和Redis连接信息：

```yaml
spring:
  datasource:
    # MySQL配置
    url: jdbc:mysql://localhost:3306/short_url_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    # PostgreSQL配置
    # url: jdbc:postgresql://localhost:5432/short_url_db
    # username: postgres
    # password: postgres
    # driver-class-name: org.postgresql.Driver
    
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 1000ms
```

## 性能调优建议

1. **Redis优化**：
   - 使用Redis集群提高可用性和性能
   - 配置适当的内存策略和过期时间

2. **数据库优化**：
   - 定期清理历史数据
   - 使用读写分离减轻主库压力
   - 考虑分库分表应对更大规模数据

3. **应用优化**：
   - 调整JVM参数
   - 使用CDN缓存静态资源
   - 实现请求限流和熔断机制

## 扩展性考虑

1. **水平扩展**：通过负载均衡部署多个实例
2. **ID生成策略**：使用雪花算法支持分布式ID生成
3. **监控告警**：集成Prometheus和Grafana进行监控
4. **安全策略**：添加URL安全检测和访问控制

## 运行项目

```bash
# 构建项目
mvn clean package

# 运行应用
java -jar target/short-url-service-0.0.1-SNAPSHOT.jar
```