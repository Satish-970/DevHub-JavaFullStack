package com.example.DevHub.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public resources and authentication endpoints
                        // Explicitly permit all static resources and your view controller paths
                        .requestMatchers("/", "/login", "/register", "/index.html", "/css/**", "/js/**", "/assets/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() // REST API auth endpoints
                        // Admin Panel specific HTML (backend API will still restrict)
                        .requestMatchers("/adminpage.html").hasRole("ADMIN")
                        // All other HTML pages require authentication.
                        // The specific data displayed on these pages will be filtered by the backend services.
                        .requestMatchers("/account.html", "/blogs.html", "/project.html", "/comments.html").authenticated()
                        // API endpoints:
                        // /api/users/**: Admins can manage all users. Non-admins (users) can ONLY access their own profile (handled by @PreAuthorize on UserController methods).
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN") // Access to specific user APIs will be handled by @PreAuthorize

                        // Comments and Blog Posts for general access or self-management
                        .requestMatchers("/api/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/blog-posts/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/projects/**").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated() // All other requests must be authenticated
                )
                // --- REMOVE THIS LINE: httpBasic(Customizer.withDefaults()) ---
                .formLogin(formLogin -> formLogin.disable()) // Explicitly disable formLogin as per previous fixes
                // Custom authentication entry point for REST API to return 401 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{ \"message\": \"Authentication Required\" }");
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session for JWT
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // This bean is likely not used if OAuth2 is removed, but keeping for compile safety.
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return mappedAuthorities;
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:63342"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}