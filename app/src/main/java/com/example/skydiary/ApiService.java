package com.example.skydiary;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {
    @POST("api/auth/register")
    Call<UserResponse> register(@Body UserRegisterRequest request);

    @POST("api/auth/login")
    Call<UserResponse> login(@Body UserLoginRequest request);

    @GET("api/notes")
    Call<List<Note>> getNotes(@Header("Authorization") String token);

    @POST("api/notes/sync")
    Call<SyncResult> syncNotes(@Header("Authorization") String token, @Body SyncRequest request);

    @POST("api/notes")
    Call<Note> createNote(@Header("Authorization") String token, @Body Note note);

    @POST("api/notes/upload-local")
    Call<SyncResult> uploadLocalNotes(@Header("Authorization") String token, @Body SyncRequest request);

    @PUT("api/auth/update")
    Call<UserResponse> updateUser(@Header("Authorization") String token, @Body UserUpdateRequest request);

    @PUT("api/notes/{id}")
    Call<Note> updateNote(@Header("Authorization") String token, @Path("id") String id, @Body Note note);

    @DELETE("api/notes/{id}")
    Call<Void> deleteNote(@Header("Authorization") String token, @Path("id") String id);
}