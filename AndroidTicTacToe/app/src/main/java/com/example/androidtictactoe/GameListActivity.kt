package com.example.androidtictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Console

class GameListActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var gamesListView: ListView
    private lateinit var createGameButton: Button
    private var games = mutableListOf<Game>()
    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        playerName = getSharedPreferences("GamePrefs", MODE_PRIVATE)
            .getString("playerName", "") ?: ""
        if (playerName.isEmpty()) {
            startActivity(Intent(this, PlayerSetupActivity::class.java))
            finish()
            return
        }

        firestore = FirebaseFirestore.getInstance()
        gamesListView = findViewById(R.id.gamesListView)
        createGameButton = findViewById(R.id.createGameButton)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, games)
        gamesListView.adapter = adapter

        firestore.collection("games")
            .whereEqualTo("status", "waiting")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading games", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                games.clear()
                snapshot?.documents?.forEach { doc ->
                    val game = doc.toObject(Game::class.java)
                    game?.let {
                        it.id = doc.id
                        if (it.player1Name != playerName) {
                            games.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

        gamesListView.setOnItemClickListener { _, _, position, _ ->
            val game = games[position]
            joinGame(game.id)
        }

        createGameButton.setOnClickListener {
            createNewGame()
        }
    }

    private fun createNewGame() {
        val game = Game(
            player1Name = playerName,
            status = "waiting"
        )

        firestore.collection("games")
            .add(game)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("gameId", documentReference.id)
                intent.putExtra("playerType", "X")
                intent.putExtra("playerName", playerName)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error creating game", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinGame(gameId: String) {
        firestore.collection("games").document(gameId)
            .update(mapOf(
                "player2Name" to playerName,
                "status" to "playing"
            ))
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("gameId", gameId)
                intent.putExtra("playerType", "O")
                intent.putExtra("playerName", playerName)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error joining game", Toast.LENGTH_SHORT).show()
            }
    }
}