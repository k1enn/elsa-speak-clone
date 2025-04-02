# English Learning App

<div align="center">
  <p><strong>Follow me:</strong></p>
</div>

<div align="center">
  <p>
    <img src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/github.png" alt="GitHub Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://github.com/k1enn" target="_blank">GitHub</a></strong>
    <img style="padding-left: 10px; " src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/codeforces.png" alt="Codeforces Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://codeforces.com/profile/dinhtrungkien" target="_blank">Codeforces</a></strong>
    <img style="padding-left: 10px;" src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/linkedin.png" alt="LinkedIn Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://www.linkedin.com/in/k1enn/" target="_blank">LinkedIn</a></strong>
  </p>
      <small> December, 2024</small>
</div>

--- 
A comprehensive Android application designed to help users learn English through interactive lessons, pronunciation practice, quizzes, and more. This app follows self-guided language learning principles to help users build a strong foundation in English.

## Features

- **Interactive Lessons**: Structured lessons organized by difficulty levels and topics relevant to daily life
- **Pronunciation Practice**: Train your pronunciation skills with difficulty selection and topic-based vocabulary
- **Progress Tracking**: Track your learning streak, view rankings, and get reports on your learning achievements
- **Personalized Learning**: Automatically increasing difficulty levels as you progress
- **Share Learning Progress**: Share your achievements and learning streaks with friends on social media
- **User Authentication**: Login/Register functionality with Google account integration
- **Offline Support**: Data stored in SQLite for offline access
- **Additional Tools**:
  - **Chatbot Assistant**: Ask questions about grammar, vocabulary, or get help improving your English skills
  - **Dictionary**: Look up word definitions, pronunciations, and examples
  - **News Feed**: Read the latest news in English to practice reading comprehension
  - **Leaderboard**: Compare your progress with other learners

## Technical Details

- **Platform**: Android (Java)
- **Minimum Android Version**: Compatible with all Android platforms
- **Backend**: Firebase for authentication and remote data storage
- **Local Storage**: SQLite for storing progress and lesson data
- **APIs Integration**: 
  - Google Authentication API
  - Speech Recognition API
  - News API
  - Dictionary API
  - ChatGPT API for the English assistant


## Installation

1. Clone this repository:
   ```
   git clone https://github.com/k1enn/elsa-speak-clone.git
   ```

2. Open the project in Android Studio

3. Configure Firebase:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add your Android app to the Firebase project
   - Download the `google-services.json` file and place it in the `~/project_name/app/` directory.
   - Enable Authentication and Firestore in your Firebase project

4. Add your API keys:
   - Create a `keys.properties` file in the project root directory
   - Add your API keys for ChatGPT, News, and other services

5. Build and run the project on your Android device or emulator

## Usage

1. Register using your email or Google account
2. Select your preferred learning topics and difficulty level
3. Complete daily lessons to maintain your learning streak
4. Practice pronunciation by speaking the displayed words
5. Track your progress in the profile section
6. Use the chatbot for any English-related questions
7. Check the leaderboard to see your ranking among other learners

## Database Structure

The app uses a combination of Firebase and SQLite with the following main tables:
- Users: Stores user information and authentication data
- Lessons: Contains lesson content organized by topics and difficulty levels
- Vocabulary: Stores words and their pronunciations
- UserProgress: Tracks user completion of lessons and learning streaks
- Quizzes: Contains questions and answers for assessments
- UserScores: Records user performance in quizzes

## Contributors

- Đinh Trung Kiên
- Nguyễn Sỹ Hoàng
- Dương Gia Huy
- Huỳnh Gia Huy
