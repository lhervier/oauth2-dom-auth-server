package fr.asso.afer.rest.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Classe initialisant SpringBoot.
 * @author Lionel HERVIER
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
