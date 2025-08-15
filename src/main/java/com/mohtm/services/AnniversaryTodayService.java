package com.mohtm.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import com.mohtm.firebase.FirebaseInitializer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class AnniversaryTodayService {


    private Firestore firestore;

    public AnniversaryTodayService() {
        // Ensure Firebase is initialized
        try {
            FirebaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Today Notification Script :Failed to initialize Firebase: " + e.getMessage());
            return;
        }
        this.firestore = FirestoreClient.getFirestore();
    }

    public void processAndSendNotifications() throws ExecutionException, InterruptedException {
        System.out.println("Today Notification Script : Daily notification job started updated ! ");

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentDay = today.getDayOfMonth();
        System.out.println("Today Notification Script : Current Date = " + new Date());


        //fetch all anniversaries from firebase
        CollectionReference anniversariesCollection = firestore.collection("anniversaries");

        QuerySnapshot anniversariesSnapshot = anniversariesCollection.get().get();

        System.out.println("Today Notification Script : " + "Start Processing " + anniversariesSnapshot.size() + " anniversaries.");
        if (anniversariesSnapshot.size() <= 0) {
            System.out.println("Today Notification Script : " + "there is not Notification found In System");
        }

        boolean isNotToday = false;
        for (DocumentSnapshot anniversaryDoc : anniversariesSnapshot) {
            // Get date fields as Timestamps from Firestore
            LocalDate anniversaryDate = anniversaryDoc.getTimestamp("date").toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate rememberBeforeDate = anniversaryDoc.getTimestamp("rememberBeforeDate").toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            // Apply your day and month logic in Java code
            if (isDateInRange(currentDay, currentMonth, anniversaryDate)) {
                isNotToday = true;
                String createdBy = anniversaryDoc.getString("createdBy");
                System.out.println("Today Notification Script : At :" +new Date()+ " ,Notification ID : " + anniversaryDoc.getId() + " ,Found matching anniversary for user ID: " + createdBy);

                // Now, fetch the user data
                fetchAndNotifyUser(createdBy, anniversaryDoc);
            }
        }
        if (!isNotToday) {
            System.out.println("Today Notification Script : " + "there is not Notification found To send for today");
        }
        System.out.println("Today Notification Script : end at : " + new Date() + ", Daily notification job finished!");
    }

    private boolean isDateInRange(int currentDay, int currentMonth, LocalDate anniversaryDate) {
        // Compare current date (day and month) to anniversaryDate
        boolean condition = false;
        condition = (currentMonth == anniversaryDate.getMonthValue() && currentDay == anniversaryDate.getDayOfMonth());
        return condition;
    }

    private void fetchAndNotifyUser(String userId, DocumentSnapshot anniversaryDoc) throws ExecutionException, InterruptedException {
        System.out.println("Today Notification Script : " + "Starting Fetch data and Send Notification For UserID= "+userId);
        DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
        if (userDoc.exists()) {
            LocalDate anniversaryDate = anniversaryDoc.getTimestamp("date").toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String relation = anniversaryDoc.getString("relationship");
            if (relation != null && !relation.isEmpty()) {
                relation = " ," + relation;
            }
            String typevalueEn = "";
            String typevalueAr = "";
            if (anniversaryDoc.getString("type").equals("4")) {
                typevalueEn = anniversaryDoc.getString("addType");
                typevalueAr = anniversaryDoc.getString("addType");
            } else {
                ApiFuture<QuerySnapshot> typeDoc = firestore.collection("eventtype").whereEqualTo("id", anniversaryDoc.getString("type")).get();
                typevalueEn = typeDoc.get().getDocuments().getFirst().get("englishName").toString();
                typevalueAr = typeDoc.get().getDocuments().getFirst().get("arabicName").toString();
            }
            String userEmail = userDoc.getString("email");
            String userName = userDoc.getString("firstName");
            String fcmToken = userDoc.getString("fcmToken");
            String lang = userDoc.getString("lang");
            // Add any other user or anniversary fields you need for the notification
            if (fcmToken != null && !fcmToken.isEmpty()) {
                String title = "";
                String body = "";
                if (lang == null || lang.isBlank()) {
                    title = "(Today) " + "Remember " + typevalueEn + relation;
                    body = anniversaryDoc.getString("title");
                } else if (lang.equals("ar")) {
                    title = "(اليوم) " + "تذكر " + typevalueAr + relation;
                    body = anniversaryDoc.getString("title");
                } else {
                    title = "(Today) " + "Remember " + typevalueEn + relation;
                    body = anniversaryDoc.getString("title");
                }

                // Build a custom notification message for this user.
                System.out.println("Today Notification Script : " + "prepare and Sending notification :"+anniversaryDoc.getId()+" to " + userName + " at " + userEmail +",At"+new Date());
                Message message = Message.builder()
                        .setToken(fcmToken) // Send to this specific device token
                        .setNotification(Notification.builder()
                                .setTitle(title) // Personalize the title
                                .setBody(body) // Personalize the body
                                .build())
                        .putData("userEmail", userEmail)
                        .putData("action", "Send_specific_Notification")
                        .build();

                // Send the message and get the response message ID.
                try {
                    String response = FirebaseMessaging.getInstance().send(message);
                    System.out.println("Today Notification Script : " + "ent success notification :"+anniversaryDoc.getId()+" to " + userName + " at " + userEmail+",At"+new Date());

                } catch (FirebaseMessagingException e) {
                    System.out.println("Today Notification Script : " +"send message exception : " + e.getMessage());
                    throw new RuntimeException(e);

                }

            } else {
                System.out.println("Today Notification Script : " + "User " + userDoc.getId() + " has no valid FCM token.");
            }

        } else {
            System.out.println("Today Notification Script : " + "User not found For UserID= "+userId);
        }
    }

    public static void main(String[] args) {
        AnniversaryTodayService service = new AnniversaryTodayService();
        try {
            service.processAndSendNotifications();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
