# 高校反诈防骗宣传平台

## 项目介绍

本项目是一个高校反诈防骗宣传平台，面向校园反诈宣传、资讯发布、案例学习和后台内容管理等场景，包含管理端前端和后端服务。

## 技术栈

- Spring Boot 后端
- MySQL
- Maven
- Vue 3
- Vite
- Element Plus
- Axios
- Pinia

## 部署要求

- JDK 8/17（以 pom.xml 为准）
- Maven
- MySQL 5.7/8.0
- Node.js 16 或以上
- npm/pnpm

## 运行流程

1. 创建数据库并导入项目 SQL。
2. 修改 anti-fraud-platform 的 application 配置。
3. 进入 anti-fraud-platform 执行 mvn spring-boot:run。
4. 进入 anti-fraud-admin 执行 npm install。
5. 执行 npm run dev 启动后台管理端。

## 项目结构

- anti-fraud-platform：后端服务
- anti-fraud-admin：Vue 管理端
- BUGFIX/OPTIMIZATION 文档：问题修复和优化记录（上传时默认不作为 README 展示）
