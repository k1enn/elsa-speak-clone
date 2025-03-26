package com.example.elsa_speak_clone.services;

import com.example.elsa_speak_clone.interfaces.DictionaryInterface;
import com.example.elsa_speak_clone.models.WordResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DictionaryService {
    private static final String BASE_URL = "https://api.dictionaryapi.dev/";
    private DictionaryInterface api;

    public DictionaryService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(DictionaryInterface.class);
    }

    public void getDefinition(String word, final DictionaryCallback callback) {
        Call<List<WordResponse>> call = api.getDefinition(word);
        call.enqueue(new Callback<List<WordResponse>>() {
            @Override
            public void onResponse(Call<List<WordResponse>> call, Response<List<WordResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<WordResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface DictionaryCallback {
        void onSuccess(List<WordResponse> response);
        void onError(String errorMessage);
    }
}
