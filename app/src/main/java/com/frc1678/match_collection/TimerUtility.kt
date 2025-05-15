// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.graphics.Color
import android.os.CountDownTimer
import android.widget.Button
import android.widget.LinearLayout
import com.frc1678.match_collection.objective.CollectionObjectiveActivity
import kotlinx.android.synthetic.main.collection_objective_activity.tb_incap
import kotlin.math.roundToInt

// Class for all timer functions.
class TimerUtility {
    // Create a thread for the match timer to run asynchronously while the app runs other tasks.
    class MatchTimerThread : Thread() {
        var time = 0f

        // Return stage to be displayed on timer.
        private fun stage(time: Int): Constants.Stage {
            return if (time >= 138) {
                Constants.Stage.AUTO
            } else {
                Constants.Stage.TELEOP
            }
        }

        // Begins CountDownTimer when timer button is clicked.
        private fun run(
            context: CollectionActivity,
            btn_timer: Button,
            btn_proceed: Button,
            layout: LinearLayout
        ) {
            isMatchTimeEnded = false
            // Create a CountDownTimer that will count down in by seconds starting from 150 seconds.
            matchTimer = object : CountDownTimer(153000, 1000) {
                // Executes tasks every second.
                override fun onTick(millisUntilFinished: Long) {
                    time = millisUntilFinished / 1000f

                    // Display stage and time on timer button.
                    if (context is CollectionObjectiveActivity) {
                        btn_timer.text = context.getString(
                            R.string.tv_time_display,
                            stage(time.roundToInt()),
                            time.roundToInt().toString()
                        )
                    } else {
                        btn_timer.text = time.roundToInt().toString()
                    }
                    // Convert time to a three-digit string to be recorded in timeline.
                    matchTime = (time - 1).toInt().toString().padStart(3, '0')

                    if (context is CollectionObjectiveActivity) {
                        if (!isTeleopActivated and (time.roundToInt() <= 138)) {
                            layout.setBackgroundColor(Color.RED)
                        } else {
                            layout.setBackgroundColor(Color.WHITE)
                        }
                    }
                }

                // Display 0 and change button states when countdown finishes.
                override fun onFinish() {
                    if (context is CollectionObjectiveActivity) {
                        btn_timer.text = context.getString(
                            R.string.tv_time_display,
                            stage(time.roundToInt()),
                            context.getString(R.string.final_time)
                        )
                    } else {
                        btn_timer.text = "0"
                    }
                    btn_timer.isEnabled = false
                    isMatchTimeEnded = true
                    if (context is CollectionObjectiveActivity) {
                        context.endAction()
                        context.enableButtons()
                        context.tb_incap.isEnabled = false
                    }
                }
            }.start()
        }

        // Initialize timer, called in CollectionObjectiveActivity.kt.
        fun initTimer(
            context: CollectionActivity,
            btn_timer: Button,
            btn_proceed: Button,
            layout: LinearLayout
        ) {
            run(
                context = context, btn_timer = btn_timer, btn_proceed = btn_proceed, layout = layout
            )
        }
    }
}

