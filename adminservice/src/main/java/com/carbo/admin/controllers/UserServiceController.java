package com.carbo.admin.controllers;

import static com.carbo.admin.utils.Constants.*;
import static com.carbo.admin.utils.Constants.USER_UPDATE_MESSAGE;
import static com.carbo.admin.utils.ControllerUtil.getUserName;
import static org.passay.CharacterCharacteristicsRule.ERROR_CODE;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.carbo.admin.model.*;
import com.carbo.admin.model.Error;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.services.DistrictService;
import com.carbo.admin.services.UserService;
import com.carbo.admin.utils.Constants;
import com.carbo.admin.utils.ControllerUtil;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mongodb.MongoWriteException;

import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@RestController
@RequestMapping (value = "v1/users")
public class UserServiceController {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength set as 12

    private final MongoTemplate mongoTemplate;

    UserService userService;

    DistrictService districtService;

    private WebClient webClient;

    @Autowired
    private SslContext sslContext;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    public UserServiceController(UserService userService, DistrictService districtService, WebClient webClient, MongoTemplate mongoTemplate) {
        this.userService = userService;
        this.districtService = districtService;
        this.webClient = webClient;
        this.mongoTemplate = mongoTemplate;
    }


    @GetMapping ("/")
    public List<User> getUsers(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        List<User> all = new ArrayList<>();
        if (((AbstractAuthenticationToken) principal).getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CARBO_ADMIN"))) {
            all = userService.getAll();
        } else {
            String organizationId = "";
            try {
                organizationId = ControllerUtil.getOrganizationId(request);
                all = userService.getByOrganizationId(organizationId);
            } catch (NullPointerException e) {
                String currentUserName = getUserName(request);
                if (currentUserName.equals("duy.nguyen@carboceramics.com")) {
                    all = userService.getAll();
                }
            }
        }
        return all;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public User getUser(@PathVariable("userId") String userId) {
        logger.debug("Looking up data for user {}", userId);
        User user = userService.getUser(userId).get();
        return user;
    }

    @RequestMapping(value = "/lastPassResetDate/{userName}", method = RequestMethod.GET)
    public Date getUserLastPassResetDate(@PathVariable("userName") String userName) {
        logger.debug("Looking up data for user {}", userName);
        User user = userService.getUserByUserName(userName).get();
        return user.getLastPassResetDate();
    }

    @RequestMapping(value = "/lastPassResetDate/{userName}", method = RequestMethod.POST)
    public void saveLastPassResetDate(HttpServletRequest request, @RequestBody User user, @PathVariable("userName") String userName) {
        User existUser = userService.getUserByUserName(userName).get();
        existUser.setLastPassResetDate(user.getLastPassResetDate());
        existUser.setStrength(user.isStrength());
        userService.saveUser(existUser);
    }

    @PostMapping (value = "/updateLastPasswordResetDate")
    public User updateLastPasswordResetDate(@RequestBody User user) {
        Optional<User> optionalExistUser = userService.getUserByUserName(user.getUserName());
        if (optionalExistUser.isPresent()) {
            User existUser = optionalExistUser.get();
            userService.updateLastPasswordResetFlagOnAzure(existUser.getAzureId());
            existUser.setLastPassResetDate(new Date());
            return userService.saveUser(existUser);
        } else {
            logger.error("User Details Not Found");
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage(USER_NOT_FOUND_MESSAGE)
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    @PutMapping (value = "/change-signature")
    public ResponseEntity updateSignature(HttpServletRequest request, @RequestBody ChangeSignature changeSignature) {
        String userName = ControllerUtil.getUserName(request);
        logger.debug("Updating signature for user {}", userName);
        User exist = userService.getUserByUserName(userName).get();
        if (exist == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            exist.setSignature(changeSignature.getSignature());
            userService.updateUser(exist);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping (value = "/{userId}", method = RequestMethod.PUT)
    public ResponseEntity updateUser(@PathVariable ("userId") String userId, @RequestBody User user,@RequestParam(required = false) Boolean isLogin) {
        User existingUser = userService.getUser(userId).get();
        if (Boolean.TRUE.equals(isLogin)){
            existingUser.setLastLogInTime(Instant.now().toEpochMilli());
            userService.saveUser(existingUser);
            Response response = Response.builder().successCode(USER_UPDATE_CODE)
                    .successMessage(USER_LOG_IN_TIME).build();
            return ResponseEntity.ok(response);
        }
        userService.updateUserOnAzureAd(user, userId);
        Response response = Response.builder().successCode(USER_UPDATE_CODE)
                .successMessage(USER_UPDATE_MESSAGE).build();
        return ResponseEntity.ok(response);
    }


    @RequestMapping (value = "/", method = RequestMethod.POST)
    ResponseEntity saveUser(HttpServletRequest request, @RequestBody final User user) {
        String newPassword = generatePassayPassword();
        try {
            user.setPassword(newPassword);
            userService.saveUserOnAzureAd(user);
            sendEmail(request, user, newPassword);
            Response response = Response.builder().successCode(USER_CREATED_CODE).successMessage(USER_CREATED_MESSAGE).build();
            return ResponseEntity.ok(response);
        } catch (DuplicateKeyException | MongoWriteException ex) {
            log.error("Exception occurred: {}", ex.getMessage());
            Error error = Error.builder().errorCode(Constants.USER_ALREADY_EXISTS_CODE).errorMessage(Constants.USER_ALREADY_EXISTS_MESSAGE).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getCause());
    }
    }



    @RequestMapping (value = "/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable ("userId") String padId) {
        userService.deleteUserOnAzureAd(padId);
    }

    @RequestMapping (value = "/change-password", method = RequestMethod.PUT)
    public ResponseEntity changePassword(HttpServletRequest request, @RequestBody ChangePassword changePassword) {
        List<String> lastFivePasswords;
        if (changePassword.getCurPassword() != null) {
            String userName = ControllerUtil.getUserName(request);
            User exist = userService.getUserByUserName(userName).get();
            lastFivePasswords = exist.getLastFivePasswords();
            if (!encoder.matches(changePassword.getCurPassword(), exist.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current Password doesn't match");
            } else {
                boolean isPasswordMatches = isPasswordCheckedRule(userName, changePassword.getNewPassword());
                if (isPasswordMatches) {
                    return ResponseEntity.status( HttpStatus.BAD_REQUEST).body("The new password must not match any of the last five passwords");
                }
                String encodedPassword = encoder.encode(changePassword.getNewPassword());
                exist.setPassword(encodedPassword);
                exist.setStrength(Boolean.parseBoolean(changePassword.getIsStrength()));
                if(lastFivePasswords==null){
                    lastFivePasswords= new ArrayList<>();
                    lastFivePasswords.add(encodedPassword);
                }else{
                    lastFivePasswords.add(encodedPassword);
                }
                if (lastFivePasswords.size() > 5) {
                    lastFivePasswords.remove(0);
                }
                exist.setLastFivePasswords(lastFivePasswords);
                userService.updateUser(exist);
                return new ResponseEntity(HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    private void sendEmail(HttpServletRequest request, User user, String password) {
        try {
            String token = request.getHeader("Authorization");

            Email email = new Email();
            email.setTo(user.getUserName());
            email.setType(SET_TYPE);
            email.setBody("Hello " + (user.getFirstName().equalsIgnoreCase("Document") ? user.getUserName() : user.getFirstName())
                    + ",<br/><br/>You have been added as a user to <a href='https://ops-ak8-test.fracpro.ai/'>FRACPRO OPS</a> application.<br/>"
                    + "Please use this <a href='https://ops-ak8-test.fracpro.ai/'>link</a>, your email address and password: <b>"
                    + password + "</b> to log in. You can change the password after logging in.<br/><br/>Regards,<br/>FracPro Support<br/>"
                    + "<a href='mailto:support@fracpro.com'>support@fracpro.com</a>");
            email.setSubject("Credentials to login to FracPro OPS");

            // Configure WebClient
            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
            WebClient webClient = webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(SEND_EMAIL)
                    .build();

            // Send email
            String response = webClient.post()
                    .uri("/v1/email/send")
                    .header("Authorization", token)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(email)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Email sent successfully, response: {}", response);

        } catch (Exception ex) {
            logger.error("Error sending password to the user", ex);
        }
    }


    private String generatePassayPassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^&*()_";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        String password = gen.generatePassword(8, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
        return password;
    }

    /**
     * This method is to check the password rules like new password should not be
     * last five passwords.
     *
     * @return boolean value
     */
    private boolean isPasswordCheckedRule(String userName, String newPassword) {
        boolean isPassMatches = false;
        if (userName != null) {
            User exist = userService.getUserByUserName(userName).get();
            List<String> lastFivePasswords = exist.getLastFivePasswords();
            if (lastFivePasswords != null) {
                for (int j = 0; j < lastFivePasswords.size(); j++) {
                    if (encoder.matches(newPassword, lastFivePasswords.get(j))) {
                        isPassMatches = true;
                        break;
                    }
                }

            }

        }
        return isPassMatches;
    }
    @RequestMapping(value = "/updateDistrictIds", method = RequestMethod.PUT)
    public void updateDistrictIds(HttpServletRequest request) {
        List<User> all = userService.getAll();

        for (User user : all) {
            // Apply the logic for districtId and districtIds
            String districtId = user.getDistrictId();
            List<String> districtIds = user.getDistrictids();

            if (districtIds == null) {
                districtIds = new ArrayList<>();
            }

            if (districtId != null && !districtId.isEmpty()) {
                // districtId is not null and not empty
                if (districtIds.contains(districtId)) {
                    // Remove the previous districtId from districtIds if it exists
                    districtIds.remove(districtId);
                }
                districtIds.add(districtId);
            } else if (districtId == null || districtId.isEmpty()) {
                // districtId is null or empty, add all districtIds from the same organization to districtIds array
                String organizationId = user.getOrganizationId(); // Assuming organizationId is a property of User object
                if (organizationId != null && !organizationId.isEmpty()) {
                    // Retrieve districtIds from the same organization
                    List<String> organizationDistrictIds = getDistrictIdsByOrganizationId(organizationId);
                    // Remove the previous districtId from districtIds if it exists
                    districtIds.removeAll(organizationDistrictIds);
                    districtIds.addAll(organizationDistrictIds);
                }
            }
            user.setDistrictids(districtIds);


            // Save the updated user object back to the database
            userService.updateUser(user);
        }
    }
    private List<String> getDistrictIdsByOrganizationId(String organizationId) {
        // Implement the logic to retrieve districtIds by organizationId
        List<District> districts = districtService.getByOrganizationId(organizationId);

        List<String> districtIds = new ArrayList<>();
        for (District district : districts) {
            districtIds.add(district.getId());
        }

        return districtIds;
    }


    @PostMapping("/update-columns")
    public void updateColumns(@RequestBody UserPatchDTO user) {
            userService.updateSelectColumn(user);
    }

    @PatchMapping("/{userId}/filters")
    public ResponseEntity<User> updateUserFilters(@PathVariable String userId,
                                                  @RequestBody Map<String, Object> filters) {
        User updatedUser = userService.updateUserFilters(userId, filters);
        return ResponseEntity.ok(updatedUser);
    }
}

