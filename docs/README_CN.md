# NB Command

[EN](../README.md) | [中文](README_CN.md) | [JA](README_JA.md) | [KO](README_KO.md)


## 简介

NB Command 是一款专为 [Nebula](https://github.com/Melledy/Nebula) 设计的图形化远程指令执行工具。 

## 功能特性

-  多语言支持：支持中文、英文、日文和韩文
-  游戏管理：提供玩家管理、物品管理、角色管理等功能
-  图形化界面：直观的操作界面，简化复杂命令的执行过程
-  配置保存：自动保存服务器地址和认证令牌
-  命令历史：记录执行过的命令历史，便于追溯和重复使用
-  数据手册：内置角色和物品数据手册，支持快速查找和选择

## 安装与运行

1. 下载最新发布对应平台的可执行包 [releases](https://github.com/HongchengQ/NB-Command/releases)
2. 解压后运行可执行文件
> 如果不知道下载哪个, nbcommand-windows.zip 你是最好的选择

或者通过源码构建：
-  Java 21
-  maven

```bash
# 运行应用
mvn javafx:run
```

```bash
# 构建可执行文件
mvn package
```
输出目录在`/target/nbcommand`

## 使用说明

1. 在顶部输入您的服务器地址和认证令牌
2. 从左侧分类列表中选择命令类别
3. 在中间列表中选择具体命令
4. 根据需要填写命令参数
5. 预览生成的命令并在确认无误后执行

### 如何获取 Token
> Nebula > 确保 `config.json` > `remoteCommand` > `useRemoteServices` 是 `true`

#### 管理员权限
> Nebula > `config.json` > `remoteCommand` > `serverAdminKey` 为 Token

#### 用户权限
> 在游戏中使用指令 `!remote` 然后屏幕会弹出您的 Token

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。