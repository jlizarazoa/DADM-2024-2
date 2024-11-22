package com.example.androidtictactoe

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

private var current_player = "X"

private val human = "X"
private val android = "O"

private var wins = 0
private var loses = 0
private var ties = 0
private var activeGame = true

enum class DifficultyLevel {
    Easy,
    Harder,
    Expert
}

private var mDifficultyLevel = DifficultyLevel.Easy

private val size = 8
private lateinit var buttons: Array<Button>
@SuppressLint("StaticFieldLeak")
private lateinit var turn: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tiesText: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var humanWins: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var androidWins: TextView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttons = arrayOf(
            findViewById<Button>(R.id.one),
            findViewById<Button>(R.id.two),
            findViewById<Button>(R.id.three),
            findViewById<Button>(R.id.four),
            findViewById<Button>(R.id.five),
            findViewById<Button>(R.id.six),
            findViewById<Button>(R.id.seven),
            findViewById<Button>(R.id.eight),
            findViewById<Button>(R.id.nine)
        )

        val newGame = findViewById<Button>(R.id.newGame)
        humanWins = findViewById<TextView>(R.id.humanWins)
        androidWins = findViewById<TextView>(R.id.androidWins)
        tiesText = findViewById<TextView>(R.id.ties)
        turn = findViewById<TextView>(R.id.turn)

        updateScores()

        newGame.setOnClickListener {
            clearBoard()
        }

        turn.text = "It's your turn"

        buttons.forEachIndexed { index, button ->
            button.text = ""
            button.setOnClickListener {
                if (activeGame && button.text == "") {
                    setMove(human, index)
                    val result = checkForWinner()
                    if (result > 0) {
                        handleGameEnd(result)
                    } else if (activeGame) {
                        current_player = android
                        turn.text = "Android's turn"
                        Handler(Looper.getMainLooper()).postDelayed({
                            getComputerMove()
                            val computerResult = checkForWinner()
                            if (computerResult > 0) {
                                handleGameEnd(computerResult)
                            } else {
                                turn.text = "It's your turn!"
                            }
                        }, 500)
                    }
                }
            }
        }
    }

    fun clearBoard() {
        buttons.forEach { button ->
            button.text = ""
        }
        turn.text = "It's your turn!"
        current_player = human
        activeGame = true
    }

    fun setMove(player: String, location: Int) {
        if (buttons[location].text == "") {
            buttons[location].text = player
            buttons[location].setTextColor(
                when (player) {
                    human -> Color.parseColor("#aabbca")
                    android -> Color.parseColor("#efb136")
                    else -> Color.BLACK
                }
            )
            current_player = if (player == human) android else human
        }
    }

    fun getDifficultyLevel(): DifficultyLevel
    {
        return mDifficultyLevel
    }

    fun setDifficultyLevel(difficultyLevel: DifficultyLevel) {
        mDifficultyLevel = difficultyLevel
    }

    fun getComputerMove() {
        when (mDifficultyLevel) {
            DifficultyLevel.Easy -> easyLevel()
            DifficultyLevel.Harder -> intermediateLevel()
            DifficultyLevel.Expert -> expertLevel()
        }
    }

    fun expertLevel()
    {

    }

    fun easyLevel()
    {
        var move: Int
        do {
            move = Random.nextInt(1, size)
        } while (buttons[move].text == human || buttons[move].text == android)
        setMove(android, move)
    }

    fun intermediateLevel()
    {
        // First, try to win
        for (i in 0..size) {
            if (buttons[i].text == "") {
                setMove(android, i)
                if (checkForWinner() == 3) {
                    return
                }
                buttons[i].text = ""
            }
        }

        // Second, block player's winning move
        for (i in 0..size) {
            if (buttons[i].text == "") {
                buttons[i].text = human
                if (checkForWinner() == 2) {
                    buttons[i].text = ""
                    setMove(android, i)
                    return
                }
                buttons[i].text = ""
            }
        }

        // Try to take center if available
        if (buttons[4].text == "") {
            setMove(android, 4)
            return
        }

        // Take a random available corner
        val corners = listOf(0, 2, 6, 8)
        val availableCorners = corners.filter { buttons[it].text == "" }
        if (availableCorners.isNotEmpty()) {
            val corner = availableCorners.random()
            setMove(android, corner)
            return
        }

        // Take any available space
        val availableSpaces = buttons.indices.filter { buttons[it].text == "" }
        if (availableSpaces.isNotEmpty()) {
            val move = availableSpaces.random()
            setMove(android, move)
        }
    }

    fun checkForWinner(): Int {
        for (i in 0..6 step 3) {
            if (buttons[i].text != "" &&
                buttons[i].text == buttons[i+1].text &&
                buttons[i].text == buttons[i+2].text) {
                return if (buttons[i].text == human) 2 else 3
            }
        }

        for (i in 0..2) {
            if (buttons[i].text != "" &&
                buttons[i].text == buttons[i+3].text &&
                buttons[i].text == buttons[i+6].text) {
                return if (buttons[i].text == human) 2 else 3
            }
        }

        if (buttons[4].text != "" &&
            ((buttons[0].text == buttons[4].text && buttons[4].text == buttons[8].text) ||
                    (buttons[2].text == buttons[4].text && buttons[4].text == buttons[6].text))) {
            return if (buttons[4].text == human) 2 else 3
        }

        return if (buttons.none { it.text == "" }) 1 else 0
    }

    private fun handleGameEnd(result: Int) {
        when (result) {
            1 -> {
                turn.text = "It's a tie!"
                ties++
            }
            2 -> {
                turn.text = "You won!"
                wins++
            }
            3 -> {
                turn.text = "You lost!"
                loses++
            }
        }
        updateScores()
        activeGame = false
    }

    @SuppressLint("SetTextI18n")
    private fun updateScores() {
        humanWins.text = "Human: $wins"
        androidWins.text = "Android: $loses"
        tiesText.text = "Ties: $ties"
    }

}