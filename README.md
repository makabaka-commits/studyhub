# StudyHub

一个学习笔记分享平台的后端。用户可以发布笔记，管理员审核后公开展示；其他用户可以搜索、评论、点赞和收藏。通知与浏览历史由 RabbitMQ 异步处理，热门笔记和列表查询使用 Redis。

仓库以 Spring Boot API 为主体，没有独立前端。可以通过 [接口文档](#接口与验证)或 [Apifox 请求集合](docs/studyhub.postman_collection.json)体验完整流程；[架构概览](project-overview.html)是单独的说明页面。

## 技术栈

Java 17 · Spring Boot 3.3 · MyBatis-Plus · MySQL 8 · Redis 7 · RabbitMQ · WebSocket/STOMP · JWT · Maven

## 业务流程

1. 用户注册、登录，发布 Markdown 笔记；新笔记默认待审核。
2. 管理员审核通过后，笔记进入公开列表，可按条件查询，也会参与热门排行。
3. 其他用户点赞、收藏或评论；通知异步入库，在线用户可收到 WebSocket 推送。
4. 阅读笔记时记录浏览量和浏览历史。可选调用 DeepSeek 生成摘要或进行笔记问答；没有配置 API Key 时不影响核心流程。

此外还包括标签管理、文件上传、用户资料与密码修改、管理员用户管理和数据统计。

## 几个实现细节

- **互动计数**：点赞、收藏的关系记录与计数更新放在数据库事务中，计数使用 SQL 原子增减，避免并发请求覆盖彼此的结果。见 [`InteractionServiceImpl`](src/main/java/com/studyhub/service/impl/InteractionServiceImpl.java) 和相关 Mapper。
- **缓存与限流**：公开笔记列表采用 Cache-Aside，内容变化时清理缓存；热门笔记使用 Redis ZSet。注册、登录限流通过 Lua 脚本将过期记录清理和计数合并为一次原子操作。见 [`NoteServiceImpl`](src/main/java/com/studyhub/service/impl/NoteServiceImpl.java) 和 [`RateLimitAspect`](src/main/java/com/studyhub/common/aspect/RateLimitAspect.java)。
- **异步消息**：通知、浏览历史与主请求解耦。消费失败时先将原消息持久化到独立暂存队列，确认保存成功后才确认原消息；暂存失败则重新入队。暂存队列不自动重试，需要人工排查。见 [`FailedMessageHandler`](src/main/java/com/studyhub/mq/FailedMessageHandler.java)。

项目按 Controller → Service → Mapper 分层，接口使用 DTO，不直接暴露数据库实体。JWT 密钥支持通过环境变量配置；统一错误响应包含 HTTP 状态码和 TraceId。

## 本地运行（Windows，无需 Docker）

需要 JDK 17、MySQL 8、Redis 7 和 RabbitMQ。仓库自带 Maven Wrapper，不必单独安装 Maven。若只验证部分同步接口，可以暂不启动 RabbitMQ；要验证通知和浏览历史，必须启动它。

1. 启动 MySQL、Redis、RabbitMQ。在 MySQL 客户端依次执行 [`sql/init.sql`](sql/init.sql) 和 [`sql/seed.sql`](sql/seed.sql)。种子脚本只在新库执行一次；已有数据库请先看 [数据库脚本说明](sql/README.md)，不要重复灌入演示数据。
2. 在项目根目录打开 PowerShell，设置本机凭据并启动服务：

   ```powershell
   $env:STUDYHUB_DB_USERNAME = "root"
   $env:STUDYHUB_DB_PASSWORD = "你的 MySQL 密码"
   $env:STUDYHUB_JWT_SECRET = "替换成至少 32 字节的随机密钥"
   .\mvnw.cmd spring-boot:run
   ```

   默认连接本机的 MySQL `3306`、Redis `6379`、RabbitMQ `5672`，服务监听 `8080`。使用其他地址或账号时，可设置 `STUDYHUB_DB_URL`、`STUDYHUB_REDIS_HOST`、`STUDYHUB_RABBITMQ_HOST`、`STUDYHUB_RABBITMQ_USERNAME` 和 `STUDYHUB_RABBITMQ_PASSWORD`。项目根目录的 `.env` **不会**被上述启动命令自动读取。
3. 访问 [`http://localhost:8080/api/v1/health`](http://localhost:8080/api/v1/health)。响应会分别给出数据库、Redis 和 RabbitMQ 的状态；依赖不可用时总体状态为 `DEGRADED`。

演示数据包含管理员 `test03` 和普通用户 `user_b`，初始密码均为 `password`，**仅用于本地演示**。如果运行旧数据库，账号和密码以实际数据为准。

也提供可选的 [`docker-compose.yml`](docker-compose.yml)，但本地运行不依赖 Docker。

## 接口与验证

服务启动后可访问 [Knife4j](http://localhost:8080/api/v1/doc.html)、[Swagger UI](http://localhost:8080/api/v1/swagger-ui.html) 或 [OpenAPI JSON](http://localhost:8080/api/v1/v3/api-docs)。

推荐将 [`docs/studyhub.postman_collection.json`](docs/studyhub.postman_collection.json) 导入 Apifox，按注册 → 登录 → 发布 → 审核 → 互动 → 查询通知的顺序执行。集合会保存 Token 和新建笔记 ID；详细步骤见 [Apifox 指南](docs/apifox-guide.md)。通知是异步的，查询前应确认 RabbitMQ 正常运行。

运行自动化测试：

```powershell
.\mvnw.cmd clean test
```

常规测试覆盖认证、笔记、互动、浏览历史、消息失败暂存及健康检查，使用 H2 和模拟依赖，不要求本机启动整套服务。GitHub Actions 会在推送时执行 Maven 验证。真实 RabbitMQ 暂存队列测试是可选的，运行方式见 [`RabbitParkingIntegrationTest`](src/test/java/com/studyhub/mq/RabbitParkingIntegrationTest.java)。

## 当前边界

- 通知采用普通异步投递，尚未实现 Outbox；消息失败暂存后也没有自动重试或人工重投工具。
- 上传文件保存在本地磁盘；WebSocket 开发环境通过查询参数传递 Token，均不适合直接照搬到生产环境。
- 推荐基于标签相似度，不是个性化模型；AI 功能依赖外部 API。
