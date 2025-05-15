// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection.objective

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.frc1678.match_collection.CollectionActivity
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Companion.previousScreen
import com.frc1678.match_collection.MatchInformationEditActivity
import com.frc1678.match_collection.PreloadedFragment
import com.frc1678.match_collection.R
import com.frc1678.match_collection.TimerUtility
import com.frc1678.match_collection.UndoRedoFragment
import com.frc1678.match_collection.actionMap
import com.frc1678.match_collection.actionToPiece
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.assignMode
import com.frc1678.match_collection.autoIntakeList
import com.frc1678.match_collection.autoScoreReefMap
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.checkEnabled
import com.frc1678.match_collection.isMatchTimeEnded
import com.frc1678.match_collection.isTeleopActivated
import com.frc1678.match_collection.matchTime
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.redoReefLocation
import com.frc1678.match_collection.redoReefSuccess
import com.frc1678.match_collection.resetCollectionReferences
import com.frc1678.match_collection.robotInvTimeline
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.specialActions
import com.frc1678.match_collection.teamNumber
import com.frc1678.match_collection.timeline
import com.frc1678.match_collection.undoReefLevel
import com.frc1678.match_collection.undoReefLocation
import com.frc1678.match_collection.updateActionCount
import kotlinx.android.synthetic.main.collection_objective_activity.btn_action_failed
import kotlinx.android.synthetic.main.collection_objective_activity.btn_action_toggle
import kotlinx.android.synthetic.main.collection_objective_activity.btn_proceed_edit
import kotlinx.android.synthetic.main.collection_objective_activity.btn_timer
import kotlinx.android.synthetic.main.collection_objective_activity.objective_match_collection_layout
import kotlinx.android.synthetic.main.collection_objective_activity.tb_incap
import kotlinx.android.synthetic.main.collection_objective_activity.tv_team_number

/**
 * Activity for Objective Match Collection to scout the objective gameplay of a single team in a
 * match.
 */
class CollectionObjectiveActivity : CollectionActivity() {

