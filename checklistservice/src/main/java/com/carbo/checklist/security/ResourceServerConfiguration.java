//package com.carbo.checklist.security;
//import com.carbo.checklist.utils.Constants;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//
//@Configuration
//public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
//
//    @Override
//    public void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .antMatchers(HttpMethod.GET, "/**").hasAnyRole(Constants.USER, Constants.READ_ONLY, Constants.OPERATION)
//                .antMatchers(HttpMethod.POST, "/**").hasAnyRole(Constants.ADMIN, Constants.BACK_OFFICE, Constants.OPERATION)
//                .antMatchers(HttpMethod.PUT, "/**").hasAnyRole(Constants.ADMIN, Constants.BACK_OFFICE, Constants.OPERATION)
//                .antMatchers(HttpMethod.DELETE, "/**").hasAnyRole(Constants.ADMIN)
//                .antMatchers("/actuator/**").permitAll()
//                .anyRequest().authenticated();
//    }
//}
