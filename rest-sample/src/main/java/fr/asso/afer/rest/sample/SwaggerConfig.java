package fr.asso.afer.rest.sample;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	/**
	 * Pour démarrer swagger
	 * @return le docket
	 */
	@Bean
	@Profile("dev")
    public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build()
				.globalOperationParameters(Arrays.asList(
						new ParameterBuilder()
						.name("Authorization")
						.description("Coller ici le access token avec le mot clé 'Bearer ' devant.")
						.modelRef(new ModelRef("string"))
						.parameterType("header")
						.required(true)
						.build()
				));
	}

}
