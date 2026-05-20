package com.example.skydiary;

import android.util.Log;

import com.google.firebase.FirebaseApp;
// import com.example.skydiary.BuildConfig;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseManager() {
        // Disable reCAPTCHA completely (client-side) to avoid Play Services errors
        auth = FirebaseAuth.getInstance();
        // In the constructor, after auth = FirebaseAuth.getInstance();
        auth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // Use debug App Check provider (already working)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
        );

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }
}