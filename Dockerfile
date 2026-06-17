# ===== 第一阶段：构建 =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先复制 pom.xml 下载依赖（利用 Docker 缓存层）
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# 复制源码并编译
COPY src ./src
RUN mvn package -DskipTests -B

# ===== 第二阶段：运行 =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从第一阶段复制构建好的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]