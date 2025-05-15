package com.frc1678.match_collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.objective.CollectionObjectiveActivity
import kotlinx.android.synthetic.main.undo_redo_fragment.view.btn_redo
import kotlinx.android.synthetic.main.undo_redo_fragment.view.btn_undo

class UndoRedoFragment : Fragment(R.layout.undo_redo_fragment) {

    /**
     * The main view of this fragment.
     */
    private var mainView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainView = super.onCreateView(inflater, container, savedInstanceState)!!
        initOnClicks()
        enableButtons()
        return mainView
    }

    private val collectionObjectiveActivity get() = activity as CollectionObjectiveActivity

    /**
     * Removes the last timeline action when the undo button is clicked
     * Replaces the last timeline action that was removed when the redo button is clicked
     */
    private fun initOnClicks() {
        if (mainView != null && activity != null) with(mainView!!) {
            // Remove previous action from timeline when undo button is clicked.
            btn_undo.setOnClickListener {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    collectionObjectiveActivity.timelineRemove()
                }
            }

            // Replace previously undone action to timeline when redo button is clicked.
            btn_redo.setOnClickListener {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    collectionObjectiveActivity.timelineReplace()
                }
            }
        }
    }

    private fun updateUndoRedo() {
        if (mainView != null && activity != null) with(mainView!!) {
            // Get the "Undo" text
            val undoText = resources.getText(R.string.btn_undo)
            btn_undo.text = if (timeline.isEmpty()) {
                // Nothing to undo
                undoText
            } else {
                if (timeline.size >= 2) {
                    if (timeline.size >= 3) {
                        "$undoText\n" +
                        (
                            if (timeline[timeline.size - 3]["action_type"] == Constants.ActionType.AUTO_REEF_FAIL.toString()
                                || timeline[timeline.size - 3]["action_type"] == Constants.ActionType.AUTO_REEF_SUCCESS.toString()) {
                                (
                                    if (timeline[timeline.size - 3]["action_type"] == Constants.ActionType.AUTO_REEF_FAIL.toString()) "Fail "
                                    else ""
                                ) +
                                "Auto Reef " +
                                (
                                    when (timeline[timeline.size - 2]["action_type"]) {
                                        Constants.ActionType.AUTO_REEF_F1.toString() -> "F1 "
                                        Constants.ActionType.AUTO_REEF_F2.toString() -> "F2 "
                                        Constants.ActionType.AUTO_REEF_F3.toString() -> "F3 "
                                        Constants.ActionType.AUTO_REEF_F4.toString() -> "F4 "
                                        Constants.ActionType.AUTO_REEF_F5.toString() -> "F5 "
                                        else -> "F6 "
                                    } +
                                    (
                                        when (timeline[timeline.size - 1]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_L1.toString() -> "L1"
                                            Constants.ActionType.AUTO_REEF_L2.toString() -> "L2"
                                            Constants.ActionType.AUTO_REEF_L3.toString() -> "L3"
                                            else -> "L4"
                                        }
                                    )
                                )
                            }
                            else {
                                (
                                    if (timeline[timeline.size - 2]["action_type"] == Constants.ActionType.FAIL.toString()) "Fail "
                                    else ""
                                    ) + timeline.last()["action_type"]?.split('_')
                                ?.joinToString(" ") {
                                    it.lowercase()
                                        .replaceFirstChar { char -> char.uppercaseChar() }
                                }
                            }
                        )
                    }
                    else {
                        "$undoText\n" + (if (timeline[timeline.size - 2]["action_type"] == Constants.ActionType.FAIL.toString()) "Fail "
                        else "") + timeline.last()["action_type"]?.split('_')?.joinToString(" ") {
                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                        }
                    }
                } else {
                    "$undoText\n" + timeline.last()["action_type"]?.split('_')?.joinToString(" ") {
                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                        }
                }
            }
            // Get the "Redo" text
            val redoText = resources.getText(R.string.btn_redo)
            btn_redo.text = if (collectionObjectiveActivity.removedTimelineActions.isEmpty()) {
                // Nothing to redo
                redoText
            } else {
                if (collectionObjectiveActivity.removedTimelineActions.size >= 2) {
                    if (collectionObjectiveActivity.removedTimelineActions.size >= 3) {
                        if (collectionObjectiveActivity.removedTimelineActions.size >= 4) {
                            "$redoText\n" +
                            (
                                if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.FAIL.toString() && collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"] == Constants.ActionType.AUTO_REEF_FAIL.toString()) {
                                    "Fail Auto Reef " +
                                    (
                                        when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 3]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_F1.toString() -> "F1 "
                                            Constants.ActionType.AUTO_REEF_F2.toString() -> "F2 "
                                            Constants.ActionType.AUTO_REEF_F3.toString() -> "F3 "
                                            Constants.ActionType.AUTO_REEF_F4.toString() -> "F4 "
                                            Constants.ActionType.AUTO_REEF_F5.toString() -> "F5 "
                                            else -> "F6 "
                                        } +
                                            when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 4]["action_type"]) {
                                                Constants.ActionType.AUTO_REEF_L1.toString() -> "L1"
                                                Constants.ActionType.AUTO_REEF_L2.toString() -> "L2"
                                                Constants.ActionType.AUTO_REEF_L3.toString() -> "L3"
                                                else -> "L4"
                                            }
                                        )
                                }
                                else if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.AUTO_REEF_SUCCESS.toString()) {
                                    "Auto Reef " +
                                    (
                                        when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_F1.toString() -> "F1 "
                                            Constants.ActionType.AUTO_REEF_F2.toString() -> "F2 "
                                            Constants.ActionType.AUTO_REEF_F3.toString() -> "F3 "
                                            Constants.ActionType.AUTO_REEF_F4.toString() -> "F4 "
                                            Constants.ActionType.AUTO_REEF_F5.toString() -> "F5 "
                                            else -> "F6 "
                                        } +
                                        when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 3]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_L1.toString() -> "L1"
                                            Constants.ActionType.AUTO_REEF_L2.toString() -> "L2"
                                            Constants.ActionType.AUTO_REEF_L3.toString() -> "L3"
                                            else -> "L4"
                                        }
                                    )
                                }
                                else {
                                    collectionObjectiveActivity.removedTimelineActions.last()["action_type"]?.split(
                                        '_'
                                    )?.joinToString(" ") {
                                        it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                                    } + if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.FAIL.toString()) {
                                        " " + collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"]?.split(
                                            '_'
                                        )?.joinToString(" ") {
                                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                                        }
                                    } else ""
                                }
                            )
                        }
                        else {
                            "$redoText\n" +
                            (
                                if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.AUTO_REEF_SUCCESS.toString()) {
                                    "Auto Reef " +
                                    (
                                        when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_F1.toString() -> "F1 "
                                            Constants.ActionType.AUTO_REEF_F2.toString() -> "F2 "
                                            Constants.ActionType.AUTO_REEF_F3.toString() -> "F3 "
                                            Constants.ActionType.AUTO_REEF_F4.toString() -> "F4 "
                                            Constants.ActionType.AUTO_REEF_F5.toString() -> "F5 "
                                            else -> "F6 "
                                        } +
                                        when (collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 3]["action_type"]) {
                                            Constants.ActionType.AUTO_REEF_L1.toString() -> "L1"
                                            Constants.ActionType.AUTO_REEF_L2.toString() -> "L2"
                                            Constants.ActionType.AUTO_REEF_L3.toString() -> "L3"
                                            else -> "L4"
                                        }
                                    )
                                }
                                else {
                                    collectionObjectiveActivity.removedTimelineActions.last()["action_type"]?.split(
                                        '_'
                                    )?.joinToString(" ") {
                                        it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                                    } + if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.FAIL.toString()) {
                                        " " + collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"]?.split(
                                            '_'
                                        )?.joinToString(" ") {
                                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                                        }
                                    } else ""
                                }
                            )
                        }
                    }
                    else {
                        "$redoText\n" + collectionObjectiveActivity.removedTimelineActions.last()["action_type"]?.split(
                            '_'
                        )?.joinToString(" ") {
                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                        } + if (collectionObjectiveActivity.removedTimelineActions.last()["action_type"] == Constants.ActionType.FAIL.toString()) {
                            " " + collectionObjectiveActivity.removedTimelineActions[collectionObjectiveActivity.removedTimelineActions.size - 2]["action_type"]?.split(
                                '_'
                            )?.joinToString(" ") {
                                it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                            }
                        } else ""
                    }
                } else {
                    "$redoText\n" + collectionObjectiveActivity.removedTimelineActions.last()["action_type"]?.split(
                            '_'
                        )?.joinToString(" ") {
                            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
                        }
                }
            }
        }
    }

    fun enableButtons() {
        if (mainView != null && activity != null) with(mainView!!) {
            // Updates the text on the undo and redo buttons
            updateUndoRedo()

            // Enables the undo button if the timeline is not empty and a popup isn't open
            btn_undo.isEnabled = timeline.size > 0

            // Enables the redo button if the removedTimelineActions is not empty and a popup isn't open
            btn_redo.isEnabled = collectionObjectiveActivity.removedTimelineActions.size > 0
        }
    }
}