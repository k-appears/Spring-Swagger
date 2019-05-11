package org.poc;

import org.poc.util.LoggingInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@EnableSwagger2
@SpringBootApplication
public class ServerApplicantTestApplication extends WebMvcConfigurerAdapter
{

    public static void main(String[] args)
    {
        SpringApplication.run(ServerApplicantTestApplication.class, args);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/**");
    }


    @Bean
    public Docket docket()
    {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage(getClass().getPackage().getName()))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(generateApiInfo())
            .securitySchemes(securityScheme())
            .securityContexts(securityContext());
    }


    private ApiInfo generateApiInfo()
    {
        return new ApiInfo(
            "Server Applicant Test Service", "Purpose: check technolofy stack.", "Version 1.0 - mw",
            "urn:tos", "", "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0");
    }


    private List<SecurityContext> securityContext()
    {
        AuthorizationScope[] authorizationScopes =
            {
                new AuthorizationScopeBuilder().scope("read").description("read access").build(),
                new AuthorizationScopeBuilder().scope("write").description("write access").build()};

        SecurityReference securityReference =
            SecurityReference
                .builder()
                .reference("test")
                .scopes(authorizationScopes)
                .build();
        SecurityContext securityContext =
            SecurityContext
                .builder().securityReferences(Collections.singletonList(securityReference)).build();
        return Collections.singletonList(securityContext);
    }


    private List<SecurityScheme> securityScheme()
    {
        return Collections.singletonList(new BasicAuth("test"));
    }

}
