//package com.carbo.activitylog.security;
//
//import com.carbo.activitylog.utils.Constants;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//
//@Configuration
//public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
//
//    //@Override
//    public void configure_old(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .mvcMatchers("/swagger-ui.html").permitAll()
//                .antMatchers("/webjars/**").permitAll()
//                .antMatchers("/swagger-resources/**").permitAll()
//                .antMatchers("/v2/api-docs/**").permitAll()
//                .antMatchers("/actuator/**").permitAll().and()
//                .authorizeRequests()
//                .antMatchers(HttpMethod.GET, "/**").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)
//                .antMatchers(HttpMethod.POST, "/**").hasAnyRole(Constants.USER)
//                .antMatchers(HttpMethod.PUT, "/**").hasAnyRole(Constants.USER)
//                .antMatchers(HttpMethod.DELETE, "/**").hasAnyRole(Constants.USER)
//                .anyRequest().authenticated();
//    }
//
//
//    public void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .mvcMatchers("/swagger-ui.html").permitAll()
//                .antMatchers("/webjars/**").permitAll()
//                .antMatchers("/swagger-resources/**").permitAll()
//                .antMatchers("/v2/api-docs/**").permitAll()
//                .antMatchers("/**/actuator/**").permitAll().and()
//                .authorizeRequests()
//                .antMatchers(HttpMethod.GET, "/**").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)
//                .antMatchers(HttpMethod.POST, "/**").hasAnyRole("USER")
//                .antMatchers(HttpMethod.PUT, "/**").hasAnyRole("USER")
//                .antMatchers(HttpMethod.DELETE, "/**").hasAnyRole("USER")
//                .anyRequest().authenticated();
//    }
//}
