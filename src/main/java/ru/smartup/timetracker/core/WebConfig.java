package ru.smartup.timetracker.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.HttpMethod;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.smartup.timetracker.core.converter.StringToEnum;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.ResultDto;
import ru.smartup.timetracker.entity.field.sort.PositionSortFieldEnum;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebConfig implements WebMvcConfigurer {
    private static final int MAXIMUM_SESSIONS = 2;
    private static final String API_PATTERN = "/api/**";
    private static final String API_AUTH_PATTERN = "/api/auth/**";
    private static final String API_PASSWORD_RECOVERY_PATTERN = "/api/passwordRecovery/**";
    private static final String API_AUTH_LOGIN = "/api/auth/login";
    private static final String ROOT_PATH = "/";
    private static final String CSRF_PARAMETER = "_csrf";
    private static final String USERNAME_PARAMETER = "email";
    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Content-Type", "X-Requested-With");

    @Bean
    public PasswordGenerator passwordGenerator() {
        return new PasswordGenerator();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           DBAuthenticationProvider dbAuthenticationProvider,
                                           SessionUserPrincipalInitFilter sessionUserPrincipalInitFilter,
                                           UnauthorizedFilter unauthorizedFilter,
                                           SessionRegistry sessionRegistry,
                                           ObjectMapper objectMapper) throws Exception {
        httpSecurity
                .cors().and()
                .authenticationProvider(dbAuthenticationProvider)
                .authorizeRequests()
                .antMatchers(API_AUTH_PATTERN, API_PASSWORD_RECOVERY_PATTERN).permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .usernameParameter(USERNAME_PARAMETER)
                .loginProcessingUrl(API_AUTH_LOGIN)
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().append(objectMapper.writeValueAsString(
                            new ResultDto(ErrorCode.INVALID_CREDENTIALS, exception.getMessage())));
                })
                .successHandler((request, response, authentication) -> {
                    if (request.getParameter(CSRF_PARAMETER) != null) {
                        response.sendRedirect(ROOT_PATH);
                    }
                });
        httpSecurity
                .sessionManagement()
                .maximumSessions(MAXIMUM_SESSIONS)
                .expiredSessionStrategy(event -> event.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .sessionRegistry(sessionRegistry);
        httpSecurity
                .csrf().ignoringAntMatchers(API_PATTERN);
        httpSecurity
                .addFilterAfter(unauthorizedFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(sessionUserPrincipalInitFilter, UnauthorizedFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowedOrigin}") String allowedOrigin) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin(allowedOrigin);
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(ALLOWED_HEADERS);
        corsConfiguration.setAllowedMethods(Arrays.asList(
                HttpMethod.OPTIONS.name(),
                HttpMethod.GET.name(),
                HttpMethod.HEAD.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name()));
        source.registerCorsConfiguration(API_PATTERN, corsConfiguration);
        return source;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEnum<>(UserSortFieldEnum.class));
        registry.addConverter(new StringToEnum<>(PositionSortFieldEnum.class));
        registry.addConverter(new StringToEnum<>(ProjectSortFieldEnum.class));
    }
}
