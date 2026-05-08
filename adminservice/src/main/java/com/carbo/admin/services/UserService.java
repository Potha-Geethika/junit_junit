package com.carbo.admin.services;

import static com.carbo.admin.utils.Constants.INVALID_OTP_CODE;
import static com.carbo.admin.utils.Constants.OTP_EXPIRED_CODE;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.*;
import com.carbo.admin.kafka.Producer;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponse;
import com.carbo.admin.repository.UserMongoDbRepository;
import com.carbo.admin.utils.Constants;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength set as 12

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    //    private static final String AZURE_USER_AI_TOPIC = "AzureUserAI";

    private final UserMongoDbRepository userRepository;

    private final MongoTemplate mongoTemplate;

    private final Producer producer;

    private final GraphServiceClient graphServiceClient;

    @Value ("${user.azure-ad.domain}")
    private String domain;

    @Value ("${user.kafka.producer.aiUserTopic}")
    private String aiUserTopic;

    @Autowired
    public UserService(UserMongoDbRepository userRepository, MongoTemplate mongoTemplate, Producer producer, GraphServiceClient graphServiceClient) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.producer = producer;
        this.graphServiceClient = graphServiceClient;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public List<User> getByOrganizationId(String organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserNameIgnoreCase(userName);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(String emailGroupId) {
        userRepository.deleteById(emailGroupId);
    }

    public void sendOtpEmail(String username) {
        Optional<User> optionalUser = userRepository.findByUserNameIgnoreCase(username);
        if (!optionalUser.isPresent()) {
            Error error = Error.builder().errorCode(Constants.USER_NOT_EXISTS_CODE).errorMessage(Constants.USER_NOT_EXISTS_MESSAGE)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
        Random random = new Random();
        String otpCode = String.valueOf(100000 + random.nextInt(900000));
        User user = optionalUser.get();
        sendEmail(user, otpCode);
        logger.info("Generated OTP for user {}: {}", user.getUserName(), otpCode);
        user.setOtpCode(otpCode);
        user.setOtpGeneratedTime(getTodayDateTime(new Date()));
        userRepository.save(user);
    }

    public void validateOtp(String username, String submittedOtp, String otpSubmittedTime) {
        Optional<User> optionalUser = userRepository.findByUserNameIgnoreCase(username);
        if (!optionalUser.isPresent()) {
            Error error = Error.builder().errorCode(Constants.USER_NOT_EXISTS_CODE).errorMessage(Constants.USER_NOT_EXISTS_MESSAGE)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        } else {
            User user = optionalUser.get();
            if (user.getOtpCode().equals(submittedOtp)) {
                String otpGeneratedTime = user.getOtpGeneratedTime();
                if (otpGeneratedTime != null && checkOtpExpiration(getFormattedCurrentDateTime(user.getOtpGeneratedTime()),
                        getFormattedCurrentDateTime(otpSubmittedTime))) {
                    user.setOtpCode(null);
                    user.setOtpGeneratedTime(null);
                    user.setAuthenticationTime(getTodayDateTime(new Date()));
                    userRepository.save(user);
                } else {
                    Error error = Error.builder().errorCode(OTP_EXPIRED_CODE).errorMessage(Constants.OTP_EXPIRED).httpStatus(HttpStatus.BAD_REQUEST)
                                       .build();
                    throw new ErrorException(error);
                }
            } else {
                Error error = Error.builder().errorCode(INVALID_OTP_CODE).errorMessage(Constants.INVALID_OTP).httpStatus(HttpStatus.BAD_REQUEST)
                                   .build();
                throw new ErrorException(error);
            }
        }
    }

    private void sendEmail(final User user, final String otp) {
        Mail mail = getMail(user, otp);
        SendGrid sendGrid = new SendGrid(Constants.SEND_GRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (ObjectUtils.isEmpty(response)) {
                Error error = Error.builder().errorCode(Constants.FAIL_OTP_MAIL_SEND_CODE).errorMessage(Constants.FAIL_OTP_MAIL_SEND)
                                   .httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        } catch (Exception ex) {
            logger.error("Error while sending OTP to the user: {}", ex.getMessage());
            Error error = Error.builder().errorCode(Constants.FAIL_OTP_MAIL_SEND_CODE).errorMessage(Constants.FAIL_OTP_MAIL_SEND)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private Mail getMail(User user, String otp) {
        Email from = new Email(Constants.FROM_EMAIL_ID);
        String subject = "Your OTP Code";
        Email to = new Email(user.getUserName());
        String userName = (user.getFirstName() != null && !user.getFirstName().isEmpty()) ? user.getFirstName() : user.getUserName();
        Content content = new Content("text/html",
                "Hello " + userName + ",<br/><br/>" + "You have requested a One-Time Password (OTP) to access your account.<br/>" + "Please use the following OTP to proceed: <strong style='font-size=40px;'>" + otp + "</strong><br/><br/>" + "This OTP is valid for a single use and will expire shortly.<br/><br/>" + "If you did not request this OTP, please ignore this email and ensure your account security by changing your password immediately.<br/><br/>" + "Regards,<br/>" + "FracPro Support<br/>" + "<a href='mailto:support@fracpro.com'>support@fracpro.com</a><br/><br/>" + "<em>Security Tip:</em> Never share your OTP with anyone. Your app support will never ask you for your OTP.");
        return new Mail(from, subject, to, content);
    }

    private boolean checkOtpExpiration(LocalDateTime otpGeneratedTime, LocalDateTime otpSubmittedTime) {
        Duration duration = Duration.between(otpGeneratedTime, otpSubmittedTime);
        return duration.toMinutes() <= 10 && duration.toDays() == 0 && duration.toHours() == 0;
    }

    private LocalDateTime getFormattedCurrentDateTime(final String unformattedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(unformattedDateTime, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime gmtZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("GMT"));
        return gmtZonedDateTime.toLocalDateTime();
    }

    private String getTodayDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public UserResponse saveUserOnAzureAd(User user) {
        Optional<User> existUser = userRepository.findByUserNameIgnoreCase(user.getUserName());
        if (!existUser.isPresent()) {
            UserResponse response = createUserOnAzureAD(user);
            User userResult = null;
            com.microsoft.graph.models.User createdUser = (com.microsoft.graph.models.User) response.getResponse();
            try {
                user.setAzureId(createdUser.getId());
                String encodedPassword = encoder.encode(user.getPassword());
                user.setPassword(encodedPassword);
                userResult = userRepository.save(user);
                Query query = new Query().addCriteria(where("_id").is(user.getOrganizationId()));
                Optional<Organization> organization = Optional.ofNullable(mongoTemplate.findOne(query, Organization.class));
                AiUser aiUserRequest = AiUser.builder().name(user.getFirstName()).surname(user.getLastName()).emailAddress(user.getUserName())
                                             .userName(user.getUserName()).azureUserId(createdUser.getId())
                                             .tenantName(organization.isPresent() ? organization.get().getName() : "").notificationType("CREATE")
                                             .build();
                producer.push(aiUserTopic, aiUserRequest);
                return new UserResponse(response.getMessage(), HttpStatus.CREATED, userResult);
            } catch (Exception e) {
                deleteUserFromAzure(createdUser.getId()); //if any exception occurred delete the created user on portal
                if (!ObjectUtils.isEmpty(userResult)) {
                    userRepository.deleteById(userResult.getId());
                }
                logger.error("Error while creating user: {}", e.getMessage());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                                   .errorMessage(Constants.ERROR_WHILE_CREATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        } else {
            Error error = Error.builder().errorCode(Constants.USER_ALREADY_EXISTS_CODE).errorMessage(Constants.USER_ALREADY_EXISTS_MESSAGE)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private void deleteUserFromAzure(String id) {
        graphServiceClient.users().byUserId(id).delete();
    }

    private UserResponse createUserOnAzureAD(User user) {
        com.microsoft.graph.models.User newUser = prepareNewUserRequest(user);
        try {
            com.microsoft.graph.models.User response = graphServiceClient.users().post(newUser);
            return new UserResponse("User created", HttpStatus.CREATED, response);
        } catch (Exception e) {
            logger.error("Error while creating user: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_CREATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private com.microsoft.graph.models.User prepareNewUserRequest(User userRequestBody) {
        com.microsoft.graph.models.User newUser = new com.microsoft.graph.models.User();
        newUser.setAccountEnabled(true);
        newUser.setDisplayName(
                (ObjectUtils.isEmpty(userRequestBody.getFirstName()) ? "" : userRequestBody.getFirstName()) + " " + (ObjectUtils.isEmpty(
                        userRequestBody.getLastName()) ? "" : userRequestBody.getLastName()));
        newUser.setGivenName(ObjectUtils.isEmpty(userRequestBody.getFirstName()) ? "" : userRequestBody.getFirstName());
        newUser.setSurname(ObjectUtils.isEmpty(userRequestBody.getLastName()) ? "" : userRequestBody.getLastName());
        if (ObjectUtils.isEmpty(userRequestBody.getUserName())) {
            logger.error("UserName is mandatory");
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(Constants.MANDATORY_USER_NAME)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
        int atIndex = userRequestBody.getUserName().indexOf('@');
        if (atIndex != -1) {
            String localPart = userRequestBody.getUserName().substring(0, atIndex);
            String domainPart = userRequestBody.getUserName().substring(atIndex + 1);
            String userPrincipalName = localPart + "_" + domainPart + "@" + domain;
            newUser.setUserPrincipalName(userPrincipalName);
            newUser.setMailNickname(localPart);
        }
        PasswordProfile passwordProfile = new PasswordProfile();
        passwordProfile.setForceChangePasswordNextSignIn(true);
        passwordProfile.setPassword(userRequestBody.getPassword());
        newUser.setPasswordProfile(passwordProfile);
        newUser.setPasswordPolicies("none");
        LinkedList<ObjectIdentity> identities = new LinkedList<>();
        ObjectIdentity objectIdentity = new ObjectIdentity();
        objectIdentity.setIssuer(domain);
        objectIdentity.setSignInType("emailAddress");
        objectIdentity.setIssuerAssignedId(userRequestBody.getUserName());
        identities.add(objectIdentity);
        newUser.setIdentities(identities);
        return newUser;
    }

    public void updateUserOnAzureAd(User user, String userId) {
        User existingUser = getUser(userId).orElseThrow(() -> new ErrorException(
                Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(Constants.USER_NOT_FOUND_MESSAGE)
                     .httpStatus(HttpStatus.BAD_REQUEST).build()));
        if (!ObjectUtils.isEmpty(user.getPassword()) && !isValidPassword(user.getPassword())) {
            logger.error("Password is not valid, Please follow the password policies");
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(Constants.PASSWORD_NOT_VALID)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
        if (ObjectUtils.isEmpty(existingUser.getAzureId())) {
            logger.error("Azure id not available in the user info to update the user details on Azure : {} ", existingUser.getAzureId());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(Constants.AZURE_ID_NOT_PRESENT)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
        updateUserOnAzureADByUserId(existingUser, user);
        try {
            List<String> lastFivePasswords = existingUser.getLastFivePasswords();
            if (user.getLastPassResetDate() == null) {
                user.setLastPassResetDate(existingUser.getLastPassResetDate());
            }
            if (user.getCreated() == null) {
                user.setCreated(existingUser.getCreated());
            }
            if (user.getCreatedBy() == null) {
                user.setCreatedBy(existingUser.getCreatedBy());
            }
            if (user.getModified() == null) {
                user.setModified(existingUser.getModified());
            }
            if (user.getLastModifiedBy() == null) {
                user.setLastModifiedBy(existingUser.getLastModifiedBy());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encodedPassword = encoder.encode(user.getPassword());
                user.setPassword(encodedPassword);
                if (lastFivePasswords == null) {
                    lastFivePasswords = new ArrayList<>();
                    lastFivePasswords.add(encodedPassword);
                } else {
                    lastFivePasswords.add(encodedPassword);
                }
                if (lastFivePasswords.size() > 5) {
                    lastFivePasswords.remove(0);
                }
                user.setLastFivePasswords(lastFivePasswords);
                user.setLastPassResetDate(Date.from(Instant.now()));
            } else {
                user.setPassword(existingUser.getPassword());
            }
            user.setAzureId(existingUser.getAzureId());
            userRepository.save(user);
            Query query = new Query().addCriteria(where("_id").is(user.getOrganizationId()));
            Organization organization = mongoTemplate.findOne(query, Organization.class);
            AiUser aiUserRequest = AiUser.builder().name(user.getFirstName()).surname(user.getLastName()).emailAddress(user.getUserName())
                                         .azureUserId(existingUser.getAzureId())
                                         .tenantName(ObjectUtils.isEmpty(organization) ? "" : organization.getName()).notificationType("UPDATE")
                                         .build();
            producer.push(aiUserTopic, aiUserRequest);
        } catch (Exception e) {
            logger.error("Error while update user: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_UPDATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < 8 || password.length() > 64) {
            return false;
        }

        int conditionsMet = 0;

        // Check for lowercase
        if (password.matches(".*[a-z].*")) conditionsMet++;
        // Check for uppercase
        if (password.matches(".*[A-Z].*")) conditionsMet++;
        // Check for digit
        if (password.matches(".*[0-9].*")) conditionsMet++;
        // Check for symbol
        if (password.matches(".*[@#$%^&+=].*")) conditionsMet++;

        // Return true if at least 3 conditions are met
        return conditionsMet >= 3;
    }

    private UserResponse updateUserOnAzureADByUserId(User existingUser, User user) {
        com.microsoft.graph.models.User updateUser = prepareUpdateUserRequest(existingUser, user);
        try {
            com.microsoft.graph.models.User response = graphServiceClient.users().byUserId(existingUser.getAzureId()).patch(updateUser);
            return new UserResponse("User updated", HttpStatus.CREATED, response);
        } catch (Exception e) {
            logger.error("Error while creating user: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_UPDATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private com.microsoft.graph.models.User prepareUpdateUserRequest(User existingUser, User userRequestBody) {
        com.microsoft.graph.models.User newUser = new com.microsoft.graph.models.User();
        if (!ObjectUtils.isEmpty(userRequestBody.getFirstName()) && !userRequestBody.getFirstName().equalsIgnoreCase(existingUser.getFirstName())) {
            newUser.setGivenName(userRequestBody.getFirstName());
            newUser.setDisplayName(userRequestBody.getFirstName() + " " + userRequestBody.getLastName());
        }
        if (!ObjectUtils.isEmpty(userRequestBody.getLastName()) && !userRequestBody.getLastName().equalsIgnoreCase(existingUser.getLastName())) {
            newUser.setSurname(userRequestBody.getLastName());
            newUser.setDisplayName(userRequestBody.getFirstName() + " " + userRequestBody.getLastName());
        }
        if (!ObjectUtils.isEmpty(userRequestBody.getPassword())) {
            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.setForceChangePasswordNextSignIn(false);
            passwordProfile.setPassword(userRequestBody.getPassword());
            newUser.setPasswordProfile(passwordProfile);
        }
        if (!ObjectUtils.isEmpty(userRequestBody.getUserName()) && !userRequestBody.getUserName().equalsIgnoreCase(existingUser.getUserName())) {
            LinkedList<ObjectIdentity> identities = new LinkedList<>();
            ObjectIdentity objectIdentity = new ObjectIdentity();
            objectIdentity.setIssuer(domain);
            objectIdentity.setSignInType("emailAddress");
            objectIdentity.setIssuerAssignedId(userRequestBody.getUserName());
            identities.add(objectIdentity);
            newUser.setIdentities(identities);
        }
        return newUser;
    }

    public void deleteUserOnAzureAd(String userId) {
        Optional<User> existUser = userRepository.findById(userId);
        if (existUser.isPresent()) {
            if (ObjectUtils.isEmpty(existUser.get().getAzureId())) {
                logger.error("Azure userId {} not exist in the user info  !!", existUser.get().getAzureId());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(Constants.AZURE_USER_ID_NOT_PRESENT)
                                   .httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
            deleteUserByUserIdFromAzureAd(existUser.get().getAzureId());
            try {
                userRepository.deleteById(userId);
                AiUser aiUserRequest = AiUser.builder().azureUserId(existUser.get().getAzureId()).notificationType("DELETE").build();
                producer.push(aiUserTopic, aiUserRequest);
            } catch (Exception e) {
                logger.error("Error while Deleting user: {}", e.getMessage());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                                   .errorMessage(Constants.ERROR_WHILE_DELETE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        } else {
            logger.error("No User Details Found With This Email [ + userId + ] in the DB");
            Error error = Error.builder().errorCode(Constants.USER_NOT_FOUND_CODE).errorMessage(Constants.USER_NOT_FOUND_MESSAGE)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private UserResponse deleteUserByUserIdFromAzureAd(String azureId) {
        try {
            graphServiceClient.users().byUserId(azureId).delete();
            return new UserResponse("User Deleted successfully : ", HttpStatus.OK, null);
        } catch (Exception e) {
            logger.error("Error while Deleting user: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_DELETE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private User convertAiUserToOpsUser(AiUser aiUser) {
        try {
            ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
            mapper.createTypeMap(AiUser.class, User.class).addMapping(AiUser::getEmailAddress, User::setUserName)
                  .addMapping(AiUser::getName, User::setFirstName).addMapping(AiUser::getSurname, User::setLastName)
                  .addMapping(AiUser::getAzureUserId, User::setAzureId).addMappings(mapping -> mapping.skip(User::setDistrictId))
                  .addMappings(mapping -> mapping.skip(User::setId)).addMappings(mapping -> mapping.skip(User::setOrganizationId));
            User opsUser = mapper.map(aiUser, User.class);
            opsUser.setCreatedBy("CreatedBy-AIUser");
            opsUser.setCreated(Instant.now().toEpochMilli());
            List<Role> list = new ArrayList<>();
            opsUser.setAuthorities(list);
            opsUser.setLastModifiedBy("ModifiedBy-AIUser");
            opsUser.setModified(Instant.now().toEpochMilli());
            opsUser.setTitle("Default-AIUser");
            opsUser.setStatus("Active");
            opsUser.setLastPassResetDate(Date.from(Instant.now()));
            return opsUser;
        } catch (Exception e) {
            logger.error("Error while convert user details: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_CONVERT_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    public void saveOpsUserComingFromAi(AiUser aiUser) {
        Optional<User> user = userRepository.findByAzureId(aiUser.getAzureUserId());
        if (ObjectUtils.isEmpty(user)) {
            Optional<Organization> organization = Optional.ofNullable(
                    mongoTemplate.findOne(query(where("name").regex("^" + aiUser.getTenantName() + "$", "i")), Organization.class));
            User convertedAiUserToOpsUser = convertAiUserToOpsUser(aiUser);
            organization.ifPresent(value -> convertedAiUserToOpsUser.setOrganizationId(value.getId()));
            try {
                userRepository.save(convertedAiUserToOpsUser);
            } catch (Exception e) {
                logger.error("Error while create user coming from AI: {}", e.getMessage());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                                   .errorMessage(Constants.ERROR_WHILE_CREATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        }
    }

    public void deleteOpsUserComingFromAi(String azureId) {
        Optional<User> existUser = userRepository.findByAzureId(azureId);
        if (existUser.isPresent()) {
            try {
                userRepository.deleteById(existUser.get().getId());
            } catch (Exception e) {
                logger.error("Error while Delete user coming from AI: {}", e.getMessage());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                                   .errorMessage(Constants.ERROR_WHILE_DELETE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        }
    }

    public void updateOpsUserComingFromAi(AiUser aiUser) {
        Optional<User> existUser = userRepository.findByAzureId(aiUser.getAzureUserId());
        if (existUser.isPresent()) {
            User user = existUser.get();
            if (!ObjectUtils.isEmpty(aiUser.getTenantName())) {
                Optional<Organization> organization = Optional.ofNullable(
                        mongoTemplate.findOne(query(where("name").regex("^" + aiUser.getTenantName() + "$", "i")), Organization.class));
                organization.ifPresent(value -> user.setOrganizationId(value.getId()));
            }
            try {
                if (aiUser.getName() != null && aiUser.getSurname() != null && aiUser.getEmailAddress() != null) {
                    user.setFirstName(aiUser.getName());
                    user.setLastName(aiUser.getSurname());
                    user.setUserName(aiUser.getEmailAddress());
                    if (!ObjectUtils.isEmpty(aiUser.getMobileNumber())) {
                        user.setPrimaryPhoneNumber(Long.parseLong(aiUser.getMobileNumber()));
                    }
                    userRepository.save(user);
                }
            } catch (Exception e) {
                logger.error("Error while Update user coming from AI: {}", e.getMessage());
                Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                                   .errorMessage(Constants.ERROR_WHILE_UPDATE_USER + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST).build();
                throw new ErrorException(error);
            }
        }
    }

    public void updateLastPasswordResetFlagOnAzure(String azureId) {
        com.microsoft.graph.models.User request = new com.microsoft.graph.models.User();
        request.setPasswordPolicies("none");
        try {
            graphServiceClient.users().byUserId(azureId).patch(request);
        } catch (Exception e) {
            logger.error("Error while updating password reset flag: {}", e.getMessage());
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString())
                               .errorMessage(Constants.ERROR_WHILE_UPDATE_PASSWORD_RESET_FLAG + e.getMessage()).httpStatus(HttpStatus.BAD_REQUEST)
                               .build();
            throw new ErrorException(error);
        }
    }

    public void updateSelectColumn(UserPatchDTO requestUser) {
        try {
            Optional<User> existingUser=userRepository.findById(requestUser.getUserId());
            if (existingUser.isPresent()){
                User user=existingUser.get();
                user.setSelectedColumns(requestUser.getSelectedColumns());
                userRepository.save(user);
            }else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                       Constants.USER_NOT_FOUND_MESSAGE);
            }
        }catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                       Constants.ERROR_WHILE_UPDATE_USER + e.getMessage());
        }
    }

    public User updateUserFilters(String userId, Map<String, Object> filters) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().set("savedFilters", filters);
        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), User.class);
    }
}



