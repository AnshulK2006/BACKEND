package com.cvns.security;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter filter;
    @Value("${app.cors.allowed-origins}") private String origins;

    @Bean
    SecurityFilterChain chain(HttpSecurity h)throws Exception{
        h.csrf(x->x.disable())
         .cors(x->x.configurationSource(cors()))
         .sessionManagement(x->x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
         .authorizeHttpRequests(x->x
             .requestMatchers("/auth/register","/auth/login","/auth/verify-email-otp","/auth/resend-email-otp","/actuator/health","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
             .requestMatchers(HttpMethod.GET,"/clinics","/vaccines/**","/locations/search").permitAll()
             .anyRequest().authenticated())
         .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class);
        return h.build();
    }

    @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
    @Bean AuthenticationManager manager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}

    @Bean
    CorsConfigurationSource cors(){
        CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        c.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(Arrays.asList("*"));c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
    }
}
