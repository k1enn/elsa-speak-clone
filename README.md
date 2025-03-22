# ELSA Speak Clone

## Cài đặt API key

Để sử dụng tính năng ChatBot, bạn cần cấu hình API key theo các bước sau:

1. Trong thư mục `app/src/main/assets/`, sao chép file `config.properties.template` và đổi tên thành `config.properties`
2. Mở file `config.properties` và thay giá trị `YOUR_API_KEY` bằng API key OpenAI của bạn
3. File `config.properties` sẽ không được commit lên Git vì đã được cấu hình trong `.gitignore`

```
# OpenAI API key
openai_api_key=YOUR_API_KEY
```

## Giới thiệu

Ứng dụng clone từ ELSA Speak, giúp người dùng học và luyện phát âm tiếng Anh.
 
