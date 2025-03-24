package com.example.elsa_speak_clone.utilities;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lớp quản lý cấu hình từ file ngoài như config.properties
 * để lưu trữ thông tin nhạy cảm an toàn hơn
 */
public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_API_KEY = ""; // Key mặc định
    
    private static Properties properties;
    

    public static void initialize(Context context) {
        properties = new Properties();
        try {
            InputStream inputStream = context.getAssets().open(CONFIG_FILE);
            properties.load(inputStream);
            inputStream.close();
            Log.d(TAG, "Successfully read property");
        } catch (IOException e) {
            Log.e(TAG, "Unable to read property: " + e.getMessage());
        }
    }

    public static String getOpenAiApiKey() {
        if (properties == null) {
            Log.e(TAG, "ConfigManager haven't initialized");
            return DEFAULT_API_KEY;
        }
        
        String apiKey = properties.getProperty("openai_api_key", DEFAULT_API_KEY);
        if (apiKey.isEmpty()) {
            Log.w(TAG, "Wrong key");
        }
        
        return apiKey;
    }
    
    public static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            Log.e(TAG, "ConfigManager chưa được khởi tạo");
            return defaultValue;
        }
        
        return properties.getProperty(key, defaultValue);
    }
} 