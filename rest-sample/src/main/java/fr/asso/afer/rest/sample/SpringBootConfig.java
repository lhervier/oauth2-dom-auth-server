package fr.asso.afer.rest.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass=true)
@ComponentScan(basePackages = {"fr.asso.afer.rest"})
public class SpringBootConfig {
	
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
						.allowedOrigins(SpringBootConfig.this.allowedOrigins)
						.allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE");
			}
		};
	}
}
