package org.pondar.tictactoegridkotlin

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.*
import org.pondar.tictactoegridkotlin.databinding.ActivityMainBinding
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {

    // If true, the game is player versus player, else versus AI
    private var isPVP = true

    // Player turn
    private val PLAYER_1 = 0
    private val PLAYER_2 = 1
    private var turn = PLAYER_1

    // 0 if empty, 1 if X and 2 if O
    private var stateFields = IntArray(9)

    // Field values
    private val EMPTY = 0
    private val X = 1
    private val O = 2

    private var turnCounter = 0
    private var gameOver = false
    private var isDraw = false
    lateinit var binding: ActivityMainBinding

    // Toast Text
    private var gameWonText = ""
    private var gameLostText = ""
    private var gameDrawText = ""

    // Called on activity start.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize toast text
        gameWonText = application.resources.getString(R.string.game_won)
        gameLostText = application.resources.getString(R.string.game_lost)
        gameDrawText = application.resources.getString(R.string.game_draw)

        // Checkbox for activating and disabling AI
        binding.cbxChallengeAi.isChecked = false
        binding.cbxChallengeAi.setOnClickListener {
            isPVP = !isPVP
            startGame(null)
        }

        // Initialise array elements
        startGame(null)

        // Add click listeners to all fields in the grid.
        binding.table.setOnClickListener(this)
        val childCount = binding.table.childCount
        for (i in 0 until childCount) {
            binding.table.getChildAt(i).setOnClickListener(this::onClick)
        }
        binding.startGameBtn.setOnClickListener(this::startGame)
    }

    // OnClick function for the Image Views
    override fun onClick(view: View?) {
        if (!gameOver && view is ImageView) {
            val parentTable = view.parent as GridLayout
            var stateField = 0

            // Find the clicked view.
            for (i in 0 until parentTable.childCount) {
                if (parentTable.getChildAt(i).id == view.id) {
                    stateField = i
                }
            }

            // Change field state.
            if (turn == PLAYER_1) {
                if (stateFields[stateField] == EMPTY) {
                    view.setImageResource(R.drawable.kryds)
                    stateFields[stateField] = X
                    turn = PLAYER_2
                    turnCounter++
                } else {
                    return
                }
            } else if (turn == PLAYER_2 && isPVP) {
                if (stateFields[stateField] == EMPTY) {
                    view.setImageResource(R.drawable.bolle)
                    stateFields[stateField] = O
                    turn = PLAYER_1
                    turnCounter++
                } else {
                    return
                }
            }
            checkGameStatus()

            // Player vs. Computer feature.
            if (!gameOver && !isPVP) {
                executeComputerMove()
                checkGameStatus()
            }
        }
    }

    // Helper function for the program to decide which move to execute.
    private fun executeComputerMove() {

        // Can be used for checking patterns where either the player or the program has 2 fields marked already.
        fun checkWinningOrInterruptingOrFutureFields(fieldValue: Int, checkFutureOpportunityFields: Boolean): Int {
            var fieldValueOrEmpty: Int = fieldValue;
            if (checkFutureOpportunityFields) {
                fieldValueOrEmpty = EMPTY;
            }

            // Check horizontal fields.
            for (i in 0..6 step 3) {
                if (stateFields[i] == EMPTY && stateFields[i + 1] == fieldValueOrEmpty && stateFields[i + 2] == fieldValue) {
                    return i
                } else if (stateFields[i] == fieldValue && stateFields[i + 1] == EMPTY && stateFields[i + 2] == fieldValueOrEmpty) {
                    return i + 1
                } else if (stateFields[i] == fieldValueOrEmpty && stateFields[i + 1] == fieldValue && stateFields[i + 2] == EMPTY) {
                    return i + 2
                }
            }
            // Check vertical fields
            for (i in 0..2) {
                if (stateFields[i] == EMPTY && stateFields[i + 3] == fieldValueOrEmpty && stateFields[i + 6] == fieldValue) {
                    return i
                } else if (stateFields[i] == fieldValue && stateFields[i + 3] == EMPTY && stateFields[i + 6] == fieldValueOrEmpty) {
                    return i + 3
                } else if (stateFields[i] == fieldValueOrEmpty && stateFields[i + 3] == fieldValue && stateFields[i + 6] == EMPTY) {
                    return i + 6
                }
            }
            // Check top left to bot right diagonal fields.
            if (stateFields[0] == EMPTY && stateFields[4] == fieldValueOrEmpty && stateFields[8] == fieldValue) {
                return 0
            } else if (stateFields[0] == fieldValue && stateFields[4] == EMPTY && stateFields[8] == fieldValueOrEmpty) {
                return 4
            } else if (stateFields[0] == fieldValueOrEmpty && stateFields[4] == fieldValue && stateFields[8] == EMPTY) {
                return 8
            }
            // Check bot left to top right diagonal fields
            if (stateFields[2] == EMPTY && stateFields[4] == fieldValueOrEmpty && stateFields[6] == fieldValue) {
                return 2
            } else if (stateFields[2] == fieldValue && stateFields[4] == EMPTY && stateFields[6] == fieldValueOrEmpty) {
                return 4
            } else if (stateFields[2] == fieldValueOrEmpty && stateFields[4] == fieldValue && stateFields[6] == EMPTY) {
                return 6
            }

            // Checks for interruptable L-shaped patterns
            if (fieldValue == X) {
                if (stateFields[0] == fieldValue && stateFields[5] == fieldValue && stateFields[2] == EMPTY) {
                    return 2
                } else if (stateFields[1] == fieldValue && stateFields[8] == fieldValue && stateFields[2] == EMPTY) {
                    return 2
                } else if (stateFields[1] == fieldValue && (stateFields[6] == fieldValue || stateFields[3] == fieldValue) && stateFields[0] == EMPTY) {
                    return 0
                } else if (stateFields[7] == fieldValue && stateFields[2] == fieldValue && stateFields[8] == EMPTY) {
                    return 8
                }
            }
            return -1
        }

        // Checks available fields in the middle, corners and lastly a random int for the rest.
        fun checkStartingFields(): Int {
            if (stateFields[4] == EMPTY) {
                return 4
            } else if (stateFields[8] == EMPTY) {
                return 8
            }
            return -1
        }

        // Retrieves the field index to mark
        var fieldToCheck = checkWinningOrInterruptingOrFutureFields(O, false) // Checks for winnable fields
        if (fieldToCheck == -1) {
            fieldToCheck = checkWinningOrInterruptingOrFutureFields(X, false) // Checks for interruptible fields.
            if (fieldToCheck == -1) {
                fieldToCheck = checkWinningOrInterruptingOrFutureFields(O, true) // Checks for future possibility fields.
                if (fieldToCheck == -1) {
                    fieldToCheck = checkStartingFields()
                }
            }
        }
        // Execute the specific move.
        val image = (binding.table.getChildAt(fieldToCheck) as ImageView)
        image.setImageResource(R.drawable.bolle)
        stateFields[fieldToCheck] = O
        turn = PLAYER_1
        turnCounter++
    }

    // Checks if the game is over and who won.
    private fun checkGameStatus() {
        var toastText = ""
        fun isGameOver(): Boolean {
            // Horizontal check
            for (i in 0..6 step 3) {
                if (stateFields[i] == X && stateFields[i + 1] == X && stateFields[i + 2] == X) {
                    toastText = gameWonText
                    return true
                } else if (stateFields[i] == O && stateFields[i + 1] == O && stateFields[i + 2] == O) {
                    toastText = gameLostText
                    return true
                }
            }
            // Vertical check
            for (i in 0..2) {
                if (stateFields[i] == X && stateFields[i + 3] == X && stateFields[i + 6] == X) {
                    toastText = gameWonText
                    return true
                } else if (stateFields[i] == O && stateFields[i + 3] == O && stateFields[i + 6] == O) {
                    toastText = gameLostText
                    return true
                }
            }
            // Left top to right bot diagonal check - 0,4,8
            if (stateFields[0] == X && stateFields[4] == X && stateFields[8] == X) {
                toastText = gameWonText
                return true
            } else if (stateFields[0] == O && stateFields[4] == O && stateFields[8] == O) {
                toastText = gameLostText
                return true
            }
            // Right top to left bot diagonal check - 2,4,6
            if (stateFields[2] == X && stateFields[4] == X && stateFields[6] == X) {
                toastText = gameWonText
                return true
            } else if (stateFields[2] == O && stateFields[4] == O && stateFields[6] == O) {
                toastText = gameLostText
                return true
            }
            // Checks if game is a draw
            if (!gameOver && turnCounter == 9) {
                isDraw = true
                toastText = gameDrawText
                return true
            }
            return false
        }
        gameOver = isGameOver()

        // Display toast with game status if the game is over
        if (gameOver) {
            if (turn == PLAYER_2 && !isDraw) {
                MediaPlayer.create(this, R.raw.success).start()
            } else {
                MediaPlayer.create(this, R.raw.fail).start()
            }
            Toast.makeText(
                    this,
                    toastText,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Helper method for clearing fields and the grid.
    private fun startGame(view: View?) {
        turn = PLAYER_1
        turnCounter = 0
        gameOver = false
        isDraw = false
        for (i in stateFields.indices) {
            stateFields[i] = EMPTY
            (binding.table.getChildAt(i) as ImageView).setImageResource(R.drawable.blank)
        }
        Toast.makeText(
                this,
                this.resources.getString(R.string.starting_new_game),
                Toast.LENGTH_SHORT
        ).show()
    }
}
