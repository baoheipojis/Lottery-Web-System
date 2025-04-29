南大Web应用开发课程项目，一个简单的在线抽奖系统。
====================
## 运行
```bash
git clone https://github.com/baoheipojis/Lottery-Web-System.git
```
进入后端所在目录
```bash
cd Lottery-Web-System/lottery
```
### 安装MySQL并配置数据库用户
#### 安装MySQL
根据系统环境自行安装，下面介绍在Ubuntu上安装MySQL的方法
```bash
sudo apt update
sudo apt install mysql-server
```


#### 配置数据库
```
sudo mysql -u root -p
```

创建数据库
```sql
CREATE DATABASE lottery;
```

在lottery/src/main/resources/application.properties中配置数据库连接，注意填你的数据库密码。
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

### 启动后端
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
### 启动前端
#### 安装前端依赖
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
## 计划
本项目的核心需求就是：使用心理学技巧，使我向远期目标迈进。
因此我们必须要支持多级计划的功能，一个大计划可以拆分成多个小计划，完成这些小计划后发放计划点奖励。计划点奖励可以抽奖，这样就可以把长期目标短期化。
## 习惯
习惯模块是我们新增的一个模块，致力于让我们养成新的好习惯，或者改掉坏习惯。
习惯拥有自动完成功能，这是为了利用人的**损失厌恶**心理。让我们在不完成习惯时有痛苦感，从而促使我们完成习惯。

## 奖品
### 抽奖规则
我们采用了某知名游戏的抽奖规则，具体如下：

简单版：
1. 奖励分为三星、四星、五星，五星又分为限定五星和普通五星。
2. 每10抽必出一个四星或以上物品。
3. 每90抽必出一个五星物品。
4. 若上一个五星不存在或为限定五星，本次五星有50%概率为限定五星，50%概率为普通五星。否则，若上次为普通五星，本次五星必为限定五星。
5. 综合概率：四星13%，五星1.6%。

完整版：
0. 奖励稀有度有三星、四星、普通五星和限定五星四种，稀有度依次上升。
1. 四星基础概率为5.1%，如果连续8抽没有四星，第9抽是4星的概率为56.1%。
2. 5星基础概率为0.6%，如果连续73抽没有五星，从第74抽起，当前抽奖为五星的概率提升6%，直至抽出五星。
3. 保证10抽必出一个四星及以上奖励。如果连续9抽是三星，第10抽必定是4星或5星，如果是5星，那么第11抽必是四星及以上，如果第11抽仍是五星，第12抽必定是四星及以上。以此类推。
4. 如果上一个五星是普通五星，本次五星必定为限定五星；如果上次抽出的五星为限定五星或没有上个五星，本次抽出的五星有50%概率为普通五星，50%概率为限定五星。
