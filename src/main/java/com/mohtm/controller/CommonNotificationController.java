package com.mohtm.controller;


import com.mohtm.services.CommonNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonNotificationController {

    @Autowired
    public CommonNotificationService commonNotificationService;
    @GetMapping("commonNotif")
    String  commonNotif() {
        commonNotificationService.sendCommonNotif();
        return "success";
    }
}
