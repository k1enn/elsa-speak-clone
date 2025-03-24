package com.example.elsa_speak_clone.services;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGptService {
    private static final String TAG = "ChatGptService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String MODEL = "gpt-3.5-turbo";
    
    private String apiKey;
    private OkHttpClient client;
    
    public interface ChatGptCallback {
        void onResponse(String response);
        void onError(String error);
    }
    
    public ChatGptService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    
    public void sendMessage(String message, ChatGptCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", MODEL);
            
            JSONArray messagesArray = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Bạn là trợ lý giáo viên tiếng Anh thông minh. " +
                    "Bạn giúp học sinh học tiếng Anh, sửa lỗi ngữ pháp, từ vựng, và phát âm. " +
                    "Khi được hỏi nghĩa tiếng Việt của từ tiếng Anh bất kỳ hãy trả lời danh từ, động từ, " +
                    "tính từ của từ đó nếu có" +
                    "Truờng hợp người dùng chỉ đưa ra một từ tiếng Anh hoặc một cụm từ ngắn, hãy tự động dịch sang tiếng Việt" +
                    "và giải thích ý nghĩa của từ hoặc cụm từ đó" +
                    "Hãy cố gắng trả lời bằng tiếng Việt trừ khi được yêu cầu dùng tiếng Anh. " +
                    "Luôn trả lời ngắn gọn, rõ ràng và thân thiện.");
            messagesArray.put(systemMessage);
            
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messagesArray.put(userMessage);
            
            jsonBody.put("messages", messagesArray);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 1024);
            
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError("Can not connect to server.");
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "Unsuccessful response: " + response.code());
                            callback.onError("Yêu cầu thất bại: " + response.code());
                            return;
                        }
                        
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject messageObj = firstChoice.getJSONObject("message");
                        String content = messageObj.getString("content");
                        
                        callback.onResponse(content.trim());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                        callback.onError("Error parsing JSON");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request", e);
            callback.onError("Error creating JSON request");
        }
    }
} 