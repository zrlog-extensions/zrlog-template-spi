# 维护约定

- 这是 zrlog-extensions 下的独立 Maven Central 制品，使用 Java 11。
- 主题源码和包内路径保持稳定，不复制回博客渲染工程；不登记主题市场。
- 原生主题版本与 template.properties 一致；Hexo 版本严格与 package.json 一致，保留上游来源、许可证与版权说明。
- SPI provider 与资源清单的变更必须通过 JVM JAR 和 Native Image 验证。生成资源索引与 native 元数据，不手工添加路径白名单。
- 发布前运行 ./mvnw clean verify，tag 必须与 pom.xml 一致；先发布公共 SPI，再发布依赖主题。
- Central 凭据通过现有 GitHub Secrets 注入，不提交 settings.xml 或签名密钥。
