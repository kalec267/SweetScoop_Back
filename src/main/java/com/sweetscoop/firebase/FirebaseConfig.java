package com.sweetscoop.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.getApplicationDefault()
                    )
                    .setProjectId("sweetscoop-f5ca9")
                    .build();

            FirebaseApp.initializeApp(options);

            System.out.println("Firebase 초기화 완료");
            System.out.println(
                    "Firebase 프로젝트 ID: "
                    + FirebaseApp.getInstance()
                            .getOptions()
                            .getProjectId()
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Firebase 초기화 실패",
                    e
            );
        }
    }
}