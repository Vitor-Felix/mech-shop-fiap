package com.vitfelix.mech_shop.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityFilter: SecurityFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { req ->
                // Rotas Públicas (Login e Visão do Cliente)
                req.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                req.requestMatchers("/api/work-orders/**").permitAll()

                // Rotas Privadas (Tudo que for admin precisa de token)
                req.requestMatchers("/api/admin/**").authenticated()

                // Qualquer outra rota que sobrar, bloqueia
                req.anyRequest().authenticated()
            }
            // Coloca o nosso filtro antes do filtro padrão do Spring
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
