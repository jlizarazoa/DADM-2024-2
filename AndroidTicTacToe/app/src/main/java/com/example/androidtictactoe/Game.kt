package com.example.androidtictactoe

data class Game(
    var id: String = "",
    val player1Name: String = "",
    var player2Name: String = "",
    var status: String = "waiting",
    var currentPlayer: String = "X",
    var board: MutableList<String> = MutableList(9) { "" },
    var winner: String = "",
    var player1Wins: Int = 0,
    var player2Wins: Int = 0,
    var ties: Int = 0,
    var requestedBy: String = ""
) {
    override fun toString(): String {
        return if (status == "waiting") {
            "Game created by: $player1Name (Waiting for opponent)"
        } else {
            "$player1Name vs $player2Name"
        }
    }
}
