pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 默认值，表示优先使用设置文件中的仓库
    repositories {
        google() // 添加 Google 仓库
        mavenCentral() // 添加 Maven Central 仓库
        maven { url = uri("https://maven.aliyun.com/repository/public") } // 添加自定义 Maven 仓库
    }
}
rootProject.name = "wxchat"
include(":app")
 