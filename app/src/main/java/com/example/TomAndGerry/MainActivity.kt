package com.example.TomAndGerry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*

class MainActivity : Activity() {

    private lateinit var highestScoreTextView: TextView
    private var highestScore = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isGameEnded = false // Declare boolean flag to track game end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        val resetButton = findViewById<Button>(R.id.reset_highest_score)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = "Highest Score: $highestScore"

        val board = findViewById<RelativeLayout>(R.id.board)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val mainMenu = findViewById<Button>(R.id.MainMenu)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<RelativeLayout>(R.id.board1)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest --- Score: $highestScore"

        val jerry = ImageView(this)
        val tom = ImageView(this)
        val spike = ImageView(this) // Add badman ImageView
        val tomSegments = mutableListOf(tom)
        val handler = Handler()
        var delayMillis = 25L
        var currentDirection = "right"
        var scorex = -1

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.VISIBLE

        resetButton.setOnClickListener {
            // Reset the highest score to 0
            highestScore = 0
            highestScoreTextView.text = " Highest --- Score: $highestScore"
            saveHighestScore()
        }

        newgame.setOnClickListener {
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            endGameButton.visibility = View.VISIBLE

            val tomWidth = 172 // Snake width in pixels
            val tomHeight = 250 // Snake height in pixels
            val jerryWidth = 112 // Meat width in pixels
            val jerryHeight = 294 // Meat height in pixels
            val spikeWidth = 160 // Badman width in pixels
            val spikeHeight = 240 // Badman height in pixels
            spike.scaleX=-1f

            tom.setImageResource(R.drawable.tom)
            tom.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            tom.layoutParams = ViewGroup.LayoutParams(tomWidth, tomHeight)
            board.addView(tom)
            tomSegments.add(tom)

            var tomX = tom.x
            var tomY = tom.y

            jerry.setImageResource(R.drawable.jerry)
            jerry.setPadding(-10, -80, -10, -60) // Add padding to increase touch-sensitive area
            jerry.layoutParams = ViewGroup.LayoutParams(jerryWidth, jerryHeight)
            board.addView(jerry)

            spike.setImageResource(R.drawable.spike) // Assuming "badman" is the name of your vector drawable
            spike.layoutParams = ViewGroup.LayoutParams(spikeWidth, spikeHeight)
            board.addView(spike)

            // Function to generate random coordinates for badman within the board bounds
            fun generateRandomPosition(): Pair<Float, Float> {
                //val randomX = Random().nextInt(500 - badmanWidth)
                //val randomY = Random().nextInt(500 - badmanHeight)
                return Pair(400f, 550f)
            }

            // Position the badman at a random location initially
            var (badmanX, badmanY) = generateRandomPosition()
            spike.x = badmanX
            spike.y = badmanY

            // Add logic to position the badman within the board layout

            fun checkFoodCollision() {
                val boyBounds = Rect()
                jerry.getHitRect(boyBounds)

                for (segment in tomSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(boyBounds, segmentBounds)) {
                        val randomX = Random().nextInt(board.width - 200)
                        val randomY = Random().nextInt(board.height - 200)

                        jerry.x = randomX.toFloat()
                        jerry.y = randomY.toFloat()



                        delayMillis--
                        scorex++
                        score2.text = "score : $scorex"

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = " Highest --- Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            fun checkBadmanCollision() {
                val badmanBounds = Rect()
                spike.getHitRect(badmanBounds)

                for (segment in tomSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(badmanBounds, segmentBounds)) {
                        isGameEnded = true // End the game if badman collision detected
                        playagain.visibility = View.VISIBLE
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.INVISIBLE
                        mainMenu.visibility = View.VISIBLE

                        return // Exit the function once collision is detected
                    }
                }
            }

            // Define a function to move the badman
            fun moveBadman() {
                // Implement your logic to move the badman here
                // For example, you can move it randomly or towards a specific direction
                // Here's a simple example of moving the badman towards the girl's current position
                val dx = tom.x - spike.x
                val dy = tom.y - spike.y

                // Move badman towards the girl's position
                spike.x += dx / 340
                spike.y += dy / 340
            }

            val badmanMovementHandler = Handler()
            val badmanMovementRunnable = object : Runnable {
                override fun run() {
                    moveBadman()
                    checkBadmanCollision()
                    badmanMovementHandler.postDelayed(this, delayMillis)
                }
            }
            // Start moving the badman
            badmanMovementHandler.postDelayed(badmanMovementRunnable, delayMillis)

            val runnable = object : Runnable {
                override fun run() {
                    for (i in tomSegments.size - 1 downTo 1) {
                        tomSegments[i].x = tomSegments[i - 1].x
                        tomSegments[i].y = tomSegments[i - 1].y
                    }

                    when (currentDirection) {
                        "up" -> {
                            tomY -= 3
                            if (tomY < -600) {
                                tomY = 760f
                            }
                            tom.translationY = tomY
                        }
                        "down" -> {
                            tomY += 3
                            if (tomY > 1020 - tom.height) {
                                tomY = -650f
                            }
                            tom.translationY = tomY
                        }
                        "left" -> {
                            tomX -= 3
                            if (tomX < -500) {
                                tomX = 560f
                            }
                            tom.scaleX = -1f // Flip the girl horizontally
                            tom.translationX = tomX
                        }
                        "right" -> {
                            tomX += 3
                            if (tomX > 600 - tom.width) {
                                tomX = -500f
                            }
                            tom.scaleX = 1f
                            tom.translationX = tomX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    checkBadmanCollision() // Check for badman collision
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

            upButton.setOnClickListener {
                currentDirection = "up"
            }
            downButton.setOnClickListener {
                currentDirection = "down"
            }
            leftButton.setOnClickListener {
                currentDirection = "left"
            }
            rightButton.setOnClickListener {
                currentDirection = "right"
            }
            pauseButton.setOnClickListener {
                currentDirection = "pause"
                board.visibility = View.INVISIBLE
                resume.visibility = View.VISIBLE
                endGameButton.visibility = View.VISIBLE
            }
            resume.setOnClickListener {
                currentDirection = "right"
                board.visibility = View.VISIBLE
                resume.visibility = View.INVISIBLE
            }
            endGameButton.setOnClickListener {
                // Create an AlertDialog
                val alertDialogBuilder = AlertDialog.Builder(this)

                // Set the title and message
                alertDialogBuilder.setTitle("Confirm Exit")
                alertDialogBuilder.setMessage("Are you sure you want to exit the game?")

                // Set a positive button and its click listener
                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    // Finish the current activity (exit the game)
                    finishAffinity()
                    System.exit(0)
                }

                // Set a negative button and its click listener
                alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog if "No" is clicked
                    dialog.dismiss()
                }

                // Create and show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        mainMenu.setOnClickListener {
            val Intent = Intent(this, newGame::class.java)
            startActivity(Intent)
        }

        hideSystemUI()
    }

    private fun saveHighestScore() {
        val editor = sharedPreferences.edit()
        editor.putInt("highestScore", highestScore)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
