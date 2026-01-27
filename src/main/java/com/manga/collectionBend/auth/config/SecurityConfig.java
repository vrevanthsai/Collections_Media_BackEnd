package com.manga.collectionBend.auth.config;

import com.manga.collectionBend.auth.services.AuthFilterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // says to spring security to follow this flow instead of default flow
@EnableMethodSecurity // this enables pre-Authorization( like permissions- Roles-based allowing user to access or not some apis)
public class SecurityConfig {

    private final AuthFilterService authFilterService;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(AuthFilterService authFilterService, AuthenticationProvider authenticationProvider) {
        this.authFilterService = authFilterService;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
//    this method handles which apis should have security filter and which should not have(like login/register/getRefreshtoken paths does not need security filters- all users can access at start)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
         http.csrf(AbstractHttpConfigurer::disable) // to disable csrf
                 .authorizeHttpRequests(auth -> auth
                         .requestMatchers("/api/v1/auth/**")// this is the Base(starting) Path of all auth-APIs like login/register etc
                         .permitAll() // allows the apis which has auth base apis(can access apis without credentials)
                         .anyRequest() // apis which are not from auth base path ,that apis are blocked and asks for authentication
                         .authenticated())
                 .sessionManagement(session -> session
                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // making session as STATELESS because we are not using session in our security
                 .authenticationProvider(authenticationProvider)
                 .addFilterBefore(authFilterService, UsernamePasswordAuthenticationFilter.class); // linking our FilterService with main flow and using a Filter-Strategy

        return http.build(); // returning the Main FilterChain flow instead of using default flow
    }
}
