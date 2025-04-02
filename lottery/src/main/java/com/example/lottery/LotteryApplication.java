package com.example.lottery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling  // Add this annotation to enable scheduling
public class LotteryApplication {

	@PostConstruct
	void started() {
		// 设置应用默认时区为亚洲/上海
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
	}

	public static void main(String[] args) {
		SpringApplication.run(LotteryApplication.class, args);
	}
}
