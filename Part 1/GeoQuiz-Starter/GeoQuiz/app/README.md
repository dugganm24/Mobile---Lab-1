# Part 1: The GeoQuiz Challenges

## Group Members
- William Tyrrell - wftyrrell@wpi.edu
- Michael Duggan - mpduggan@wpi.edu

## App Description
This project extends the functionality of the GeoQuiz app by implementing two new features:

---

## Feature 1: Persist CheatActivity's UI State Across Rotation and Process Death

**Problem:**  
In the original app, users could rotate the screen after cheating to reset the activity and answer the question without getting the 
"cheating is wrong" message

**Solution:**  
We fixed this by editing CheatActivity.kt with these changes:
- Adding a variable `isAnswerShown` to track whether the answer has been shown
- Using `onSaveInstanceState()` and `savedInstanceState` to save and restore this value during activity recreation
- Re-displaying the answer in `CheatActivity` if `isAnswerShown` is `true`, and correctly returning the cheat result to `MainActivity`.

This ensures that once a user cheats, they cannot hide that action by rotating or killing the app.

---

## Feature 2: Track Cheating on a Question-by-Question Basis

**Problem:**  
In the original app, once a user cheated the "Cheating is wrong" message would appear for all questions even if they didn’t cheat again.

**Solution:**  
- We replaced the single `isCheater` flag with a `cheatedQuestions` list in QuizViewModel where each element tracks whether the user cheated on that specific question index.
- Updated MainActivity to call `markCurrentQuestionAsCheated()` from QuizViewModel.kt after the user cheats.
- Modified the `checkAnswer()` logic to show the “Cheating is wrong” toast only if the user cheated on the current question using the `currentQuestionCheated` flag from QuizViewModel.kt.

This makes it so the user can cheat on one question and answer the next normally without the "cheating is wrong" message persisting


