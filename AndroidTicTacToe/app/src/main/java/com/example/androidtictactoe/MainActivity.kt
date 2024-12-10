package com.example.androidtictactoe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
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
private const val PREFERENCES_NAME = "TicTacToePrefs"
private const val KEY_WINS = "wins"
private const val KEY_LOSES = "loses"
private const val KEY_TIES = "ties"
private const val KEY_DIFFICULTY = "difficulty"
private const val KEY_BOARD_STATE = "board_state"

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
@SuppressLint("StaticFieldLeak")
private lateinit var difficultyText: TextView

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main)
        }

        initializeUI()
        loadGameData()

    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun initializeUI() {
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

        setDifficultyLevel(DifficultyLevel.Easy)

        val difficulty = findViewById<Button>(R.id.difficulty)
        val quit = findViewById<Button>(R.id.quit)
        val newGame = findViewById<Button>(R.id.newGame)

        difficultyText = findViewById<TextView>(R.id.difficultyText)
        humanWins = findViewById<TextView>(R.id.humanWins)
        androidWins = findViewById<TextView>(R.id.androidWins)
        tiesText = findViewById<TextView>(R.id.ties)
        turn = findViewById<TextView>(R.id.turn)

        val mediaPlayer = MediaPlayer.create(this, R.raw.tapsound)

        updateScores()

        newGame.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.clear_popup_component, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.clearRadioGroup)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)
            builder.setPositiveButton("Select") { _, _ ->
                when (radioGroup.checkedRadioButtonId) {
                    R.id.clearRadio -> clearHistory()
                    R.id.restartRadio -> clearBoard()
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        quit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Quit Game")
            builder.setMessage("Are you sure you want to quit?")
            builder.setPositiveButton("Yes") { _, _ ->
                finish()
            }
            builder.setNegativeButton("No", null)
            builder.show()
        }

        difficulty.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.popup_component, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficultyRadioGroup)

            // Marcar el nivel actual
            when (mDifficultyLevel) {
                DifficultyLevel.Easy -> radioGroup.check(R.id.easyRadio)
                DifficultyLevel.Harder -> radioGroup.check(R.id.harderRadio)
                DifficultyLevel.Expert -> radioGroup.check(R.id.expertRadio)
            }

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)
            builder.setPositiveButton("Select") { _, _ ->
                mDifficultyLevel = when (radioGroup.checkedRadioButtonId) {
                    R.id.easyRadio -> DifficultyLevel.Easy
                    R.id.harderRadio -> DifficultyLevel.Harder
                    R.id.expertRadio -> DifficultyLevel.Expert
                    else -> DifficultyLevel.Easy
                }
                difficultyText.text = "Difficulty: ${
                    when (mDifficultyLevel) {
                        DifficultyLevel.Easy -> "Easy"
                        DifficultyLevel.Harder -> "Harder"
                        DifficultyLevel.Expert -> "Expert"
                    }
                }"
                clearBoard()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }


        turn.text = "It's your turn!"

        buttons.forEachIndexed { index, button ->
            button.text = ""
            button.setOnClickListener {
                mediaPlayer.start()
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

    private fun clearHistory() {
        wins = 0
        loses = 0
        ties = 0
        mDifficultyLevel = DifficultyLevel.Easy
        buttons.forEachIndexed { _, button ->
            button.text = ""
            button.background = ContextCompat.getDrawable(this, R.drawable.button)
        }
        saveGameData()
        loadGameData()
    }

    @SuppressLint("SetTextI18n")
    private fun loadGameData() {
        wins = sharedPreferences.getInt(KEY_WINS, 0)
        loses = sharedPreferences.getInt(KEY_LOSES, 0)
        ties = sharedPreferences.getInt(KEY_TIES, 0)
        mDifficultyLevel = DifficultyLevel.valueOf(sharedPreferences.getString(KEY_DIFFICULTY, DifficultyLevel.Easy.name)!!)

        updateScores()
        setDifficultyLevel(mDifficultyLevel)
        difficultyText.text = "Difficulty: ${
            when (mDifficultyLevel) {
                DifficultyLevel.Easy -> "Easy"
                DifficultyLevel.Harder -> "Harder"
                DifficultyLevel.Expert -> "Expert"
            }
        }"
        restoreBoardState()
    }

    private fun saveGameData() {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_WINS, wins)
        editor.putInt(KEY_LOSES, loses)
        editor.putInt(KEY_TIES, ties)
        editor.putString(KEY_DIFFICULTY, mDifficultyLevel.name)
        val boardState = buttons.joinToString(",") { it.text.toString() }
        editor.putString(KEY_BOARD_STATE, boardState)
        editor.apply()
        saveBoardState()
    }

    private fun saveBoardState() {
        val boardStateBuilder = StringBuilder()
        buttons.forEachIndexed { index, button ->
            val buttonText = button.text.toString()
            val backgroundResourceName = when (buttonText) {
                human -> "xbutton"
                android -> "obutton"
                else -> "button"
            }

            boardStateBuilder.append("$buttonText,$backgroundResourceName")

            if (index < buttons.size - 1) {
                boardStateBuilder.append("|")
            }
        }
        sharedPreferences.edit().putString(KEY_BOARD_STATE, boardStateBuilder.toString()).apply()
    }


    private fun restoreBoardState() {
        val boardStateString = sharedPreferences.getString(KEY_BOARD_STATE, null)
        boardStateString?.let { stateStr ->
            val buttonStates = stateStr.split("|")
            if (buttonStates.size == buttons.size) {
                buttons.forEachIndexed { index, button ->
                    val (buttonText, backgroundResourceName) = buttonStates[index].split(",")

                    button.text = buttonText
                    button.setTextColor(Color.TRANSPARENT)

                    button.background = when (backgroundResourceName) {
                        "xbutton" -> ContextCompat.getDrawable(this, R.drawable.xbutton)
                        "obutton" -> ContextCompat.getDrawable(this, R.drawable.obutton)
                        else -> ContextCompat.getDrawable(this, R.drawable.button)
                    }
                }
            }
        }
    }

    private fun clearBoard() {
        buttons.forEach { button ->
            button.text = ""
            button.setTextColor(Color.BLACK)
            button.background = ContextCompat.getDrawable(this, R.drawable.button)
        }
        turn.text = "It's your turn!"
        current_player = human
        activeGame = true
        saveGameData()
    }

    override fun onPause() {
        super.onPause()
        saveGameData()
    }

    override fun onStop() {
        super.onStop()
        saveGameData()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        saveGameData()

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape) // Layout para horizontal
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main) // Layout para vertical
        }

        initializeUI()
        loadGameData()
    }

    fun setMove(player: String, location: Int) {
        if (buttons[location].text == "") {
            buttons[location].text = player

            buttons[location].setTextColor(Color.TRANSPARENT)

            buttons[location].background = when (player) {
                human -> ContextCompat.getDrawable(this, R.drawable.xbutton)
                android -> ContextCompat.getDrawable(this, R.drawable.obutton)
                else -> ContextCompat.getDrawable(this, R.drawable.button)
            }

            current_player = if (player == human) android else human
        }
    }

    fun getDifficultyLevel(): DifficultyLevel
    {
        return mDifficultyLevel
    }

    @SuppressLint("SetTextI18n")
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

    private fun expertLevel() {
        val (_, move) = minimax(buttons, 0, true)
        if (move != null) {
            setMove(android, move)
        }
    }

    private fun easyLevel()
    {
        var move: Int
        do {
            move = Random.nextInt(1, size)
        } while (buttons[move].text == human || buttons[move].text == android)
        setMove(android, move)
    }

    private fun intermediateLevel()
    {
        for (i in 0..size) {
            if (buttons[i].text == "") {
                setMove(android, i)
                if (checkForWinner() == 3) {
                    return
                }
                buttons[i].text = ""
                buttons[i].background = ContextCompat.getDrawable(this, R.drawable.button)
            }
        }

        for (i in 0..size) {
            if (buttons[i].text == "") {
                buttons[i].text = human
                if (checkForWinner() == 2) {
                    buttons[i].text = ""
                    setMove(android, i)
                    return
                }
                buttons[i].text = ""
                buttons[i].background = ContextCompat.getDrawable(this, R.drawable.button)
            }
        }

        if (buttons[4].text == "") {
            setMove(android, 4)
            return
        }

        val corners = listOf(0, 2, 6, 8)
        val availableCorners = corners.filter { buttons[it].text == "" }
        if (availableCorners.isNotEmpty()) {
            val corner = availableCorners.random()
            setMove(android, corner)
            return
        }

        val availableSpaces = buttons.indices.filter { buttons[it].text == "" }
        if (availableSpaces.isNotEmpty()) {
            val move = availableSpaces.random()
            setMove(android, move)
        }
    }

    private fun checkForWinner(): Int {
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
        val winPlayer = MediaPlayer.create(this, R.raw.winsound)
        val losePlayer = MediaPlayer.create(this, R.raw.losesound)
        when (result) {
            1 -> {
                turn.text = "It's a tie!"
                ties++
            }
            2 -> {
                turn.text = "You won!"
                winPlayer.start()
                wins++
            }
            3 -> {
                turn.text = "You lost!"
                losePlayer.start()
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

    private fun minimax(board: Array<Button>, depth: Int, isMaximizing: Boolean): Pair<Int, Int?> {
        val humanScore = -1
        val androidScore = 1
        val tieScore = 0

        val winner = checkForWinner()
        if (winner == 2) return Pair(humanScore, null)  // Humano gana
        if (winner == 3) return Pair(androidScore, null)  // Android gana
        if (board.all { it.text != "" }) return Pair(tieScore, null)  // Empate

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            var bestMove: Int? = null

            for (i in board.indices) {
                if (board[i].text == "") {
                    board[i].text = android
                    val (score, _) = minimax(board, depth + 1, false)
                    board[i].text = ""
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = i
                    }
                }
            }
            return Pair(bestScore, bestMove)
        } else {
            var bestScore = Int.MAX_VALUE
            var bestMove: Int? = null

            for (i in board.indices) {
                if (board[i].text == "") {
                    board[i].text = human
                    val (score, _) = minimax(board, depth + 1, true)
                    board[i].text = ""
                    if (score < bestScore) {
                        bestScore = score
                        bestMove = i
                    }
                }
            }
            return Pair(bestScore, bestMove)
        }
    }

}