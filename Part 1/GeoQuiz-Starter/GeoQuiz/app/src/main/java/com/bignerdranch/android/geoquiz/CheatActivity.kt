package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.geoquiz.databinding.ActivityCheatBinding

const val EXTRA_ANSWER_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown"
private const val EXTRA_ANSWER_IS_TRUE =
    "com.bignerdranch.android.geoquiz.answer_is_true"

class CheatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheatBinding

    private var answerIsTrue = false
    private var isAnswerShown = false //Track is answer is shown default false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        isAnswerShown = savedInstanceState?.getBoolean(EXTRA_ANSWER_SHOWN, false) ?: false
        //save isAnswerShown state for screen rotation

        //Show answer again if the user cheated and phone rotates
        if (isAnswerShown){
            showAnswer()
            setAnswerShownResult(true)
        }

        binding.showAnswerButton.setOnClickListener {
            val answerText = when {
                answerIsTrue -> R.string.true_button
                else -> R.string.false_button
            }
            showAnswer() //added show answer to persist
            isAnswerShown = true //show if cheat show is pressed
            binding.answerTextView.setText(answerText)
            setAnswerShownResult(true)
        }
    }

    // Save isAnswershown for screen rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_ANSWER_SHOWN, isAnswerShown) //save isAnswerShown for rotation
    }

    // Show answer logic
    private fun showAnswer() {
        val answerText = if (answerIsTrue) R.string.true_button else R.string.false_button
        binding.answerTextView.setText(answerText)
    }

    private fun setAnswerShownResult(isAnswerShown: Boolean) {
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown)
        }
        setResult(Activity.RESULT_OK, data)
    }

    companion object {
        fun newIntent(packageContext: Context, answerIsTrue: Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)
            }
        }
    }
}
