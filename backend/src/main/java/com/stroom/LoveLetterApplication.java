package com.stroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoveLetterApplication {
	//Two modules in one: https://blog.jdriven.com/2016/12/angular2-spring-boot-getting-started/ - Source of frontend+backend
	/*
	in project root directory (LoveLetterCombo):
	mvn clean install
	cd backend
	mvn spring-boot:run
	
	ng dev at frontend/src/main/frontend: npm start
	 */
	
	//Spring   JWT https://github.com/szerhusenBC/jwt-spring-security-demo
	//Angular2 JWT http://chariotsolutions.com/blog/post/angular-2-spring-boot-jwt-cors_part2/
	//TODO add SecurityWebSocket JWT too.
	public static void main(String[] args) {
		SpringApplication.run(LoveLetterApplication.class, args);
	}
}
