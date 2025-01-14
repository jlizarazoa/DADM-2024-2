package com.example.androidtictactoe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

private val human = "X"


private const val PREFERENCES_NAME = "TicTacToePrefs"

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var gameId: String
    private lateinit var playerType: String
    private lateinit var firestore: FirebaseFirestore
    private var game: Game? = null
    private lateinit var buttons: Array<Button>
    private lateinit var turn: TextView
    private lateinit var humanWins: TextView
    private lateinit var androidWins: TextView
    private lateinit var ties: TextView
    private var activeGame = true
    private var waitingDialog: Dialog? = null

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

        gameId = intent.getStringExtra("gameId") ?: return finish()
        playerType = intent.getStringExtra("playerType") ?: return finish()

        firestore = FirebaseFirestore.getInstance()

        firestore.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    game = document.toObject(Game::class.java)
                    game?.let {
                        updateGameUI(it)
                        if (it.status == "waiting") {
                            showWaitingDialog()
                        }
                    }
                } else {
                    finish()
                }
            }

        initializeUI()
        checkGameExists()
        listenToGameChanges()
    }

    private fun showWaitingDialog() {
        if (waitingDialog == null) {
            waitingDialog = Dialog(this, R.style.FullScreenDialog).apply {
                setContentView(R.layout.dialog_waiting)
                setCancelable(false)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
        }

        if (!waitingDialog!!.isShowing) {
            waitingDialog?.show()
        }
    }

    private fun hideWaitingDialog() {
        Log.d("DialogDebug", "Attempting to dismiss dialog: ${waitingDialog?.isShowing}")
        runOnUiThread {
            waitingDialog?.dismiss()
            waitingDialog = null
            Log.d("DialogDebug", "Dialog dismissed")
        }
    }


    private fun checkGameExists() {
        firestore.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    turn.text = "Game no longer exists"
                    activeGame = false
                }
            }
            .addOnFailureListener {
                turn.text = "Error connecting to game"
                activeGame = false
            }
    }

    private fun listenToGameChanges() {
        firestore.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    turn.text = "Connection error"
                    return@addSnapshotListener
                }

                game = snapshot.toObject(Game::class.java)
                game?.let {
                    updateGameUI(it)

                    when (it.status) {
                        "playing" -> {
                            if (it.player2Name.isNotEmpty()) {
                                if (waitingDialog != null) {
                                    hideWaitingDialog()
                                }
                                buttons.forEach { button -> button.isEnabled = true }
                            }
                        }
                        "waiting" -> {
                            if (playerType == "X" && waitingDialog == null) {
                                showWaitingDialog()
                            }
                        }
                        "finished" -> {
                            if (it.winner.isNotEmpty()) {
                                showRestartDialog()
                            }
                        }
                        "ended" -> {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Game finished")
                                .setMessage("You or your opponent have decided not to play")
                                .setPositiveButton("OK") { _, _ -> finish() }
                                .setCancelable(false)
                                .show()
                        }
                    }
                }
            }
    }

    private fun updateGameUI(game: Game) {
        val boardEnabled = game.player2Name.isNotEmpty() &&
                game.currentPlayer == playerType &&
                game.winner.isEmpty() && game.status == "playing"

        buttons.forEach { it.isEnabled = boardEnabled }

        game.board.forEachIndexed { index, value ->
            buttons[index].setTextColor(Color.TRANSPARENT)
            buttons[index].text = value
            buttons[index].background = when (value) {
                "X" -> ContextCompat.getDrawable(this, R.drawable.xbutton)
                "O" -> ContextCompat.getDrawable(this, R.drawable.obutton)
                else -> ContextCompat.getDrawable(this, R.drawable.button)
            }
        }

        turn.text = when {
            game.winner.isNotEmpty() -> {
                activeGame = false
                when (game.winner) {
                    playerType -> "You won this round!"
                    "tie" -> "It's a tie!"
                    else -> "You lost this round!"
                }
            }
            else -> if (game.currentPlayer == playerType) "Your turn!" else "Opponent's turn"
        }


        updateGameStatsUI(game)
    }


    @SuppressLint("SetTextI18n")
    private fun updateGameStatsUI(game: Game) {
        humanWins.text = "${game.player1Name}: ${game.player1Wins}"
        androidWins.text = "${game.player2Name}: ${game.player2Wins}"
        ties.text = "Ties: ${game.ties}"
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

        val quit = findViewById<Button>(R.id.quit)

        humanWins = findViewById<TextView>(R.id.humanWins)
        androidWins = findViewById<TextView>(R.id.androidWins)
        ties = findViewById<TextView>(R.id.ties)
        turn = findViewById<TextView>(R.id.turn)

        val mediaPlayer = MediaPlayer.create(this, R.raw.tapsound)

        quit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Quit Game")
            builder.setMessage("Are you sure you want to quit?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteGame()
            }
            builder.setNegativeButton("No", null)
            builder.show()
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (activeGame && game?.currentPlayer == playerType && button.text == "") {
                    mediaPlayer.start()
                    button.setTextColor(Color.TRANSPARENT)
                    makeMove(index)
                }
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape)
        } else {
            setContentView(R.layout.activity_main)
        }

        initializeUI()
        game?.let { updateGameUI(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("board", ArrayList(game?.board))
        outState.putString("currentPlayer", game?.currentPlayer)
        outState.putString("winner", game?.winner)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val board = savedInstanceState.getStringArrayList("board") ?: return
        val currentPlayer = savedInstanceState.getString("currentPlayer")
        val winner = savedInstanceState.getString("winner")

        game?.board = board
        game?.currentPlayer = currentPlayer ?: "X"
        game?.winner = winner ?: ""
        updateGameUI(game!!)
    }

    private fun makeMove(location: Int) {
        val newBoard = game?.board?.toMutableList() ?: return
        newBoard[location] = playerType

        firestore.collection("games").document(gameId)
            .update(mapOf(
                "board" to newBoard,
                "currentPlayer" to if (playerType == "X") "O" else "X"
            ))
            .addOnSuccessListener {
                logBoardState()
                checkForWinnerAndUpdate()
            }
            .addOnFailureListener {
                turn.text = "Failed to make a move"
            }
    }

    private fun checkForWinnerAndUpdate() {
        val winner = checkForWinner()
        val winnerValue = when (winner) {
            1 -> "tie"
            2 -> "X"
            3 -> "O"
            else -> ""
        }

        if (winnerValue.isNotEmpty()) {
            firestore.collection("games").document(gameId)
                .update(
                    mapOf(
                        "winner" to winnerValue,
                        "status" to "finished"
                    )
                )
                .addOnSuccessListener {
                    updateGameStats(winnerValue)
                }
        }
    }

    private fun updateGameStats(winner: String) {
        val updates = when (winner) {
            "X" -> mapOf("player1Wins" to (game?.player1Wins ?: 0) + 1)
            "O" -> mapOf("player2Wins" to (game?.player2Wins ?: 0) + 1)
            "tie" -> mapOf("ties" to (game?.ties ?: 0) + 1)
            else -> return
        }

        firestore.collection("games").document(gameId)
            .update(updates)
            .addOnSuccessListener { resetBoard() }
            .addOnFailureListener {
                turn.text = "Error updating game stats"
            }
    }

    private fun resetBoard() {
        val newBoard = MutableList(9) { "" }
        firestore.collection("games").document(gameId)
            .update(
                mapOf(
                    "board" to newBoard,
                    "currentPlayer" to "X",
                    "winner" to "",
                    "status" to "playing",
                    "restartConfirmation" to mapOf("X" to false, "O" to false)
                )
            )
            .addOnSuccessListener {
                activeGame = true
                buttons.forEach { button ->
                    button.text = ""
                    button.isEnabled = game?.player2Name?.isNotEmpty() == true
                    button.background = ContextCompat.getDrawable(this, R.drawable.button)
                }
                turn.text = if (playerType == "X") "New round! Your turn!" else "New round! Opponent's turn!"
            }
            .addOnFailureListener {
                turn.text = "Error resetting the board"
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideWaitingDialog()
        restartDialog?.dismiss()
        restartDialog = null
    }

    private var restartDialog: AlertDialog? = null

    private fun showRestartDialog() {
        if (isFinishing || restartDialog?.isShowing == true) {
            return
        }

        val builder = AlertDialog.Builder(this)

        builder.setMessage("Play Again?")

        val message = when (game?.winner) {
            playerType -> "You won this round!"
            "tie" -> "It's a tie!"
            else -> "You lost this round!"
        }

        builder.setTitle(message)

        builder.setPositiveButton("Yes") { _, _ ->
            firestore.collection("games").document(gameId)
                .update("restartConfirmation.${playerType}", true)
                .addOnSuccessListener {
                    turn.text = "Waiting for oponent's confirmation..."
                    listenForRestart()
                }
        }

        builder.setNegativeButton("No") { _, _ ->
            firestore.collection("games").document(gameId)
                .update(
                    mapOf(
                        "status" to "ended",
                        "restartConfirmation" to mapOf("X" to false, "O" to false)
                    )
                )
                .addOnSuccessListener {

                }
        }

        builder.setCancelable(false)
        restartDialog = builder.create()
        restartDialog?.show()
    }

    private fun listenForRestart() {
        firestore.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val restartConfirmation = snapshot.get("restartConfirmation") as? Map<String, Boolean>
                val status = snapshot.getString("status")

                when {
                    status == "ended" -> {
                        restartDialog?.dismiss()
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Game finished")
                            .setMessage("The other player has decided not to play")
                            .setPositiveButton("OK") { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                    }
                    restartConfirmation?.get("X") == true && restartConfirmation.get("O") == true -> {
                        resetBoard()
                    }
                }
            }
    }

    private fun checkForWinner(): Int {
        logBoardState()
        for (i in 0..6 step 3) {
            if (buttons[i].text.toString() != "" &&
                buttons[i].text.toString() == buttons[i+1].text.toString() &&
                buttons[i].text.toString() == buttons[i+2].text.toString()) {
                return if (buttons[i].text.toString() == human) 2 else 3
            }
        }

        for (i in 0..2) {
            if (buttons[i].text.toString() != "" &&
                buttons[i].text.toString() == buttons[i+3].text.toString() &&
                buttons[i].text.toString() == buttons[i+6].text.toString()) {
                return if (buttons[i].text.toString() == human) 2 else 3
            }
        }

        if (buttons[4].text.toString() != "" &&
            ((buttons[0].text.toString() == buttons[4].text.toString() &&
                    buttons[4].text.toString() == buttons[8].text.toString()) ||
                    (buttons[2].text.toString() == buttons[4].text.toString() &&
                            buttons[4].text.toString() == buttons[6].text.toString()))) {
            return if (buttons[4].text.toString() == human) 2 else 3
        }

        return if (buttons.none { it.text.toString() == "" }) 1 else 0
    }

    private fun logBoardState() {
        val boardState = StringBuilder("\nCurrent Board State:\n")
        for (i in 0..8 step 3) {
            boardState.append("| ${buttons[i].text} | ${buttons[i+1].text} | ${buttons[i+2].text} |\n")
        }
        Log.d("BoardState", boardState.toString())
    }


    private fun deleteGame() {
        firestore.collection("games").document(gameId)
            .delete()
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                finish()
            }
    }
}