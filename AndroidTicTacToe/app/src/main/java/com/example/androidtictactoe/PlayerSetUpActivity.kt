package com.example.androidtictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import android.widget.Toast

class PlayerSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_setup)

        val playerNameInput = findViewById<EditText>(R.id.playerNameInput)
        val continueButton = findViewById<Button>(R.id.continueButton)

        continueButton.setOnClickListener {
            val playerName = playerNameInput.text.toString().trim()
            if (playerName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getSharedPreferences("GamePrefs", MODE_PRIVATE)
                .edit()
                .putString("playerName", playerName)
                .apply()

            val intent = Intent(this, GameListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(intent)

            finish()
        }
    }
}