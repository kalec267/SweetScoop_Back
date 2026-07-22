package com.sweetscoop.firebase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Component
public class FirebaseConfig {

    private static final String FIREBASE_KEY_PATH =
            "C:/Users/kalec/Documents/firebase/serviceAccountKey.json";

    @PostConstruct
    public void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            try (InputStream serviceAccount =
                    new FileInputStream(FIREBASE_KEY_PATH)) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials.fromStream(serviceAccount)
                        )
                        .build();

                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase 초기화 성공");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Firebase 키 파일을 읽을 수 없습니다: "
                            + FIREBASE_KEY_PATH,
                    e
            );
        }
    }
}