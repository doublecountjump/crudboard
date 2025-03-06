package test.crudboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CrudboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudboardApplication.class, args);
	}

}
