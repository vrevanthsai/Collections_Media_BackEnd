package com.manga.collectionBend.auth.config;

import com.manga.collectionBend.auth.repositories.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//used for marking this file as a spring security configuration file
@Configuration
public class ApplicationConfig {

    private final UserRepo userRepo;

    public ApplicationConfig(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Bean // creates object for spring security
    //  UserDetailsService - which loads user-specific data and used as user DAO/Object
    public UserDetailsService userDetailsService(){
//  UserDetailsService -is functional Interface - so we can use Lambda expression to get its object
//  we are providing its implementation in the form of lambda expressions
//        here we use email for username
        return username -> userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

//    -- contains Deprecated Methods for DaoAuthenticationProvider() and setPasswordEncoder()
//    You actually don’t need DaoAuthenticationProvider at all in most cases. so use only AuthenticationManager() method only
//    @Bean
////    this method used for authenticating the user
////    indicates a class can process a specific Authentication implementation
//    public AuthenticationProvider authenticationProvider(){
////        as UserDeatilsService uses DaoAuthentication- we create a object for it
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
////        this will set all userDetails with provider
//        authenticationProvider.setUserDetailsService(userDetailsService());
////        this will encode password of user before storing instead of raw data
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){
//        returns encoder object to be used for above configuration
        return new BCryptPasswordEncoder();
    }

//    we need a method for processing Authentication(username/pwd) Request
//    You actually don’t need DaoAuthenticationProvider at all in most cases but Both UserDetailsService and PasswordEncoder methods must be available in ApplicationConfig file
//    Spring Security auto-configures it when you expose UserDetailsService and PasswordEncoder.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
