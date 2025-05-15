package com.frc1678.match_collection.objective

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.frc1678.match_collection.CollectionActivity
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.AllianceColor
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Companion.previousScreen
import com.frc1678.match_collection.MatchInformationEditActivity
import com.frc1678.match_collection.MatchInformationInputActivity
import com.frc1678.match_collection.R
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.preloaded
import com.frc1678.match_collection.resetCollectionReferences
import com.frc1678.match_collection.robotInvTimeline
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.startingPosition
import com.frc1678.match_collection.teamNumber
import com.frc1678.match_collection.theme.disabledBackground
import com.frc1678.match_collection.theme.disabledBorder
import com.frc1678.match_collection.timeline
import kotlinx.android.synthetic.main.starting_position_activity.btn_no_show
import kotlinx.android.synthetic.main.starting_position_activity.btn_proceed_starting_position
import kotlinx.android.synthetic.main.starting_position_activity.btn_switch_orientation
import kotlinx.android.synthetic.main.starting_position_activity.compose_map
import kotlinx.android.synthetic.main.starting_position_activity.toggle_preload
import kotlinx.android.synthetic.main.starting_position_activity.tv_pos_team_number

class StartingPositionObjectiveActivity : CollectionActivity() {

    @SuppressLint("ResourceAsColor")
    private fun initOnClicks() {
        btn_switch_orientation.setOnClickListener {
            orientation = !orientation
        }/*
        Toggles if the robot has a preload or not, then sets
        preloaded, scoring, and the color to correspondingly
         */
        toggle_preload.setOnClickListener {
            preloaded = !preloaded

            if (toggle_preload.isChecked) {
                toggle_preload.setBackgroundColor(getColor(R.color.action_orange))
            } else {
                toggle_preload.setBackgroundColor(getColor(R.color.light_gray))
            }
        }

        /*
        Sets the starting position to no show, disables the preload
        toggle button and sets preloaded, scoring, and the button's
        state to no preload.
         */
        btn_no_show.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                startingPosition = 0
                preloaded = false
                scoring = false
                toggle_preload.setBackgroundColor(getColor(R.color.light_gray))
                toggle_preload.isEnabled = false
                toggle_preload.isChecked = false
                btn_no_show.setBackgroundColor(getColor(R.color.starting_position_selected))
            }
        }

        // Moves onto the next screen if you have inputted all the information
        btn_proceed_starting_position.setOnClickListener { view ->
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (startingPosition != null) {
                    // If you did not select a starting position, the team is assumed to be a no-show.
                    // This will allow you to skip the collection activity.
                    intent = if (startingPosition == 0) {
                        Intent(this, MatchInformationEditActivity::class.java)
                    } else {
                        Intent(this, CollectionObjectiveActivity::class.java)
                    }.putExtra(PREVIOUS_SCREEN, Constants.Screens.STARTING_POSITION_OBJECTIVE)
                    startActivity(
                        intent, ActivityOptions.makeSceneTransitionAnimation(
                            this, btn_proceed_starting_position, "proceed_button"
                        ).toBundle()
                    )
                    // preloaded, start with a robot inventory of just Coral
                    if (preloaded) {
                        robotInvTimeline.add(arrayOf('C'))
                    } else {
                        robotInvTimeline.add(emptyArray())
                    }
                } else createErrorMessage(getString(R.string.error_missing_information), view)
            }
        }
    }

    /**
     * Show dialog to restart from [MatchInformationInputActivity] when back button is long pressed.
     */
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setMessage(R.string.error_back_reset)
                    .setPositiveButton("Yes") { _, _ ->
                        startActivity(
                            Intent(
                                this,
                                MatchInformationInputActivity::class.java
                            ).putExtra("team_one", teamNumber).putExtra(
                                    PREVIOUS_SCREEN, Constants.Screens.STARTING_POSITION_OBJECTIVE
                                ), ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                        )
                    }.show()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.starting_position_activity)
        compose_map.setContent { MapContent() }

        // Sets whether or not the robot has a preload depending on the previous screen
        if (previousScreen == Constants.Screens.MATCH_INFORMATION_INPUT) {
            preloaded = true

            scoring = true
            toggle_preload.isChecked = true
            toggle_preload.setBackgroundColor(getColor(R.color.action_orange))
        } else {
            toggle_preload.isChecked = preloaded


            scoring = false
            if (preloaded) {
                toggle_preload.setBackgroundColor(getColor(R.color.action_orange))
            } else {
                toggle_preload.setBackgroundColor(getColor(R.color.light_gray))
            }
        }

        // Initialize the team number text
        tv_pos_team_number.text = teamNumber
        tv_pos_team_number.setTextColor(
            resources.getColor(
                if (allianceColor == AllianceColor.RED) R.color.alliance_red_light
                else R.color.alliance_blue_light, null
            )
        )

        resetCollectionReferences()

        initOnClicks()
    }

    /**
     * All content for the map, including the buttons on the map.
     */
    @Composable
    fun MapContent() {
        val rotated = orientation != (allianceColor == Constants.AllianceColor.BLUE)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column {
                /*
                This box contains all the elements that will be displayed, it is rotated based on your orientation.
                The elements within the box are aligned to the left or the right depending on the alliance color.
                 */
                BoxWithConstraints(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .rotate(if (rotated) 0f else 180f)
                ) {
                    /*
                    This image view is behind everything else in the box
                    and displays one of two images based on your alliance.
                    */
                    Image(
                        painter = painterResource(
                            id = if (allianceColor == AllianceColor.BLUE) R.drawable.reefscape_map_auto_blue
                            else R.drawable.reefscape_map_auto_red
                        ),
                        contentDescription = "FIELD MAP",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (allianceColor == Constants.AllianceColor.BLUE) 180f else 0f)
                    )
                    //STARTING POSITION BUTTONS
                    /*
                    When clicked, the buttons will set the starting position to
                    the corresponding value and re-enable the preload toggle button
                    */
                    Column(
                        modifier = Modifier
                            .size(160 * maxWidth / 1152, 92 * maxHeight / 63)
                            .offset(
                                263 * maxWidth / -358,
                                maxHeight / 2048
                            )
                    ) {
                        PositionSelectionGrid(
                            startingPosition = startingPosition ?: 0,
                            setStartingPosition = { newPosition -> startingPosition = newPosition },
                            allianceColor = allianceColor,
                            orientation = rotated
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PositionBox(
        modifier: Modifier,
        position: Int,
        currentPosition: Int,
        onClick: (Int) -> Unit,
        allianceColor: AllianceColor,
        orientation: Boolean
    ) {
        Box(modifier = modifier
            .fillMaxWidth()
            .clickable {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    onClick(position)
                }
            }
            .border(
                4.dp, if (currentPosition != position) disabledBorder
                else if (allianceColor == AllianceColor.RED) {
                    Color.Red.copy(alpha = 0.6f)
                } else {
                    Color.Blue.copy(alpha = 0.6f)
                }
            )
            .background(
                if (currentPosition != position) disabledBackground
                else if (allianceColor == AllianceColor.RED) {
                    Color.Red.copy(alpha = 0.6f)
                } else {
                    Color.Blue.copy(alpha = 0.6f)
                }
            )
            .rotate(if (orientation) 0f else 180f), contentAlignment = Alignment.Center) {
            Text(text = position.toString(), style = TextStyle(fontWeight = FontWeight.Bold))
        }
    }

    @Composable
    fun PositionSelectionGrid(
        startingPosition: Int,
        setStartingPosition: (Int) -> Unit,
        allianceColor: AllianceColor,
        orientation: Boolean
    ) {
        Column {
            (1..5).forEach { position ->
                PositionBox(
                    modifier = Modifier.weight(1 / 4.toFloat()),
                    position = position,
                    currentPosition = startingPosition,
                    onClick = { newPosition ->
                        setStartingPosition(newPosition)
                        toggle_preload.isEnabled = true
                        btn_no_show.setBackgroundColor(getColor(R.color.light_gray))
                    },
                    allianceColor = allianceColor,
                    orientation = orientation
                )
            }
        }
    }
}
