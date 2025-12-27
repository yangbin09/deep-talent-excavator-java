# 深度天赋挖掘机 (Java Version)

这是一个基于 Spring Boot 重构的“深度天赋挖掘机”后端服务。原项目为 Python/LangGraph 版本，本版本实现了完全相同的业务逻辑和 API 接口。

## 1. 项目简介

- **核心功能**：阶段化深度访谈、信息抽取质检、自动报告生成。
- **技术栈**：
  - Java 17+
  - Spring Boot 3.3.0
  - Spring Data JPA + SQLite
  - DeepSeek API (REST Client)

## 2. 快速开始

### 2.1 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- 环境变量 `DEEPSEEK_API_KEY` 已配置（或在 `application.yml` 中直接修改，但不推荐）。

### 2.2 编译与运行

1. 进入 `java` 目录：
   ```bash
   cd d:\code\心里咨询\html\java
   ```

2. 编译项目：
   ```bash
   mvn clean package
   ```

3. 运行服务：
   ```bash
   java -jar target/deep-talent-excavator-0.0.1-SNAPSHOT.jar
   ```

服务启动后默认监听端口 `8080`。

### 2.3 API 使用说明

#### 开启新会话
- **URL**: `POST /api/chat/start?threadId={your_session_id}`
- **Response**:
  ```json
  {
    "threadId": "test001",
    "message": "你好，我是你的天赋挖掘师..."
  }
  ```

#### 发送消息
- **URL**: `POST /api/chat/message`
- **Body**:
  ```json
  {
    "threadId": "test001",
    "message": "我觉得自己没有什么特长..."
  }
  ```
- **Response**:
  ```json
  {
    "threadId": "test001",
    "message": "这很正常，让我们试着回忆一下..."
  }
  ```

## 3. 迁移说明 (Python -> Java)

| Python 模块 | Java 对应组件 | 说明 |
|------------|--------------|------|
| `src/domain/models.py` | `com.deeptalent.domain.*` | 数据模型转换 (Pydantic -> POJO) |
| `src/graph/*` | `com.deeptalent.service.InterviewService` | LangGraph 逻辑用 Service 实现 |
| `src/llm/client.py` | `com.deeptalent.service.DeepSeekClient` | `RestClient` 封装 DeepSeek API |
| `src/persistence/*` | `com.deeptalent.repository.*` | `SqliteSaver` 替换为 Spring Data JPA |
| `app.py` | `com.deeptalent.controller.InterviewController` | Streamlit UI 逻辑转为 REST API |

## 4. 注意事项

- 数据库文件存储在 `data/deep_talent.sqlite`，运行时自动创建。
- 确保 `pom.xml` 中配置了阿里云镜像源以加速依赖下载（已默认配置）。
