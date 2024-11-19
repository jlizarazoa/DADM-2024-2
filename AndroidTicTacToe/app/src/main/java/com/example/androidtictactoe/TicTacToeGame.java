package com.example.androidtictactoe;

/* TicTacToeConsole.java
 * By Frank McCown (Harding University)
 *
 * This is a tic-tac-toe game that runs in the console window.  The human
 * is X and the computer is O.
 */

import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class TicTacToeGame {

    private final int BOARD_SIZE = 9;

    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';

    private Random mRand;

    public TicTacToeGame() {

        mRand = new Random();

        char turn = HUMAN_PLAYER;
        int  win = 0;
        while (win == 0)
        {

            if (turn == HUMAN_PLAYER)
            {
                turn = COMPUTER_PLAYER;
            }
            else
            {
                getComputerMove();
                turn = HUMAN_PLAYER;
            }

            win = checkForWinner();
        }

        // Report the winner
        System.out.println();
        if (win == 1)
            System.out.println("It's a tie.");
        else if (win == 2)
            System.out.println(HUMAN_PLAYER + " wins!");
        else if (win == 3)
            System.out.println(COMPUTER_PLAYER + " wins!");
        else
            System.out.println("There is a logic problem!");
    }

    // Check for a winner.  Return
    //  0 if no winner or tie yet
    //  1 if it's a tie
    //  2 if X won
    //  3 if O won
    private int checkForWinner() {
        return 1;
    }

    private void getComputerMove()
    {

    }
}