    /**
     * Determines which scoring/intake screen should be displayed based on the current mode
     * (Auto, Teleop, or Endgame)
     */
    var fragmentScreen = Constants.Stage.AUTO
        set(value) {
            field = value
            /* Set the current fragment to scoring or intake depending on the new value,
            if teleop is activated set intakePanel. If auto, set intakeAutoPanel */
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_frame,
                when (value) {
                    Constants.Stage.AUTO -> autoPanel
                    Constants.Stage.TELEOP -> teleopPanel
                    else -> endgamePanel
                }
            ).commit()
            enableButtons()
        }

    /**
     * Determines if the change preload fragment or undo/redo fragment should be displayed
     */
    var topFragment = true
        set(value) {
            field = value
            /* Set the current fragment to scoring or intake depending on the new value,
            if teleop is activated set intakePanel. If auto, set intakeAutoPanel */
            supportFragmentManager.beginTransaction().replace(
                R.id.undo_redo_btn_frame,
                when (value) {
                    true -> preloadedFragment
                    else -> undoRedoFragment
                }
            ).commit()
        }

    /**
     * The fragment with the map and buttons for teleop.
     */
    private val teleopPanel = TeleopFragment()

    /**
     * The fragment with the endgame buttons.
     */
    private val endgamePanel = EndgameFragment()

    /**
     * The fragment with the map and buttons for auto
     */
    private val autoPanel = AutoFragment()

    /**
     * The fragment with the button for changing the preload
     */
    private val preloadedFragment = PreloadedFragment()

    /**
     * The fragment with the undo and redo buttons
     */
    private val undoRedoFragment = UndoRedoFragment()

    /**
     * True if the match timer is running or the timer has ended.
     */
    var isTimerRunning by mutableStateOf(false)

    /**
     * Whether the robot is currently incap.
     */
    var isIncap by mutableStateOf(false)

    var removedTimelineActions = mutableListOf<Map<String, String>>()

    var failing by mutableStateOf(false)

    /**
     * Set timer to start match when timer is started or reset.
     * Resets all actions and resets the timeline
     */
    private fun timerReset() {
        resetCollectionReferences()
        isTeleopActivated = false
        isTimerRunning = false
        matchTimer?.cancel()
        matchTimer = null
        timeline.clear()
        isIncap = false
        removedTimelineActions.clear()
        btn_timer.text = getString(R.string.btn_timer_start)
        isMatchTimeEnded = false
        fragmentScreen = Constants.Stage.AUTO
        scoring = false
        failing = false
    }

    /**
     * Add performed action to timeline, including action type and time of action.
     */
    private fun timelineAdd(matchTime: String, actionType: Constants.ActionType) {
        timeline.add(mapOf("match_time" to matchTime, "action_type" to "$actionType"))
        processInventoryAction(actionType.toString())
        removedTimelineActions.clear()
        enableButtons()
    }

    /**
     * Process the action into the robot inventory :)
     */
    private fun processInventoryAction(actionType: String) {
        val change = actionToPiece[actionType]
        if (change == "") {
            return
        }
        val oldInv =
            if (robotInvTimeline.size == 0) {
                emptyArray<Char>()
            } else {
                robotInvTimeline.get(robotInvTimeline.size - 1)
            }
        var newInv: Array<Char> = emptyArray()
        // the change will always be in the format of '{+ or -}{C, A, or U}'
        // +{piece} means it intakes a  piece, -{piece} means it scores or drops a piece
        if ((change?.get(0) ?: ' ') == '-') {
            // lose (score or drop) a piece
            val piece = change?.get(1) ?: 'C'
            if (oldInv.size == 2) {
                if (piece == 'C') {
                    newInv = arrayOf('A')
                } else {
                    newInv = arrayOf('C')
                }
            }
        }
        if ((change?.get(0) ?: ' ') == '+') {
            // intake a piece
            val piece = change?.get(1) ?: 'U'
            newInv = oldInv + arrayOf(piece)
            if (newInv.size == 2) {
                newInv = arrayOf('C', 'A')
            }
        }
        // add the resulting inventory to the timeline
        robotInvTimeline.add(newInv)
    }

    /**
     * Always run this when undoing an action
     */
    private fun undoInventoryAction(actionType: String) {
        if (actionToPiece.get(actionType) == "") {
            return
        }
        robotInvTimeline.removeAt(robotInvTimeline.size - 1)
    }

    /**
     * If stage and time contradict when action is recorded, add action to timeline with time value
     * dictated by stage.
     */
    fun timelineAddWithStage(action_type: Constants.ActionType) = when {
        !isTeleopActivated and (matchTime.toInt() < getString(R.string.final_auto_time).toInt()) -> {
            timelineAdd(
                matchTime = getString(R.string.final_auto_time),
                actionType = action_type
            )
        }

        isTeleopActivated and (matchTime.toInt() > getString(R.string.initial_teleop_time).toInt()) -> {
            timelineAdd(
                matchTime = getString(R.string.initial_teleop_time),
                actionType = action_type
            )
        }

        else -> timelineAdd(matchTime = matchTime, actionType = action_type)
    }

    /**
     * Remove previously inputted action from timeline.
     */
    fun timelineRemove() {
        //Decrement action values displayed on action counters.
        if (timeline.isNotEmpty()) {
            when (timeline.last()["action_type"].toString()) {
                /* Removes auto intake and scoring screen in timeline for specific
                game piece when undo button is used */

                Constants.ActionType.FAIL.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Fail", -1)
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_L1.toString() -> {
                    undoReefLevel = 0
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_REEF_L2.toString() -> {
                    undoReefLevel = 1
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_REEF_L3.toString() -> {
                    undoReefLevel = 2
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_REEF_L4.toString() -> {
                    undoReefLevel = 3
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_REEF_F1.toString() -> {
                    undoReefLocation = 0
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_F2.toString() -> {
                    undoReefLocation = 1
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_F3.toString() -> {
                    undoReefLocation = 2
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_F4.toString() -> {
                    undoReefLocation = 3
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_F5.toString() -> {
                    undoReefLocation = 4
                    failing = false
                }

                Constants.ActionType.AUTO_REEF_F6.toString() -> {
                    undoReefLocation = 5
                    failing = false
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F1.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    6,
                                    autoIntakeList[6] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F2.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    7,
                                    autoIntakeList[7] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F3.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    8,
                                    autoIntakeList[8] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F4.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    9,
                                    autoIntakeList[9] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F5.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    10,
                                    autoIntakeList[10] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F6.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            // Adds 1 to the corresponding reef button's index in autoIntakeList
                            .apply {
                                set(
                                    11,
                                    autoIntakeList[11] - 1
                                )
                            }
                    scoring = false
                    updateActionCount("Intake Reef", -1)
                }

                Constants.ActionType.AUTO_REEF_SUCCESS.toString() -> {
                    autoScoreReefMap = autoScoreReefMap
                        .toMutableMap()
                        .apply {
                            set(
                                undoReefLocation,
                                mutableStateOf(
                                    autoScoreReefMap[undoReefLocation]
                                        ?.value
                                        ?.toMutableList()
                                        ?.apply {
                                            set(
                                                undoReefLevel,
                                                autoScoreReefMap[undoReefLocation]
                                                    ?.value
                                                    ?.get(undoReefLevel)
                                                    ?.plus(-1)
                                                    ?: 0
                                            )
                                        }
                                        ?: listOf(0, 0, 0, 0)
                                )
                            )
                        }
                    failing = false
                }

                Constants.ActionType.AUTO_INTAKE_GROUND_1.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Ground 1",
                                    actionMap["Auto Intake Ground 1"]?.plus(-1) ?: 0
                                )
                                set(
                                    "Intake Ground",
                                    actionMap["Intake Ground"]?.plus(-1) ?: 0
                                )
                            }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_GROUND_2.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Ground 2",
                                    actionMap["Auto Intake Ground 2"]?.plus(-1) ?: 0
                                )
                                set(
                                    "Intake Ground",
                                    actionMap["Intake Ground"]?.plus(-1) ?: 0
                                )
                            }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_STATION_1.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Station 1",
                                    actionMap["Auto Intake Station 1"]?.plus(-1) ?: 0
                                )
                                set(
                                    "Intake Station",
                                    actionMap["Intake Station"]?.plus(-1) ?: 0
                                )
                            }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_STATION_2.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Station 2",
                                    actionMap["Auto Intake Station 2"]?.plus(-1) ?: 0
                                )
                                set(
                                    "Intake Station",
                                    actionMap["Intake Station"]?.plus(-1) ?: 0
                                )
                            }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_1_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(0, autoIntakeList[0] - 1) }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_2_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(1, autoIntakeList[1] - 1) }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_3_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(2, autoIntakeList[2] - 1) }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_1_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(3, autoIntakeList[3] - 1) }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_2_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(4, autoIntakeList[4] - 1) }
                    scoring = false

                }

                Constants.ActionType.AUTO_INTAKE_MARK_3_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(5, autoIntakeList[5] - 1) }
                    scoring = false

                }


                Constants.ActionType.AUTO_DROP_ALGAE.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Auto Drop Algae", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_DROP_CORAL.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Auto Drop Coral", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_NET.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Auto Net", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.AUTO_PROCESSOR.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Auto Processor", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_NET.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Net", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_PROCESSOR.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Processor", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_CORAL_L1.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Coral L1", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_CORAL_L2.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Coral L2", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_CORAL_L3.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Coral L3", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_CORAL_L4.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Coral L4", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_DROP_CORAL.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Drop Coral", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_DROP_ALGAE.toString() -> {
                    if ((timeline.size >= 2 &&
                                timeline[timeline.size - 2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2
                    ) updateActionCount("Tele Drop Algae", -1)
                    failing = false
                    scoring = true

                }

                Constants.ActionType.TELE_INTAKE_REEF.toString() -> {
                    updateActionCount("Intake Reef", -1)
                    scoring = false
                }

                Constants.ActionType.TELE_INTAKE_GROUND.toString() -> {
                    updateActionCount("Intake Ground", -1)
                    scoring = false

                }


                Constants.ActionType.TELE_INTAKE_STATION.toString() -> {
                    updateActionCount("Intake Station", -1)
                    scoring = false

                }

                Constants.ActionType.TELE_INTAKE_POACH.toString() -> {
                    updateActionCount("Intake Poach", -1)
                    scoring = false
                }

                Constants.ActionType.START_INCAP_TIME.toString() -> {
                    tb_incap.isChecked = false
                    isIncap = false
                }

                Constants.ActionType.END_INCAP_TIME.toString() -> {
                    tb_incap.isChecked = true
                    isIncap = true
                }

                Constants.ActionType.TO_TELEOP.toString() -> {
                    fragmentScreen = Constants.Stage.AUTO
                    isTeleopActivated = false
                }

                Constants.ActionType.TO_ENDGAME.toString() -> {
                    fragmentScreen = Constants.Stage.TELEOP
                }
            }
            undoInventoryAction(timeline.last()["action_type"].toString())

            // Add removed action to removedTimelineActions, so it can be redone if needed.
            removedTimelineActions.add(timeline.last())

            // Remove most recent timeline entry.
            timeline.removeAt(timeline.lastIndex)
            enableButtons()

            // If the last action in the timeline is a fail, undo again
            if (timeline.isNotEmpty()) {
                if (
                    timeline.last()["action_type"].toString() in specialActions
                ) {
                    timelineRemove()
                }
            }
        }
    }

    /**
     * Redoes timeline actions after undo.
     */
    fun timelineReplace() {
        if (removedTimelineActions.isNotEmpty()) {
            // Increment action values and display on action counters if re-adding a counter action from the timeline.
            when (removedTimelineActions.last()["action_type"].toString()) {

                Constants.ActionType.AUTO_INTAKE_REEF_F1.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(3, autoIntakeList[6] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_REEF_F2.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(4, autoIntakeList[7] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_REEF_F3.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(5, autoIntakeList[8] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_REEF_F4.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(6, autoIntakeList[9] + 1) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_REEF_F5.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(7, autoIntakeList[10] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_REEF_F6.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(8, autoIntakeList[11] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_GROUND_1.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Ground 1",
                                    actionMap["Auto Intake Ground 1"]?.plus(1) ?: 0
                                )
                                set(
                                    "Intake Ground",
                                    actionMap["Intake Ground"]?.plus(1) ?: 0
                                )
                            }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_GROUND_2.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Ground 2",
                                    actionMap["Auto Intake Ground 2"]?.plus(1) ?: 0
                                )
                                set(
                                    "Intake Ground",
                                    actionMap["Intake Ground"]?.plus(1) ?: 0
                                )
                            }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_STATION_1.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Station 1",
                                    actionMap["Auto Intake Station 1"]?.plus(1) ?: 0
                                )
                                set(
                                    "Intake Station",
                                    actionMap["Intake Station"]?.plus(1) ?: 0
                                )
                            }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_STATION_2.toString() -> {
                    actionMap =
                        actionMap
                            .toMutableMap()
                            .apply {
                                set(
                                    "Auto Intake Station 2",
                                    actionMap["Auto Intake Station 2"]?.plus(1) ?: 0
                                )
                                set(
                                    "Intake Station",
                                    actionMap["Intake Station"]?.plus(1) ?: 0
                                )
                            }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_MARK_1_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(0, autoIntakeList[0] + 1) }
                    scoring = true


                }

                Constants.ActionType.AUTO_INTAKE_MARK_2_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(1, autoIntakeList[1] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_MARK_3_CORAL.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(2, autoIntakeList[2] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_MARK_1_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(3, autoIntakeList[3] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_MARK_2_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(4, autoIntakeList[4] + 1) }
                    scoring = true

                }

                Constants.ActionType.AUTO_INTAKE_MARK_3_ALGAE.toString() -> {
                    autoIntakeList =
                        autoIntakeList
                            .toMutableList()
                            .apply { set(5, autoIntakeList[5] + 1) }
                    scoring = true

                }


                Constants.ActionType.AUTO_REEF_L1.toString() -> {
                    if (redoReefSuccess) {
                        autoScoreReefMap = autoScoreReefMap
                            .toMutableMap()
                            .apply {
                                set(
                                    redoReefLocation,
                                    mutableStateOf(
                                        autoScoreReefMap[redoReefLocation]
                                            ?.value
                                            ?.toMutableList()
                                            ?.apply {
                                                set(
                                                    0,
                                                    autoScoreReefMap[redoReefLocation]
                                                        ?.value
                                                        ?.get(0)
                                                        ?.plus(1)
                                                        ?: 0
                                                )
                                            }
                                            ?: listOf(0, 0, 0, 0)
                                    )
                                )
                            }
                        scoring = false

                    }
                }

                Constants.ActionType.AUTO_REEF_L2.toString() -> {
                    if (redoReefSuccess) {
                        autoScoreReefMap = autoScoreReefMap
                            .toMutableMap()
                            .apply {
                                set(
                                    redoReefLocation,
                                    mutableStateOf(
                                        autoScoreReefMap[redoReefLocation]
                                            ?.value
                                            ?.toMutableList()
                                            ?.apply {
                                                set(
                                                    1,
                                                    autoScoreReefMap[redoReefLocation]
                                                        ?.value
                                                        ?.get(1)
                                                        ?.plus(1)
                                                        ?: 0
                                                )
                                            }
                                            ?: listOf(0, 0, 0, 0)
                                    )
                                )
                            }
                        scoring = false

                    }

                }

                Constants.ActionType.AUTO_REEF_L3.toString() -> {
                    if (redoReefSuccess) {
                        autoScoreReefMap = autoScoreReefMap
                            .toMutableMap()
                            .apply {
                                set(
                                    redoReefLocation,
                                    mutableStateOf(
                                        autoScoreReefMap[redoReefLocation]
                                            ?.value
                                            ?.toMutableList()
                                            ?.apply {
                                                set(
                                                    2,
                                                    autoScoreReefMap[redoReefLocation]
                                                        ?.value
                                                        ?.get(2)
                                                        ?.plus(1)
                                                        ?: 0
                                                )
                                            }
                                            ?: listOf(0, 0, 0, 0)
                                    )
                                )
                            }
                        scoring = false

                    }
                }

                Constants.ActionType.AUTO_REEF_L4.toString() -> {
                    if (redoReefSuccess) {
                        autoScoreReefMap = autoScoreReefMap
                            .toMutableMap()
                            .apply {
                                set(
                                    redoReefLocation,
                                    mutableStateOf(
                                        autoScoreReefMap[redoReefLocation]
                                            ?.value
                                            ?.toMutableList()
                                            ?.apply {
                                                set(
                                                    3,
                                                    autoScoreReefMap[redoReefLocation]
                                                        ?.value
                                                        ?.get(3)
                                                        ?.plus(1)
                                                        ?: 0
                                                )
                                            }
                                            ?: listOf(0, 0, 0, 0)
                                    )
                                )
                            }
                        scoring = false

                    }
                }

                Constants.ActionType.AUTO_REEF_F1.toString() -> {
                    redoReefLocation = 0
                }

                Constants.ActionType.AUTO_REEF_F2.toString() -> {
                    redoReefLocation = 1
                }

                Constants.ActionType.AUTO_REEF_F3.toString() -> {
                    redoReefLocation = 2
                }

                Constants.ActionType.AUTO_REEF_F4.toString() -> {
                    redoReefLocation = 3
                }

                Constants.ActionType.AUTO_REEF_F5.toString() -> {
                    redoReefLocation = 4
                }

                Constants.ActionType.AUTO_REEF_F6.toString() -> {
                    redoReefLocation = 5
                }

                Constants.ActionType.AUTO_REEF_SUCCESS.toString() -> {
                    redoReefSuccess = true
                }

                Constants.ActionType.AUTO_REEF_FAIL.toString() -> {
                    redoReefSuccess = false
                }

                Constants.ActionType.FAIL.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Fail", 1)
                }

                Constants.ActionType.AUTO_DROP_ALGAE.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Auto Drop Algae", 1)
                    scoring = false

                }

                Constants.ActionType.AUTO_DROP_CORAL.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Auto Drop Coral", 1)
                    scoring = false

                }

                Constants.ActionType.AUTO_NET.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Auto Net", 1)
                    scoring = false

                }

                Constants.ActionType.AUTO_PROCESSOR.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Auto Processor", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_NET.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    )
                        updateActionCount("Tele Net", 1)
                    scoring = false


                }

                Constants.ActionType.TELE_PROCESSOR.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Processor", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_CORAL_L1.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Coral L1", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_CORAL_L2.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Coral L2", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_CORAL_L3.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Coral L3", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_CORAL_L4.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Coral L4", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_INTAKE_REEF.toString() -> {
                    updateActionCount("Intake Reef", 1)
                    scoring = true
                }

                Constants.ActionType.TELE_DROP_ALGAE.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Drop Algae", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_DROP_CORAL.toString() -> {
                    if ((timeline.isNotEmpty() &&
                                timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()
                    ) updateActionCount("Tele Drop Coral", 1)
                    scoring = false

                }

                Constants.ActionType.TELE_INTAKE_GROUND.toString() -> {
                    updateActionCount("Intake Ground", 1)
                    scoring = true


                }

                Constants.ActionType.TELE_INTAKE_STATION.toString() -> {
                    updateActionCount("Intake Station", 1)
                    scoring = true

                }

                Constants.ActionType.TELE_INTAKE_POACH.toString() -> {
                    updateActionCount("Intake Poach", 1)
                    scoring = true
                }

                Constants.ActionType.START_INCAP_TIME.toString() -> {
                    tb_incap.isChecked = true
                    isIncap = true
                }

                Constants.ActionType.END_INCAP_TIME.toString() -> {
                    tb_incap.isChecked = false
                    isIncap = false
                }

                Constants.ActionType.TO_TELEOP.toString()
                -> {
                    fragmentScreen = Constants.Stage.TELEOP
                    isTeleopActivated = true
                }

                Constants.ActionType.TO_ENDGAME.toString()
                -> {
                    fragmentScreen = Constants.Stage.ENDGAME
                }
            }

            // Add most recently undone action from removedTimelineActions back to timeline.
            timeline.add(removedTimelineActions.last())
            processInventoryAction(removedTimelineActions.last()["action_type"].toString())

            // Remove the redone action from removedTimelineActions.
            removedTimelineActions.removeAt(removedTimelineActions.lastIndex)
            enableButtons()

            // If the last action in the timeline is a fail, redo again
            if (timeline.isNotEmpty()) {
                if (
                    timeline.last()["action_type"].toString() in specialActions
                ) {
                    timelineReplace()
                }
            }
        }
    }


    /**
     * Enable and disable buttons based on actions in timeline and timer stage. If in teleop, enable [teleopPanel];
     * if teleop is not activated, enable [autoPanel]
     */
    fun enableButtons() {
        // Determines if the change preload button should still be shown
        topFragment =
            when (timeline.size) {
                1 -> timeline.last()["action_type"].toString() == Constants.ActionType.TO_TELEOP.toString()
                2 -> timeline.last()["action_type"].toString() == Constants.ActionType.TO_ENDGAME.toString()
                else -> timeline.isEmpty()
            }
        // Updates the undo and redo buttons if necessary
        if (!topFragment) undoRedoFragment.enableButtons()

        // Enables the incap toggle button if not in auto, not recording a failed score, and the match hasn't ended
        tb_incap.isEnabled = fragmentScreen != Constants.Stage.AUTO && !isMatchTimeEnded &&
                !failing

        // Updates the text on the failed button
        if (scoring) {
            btn_action_failed.text =
                getString(R.string.btn_action_failed, actionMap["Fail"].toString())
            btn_action_toggle.text = "Scoring"
        } else {
            btn_action_failed.text =
                getString(R.string.btn_action_poach, actionMap["Intake Poach"].toString())
            btn_action_toggle.text = "Intaking"
        }

        // Ensure the scoring/intaking button text properly updates
        btn_action_toggle.text = if (scoring) "Scoring" else "Intaking"

        // Enables the button timer if no buttons have been pressed and a popup isn't open
        btn_timer.isEnabled = timeline.size <= 0

        // Enables the fail button if not incap and not already recording a failed score
        btn_action_failed.isEnabled = !isIncap && !failing &&
                (isTimerRunning || isMatchTimeEnded) && (if (fragmentScreen == Constants.Stage.AUTO) {
            scoring
        } else {
            fragmentScreen != Constants.Stage.ENDGAME
        }) && robotInvTimeline.isNotEmpty()

        // Enables the score poach button if not incap and intaking and on the teleop screen.
        btn_action_toggle.isEnabled = (isTimerRunning || isMatchTimeEnded)

        // Enables the proceed button if the match has started or is over and if not failing.
        btn_proceed_edit.isEnabled = (isTimerRunning || isMatchTimeEnded) && failing == false

        /*
        Sets the proceed button text to "To Teleop" if it's auto, to "To Endgame" if it's teleop
        and otherwise sets it to "Proceed"
         */
        btn_proceed_edit.text = when (fragmentScreen) {
            Constants.Stage.AUTO -> getString(R.string.btn_to_teleop)
            Constants.Stage.TELEOP -> getString(R.string.btn_to_endgame)
            else -> getString(R.string.btn_proceed)
        }


    }

    /**
     * Ends incap if still activated at end of the match.
     */
    fun endAction() {
        if (tb_incap.isChecked) {
            tb_incap.isChecked = false
            isIncap = false
            timelineAdd(matchTime = matchTime, actionType = Constants.ActionType.END_INCAP_TIME)
        }
    }

    /**
     * Initialize button and toggle button `onClickListeners`.
     */
    @SuppressLint("SuspiciousIndentation")
    private fun initOnClicks() {
        // When clicked, switches to next screen
        btn_proceed_edit.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                // Switches the screen from auto to teleop to endgame
                if (fragmentScreen == Constants.Stage.AUTO) {
                    fragmentScreen = Constants.Stage.TELEOP
                    timelineAdd(matchTime, Constants.ActionType.TO_TELEOP)
                    enableButtons()
                    isTeleopActivated = true
                    objective_match_collection_layout.setBackgroundColor(Color.WHITE)
                } else if (fragmentScreen == Constants.Stage.TELEOP) {
                    fragmentScreen = Constants.Stage.ENDGAME
                    enableButtons()
                    timelineAdd(matchTime, Constants.ActionType.TO_ENDGAME)
                } else {
                    /* If you are in Override and the match has not finished and currently in Teleop mode,
                    brings up an alert that confirms you want to proceed, then moves onto MatchInformationEdit */
                    if (!isMatchTimeEnded && assignMode == Constants.AssignmentMode.OVERRIDE) {
                        AlertDialog.Builder(this)
                            .setMessage(R.string.warning_proceed_override)
                            .setPositiveButton("Yes") { _, _ ->
                                matchTimer?.cancel()
                                startActivity(
                                    Intent(this, MatchInformationEditActivity::class.java).putExtra(
                                        PREVIOUS_SCREEN,
                                        Constants.Screens.COLLECTION_OBJECTIVE
                                    ),
                                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                                )
                            }
                            .show()
                    } else if (isMatchTimeEnded) {
                        endAction()
                        val intent = Intent(this, MatchInformationEditActivity::class.java)
                            .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_OBJECTIVE)
                        startActivity(
                            intent, ActivityOptions.makeSceneTransitionAnimation(
                                this,
                                btn_proceed_edit, "proceed_button"
                            ).toBundle()
                        )
                    }
                }
            }
        }
        // When long clicked, switches to previous screen
        btn_proceed_edit.setOnLongClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                // Switches the screen from endgame to teleop to auto
                if (timeline.isNotEmpty()) {
                    if (fragmentScreen == Constants.Stage.TELEOP && timeline.last()["action_type"] == Constants.ActionType.TO_TELEOP.toString()) {
                        fragmentScreen = Constants.Stage.AUTO
                        enableButtons()
                        isTeleopActivated = false
                        timelineRemove()
                    } else if (
                        (
                                timeline.last()["action_type"] == Constants.ActionType.TO_ENDGAME.toString() ||
                                        timeline.last()["action_type"] == Constants.ActionType.END_INCAP_TIME.toString()
                                )
                    ) {
                        fragmentScreen = Constants.Stage.TELEOP
                        enableButtons()
                        timeline.removeIf { it["action_type"] == Constants.ActionType.TO_ENDGAME.toString() }
                    }
                }
            }
            return@setOnLongClickListener true
        }

        // Start timer on normal click if timer is not running.
        btn_timer.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (!isTimerRunning) {
                    TimerUtility.MatchTimerThread().initTimer(
                        context = this,
                        btn_timer = btn_timer,
                        btn_proceed = btn_proceed_edit,
                        layout = objective_match_collection_layout
                    )
                    isTimerRunning = true
                    enableButtons()
                    btn_proceed_edit.isEnabled = true
                }
            }
        }

        // Reset timer on long click if timer is running.
        btn_timer.setOnLongClickListener(
            View.OnLongClickListener {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    if ((isTimerRunning && fragmentScreen == Constants.Stage.AUTO) or isMatchTimeEnded) {
                        fragmentScreen = Constants.Stage.AUTO
                        timerReset()
                        timeline = ArrayList()
                        isTimerRunning = false
                        enableButtons()
                        btn_proceed_edit.isEnabled = false
                        objective_match_collection_layout.setBackgroundColor(Color.WHITE)
                    }
                }
                return@OnLongClickListener true
            }
        )

        /*
        Start incap if clicking the incap toggle button checks the toggle button.
        Otherwise, end incap.
         */
        tb_incap.setOnClickListener {
            if (!isMatchTimeEnded) {
                if (tb_incap.isChecked) {
                    timelineAdd(
                        matchTime = matchTime,
                        actionType = Constants.ActionType.START_INCAP_TIME
                    )
                    isIncap = true
                    enableButtons()
                } else {
                    timelineAdd(
                        matchTime = matchTime,
                        actionType = Constants.ActionType.END_INCAP_TIME
                    )
                    isIncap = false
                    enableButtons()
                }
            } else {
                tb_incap.isChecked = false
                isIncap = false
                tb_incap.isEnabled = false
            }
        }

        /*
        Adds a score in the amplified speaker to the timeline, then
        increases the count if not failing the score, then switches
        to intaking and enables buttons.
         */
        btn_action_toggle.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime && !failing) {
                buttonPressedTime = newPressTime
                scoring = !scoring
                enableButtons()
            }
        }

        // Adds a fail to the timeline, sets failing to true, increments the count, and enables buttons
        btn_action_failed.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 255 < newPressTime) {
                buttonPressedTime = newPressTime
                if (scoring) {
                    timelineAddWithStage(action_type = Constants.ActionType.FAIL)
                    updateActionCount("Fail", 1)
                    failing = true
                    enableButtons()
                } else {
                    //check if poaching is allowed (if it has less than 2 game pieces)
                    if (checkEnabled(Constants.ActionType.TELE_INTAKE_POACH)) {
                        timelineAddWithStage(action_type = Constants.ActionType.TELE_INTAKE_POACH)
                        updateActionCount("Intake Poach", 1)
                        scoring = true
                        enableButtons()
                    }
                }
            }
        }
    }

    /**
     * Set team number view to team number defined in `References` and set team number to alliance
     * color.
     */
    private fun initTeamNum() {
        tv_team_number.text = teamNumber

        if (allianceColor == Constants.AllianceColor.RED) {
            tv_team_number.setTextColor(resources.getColor(R.color.alliance_red_light, null))
        } else {
            tv_team_number.setTextColor(resources.getColor(R.color.alliance_blue_light, null))
        }
    }

    /**
     * Initialize intent used in [onKeyLongPress] to restart app from
     * [StartingPositionObjectiveActivity].
     */
    private fun intentToPreviousActivity() {
        timerReset()
        startActivity(
            Intent(this, StartingPositionObjectiveActivity::class.java)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_OBJECTIVE),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    /**
     * Resets and enables everything if the user entered this screen by pressing the back button.
     */
    private fun comingBack() {
        isTimerRunning = false
        isMatchTimeEnded = true
        isTeleopActivated = true
        btn_proceed_edit.text = getString(R.string.btn_proceed)
        btn_proceed_edit.isEnabled = true
        btn_timer.isEnabled = false
        btn_timer.text = getString(R.string.timer_run_down)
        tb_incap.isEnabled = false
        fragmentScreen = Constants.Stage.ENDGAME
        enableButtons()
        topFragment =
            timeline.last()["action_type"].toString() == Constants.ActionType.TO_ENDGAME.toString() && timeline.size == 2
    }

    /**
     * Restart app from [StartingPositionObjectiveActivity] when back button is long pressed.
     */
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setMessage(R.string.error_back_reset)
                    .setPositiveButton("Yes") { _, _ -> intentToPreviousActivity() }
                    .show()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.collection_objective_activity)
        fragmentScreen = fragmentScreen
        topFragment = topFragment

        if (previousScreen != Constants.Screens.MATCH_INFORMATION_EDIT && previousScreen != Constants.Screens.QR_GENERATE) timerReset()
        else comingBack()

        enableButtons()
        initOnClicks()
        initTeamNum()
    }
}