package com.mohtm.services;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mohtm.firebase.FirebaseInitializer;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CommonNotificationService {
    private Firestore firestore;

    public CommonNotificationService() {
        // Ensure Firebase is initialized
        try {
            FirebaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("common Script : Failed to initialize Firebase: " + e.getMessage());
            return;
        }
        this.firestore = FirestoreClient.getFirestore();
    }

    public void sendCommonNotif() {
        System.out.println("common Script : Daily notification job started updated-2 ! ");
        LocalDate currentDate = LocalDate.now();
        System.out.println("common Script : Current Date = " + new Date());

        try {
            // 1. Fetch all user FCM tokens from Firestore
            //tokens for Ar and En users
            List<String> tokensEn = new ArrayList<>();
            List<String> tokensAr = new ArrayList<>();
            try {
                // fetch users for DB
                QuerySnapshot snapshot = firestore.collection("users").get().get();

                if (snapshot.isEmpty()) {
                    System.out.println("common Script : " + "No users found to send notifications to.");
                    return; // Exit if no users are found
                }

                for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                    String fcmToken = document.getString("fcmToken");
                    String lang = document.getString("lang");
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        if (lang == null || lang.isBlank()) {
                            tokensEn.add(fcmToken);
                        } else if (lang.equals("ar")) {
                            tokensAr.add(fcmToken);
                        } else {
                            tokensEn.add(fcmToken);
                        }
                        System.out.println("common Script : " + "User " + document.getId() + " has FCM token , email= " + document.getString("email") + " ,and lang = " + lang);
                    } else {
                        System.out.println("common Script : " + "User " + document.getId() + " has no valid FCM token.");
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                System.err.println("common Script : " + "Error fetching FCM tokens from Firestore: " + e.getMessage());
                return;
            }

            if (tokensEn.isEmpty() && tokensAr.isEmpty()) {
                System.out.println("common Script : " + "No valid FCM tokens found.");
                return;
            }
            // notification english
            System.out.println("common Script : " + "Start prepare Notification and Messages.");
            Notification notificationEn = Notification.builder()
                    .setTitle("Reminder from Mohtm!")
                    .setBody("Oh ,Now you can add occasion for today!")
                    .build();

            Notification notificationAr = Notification.builder()
                    .setTitle("مهتم بيقولك ..")
                    .setBody("دلوقتى , يمكنك إضافه مناسبات لهذا اليوم")
                    .build();

            Map<String, String> data = new HashMap<>();
            data.put("type", "daily_schedule_reminder");

            if(!tokensEn.isEmpty()) {
                MulticastMessage messageEn = MulticastMessage.builder()
                        .setNotification(notificationEn)
                        .putAllData(data)
                        .addAllTokens(tokensEn)
                        .build();
                try {
                    System.out.println("common Script : start at :" + new Date()+ " ,Start Send Notification and Messages.");
                    FirebaseMessaging.getInstance().sendEachForMulticast(messageEn);
                    System.out.println("common Script : " + "Successfully sent messages to " + tokensEn.size() + " devices, with english language");

                    System.out.println("common Script : end at : " + new Date() + ", Daily notification job finished!");

                } catch (FirebaseMessagingException e) {
                    System.out.println("common Script : " + "Exception occurred while sending English lang , " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
            if(!tokensAr.isEmpty()) {
                MulticastMessage messageAr = MulticastMessage.builder()
                        .setNotification(notificationAr)
                        .putAllData(data)
                        .addAllTokens(tokensAr)
                        .build();

                try {
                    System.out.println("common Script : start at :" + new Date()+ " ,Start Send Notification and Messages.");
                    FirebaseMessaging.getInstance().sendEachForMulticast(messageAr);
                    System.out.println("common Script : " + "Successfully sent messages to " + tokensEn.size() + " devices, with arabic language");

                    System.out.println("common Script : end at : " + new Date() + ", Daily notification job finished!");

                } catch (FirebaseMessagingException e) {
                    System.out.println("common Script : " + "Exception occurred while sending Arabic lang , " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }


        } catch (Exception e) {
            System.out.println("common Script : " + "Exception occurred , " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        CommonNotificationService service = new CommonNotificationService();
        service.sendCommonNotif();
    }
}
