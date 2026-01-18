package com.online.lms.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Value("${spring.app.security.remember-me.key}")
    private String rememberMeKey;

    @Value("604800")
    private int rememberMeValidity;

    @Bean
    public AuthenticationSuccessHandler  authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/";

            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isUser = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (isAdmin) {
                redirectUrl = "/admin/dashboard";
            } else if (isUser) {
                redirectUrl = "/";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String redirectUrl = "/login?error=true";

            if (exception instanceof DisabledException) {
                redirectUrl = "/login?disabled=true";
            } else if (exception instanceof LockedException) {
                redirectUrl = "/login?locked=true";
            } else if (exception instanceof BadCredentialsException) {
                redirectUrl = "/login?error=true";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("wallet/deposit/webhook")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/wishlist/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/", "/login", "/perform-login", "/register", "/verify-email/**",
                                "/forgot-password", "/reset-password/**", "/verify-otp/**", "/resend-otp",
                                "/css/**", "/js/**", "/images/**", "/output.css", "/wallet/deposit/webhook",
                                "/properties/search", "/properties/{id}")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .tokenValiditySeconds(rememberMeValidity)
                        .userDetailsService(userDetailsService)
                        .rememberMeParameter("remember-me"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true"))
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403"));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

}
