# StudyHub 📚

一个基于 Spring Boot 3 的学习笔记分享平台。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.3.5 |
| MyBatis-Plus | 3.5.9 |
| MySQL | 8.0 |
| Redis | 7 |
| RabbitMQ | 3.x |
| JWT | 0.12.6 |
| Knife4j | 4.5.0 |

## 功能特性

- ✅ 用户注册/登录（JWT 认证）
- ✅ 笔记发布/编辑/删除/搜索
- ✅ 标签系统（创建/绑定/查询）
- ✅ 评论系统
- ✅ 点赞/收藏
- ✅ 通知系统（数据库 + WebSocket 实时推送）
- ✅ 浏览历史
- ✅ 个人中心（我的笔记/评论/收藏）
- ✅ AI 摘要生成 + 智能问答（DeepSeek API）
- ✅ 文件上传（头像/图片）
- ✅ 管理员后台（笔记审核/用户管理/数据统计）
- ✅ Markdown 渲染
- ✅ 接口限流（Redis 滑动窗口）
- ✅ XSS 防护
- ✅ WebSocket 实时通知

## 快速启动

### 前置要求

- JDK 17+
- MySQL 8.0+
- Redis 7+
- Maven 3.9+

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS studyhub
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化表结构

执行 `sql/init.sql` 创建所有表。

### 3. 配置环境变量

创建 `.env` 文件或设置环境变量：

```bash
# 数据库配置
STUDYHUB_DB_URL=jdbc:mysql://localhost:3306/studyhub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
STUDYHUB_DB_USERNAME=root
STUDYHUB_DB_PASSWORD=your_password

# Redis 配置
STUDYHUB_REDIS_HOST=localhost
STUDYHUB_REDIS_PORT=6379

# AI 配置（可选）
DEEPSEEK_API_KEY=your_api_key
```

### 4. 启动项目

```bash
# 开发环境
mvn spring-boot:run

# 生产环境
mvn package -DskipTests
java -jar target/studyhub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. 使用 Docker 启动

```bash
docker-compose up -d
```

## API 文档

启动项目后访问：

- Knife4j UI: http://localhost:8080/api/v1/doc.html
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html

### 核心 API 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/register | 用户注册 |
| POST | /api/v1/auth/login | 用户登录 |
| GET | /api/v1/auth/me | 获取当前用户 |
| PUT | /api/v1/auth/me/profile | 修改个人资料 |
| PUT | /api/v1/auth/me/password | 修改密码 |
| POST | /api/v1/notes | 发布笔记 |
| GET | /api/v1/notes | 笔记列表 |
| GET | /api/v1/notes/{id} | 笔记详情 |
| GET | /api/v1/notes/page | 分页搜索笔记 |
| GET | /api/v1/notes/hot | 热门笔记 |
| PUT | /api/v1/notes | 修改笔记 |
| DELETE | /api/v1/notes/{id} | 删除笔记 |
| POST | /api/v1/notes/{id}/summary | 生成 AI 摘要 |
| POST | /api/v1/notes/{id}/ask | AI 智能问答 |
| GET | /api/v1/notes/{id}/recommend | 推荐相似笔记 |
| POST | /api/v1/notes/{id}/like | 点赞 |
| DELETE | /api/v1/notes/{id}/like | 取消点赞 |
| POST | /api/v1/notes/{id}/favorite | 收藏 |
| DELETE | /api/v1/notes/{id}/favorite | 取消收藏 |
| POST | /api/v1/comments | 发表评论 |
| GET | /api/v1/comments/note/{noteId} | 查询评论 |
| DELETE | /api/v1/comments/{id} | 删除评论 |
| POST | /api/v1/tags | 创建标签 |
| GET | /api/v1/tags | 标签列表 |
| POST | /api/v1/tags/bind | 绑定标签到笔记 |
| DELETE | /api/v1/tags/bind | 取消绑定 |
| GET | /api/v1/tags/note/{noteId} | 查询笔记标签 |
| GET | /api/v1/tags/{tagId}/notes | 标签下的笔记 |
| POST | /api/v1/files/avatar | 上传头像 |
| POST | /api/v1/files/image | 上传笔记图片 |
| GET | /api/v1/notifications | 通知列表 |
| GET | /api/v1/notifications/unread-count | 未读通知数 |
| PUT | /api/v1/notifications/{id}/read | 标记已读 |
| PUT | /api/v1/notifications/read-all | 全部标记已读 |
| GET | /api/v1/me/notes | 我的笔记 |
| GET | /api/v1/me/comments | 我的评论 |
| GET | /api/v1/me/favorites | 我的收藏 |
| GET | /api/v1/me/browse-history | 浏览历史 |
| GET | /api/v1/admin/dashboard | 数据统计（管理员） |
| GET | /api/v1/admin/notes/pending | 待审核笔记（管理员） |
| PUT | /api/v1/admin/notes/{id}/approve | 审核通过（管理员） |
| PUT | /api/v1/admin/notes/{id}/reject | 拒绝笔记（管理员） |
| GET | /api/v1/admin/users | 用户列表（管理员） |
| PUT | /api/v1/admin/users/{id}/ban | 禁用用户（管理员） |
| PUT | /api/v1/admin/users/{id}/unban | 启用用户（管理员） |
| GET | /api/v1/health | 健康检查 |
| GET | /api/v1/version | 版本信息 |

## 项目结构

```
src/main/java/com/studyhub/
├── common/              # 公共工具类
│   ├── annotation/      # 自定义注解（@RequireAdmin、@RateLimit）
│   ├── aspect/          # AOP 切面（权限检查、限流）
│   ├── CacheUtil.java   # Redis 缓存工具
│   ├── ErrorCode.java   # 错误码枚举
│   ├── JwtUtil.java     # JWT 工具
│   ├── LoginUserHolder.java  # 当前登录用户上下文
│   ├── MarkdownUtil.java     # Markdown 转 HTML
│   ├── Result.java      # 统一响应体
│   ├── TraceIdHolder.java    # 链路追踪 ID
│   └── XssUtil.java     # XSS 防护工具
├── config/              # 配置类
│   ├── AiConfig.java         # AI API 配置
│   ├── JacksonConfig.java    # JSON 序列化配置
│   ├── JwtInterceptor.java   # JWT 拦截器
│   ├── MybatisPlusConfig.java # MyBatis-Plus 分页配置
│   ├── OpenApiConfig.java    # Knife4j 文档配置
│   ├── PasswordConfig.java   # BCrypt 密码加密
│   ├── RabbitConfig.java     # RabbitMQ 配置
│   ├── RequestLogInterceptor.java # 请求日志拦截器
│   ├── WebConfig.java        # Web 总配置（拦截器、CORS、静态资源）
│   ├── WebSocketAuthInterceptor.java  # WebSocket 认证
│   ├── WebSocketConfig.java  # WebSocket STOMP 配置
│   ├── WebSocketUserInterceptor.java  # WebSocket 用户识别
│   └── XssDeserializer.java  # Jackson XSS 反序列化器
├── controller/          # 控制器层
├── converter/           # DTO 转换器
├── dto/                 # 数据传输对象
├── entity/              # 数据库实体
├── exception/           # 异常处理
├── mapper/              # MyBatis-Plus 数据访问层
├── mq/                  # RabbitMQ 消息队列
└── service/             # 业务逻辑层
    └── impl/            # 业务逻辑实现
```

## 项目状态

✅ 核心功能全部完成，可直接用于生产环境。
