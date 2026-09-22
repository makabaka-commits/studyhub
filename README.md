# StudyHub

StudyHub 是一个面向 Java 后端研发岗位的学习笔记分享平台。项目重点展示认证授权、内容管理、互动通知、缓存、消息队列、实时推送、AI 接口集成、测试和容器化部署。

> 当前仓库是后端项目。`project-overview.html` 用于展示架构，接口可以通过 Knife4j、Swagger UI 或 `docs/studyhub.postman_collection.json` 演示。

## 核心功能

- 用户注册、登录、资料和密码管理
- 笔记发布、审核、搜索、推荐与 Markdown 渲染
- 标签、评论、点赞、收藏和浏览历史
- RabbitMQ 异步通知、WebSocket 实时推送
- Redis 缓存、热门排行和 Lua 原子限流
- DeepSeek 笔记摘要与问答，调用失败时提供降级结果
- 管理员审核、用户状态管理和数据仪表盘
- 文件上传、统一异常响应、TraceId 和健康检查

## 技术栈

| 分类 | 技术 |
|---|---|
| 基础框架 | Java 17、Spring Boot 3.3、MyBatis-Plus |
| 数据与缓存 | MySQL 8、Redis 7 |
| 异步与实时 | RabbitMQ 4.3.6、WebSocket、STOMP |
| 安全 | JWT、BCrypt、参数校验、管理员权限切面 |
| 工程化 | Maven、JUnit 5、Mockito、H2、Docker Compose、GitHub Actions |
| 文档 | Springdoc OpenAPI、Knife4j、Apifox/Postman Collection |

## 架构与关键取舍

项目采用 Controller、Service、Mapper 的分层结构，DTO 与数据库实体分离。

- Redis 使用 Cache-Aside 模式缓存列表，并用 ZSet 维护热门笔记。
- 注册和登录限流使用 Redis ZSet 与 Lua 脚本，保证“清理、计数、写入”原子执行。
- 点赞和收藏使用数据库事务；统计字段通过 SQL 原子递增/递减，避免并发覆盖。
- RabbitMQ 将通知落库和浏览历史写入从主请求中解耦；WebSocket 用于在线实时提示。
- 浏览历史按用户与笔记的唯一键原子写入，重复浏览只更新时间，避免并发重复插入。
- 消费失败的消息先写入独立暂存队列，确认保存成功后才确认原消息，避免直接丢弃。
- JWT 密钥由环境变量提供，生产环境不使用仓库中的开发默认值。
- 测试环境使用 H2，避免单元测试依赖本地 MySQL、Redis 和 RabbitMQ。

详细结构可打开 [project-overview.html](project-overview.html)。

## 快速启动

### 不使用 Docker（Windows）

前置要求：JDK 17、MySQL 8、Redis 7；完整演示异步通知和浏览历史还需要 RabbitMQ（本地安装可使用 4.3.6，并搭配兼容的 Erlang/OTP 27）。仓库自带 Maven Wrapper，不必单独安装 Maven。

1. 确认 MySQL 和 Redis 已启动；若要演示通知，再确认 RabbitMQ 已启动。可以在 PowerShell 中检查端口：

   ```powershell
   Test-NetConnection localhost -Port 3306
   Test-NetConnection localhost -Port 6379
   Test-NetConnection localhost -Port 5672
   ```

2. 在 MySQL 客户端执行 `sql/init.sql` 创建新库和表，再执行一次 `sql/seed.sql` 创建演示账号。若是已有旧库，先备份，再根据 [SQL 说明](sql/README.md)执行迁移；不要直接重新灌入种子数据。
3. 在项目根目录打开 PowerShell，按实际配置设置环境变量，然后启动：

   ```powershell
   $env:STUDYHUB_DB_USERNAME = "root"
   $env:STUDYHUB_DB_PASSWORD = "你的 MySQL 密码"
   $env:STUDYHUB_JWT_SECRET = "替换成至少 32 字节的本地密钥"
   .\mvnw.cmd clean test
   .\mvnw.cmd spring-boot:run
   ```

   环境变量只对当前 PowerShell 窗口及其启动的程序生效；项目根目录的 `.env` 文件不会被 `spring-boot:run` 自动读取。启动后访问 `http://localhost:8080/api/v1/health`，再按 [Apifox 演示指南](docs/apifox-guide.md)发送请求。

