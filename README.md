# 饿了么外卖平台 - 软件工程实训项目

本项目是一个基于真实互联网案例“饿了么”设计并实现的外卖平台系统，旨在通过前后端分离架构完整模拟线上点餐、支付履约、商家运营与平台治理的核心业务闭环。系统严格遵循企业级开发规范，涵盖了需求分析、系统设计、编码实现、测试部署的全流程，是软件工程实训（中级）课程的综合性实践成果。

## 📚 项目简介

系统以“消费者—商家—管理员”三元角色模型为基础，实现了从地理位置检索、商品浏览、购物车管理、订单创建与支付，到商家菜品管理、订单处理、经营数据统计，再到管理员用户/商家清退、数据一致性维护等完整功能。后端采用 **Spring Boot 3 + MyBatis-Plus + JWT** 构建RESTful API，前端使用 **Vue 3 + Pinia + Element Plus** 实现组件化交互，并通过 **JWT** 无状态认证与拦截器权限控制保障接口安全，通过 **Spring 声明式事务** 保证订单等关键写操作的原子性。

## 🧰 技术栈

| 层次       | 技术选型                                                                 |
|------------|--------------------------------------------------------------------------|
| **前端**   | Vue 3 + Composition API + Vue Router + Pinia + Element Plus + Axios     |
| **后端**   | Java 17 + Spring Boot 3.3.6 + MyBatis-Plus + JWT + Spring Security Crypto |
| **数据库** | MySQL 8.0 + HikariCP                                                     |
| **构建工具** | Maven (后端) + Vite / NPM (前端)                                        |
| **接口文档** | Knife4j / Swagger                                                        |
| **开发工具** | IntelliJ IDEA + WebStorm + DataGrip + Postman + Git                     |

## 🧩 主要功能

### 👤 消费者端
- 基于LBS（位置服务）的商家浏览与搜索
- 商品分类筛选、购物车实时金额计算（Pinia状态管理）
- 收货地址管理（增删改查、默认地址）
- 订单创建（服务端计价、事务写入订单主表与明细表）
- 在线支付模拟与15分钟支付倒计时
- 订单状态跟踪（待支付/已支付/已取消等）

### 🏪 商家端
- 店铺信息维护与营业状态切换
- 菜品发布（图片上传 + 表单保存分离）
- 菜品上下架、编辑与删除
- 订单处理（接单、完成等）
- 经营数据看板（今日订单数/收入/访客）

### 👑 管理员端
- 用户/商家信息查看与违规清退
- 级联清理（删除商家时同步清理关联订单、日志等）
- 操作权限校验（仅admin角色可访问）

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+ 与 NPM
- MySQL 8.0
- Maven 3.6+

### 安装步骤

1. **克隆仓库**
   ```bash
   ```

2. **后端配置与启动**
   - 创建数据库 `elm_db`，执行 `sql/init.sql` 初始化表结构。
   - 修改 `backend/src/main/resources/application.properties` 中的数据库连接信息。
   - 进入 `backend` 目录，使用 Maven 打包并运行：
     ```bash
     mvn clean install
     mvn spring-boot:run
     ```
   - 后端默认运行在 `http://localhost:8080`，接口文档访问 `http://localhost:8080/doc.html`（Knife4j）。

3. **前端配置与启动**
   - 进入 `frontend` 目录，安装依赖：
     ```bash
     npm install
     ```
   - 修改 `.env.development` 中的 `VITE_API_BASE_URL` 为后端地址（如 `http://localhost:8080`）。
   - 启动开发服务器：
     ```bash
     npm run dev
     ```
   - 前端默认运行在 `http://localhost:5173`。

4. **默认测试账号**
   - 消费者：`user1` / `123456`
   - 商家：`shop1` / `123456`
   - 管理员：`admin` / `123456`（需提前插入数据库）

## 📁 项目结构

```
elm-training-project/
├── backend/                     # 后端源码
│   ├── src/main/java/...        # 业务代码 (controller, service, mapper, entity)
│   ├── src/main/resources/      # 配置文件、Mapper XML
│   └── pom.xml                  # Maven 依赖
├── frontend/                    # 前端源码
│   ├── src/
│   │   ├── api/                 # API 请求封装
│   │   ├── views/               # 页面组件
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理 (user, cart)
│   │   └── ...
│   ├── package.json             # NPM 依赖
│   └── vite.config.js           # Vite 配置
├── sql/                         # 数据库初始化脚本
└── README.md
```

## ✨ 关键实现亮点

