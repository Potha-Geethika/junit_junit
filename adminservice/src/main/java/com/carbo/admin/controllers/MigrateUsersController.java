package com.carbo.admin.controllers;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.services.MigrateUsersService;

@RestController
@RequestMapping(value = "/v1/user/migrate")
public class MigrateUsersController {
    private final MigrateUsersService migrateusersService;

    public MigrateUsersController(MigrateUsersService migrateusersService) {
        this.migrateusersService = migrateusersService;
    }

    /**
     * API to save existing AI users in Ops for migration
     */
    @PostMapping (value = "/saveAiUsers")
    public List<AiUser> saveAiUsersAndCollectUnsaved(@RequestBody List<AiUser> aiUserList) {
        return migrateusersService.saveAiUsersAndCollectUnsaved(aiUserList);
    }

    /**
     * API to fetch all the user from DB for migration
     */
    @GetMapping (value = "/getAllUsersForAi")
    public List<UserResponseDTO> getAllUsersForAi() {
        return migrateusersService.getAllUsersForAi();
    }

    /**
     * API to update the azureUserId of ops User after migration of AI user on azure ad
     */
    @PutMapping (value = "/setUserAzureId")
    public List<User> setUserAzureId(@RequestBody List<AiUser> list) {
        return migrateusersService.setUserAzureId(list);
    }

}

