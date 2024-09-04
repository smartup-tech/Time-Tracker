package ru.smartup.timetracker.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(SessionUserPrincipal.class, Timestamp.class, HttpSession.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("ru.smartup.timetracker.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
