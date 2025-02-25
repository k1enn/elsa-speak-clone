package com.example.elsa_speak_clone.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FluentMeApi {
    @POST("pronunciation/score")
    Call<PronunciationScore> getPronunciationScore(
            @Part("text") RequestBody text,
            @Part MultipartBody.Part audio
    );


}