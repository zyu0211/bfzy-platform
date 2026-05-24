package com.bfzy.platform.security.config;

import com.bfzy.platform.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置.
 * <p>
 * 禁用 formLogin / csrf，无状态 Session 策略。
 * {@code POST /api/auth/**} + {@code /api/health} + {@code /api/hello} + {@code /actuator/**} 放行，其余接口需认证。
 * 通过自定义 {@link JwtAuthFilter} 实现 Bearer Token 校验。
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ========== 请求白名单（增删只需改 WHITELIST 列表）==========

    private record Rule(HttpMethod method, String... patterns) {
    }

    private static final List<Rule> WHITELIST = List.of(
            new Rule(HttpMethod.POST, "/api/auth/**"),
            new Rule(null, "/api/health", "/api/hello"),
            new Rule(null, "/actuator/**")
    );

    // ========== 注入字段 ==========

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    for (Rule rule : WHITELIST) {
                        if (rule.method() != null) {
                            auth.requestMatchers(rule.method(), rule.patterns()).permitAll();
                        } else {
                            auth.requestMatchers(rule.patterns()).permitAll();
                        }
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
