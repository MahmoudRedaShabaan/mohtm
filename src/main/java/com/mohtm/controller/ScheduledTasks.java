package com.mohtm.controller;

import com.mohtm.services.AnniversaryNotificationService;
import com.mohtm.services.AnniversaryTodayService;
import com.mohtm.services.CommonNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class ScheduledTasks {

    @Autowired
    public CommonNotificationService commonNotificationService;

    @Autowired
    public AnniversaryTodayService anniversaryTodayService;

    @Autowired
    public AnniversaryNotificationService anniversaryNotificationService;



    //
    @Scheduled(cron = "0 0 17 * * ?",zone="Africa/Cairo")
    public void commonNotif() {
        commonNotificationService.sendCommonNotif();
    }

    @Scheduled(cron = "0 0 9 * * ?",zone="Africa/Cairo")
    public void todayNotif() {
        try {
            anniversaryTodayService.processAndSendNotifications();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("todayNotif Exception"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 22 * * ?",zone="Africa/Cairo")
    public void specialNotif() {
        try {
            anniversaryNotificationService.processAndSendNotifications();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("specialNotif Exception"+e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
