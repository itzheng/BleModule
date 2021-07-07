# BleModule
对于蓝牙操作的简单工具封装

# 使用说明：
## 第一步：
### 在 Project 的 build.gradle 文件中 添加仓库支持
```groovy
allprojects {
    repositories {
        
        maven { url 'https://jitpack.io' }
    }
} 
```
## 第二步：
### 在需要引用的项目的 build.gradle 添加依赖
[see javadoc](https://javadoc.jitpack.io/com/github/itzheng/BleModule/latest/javadoc/index.html)
[![](https://jitpack.io/v/itzheng/BleModule.svg)](https://jitpack.io/#itzheng/BleModule)
```groovy
dependencies {
        
       implementation 'com.github.itzheng:BleModule:0.0.3'
}
```


