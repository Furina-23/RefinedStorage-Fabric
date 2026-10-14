# Energy API 兼容性验证

验证日期：2026-10-14。当前项目：Minecraft 1.20.1、JDK 17、Fabric Transfer API 3.x。

从 Fabric Maven 下载官方发布 JAR，检查 fabric.mod.json 与 class 文件版本，并使用 JDK 17 尝试加载 team.reborn.energy.api.EnergyStorage。

| Energy API | 声明的最低 Minecraft | 最低 Fabric Transfer API | class 文件版本 | JDK 17 加载结果 |
|---|---|---|---|---|
| 4.0.0 | 1.20.5（含预发布范围） | 5.0.1 | 65（Java 21） | UnsupportedClassVersionError |
| 4.0.1 | 1.20.5（含预发布范围） | 5.0.1 | 65（Java 21） | UnsupportedClassVersionError |
| 4.1.0 | 1.21（含预发布范围） | 5.1.0 | 65（Java 21） | UnsupportedClassVersionError |
| 4.2.0 | 1.21.5（含预发布范围） | 5.1.0 | 65（Java 21） | UnsupportedClassVersionError |

结论：当前 Minecraft 1.20.1 项目不支持上述任何 Energy API 4.x 版本。4.0.x 虽声明 Java >=17，其实际字节码要求 Java 21；即使升级 Java，Minecraft 与 Transfer API 的版本要求仍不满足。保留依赖范围 >=3.0.0 <4.0.0，并内置 Energy API 3.0.0。

未启动游戏，也未通过修改依赖声明绕过版本要求。此验证确认当前运行环境不兼容，不能推断移植到较新 Minecraft 后仍不兼容。

发布版本查询：https://maven.fabricmc.net/teamreborn/energy/maven-metadata.xml
