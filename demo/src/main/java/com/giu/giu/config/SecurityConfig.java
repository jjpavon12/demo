package com.giu.giu.config;

import com.giu.giu.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioDetailsService usuarioDetailsService) throws Exception {
        http
            .userDetailsService(usuarioDetailsService)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/registro", "/api/auth/login", "/api/auth/registro", "/hola").permitAll()
                .requestMatchers("/dashboard/ciudadano", "/ciudadano/incidencias/**").hasRole("CIUDADANO")
                .requestMatchers("/dashboard/operador", "/dashboard/operador/**").hasRole("OPERADOR")
                .requestMatchers("/dashboard/tecnico", "/dashboard/tecnico/**").hasRole("TECNICO")
                .requestMatchers("/dashboard/admin", "/dashboard/admin/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
