package Hisopi.Hisopi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan
@EntityScan
public class HisopiApplication {

	public static void main(String[] args) {
		System.out.println("Working dir: " + System.getProperty("user.dir"));
		SpringApplication.run(HisopiApplication.class, args);
	}

}
