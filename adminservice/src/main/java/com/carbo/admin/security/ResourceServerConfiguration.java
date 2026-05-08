//package com.carbo.admin.security;
//
//import com.carbo.admin.utils.Constants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
//
//import java.util.function.Function;
//import com.carbo.admin.utils.Constants;
//
//@Configuration
//@EnableResourceServer
//@Order(99)
//public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
//    private static final Logger logger = LoggerFactory.getLogger(ResourceServerConfiguration.class);
//
//    public static Function<HttpSecurity, Void> setRule = (HttpSecurity http) -> {
//        try {
//            http.anonymous().disable()
//                    .requestMatchers().antMatchers("/**")
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/**/change-password").permitAll()
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/**/lastPassResetDate/**").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/**/change-signature").hasAnyRole("USER")
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers(HttpMethod.GET, "/v1/users/").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)
//                    .antMatchers(HttpMethod.POST, "/v1/users/").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)
//                    .antMatchers(HttpMethod.PUT, "/v1/users/**").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW,Constants.LEAD_OWNER,Constants.LEAD_MANAGER)
//                    .antMatchers("/actuator/**").permitAll()
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/**").hasAnyRole("ADMIN", "USER_MANAGEMENT")
//                    .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
//
//        } catch (Exception ex) {
//            logger.error("Unable to set security rules", ex);
//            System.exit(1);
//        }
//
//        return null;
//    };
//
//    @Override
//    public void configure(HttpSecurity http) throws Exception {
//        logger.info("SETTING UP SECURITY CONFIGURATION");
//        setRule.apply(http);
//    }
//}
