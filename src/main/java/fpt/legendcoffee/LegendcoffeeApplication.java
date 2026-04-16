package fpt.legendcoffee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LegendcoffeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegendcoffeeApplication.class, args);
	}

}
