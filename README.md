# ZrLog Template SPI

Maven 坐标：`com.hibegin:zrlog-template-spi:1.0.0`。纯 Java 11 契约，不依赖博客服务、FreeMarker 或 GraalVM SDK。

每个内置主题 JAR 实现 `com.zrlog.theme.spi.BundledThemeProvider`，并在 `META-INF/services/com.zrlog.theme.spi.BundledThemeProvider` 声明实现类。Provider 声明稳定 id、引擎、是否为默认主题及可选的渲染兼容适配器；主题资源保留在 `include/templates/<id>/`。

`BundledThemes` 通过 ServiceLoader 建立只读注册表。重复 id、多默认主题、非法路径、丢失资源索引均快速失败；无默认 provider 时，在请求默认主题时报告清楚错误，不静默挑选另一个主题。目录判断采用完整路径边界，避免把用户的 `default-copy` 当作内置主题。

主题构建在 Maven `process-classes` 阶段执行 `ThemeResourceIndexer`，根据实际输出资源生成：

- `META-INF/zrlog/themes/<id>.resources`：运行时枚举，不扫描文件系统。
- `META-INF/native-image/com.hibegin/.../resource-config.json`：主题资源、索引、SPI 声明。
- `reflect-config.json`：provider 的公开无参构造器。

服务声明由 GraalVM ServiceLoader 支持发现，资源与构造器还有显式注册；普通 JVM 无需 GraalVM 依赖。生成器随 SPI 发布，不要求各主题复制资源扫描代码。

`./mvnw verify` 执行契约测试。实际 JAR 集成与 Native Image 验收使用测试类 `com.zrlog.theme.spi.RegistrySmoke`，参数为预期 provider 数量，将主题 JAR 加入 classpath 后运行；它会读取所有索引资源并验证默认 provider。验证目录扫描可用不能替代此项 JAR/Native 验收。

发布沿用 Central Portal + GPG：配置 `OSSRH_USERNAME`、`OSSRH_PASSWORD`、`GPG_PRIVATE_KEY`、`GPG_PASSPHRASE`，推送 `v1.0.0` 触发 `central-release`。应先完成本 SPI 的 Maven Central 发布，再发布主题，最后更新核心消费者。主题发布不登记 zrlog-www 市场。
