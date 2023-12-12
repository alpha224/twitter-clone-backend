package com.twitterclone.demo.config

import com.twitterclone.demo.auth.JwtTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtTokenFilter tokenFilter

    SecurityConfiguration(JwtTokenFilter tokenFilter) {
        this.tokenFilter = tokenFilter
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues()
        corsConfiguration.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login", "/registration").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security", "/swagger-ui.html", "/webjars/**").permitAll()
                .antMatchers(HttpMethod.POST, "/logout", "/posts/create/*").authenticated()
                .antMatchers(HttpMethod.PATCH, "/users/*", "/posts/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/users", "/users/*/subscribe/", "/posts/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/users/*/subscribe/", "/posts/*/like/*").authenticated()
                .antMatchers(HttpMethod.POST, "/posts/*/comment/*").authenticated()
                .antMatchers(HttpMethod.GET, "/posts/comments", "/posts/myFeed", "/posts/feed/*").authenticated()
                .anyRequest().denyAll()
                .and()
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
    }
}