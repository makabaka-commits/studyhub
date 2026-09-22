# Apifox 演示指南

## 启动与导入

先按 [README 的无 Docker 步骤](../README.md)启动后端、MySQL 和 Redis。要演示异步通知，还必须启动 RabbitMQ。Apifox 只是发送请求的工具，不负责启动这些服务。

推荐直接导入仓库中的 `docs/studyhub.postman_collection.json`。Apifox 支持 Postman Collection 2.1 格式。

在 Apifox 项目中选择“项目设置 → 导入数据 → Postman → 文件导入”。如果之前导入过旧版集合，建议先在 Apifox 中确认新集合的 14 个接口，避免同名接口重复。导入后检查 `baseUrl` 为 `http://localhost:8080/api/v1`；如果 Apifox 未映射集合变量，请在项目变量或环境中手动设置。

也可以在应用启动后通过 OpenAPI 地址导入：

```text
http://localhost:8080/api/v1/v3/api-docs
```

本地接口基地址：

```text
http://localhost:8080/api/v1
```

## 演示顺序

1. 执行“01 用户注册”“02 用户登录”，脚本会保存作者的 `userToken`。重复演示时，如果注册提示用户名已存在，可跳过注册直接登录，或修改 `username` 变量。
2. 执行“03 发布笔记”，脚本会保存 `noteId`；新笔记处于待审核状态。
3. 执行“04 管理员登录”“05 审核通过笔记”，脚本会单独保存 `adminToken`。管理员账号须先由 `sql/seed.sql` 创建。
4. 执行“06 分页查询笔记”“07 查询笔记详情”。
5. 执行“08 互动用户注册”“09 互动用户登录”，脚本会保存第二个用户的 `interactorToken`；然后用第二个用户执行“10 点赞笔记”“11 发表评论”。如果第二个用户名已存在，同样跳过注册直接登录或修改 `interactorUsername`。作者给自己的笔记点赞不会产生点赞通知，所以这里必须使用两个账号。
6. 用作者账号执行“12 作者查询通知”，最后执行健康检查与管理员仪表盘。通知由 RabbitMQ 异步消费，若列表暂时为空，确认 RabbitMQ 已启动、消费者正常后稍等再查。

如果导入后登录 Token 或笔记 ID 没有自动写入变量，就从响应的 `data.token` / `data` 复制到 `userToken`、`adminToken`、`interactorToken`、`noteId`。不要把 `noteId` 留空后执行互动接口。

执行 `sql/seed.sql` 后的演示账号：

- 管理员：`test03` / `password`
- 普通用户：`user_b` / `password`

已有旧数据库时，先备份并按 [SQL 说明](../sql/README.md)判断是否需要迁移；不要在已有数据上直接反复执行 `seed.sql`。

## 面试演示建议

- 演示前先执行一次 `mvn clean test`。
- 展示后端健康检查中 MySQL、Redis、RabbitMQ 三项状态；若演示异步通知，另展示消费者日志及作者通知列表。
- 演示成功请求的同时，展示一次 400、401、403 响应，说明 HTTP 状态码和统一响应体的设计。
- 解释点赞/收藏为什么使用事务和数据库原子计数，以及通知为什么异步发送。
