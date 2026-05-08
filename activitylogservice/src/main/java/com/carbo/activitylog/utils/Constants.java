package com.carbo.activitylog.utils;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final String ADMIN = "ADMIN";
    public static final String CARBO_ADMIN = "CARBO_ADMIN";
    public static final String BACK_OFFICE = "BACK_OFFICE";
    public static final String USER = "USER";
    public static final String READ_ONLY = "READ_ONLY";
    public static final String OPERATION = "OPERATION";
    public static final String SALES_USER = "SALES_USER";
    public static final String MOVE_ONSITE_EQUIPMENT = "MOVE_ONSITE_EQUIPMENT";
    public static final String APP = "APP";
    public static final String SUPER_SALES_USER = "SUPER_SALES_USER";
    public static final String MS_ORGANIZATION = "MS_ORGANIZATION";
    public static final String MS_ADMIN = "MS_ADMIN";
    public static final String MS_WELL = "MS_WELL";
    public static final String MS_PAD = "MS_PAD";
    public static final String MS_OPERATOR = "MS_OPERATOR";
    public static final String MS_DISTRICT = "MS_DISTRICT";
    public static final String MS_FLEET = "MS_FLEET";
    public static final String MS_VENDOR = "MS_VENDOR";
    public static final String MS_EMAIL = "MS_EMAIL";
    public static final String MS_MISC_DATA = "MS_MISC_DATA";
    public static final String MS_SERVICE_COMPANY = "MS_SERVICE_COMPANY";
    public static final String MS_JOB = "MS_JOB";
    public static final String MS_PUMP_ISSUE = "MS_PUMP_ISSUE";
    public static final String MS_ACTIVITY_LOG = "MS_ACTIVITY_LOG";
    public static final String MS_FIELD_TICKET = "MS_FIELD_TICKET";
    public static final String MS_ONSITE_EQUIPMENT = "MS_ONSITE_EQUIPMENT";
    public static final String MS_CHANGE_LOG = "MS_CHANGE_LOG";
    public static final String MS_PROPPANT_DELIVERY = "MS_PROPPANT_DELIVERY";
    public static final String MS_CHEMICAL_DELIVERY = "MS_CHEMICAL_DELIVERY";
    public static final String MS_PROPPANT_STAGE = "MS_PROPPANT_STAGE";
    public static final String MS_CHEMICAL_STAGE = "MS_CHEMICAL_STAGE";
    public static final String MS_WS = "MS_WS";
    public static final String MS_PUMP_SCHEDULE = "MS_PUMP_SCHEDULE";
    public static final String MS_WELL_INFO = "MS_WELL_INFO";
    public static final String MS_CHECKLIST = "MS_CHECKLIST";
    public static final String MS_WORKOVER = "MS_WORKOVER";
    public static final String MS_MAINTENANCE = "MS_MAINTENANCE";
    public static final String MS_CONSUMABLE = "MS_CONSUMABLE";
    public static final String MS_OPERATION_OVERVIEW = "MS_OPERATION_OVERVIEW";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";

    public static final String PRICEBOOK = "PRICEBOOK";
    public static final String PROCUREMENT = "PROCUREMENT";

    public static final String CREW_SCHEDULING = "CREW_SCHEDULING";

    public static final String FIELDCOORDINATOR = "FIELDCOORDINATOR";

    public static final String SALES_FIELD_USER = "SALES_FIELD_USER";

    public static final String SERVICEMANAGER = "SERVICEMANAGER";

    public static final int TIME_LENGTH = 5;

    public static final DateTimeFormatter startEndTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter startEndDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");

    public static final String OPERATOR = "OPERATOR";
    public static final String SIMUL_FRAC_OPS_DB = "Simul Frac OPS - Double Barrel";
    public static final String SIMUL_FRAC_OPS = "Simul Frac OPS";
    public static final String DUPLICATE_RECORD_FOUND = "Duplicate data found";
    public static final String ERROR_WHILE_CREATE_OR_UPDATE = "error while create or update the data";
    public static final String ERROR_INVALID_DATE_TIME_FORMAT = "Invalid startDateTime or endDateTime format. Please use a valid ISO-8601 format (e.g., '2024-11-18T14:30:00Z').";
    public static final String ERROR_JOB_DOES_NOT_EXISTS_FOR_ACTIVITY = "Job does not exist for which you are trying to create/update this activity log entry.";
    public static final String ERROR_LAST_DAY_24_HOURS_NOT_COMPLETED = "24 hours of activities are not created yet for the last day.";
    public static final String ERROR_ORGANIZATIONID_MISMATCH = "The organization of the job for which you are trying to create/update this activity log is different from the organization of the current logged-in user.";


    public static final String LASTMODIFIED = "LastModified";
    public static final String ORGANISATIONID = "organizationId";
    public static final String ID = "id";
    public static final String TS = "ts";
    public static final String MISMATCH_ACTIVITY_LOGS_BY_EMAIL = "xops@linqx.io";
    public static final String MISMATCH_ACTIVITY_LOGS_NOTIFICATION = "Mismatch Activity Logs Notification";
    public static final String MISMATCH_ACTIVITY_LOGS_TO_EMAILS = "dhruv.singh@walkingtree.tech;narendra.bandhamneni@walkingtree.tech;rahul.saxena@walkingtree.tech;rishabh.suri@walkingtree.tech;rama.shanker@walkingtree.tech";
    public static final String SENDGRID_API_KEY = "your-key";

    public static final String PUMP_TIME = "Pump Time";
    public static final String SCHEDULED_TIME = "Scheduled Time";
    public static final String NPT = "NPT";

}
