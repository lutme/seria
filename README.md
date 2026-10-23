# seria 项目环境说明

## 1. 项目简介

本项目用于学习和实践基于 Spring Cloud Alibaba 的微服务架构，目前包含服务提供者、服务消费者和 Spring Cloud Gateway 网关，并使用 Nacos 进行服务注册发现与配置管理。

> 本文档记录的是当前开发环境和已知项目配置。部分依赖版本来自当前项目记录，提交前建议与项目 `pom.xml`、Maven 依赖树及实际运行日志再次核对。

## 2. 本地开发环境

| 项目 | 版本 / 配置 | 备注 |
|---|---|---|
| 操作系统 | Windows 11（amd64） | 本地开发环境 |
| Java JDK | Oracle JDK 17.0.20.1 LTS | Java 版本 |
| Maven | Apache Maven 3.10.0 | Maven 版本 |
| IDE | IntelliJ IDEA 2026.2.3 | 开发工具版本 |
| 字符编码 | Windows 环境曾显示 GBK；项目编译编码为 UTF-8 | 建议源码、资源文件统一 UTF-8 |
| 时区 | Asia/Shanghai（运行环境记录） | 以操作系统/JVM 实际设置为准 |

## 3. 核心框架与中间件版本

| 组件 | 当前项目记录的版本 | 用途 / 说明 |
|---|---|---|
| Java | 17 | 项目使用的 Java 版本 |
| Spring Boot | 4.0.0 | 应用基础框架 |
| Spring Cloud | 2025.1.0 | 微服务基础组件版本线 |
| Spring Cloud Alibaba | 2025.1.0.0 | Alibaba 微服务组件版本线 |
| Nacos | 3.2.4 | 服务注册发现与配置中心服务端 |

**版本核对提醒：** 上表是当前聊天中记录到的项目版本，不代表所有组合都已被官方确认兼容。尤其是 Spring Boot 4、Spring Cloud 2025.1 与 Spring Cloud Alibaba 2025.1.0.0 的兼容性，应以该版本的官方发布说明、依赖管理和实际集成测试为准。Nacos Server 与 Client 的版本也应分别记录，不能将客户端版本当作服务端版本。

## 4. Maven 项目结构与坐标

- 根项目：`com.nex:seria:1.0-SNAPSHOT`
- 根项目打包类型：`pom`
- 已知模块：
  - `service-provider`
  - `service-consumer`
  - `gateway-service`
- Java 编译版本：`17`
- 项目源码编码：`UTF-8`

建议保持根目录 `pom.xml` 中的 Java 版本、Spring Boot、Spring Cloud 和 Spring Cloud Alibaba 版本集中管理，子模块尽量不要重复声明由父 POM/BOM 管理的依赖版本。

## 5. 服务与端口

| 模块 / 服务 | Spring 应用名 | 端口 / 地址 | 说明 |
|---|---|---|---|
| Nacos Server | — | `127.0.0.1:8848` | 本地 Nacos 服务地址 |
| Gateway | `gateway-service` | `8080` | 网关入口 |
| Provider | `service-provider` | `8081` | 服务提供者 |
| Consumer | `service-consumer` | 以当前配置文件为准 | 服务消费者；端口尚需核对 |

## 6. 当前已实践的功能

- [x] Nacos 服务注册与发现
- [x] Nacos 配置中心读取配置
- [x] 验证 Nacos 配置变更与应用配置刷新
- [x] Spring Cloud Gateway 基础路由
- [x] Gateway 全局请求日志与请求耗时记录
- [x] Gateway CORS 跨域配置
- [x] Gateway 异常处理器的基础实践
- [x] 将 Gateway 路由配置放入 Nacos
- [x] 将 Gateway CORS 配置放入 Nacos
- [ ] Sentinel 流量控制、熔断与降级
- [ ] Redis 缓存与分布式场景实践
- [ ] RabbitMQ 消息队列
- [ ] 完整业务场景与分布式事务实践

> 上述勾选项按当前学习进度整理。动态路由和 CORS 配置更新应以实际测试结果为准。

## 7. 本地启动前检查

1. 确认 JDK 为 Java 17：
   ```powershell
   java -version
   ```
2. 确认 Maven 版本：
   ```powershell
   mvn -version
   ```
3. 确认 Nacos Server 已启动，并能访问 `127.0.0.1:8848`。
4. 确认 Nacos 中所需配置的 Data ID、Group 和内容正确。
5. 确认各服务的 `spring.application.name` 与 Nacos 注册名、Gateway 中的 `lb://服务名` 一致。
6. 按依赖关系启动服务，并检查启动日志中的端口、Nacos 连接与服务注册信息。

## 8. 常用排查命令

查看 Maven 实际使用的 Java 和 Maven 安装路径：

```powershell
mvn -version
```

查看项目依赖树：

```powershell
mvn dependency:tree
```

仅查看 Spring Boot / Spring Cloud Alibaba / Nacos 相关依赖时，可根据项目实际依赖坐标使用 `-Dincludes` 过滤，例如：

```powershell
mvn dependency:tree "-Dincludes=com.alibaba.cloud,com.alibaba.nacos,org.springframework.boot,org.springframework.cloud"
```

## 9. 环境维护建议

- 不要将真实密码、Token 或生产环境凭据提交到 Git。
- 本地开发可使用 `application-local.yml` 或环境变量存放本地差异配置，并将敏感配置加入 `.gitignore` 或通过安全的配置管理方式提供。
- 更新框架版本时，一次只调整一个版本线，并执行完整的 Maven 构建和服务间集成测试。
- 版本升级后，建议重新导出依赖树并更新本 README，确保文档与实际运行环境一致。
