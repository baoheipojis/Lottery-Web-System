南大Web应用开发课程项目，一个简单的在线抽奖系统。
====================
# 运行
```bash
git clone https://github.com/baoheipojis/Lottery-Web-System.git
```
进入后端所在目录
```bash
cd Lottery-Web-System/lottery
```
## 安装MySQL并配置数据库用户
### 安装MySQL
根据系统环境自行安装，不在本文讨论范围。
### 配置数据库

创建数据库
```sql
CREATE DATABASE lottery;
```

在lottery/src/main/resources/application.properties中配置数据库连接
```
spring.datasource.url=jdbc:mysql://localhost:3306/lottery
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
```

## 启动后端
```bash
./mvnw spring-boot:run
```
如果最后没有自动退出，那就说明成功了
```
2025-03-30T01:17:11.111Z  INFO 25973 --- [           main] com.example.lottery.LotteryApplication   : Started LotteryApplication in 10.241 seconds (process running for 10.961)
```
你可以使用诸如
```bash
curl http://localhost:8080/api/prizes
```
之类的方法来直接测试后端的api。

上述命令会返回一个空的json数组，表示当前没有奖品。
## 启动前端
### 安装前端依赖
前端基于vue，需要安装nodejs和npm。同样，请根据系统环境自行安装。

```
cd frontend/my-project
npm install
```
之后用
```bash
npm run serve
```
就可以启动前端了。