- **JWT 无状态认证**：登录后服务端签发令牌，前端通过 Axios 拦截器自动携带，后端拦截器统一验签与角色鉴权。
- **RBAC 权限控制**：基于拦截器 + 角色枚举实现接口级权限隔离，管理员、商家、消费者各司其职。
- **事务一致性**：订单创建使用 `@Transactional` 保证订单主表与明细表写入的原子性，避免脏数据。
- **存算分离**：菜品图片等静态资源以文件形式存储（如 `C:/elm_upload/`），数据库仅保存访问路径，降低数据库 I/O 压力。
- **级联清理**：管理员删除商家时，通过事务手动清理关联订单、日志等，确保数据完整性。
- **前端状态管理**：使用 Pinia 统一管理购物车、用户信息，实现跨页面数据同步与实时金额计算。
- **接口文档**：集成 Knife4j 自动生成 API 文档，前后端契约清晰，便于联调。

## 🧪 测试与部署

- **单元测试**：JUnit 5 覆盖核心业务逻辑（订单创建、金额计算、权限校验）。
- **接口测试**：Postman 脚本模拟完整业务流程（注册→登录→地址添加→下单→支付）。
- **前端调试**：Vue Devtools 检查 Pinia 状态、组件数据流与路由。
- **部署**：
  - 后端打包为可执行 JAR：`mvn package`，运行 `java -jar target/*.jar`
  - 前端构建生产静态资源：`npm run build`，产出 `dist/` 目录，可部署至 Nginx 等服务器。

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request。请确保代码风格符合项目规范，并补充必要的测试用例。

## 📄 许可证

本项目为课程实训作品，仅供学习交流使用，未经许可不得用于商业用途。

---

**项目状态**：已完成核心业务闭环，具备可演示、可测试、可扩展的基础，后续可优化高并发处理、引入缓存与消息队列等。






<img width="216" height="460" alt="image" src="https://github.com/user-attachments/assets/a3b7ecdf-6e79-4d11-8b24-c04067dfc344" />
<img width="217" height="462" alt="image" src="https://github.com/user-attachments/assets/ddd195e3-f459-42b8-a4c9-6d459d3007b1" />
<img width="214" height="459" alt="image" src="https://github.com/user-attachments/assets/e5896235-9482-4692-ad6f-898e41de6aae" />
<img width="335" height="731" alt="image" src="https://github.com/user-attachments/assets/6597111f-31d5-490f-9215-3aa2627e06dd" />
<img width="338" height="732" alt="image" src="https://github.com/user-attachments/assets/f0cd3d60-b089-437b-84b9-83a3a882823a" />
<img width="332" height="723" alt="image" src="https://github.com/user-attachments/assets/c6cb5b83-cf02-4a78-8ff8-4a99f452cb2d" />
<img width="341" height="724" alt="image" src="https://github.com/user-attachments/assets/c0f3a84a-8fd1-4d73-9d7e-d3596bf27017" />
<img width="218" height="465" alt="image" src="https://github.com/user-attachments/assets/8284b71a-c99d-491a-8375-38f9e66a950a" />
<img width="226" height="469" alt="image" src="https://github.com/user-attachments/assets/d128f861-22e5-41c3-b9c6-0148b3aaad74" />
<img width="218" height="471" alt="image" src="https://github.com/user-attachments/assets/cc4dbee4-46d9-4a96-ac44-a883c1157fc5" />
<img width="225" height="494" alt="image" src="https://github.com/user-attachments/assets/74ac447f-d887-4491-8c4d-aab092e85924" />
<img width="231" height="496" alt="image" src="https://github.com/user-attachments/assets/d5601296-5fe0-4ec8-927e-d037eb5c89af" />
<img width="228" height="491" alt="image" src="https://github.com/user-attachments/assets/45188b10-d858-46a0-84e5-735f6f2432bd" />
<img width="218" height="471" alt="image" src="https://github.com/user-attachments/assets/7ae2a69c-e160-4ba2-b28d-7acb662cafb8" />
<img width="217" height="468" alt="image" src="https://github.com/user-attachments/assets/2ec23edb-3068-4bcc-82f9-14f84e4cfbc5" />
<img width="256" height="552" alt="image" src="https://github.com/user-attachments/assets/c9672c9c-d1b3-4ad2-9086-2244eda4aaaf" />
<img width="259" height="549" alt="image" src="https://github.com/user-attachments/assets/d472541b-1fb3-41f0-af09-583e5ba66a47" />
<img width="219" height="472" alt="image" src="https://github.com/user-attachments/assets/8d3ceca0-3504-4661-95fa-77cc2e2a0305" />
