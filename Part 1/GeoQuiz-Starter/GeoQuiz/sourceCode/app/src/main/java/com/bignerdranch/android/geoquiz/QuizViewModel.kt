package com.bignerdranch.android.geoquiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"
const val CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY"
//const val IS_CHEATER_KEY = "IS_CHEATER_KEY" no longer need global flag

class QuizViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var currentIndex: Int
        get() = savedStateHandle.get(CURRENT_INDEX_KEY) ?: 0
        set(value) = savedStateHandle.set(CURRENT_INDEX_KEY, value)

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )
    private val cheatedQuestions = MutableList(questionBank.size) {false}
    //added list to track if question was cheated

//    var isCheater: Boolean
//        get() = savedStateHandle.get(IS_CHEATER_KEY) ?: false
//        set(value) = savedStateHandle.set(IS_CHEATER_KEY, value)

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun markCurrentQuestionAsCheated() {  //called by main activity to mark the current question as cheated
        cheatedQuestions[currentIndex] = true
    }

    val currentQuestionCheated: Boolean  //main activity uses to check cheat status of current question
        get() = cheatedQuestions[currentIndex]

}

