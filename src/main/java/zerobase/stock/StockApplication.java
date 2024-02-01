package zerobase.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class StockApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockApplication.class, args);

	}

}
