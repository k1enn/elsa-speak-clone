package com.example.elsa_speak_clone.interfaces;

import com.example.elsa_speak_clone.models.WordResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryInterface {
    @GET("api/v2/entries/en/{word}")
    Call<List<WordResponse>> getDefinition(@Path("word") String word);
}
