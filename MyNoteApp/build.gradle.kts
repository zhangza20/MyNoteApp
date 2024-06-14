// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
}
buildscript {
    repositories {
        // 如果您的项目使用了其他仓库，可以在这里添加
        google()
        // 如果您使用了其他第三方仓库，也可以在这里添加
        mavenCentral()
    }
    dependencies {
        // 添加 Google Services 插件依赖
        classpath("com.google.gms:google-services:4.4.2")
    }
}
