package com.example.elsa_speak_clone.services;

import com.example.elsa_speak_clone.interfaces.NewsApiInterface;
import com.example.elsa_speak_clone.models.NewsResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiService {
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final String API_KEY = "59f053d29ae04e0686c904d704155655";
    
    private static NewsApiService instance;
    private NewsApiInterface newsApiInterface;
    
    private NewsApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        newsApiInterface = retrofit.create(NewsApiInterface.class);
    }
    
    public static synchronized NewsApiService getInstance() {
        if (instance == null) {
            instance = new NewsApiService();
        }
        return instance;
    }
    
    public Call<NewsResponse> getTopHeadlines(String country) {
        return newsApiInterface.getTopHeadlines(country, API_KEY);
    }
    
    public Call<NewsResponse> getTopHeadlinesBySource(String source) {
        return newsApiInterface.getTopHeadlinesBySource(source, API_KEY);
    }
} 