健康检查返回数据库、Redis 和 RabbitMQ 三项状态；任一项不可用时总体状态为 `DEGRADED`。RabbitMQ 未启动时，点赞和评论等主操作可能返回成功，但异步通知不会落库；不能把这当作通知功能验证通过。完整面试演示应启动 RabbitMQ 并检查消费者日志和通知列表。

消费失败的通知（包括延迟通知）保留在 `studyhub.queue.notification.failed`，浏览历史保留在 `studyhub.queue.browse.history.failed`。它们是持久化的失败消息暂存队列，**不会自动重试**。排查原始异常和数据库状态后再人工处理，不要直接清空或批量重投；重复投递可能产生重复通知。新增的是独立队列，不修改已有业务队列的参数。

### Docker Compose（可选）

若以后需要一键部署，再安装 Docker 与 Docker Compose，运行：

```bash
docker compose up -d --build
```

首次创建 MySQL 数据卷时会自动执行 `sql/init.sql` 和 `sql/seed.sql`。已有旧数据库时，先备份并按 [SQL 说明](sql/README.md)迁移。

常用环境变量：

```text
STUDYHUB_DB_URL
STUDYHUB_DB_USERNAME
STUDYHUB_DB_PASSWORD
STUDYHUB_REDIS_HOST
STUDYHUB_RABBITMQ_HOST
STUDYHUB_RABBITMQ_USERNAME
STUDYHUB_RABBITMQ_PASSWORD
STUDYHUB_JWT_SECRET
DEEPSEEK_API_KEY
```

## 接口文档与 Apifox

应用启动后：

- Knife4j：`http://localhost:8080/api/v1/doc.html`
- Swagger UI：`http://localhost:8080/api/v1/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/api/v1/v3/api-docs`
- RabbitMQ 管理页：`http://localhost:15672`

Apifox 可以直接导入：

```text
docs/studyhub.postman_collection.json
```

集合中的脚本会自动保存登录 Token 和新建笔记 ID。完整流程见 [Apifox 演示指南](docs/apifox-guide.md)。

执行 `sql/seed.sql` 后提供以下开发演示账号，密码均为 `password`：

- `test03`：管理员
- `user_b`：普通用户

## 测试

```powershell
.\mvnw.cmd clean test
```

当前测试覆盖：

- 注册、登录和参数校验
- 用户资料与密码修改
- 笔记创建、详情、分页与权限校验
- 浏览历史消息的用户 ID 传递、匿名访问处理与原子写入
- RabbitMQ 消费失败暂存、暂存失败重入队和健康状态判断
- Spring 应用上下文加载

如已在本机启动 RabbitMQ，并使用默认的本地 `guest/guest` 账号，可选运行真实 broker 集成测试。该测试仅创建并清理一个随机命名的临时队列，不使用业务队列：

```powershell
$env:STUDYHUB_RABBIT_INTEGRATION = "true"
.\mvnw.cmd -Dtest=RabbitParkingIntegrationTest test
```

后续可补充真实 MySQL/Redis 集成测试、RabbitMQ 端到端测试和并发互动测试。本机不安装 Docker 也可以运行现有测试；Testcontainers 则需要容器运行时。

## API 示例

登录：

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user_b",
  "password": "password"
}
```

访问受保护接口：

```http
Authorization: Bearer <token>
```

错误响应同时使用正确的 HTTP 状态码和统一响应体：

```json
{
  "code": 401,
  "message": "please login",
  "traceId": "...",
  "data": null
}
```

## 已知限制

- 通知采用常规异步消息，尚未实现 Outbox 最终一致性方案。
- 文件存储使用本地磁盘，生产环境应替换为对象存储。
- 推荐算法目前基于标签相似度，不是个性化推荐模型。
- WebSocket 开发环境通过查询参数传递 Token，生产环境建议改为短期握手票据。
- 消费失败消息会进入暂存队列，但暂未提供自动重试和人工重投工具；生产化前仍需补充幂等键、告警与补偿流程。

这些限制被保留为后续演进方向，而不是将项目描述为可直接投入生产。
