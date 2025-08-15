package com.mohtm.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseInitializer {
    public static void initialize() throws FileNotFoundException, IOException {
       // FileInputStream serviceAccount = new FileInputStream("path/to/mohtm-serviceAccountKey.json");
        FirebaseOptions options;
        InputStream serviceAccount = FirebaseInitializer.class.getClassLoader().getResourceAsStream("mohtm-serviceAccountKey.json");

            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    // .setDatabaseUrl("https://<YOUR-DATABASE-NAME>.firebaseio.com") // For Realtime Database
                    .build();


        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
