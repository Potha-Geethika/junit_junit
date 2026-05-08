package com.carbo.admin.kafka;
// Importing required classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Consumer {
    private final ObjectMapper objectMapper;

    private final UserService userService;

    @Value ("${user.kafka.consumer.opsUserTopic}")
    private String opsUserTopic;

    @Value ("${user.kafka.groupId}")
    private String groupId;

    @Autowired
    public Consumer(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @KafkaListener (topics = "AzureUserOps", groupId = "ops-consumer-group")
    public void listen(String record) {
        try {
            log.info("Received record from AI");
            AiUser user = objectMapper.readValue(record, AiUser.class);
            //            System.out.println("after conversion :" + objectMapper.writeValueAsString(user));
            if (user.getNotificationType().equals("CREATE")) {
                log.info("Received record for creating the user");
                userService.saveOpsUserComingFromAi(user);
            }
            if (user.getNotificationType().equals("DELETE")) {
                log.info("Received record for deleting the user");
                userService.deleteOpsUserComingFromAi(user.getAzureUserId());
            }
            if (user.getNotificationType().equals("UPDATE")) {
                log.info("Received record for updating the user");
                userService.updateOpsUserComingFromAi(user);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}