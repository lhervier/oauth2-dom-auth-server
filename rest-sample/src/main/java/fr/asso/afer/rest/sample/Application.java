package fr.asso.afer.rest.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Classe initialisant SpringBoot.
 * @author Lionel HERVIER
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
	
	/**
	 * Les origines autorisées
	 */
	@Value("${cors.allowedOrigins}")
	private String[] allowedOrigins;
	
	/**
	 * Pour envoyer les en têtes CORS
	 * @return
	 */
	@Bean
    public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(Application.this.allowedOrigins)
						.allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE");
			}
		};
	}
}
