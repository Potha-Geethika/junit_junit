package com.carbo.activitylog.utils;

public class ErrorConstants {
    public static final String UNABLE_TO_DELETE_ACTIVITY_LOG_ERROR_CODE = "UNABLE_TO_DELETE_ACTIVITY_LOG_ERROR_CODE";
    public static final String UNABLE_TO_DELETE_ACTIVITY_LOG_ERROR_MESSAGE = "Unable to delete activity log entry";

    public static final String NO_ACTIVITY_LOG_FOUND_ERROR_CODE = "NO_ACTIVITY_LOG_FOUND_ERROR_CODE";
    public static final String NO_ACTIVITY_LOG_FOUND_ERROR_MESSAGE = "No activity log entry found from given Id";

    public static final String ERROR_WHILE_COPY_ACTIVITY_CODE = "ERROR_WHILE_COPY_ACTIVITY_CODE";
    public static final String ERROR_WHILE_COPY_ACTIVITY_MESSAGE = "Error While Copy Activity Log Data";

    public static final String ERROR_WHILE_ACTIVITY_COMPLETE_CODE = "ERROR_WHILE_ACTIVITY_COMPLETE_CODE";
    public static final String ERROR_WHILE_ACTIVITY_COMPLETE_MESSAGE = "Cannot copy activity logs because one or more activities are already marked as completed.";

    public static final String ERROR_WHILE_ACTIVITY_ALREADY_EXIST_CODE = "ERROR_WHILE_ACTIVITY_ALREADY_EXIST_CODE";
    public static final String ERROR_WHILE_ACTIVITY_ALREADY_EXIST_MESSAGE = "Cannot copy activity logs because data already exists";
    public static final String COPY_SUCCESSFULLY_CODE = "COPY_SUCCESSFULLY_CODE";
    public static final String COPY_SUCCESSFULLY_MESSAGE = "Copy successfully";
    public static final String ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_CODE = "ERROR_WHILE_GETTING_PUMP_TIME_HISTORY";
    public static final String ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_MESSAGE = "Error while getting pump time history";

    public static final String ERROR_WHILE_GETTING_JOB_CODE = "JOB_NOT_FOUND";
    public static final String ERROR_WHILE_GETTING_JOB_MESSAGE = "Job not found";

    public static final String ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_CODE = "ACTIVITY_LOG_NOT_FOUND";
    public static final String ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_MESSAGE = "No activity logs found for the job with given eventOrNpt code";
}
