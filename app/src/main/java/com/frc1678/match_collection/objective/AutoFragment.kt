package com.frc1678.match_collection.objective

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.AllianceColor
import com.frc1678.match_collection.R
import com.frc1678.match_collection.TriangleShape
import com.frc1678.match_collection.actionMap
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.autoIntakeList
import com.frc1678.match_collection.autoIntakeReefActionMap
import com.frc1678.match_collection.autoScoreFaceReefActionMap
import com.frc1678.match_collection.autoScoreLevelReefActionMap
import com.frc1678.match_collection.autoScoreReefLocation
import com.frc1678.match_collection.autoScoreReefMap
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.checkEnabled
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.preloaded
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.theme.algaeScoreBorderColor
import com.frc1678.match_collection.theme.algaeScoreColor
import com.frc1678.match_collection.theme.blueAllianceBorderColor
import com.frc1678.match_collection.theme.blueAllianceColor
import com.frc1678.match_collection.theme.coralBorderColor
import com.frc1678.match_collection.theme.coralColor
import com.frc1678.match_collection.theme.disabledBackground
import com.frc1678.match_collection.theme.disabledBorder
import com.frc1678.match_collection.theme.groundBorderColor
import com.frc1678.match_collection.theme.groundColor
import com.frc1678.match_collection.theme.redAllianceBorderColor
import com.frc1678.match_collection.theme.redAllianceColor
import com.frc1678.match_collection.updateActionCount
import kotlinx.android.synthetic.main.collection_objective_auto_fragment.view.auto_compose_view
import kotlin.math.absoluteValue

/**
 * [Fragment] used for showing intake buttons in [AutoFragment]
 */
class AutoFragment : Fragment(R.layout.collection_objective_auto_fragment) {

    /**
     * The main view of this fragment.
     */
    private var mainView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainView = super.onCreateView(inflater, container, savedInstanceState)!!
        setContent()
        return mainView
    }

    /**
     * Parent activity of this fragment
     */
    private val collectionObjectiveActivity get() = activity as CollectionObjectiveActivity

    val rotated = orientation != (allianceColor == AllianceColor.BLUE)

    /**
     * This is the compose function that creates the layout for the compose view in collection_objective_auto_fragment.
     */
    private fun setContent() {
        scoring = preloaded // Set scoring mode if preloaded is true

        mainView!!.auto_compose_view.setContent {
            /*
            This box contains all the elements that will be displayed, it is rotated based on your orientation.
            The elements within the box are aligned to the left or the right depending on the alliance color.
             */
            BoxWithConstraints(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    // Maybe removed
                    .fillMaxSize()
                    .rotate(if (rotated) 0f else 180f)
            ) {
                /*
                    This image view is behind everything else in the box
                    and displays one of two images based on your alliance.
                    */
                Image(
                    painter = painterResource(
                        id = when {
                            (allianceColor == AllianceColor.BLUE) -> R.drawable.reefscape_map_auto_blue
                            else -> R.drawable.reefscape_map_auto_red
                        }
                    ),
                    contentDescription = "Map with game pieces",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .matchParentSize()
                        .fillMaxSize()
                        .rotate(if (allianceColor == AllianceColor.BLUE) 180f else 0f),
                )


                //SCORING BUTTONS
                /*
                    When a scoring button is clicked, adds the action to the timeline,
                    adds to the count for that action if they are not failing the score,
                    then switches to intaking and enables buttons.
                    The background, border, and text colors are set depending on if they
                    are incap or not and the alliance. The contents of the buttons are
                    rotated depending on the orientation so that they are not upside
                    down for certain orientations.
                     */
                if (scoring) {
                    //AutoDropAlgae button function
                    AutoDropAlgae(
                        1, modifier = Modifier
                            .height(maxHeight)
                            .width(maxWidth / 2)
                    )
                    //AutoDropCoral button function
                    AutoDropCoral(
                        1,
                        modifier = Modifier
                            .height(maxHeight)
                            .width(maxWidth / 2)
                            .offset(-maxWidth / 2)
                    )
                    //Auto Reef Level Buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(
                                maxWidth / -2000, 0.dp
                            )

                            .size(maxWidth / 6, maxHeight)
                    ) {
                        val levels = if (!rotated) listOf(0, 1, 2, 3) else listOf(3, 2, 1, 0)
                        levels.forEach { level ->
                            AutoScoreReefLevelButton(
                                level = level, modifier = Modifier.weight(1 / 4f)
                            )
                        }
                    }
                    //Auto Score Net Button
                    AutoScoreNet(
                        modifier = Modifier
                            .height(maxHeight / 2)
                            .width(2 * maxWidth / 17)
                            .offset(
                                // Changes offset based off of alliance color
                                1785 * maxWidth / -2000,
                                2 * maxHeight / 4,
                            )
                    )
                    //Auto Score Processor Button
                    AutoScoreProcessor(
                        modifier = Modifier
                            .height(maxHeight / 5)
                            .width(maxWidth / 4.2f)
                            // Changes offset based off of alliance color
                            .offset(
                                1 * maxWidth / -1.55f,
                                maxHeight / 250,
                            )
                    )


                }

                // INTAKE BUTTONS
                // Refer to the comment for AutoIntakeButton() for details on the intake buttons
                else {
                    // STATION/GROUND 1 INTAKE BUTTONS
                    val stationGroundButtons1 = listOf(
                        "Station 1" to Constants.ActionType.AUTO_INTAKE_STATION_1,
                        "Ground 1" to Constants.ActionType.AUTO_INTAKE_GROUND_1
                    )
                    // Reverse the order if alliance is RED
                    val orderedButtons =
                        stationGroundButtons1.reversed()
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .offset(
                                maxWidth / -2000,
                                0.dp
                            )
                            .size((maxWidth / 0.75f) / 1.2f, (maxHeight / 5))
                    ) {
                        orderedButtons.forEach { (location, actionType) ->
                            AutoIntakeStationGroundButton(
                                location = location,
                                actionType = actionType,
                                modifier = Modifier.weight(1 / 2f)
                            )
                        }
                    }
                    // MARK CORAL INTAKE BUTTONS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            // Offset based off of the alliance color
                            .offset(
                                maxWidth / -2000, maxHeight / 5
                            )

                            .size((maxWidth / 3f) / 1.2f, (3 * maxHeight / 5))
                    ) {
                        // AUTO INTAKE MARK CORAL BUTTON 1
                        AutoIntakeMarkCoralButton(
                            intakeNum = 0,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_1_CORAL,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        // AUTO INTAKE MARK CORAL BUTTON 2
                        AutoIntakeMarkCoralButton(
                            intakeNum = 1,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_2_CORAL,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        // AUTO INTAKE MARK CORAL BUTTON 3
                        AutoIntakeMarkCoralButton(
                            intakeNum = 2,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_3_CORAL,
                            modifier = Modifier.weight(1 / 3f)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            // Offset based off of the alliance color
                            .offset(
                                1440 * maxWidth / -2000,
                                maxHeight / 5
                            )
                            .size((maxWidth / 3f) / 1.2f, (3 * maxHeight / 5))
                    ) {
                        // AUTO INTAKE MARK ALGAE BUTTON 1
                        AutoIntakeMarkAlgaeButton(
                            intakeNum = 3,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_1_ALGAE,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        // AUTO INTAKE MARK ALGAE BUTTON 2
                        AutoIntakeMarkAlgaeButton(
                            intakeNum = 4,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_2_ALGAE,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        // AUTO INTAKE MARK ALGAE BUTTON 3
                        AutoIntakeMarkAlgaeButton(
                            intakeNum = 5,
                            actionType = Constants.ActionType.AUTO_INTAKE_MARK_3_ALGAE,
                            modifier = Modifier.weight(1 / 3f)
                        )
                    }

                    // STATION/GROUND 2 INTAKE BUTTONS
                    val stationGroundButtons2 = listOf(
                        "Station 2" to Constants.ActionType.AUTO_INTAKE_STATION_2,
                        "Ground 2" to Constants.ActionType.AUTO_INTAKE_GROUND_2
                    )

                    // Reverse for RED alliance
                    val orderedButtons2 =
                        stationGroundButtons2.reversed()

                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .offset(
                                maxWidth / -2000,
                                4 * maxHeight / 5
                            )
                            .size((maxWidth / 0.75f) / 1.2f, (maxHeight / 5))
                    ) {
                        orderedButtons2.forEach { (location, actionType) ->
                            AutoIntakeStationGroundButton(
                                location = location,
                                actionType = actionType,
                                modifier = Modifier.weight(1 / 2f)
                            )
                        }
                    }
                }
                // Hexagon function is called here only to pass maxWidth and maxHeight for the hexagon buttons
                Hexagon(offsetX = -4.51 * maxWidth / 40, offsetY = -0.01 * maxHeight / 7)
            }
        }
    }

    /*
     * This button is only enabled when a face on the Reef is selected. When it is pressed, it will add 3 actions to the timeline. It will first add a Reef Success or Reef Fail action,
     * depending on if they are failing or not. Then it will add a action depending on the face of the Reef that was selected. Finally, it will add a action depending on the level of
     * the Reef that the button represents. Afterwards, it will unselect the Reef face.
     */
    @Composable
    fun AutoScoreReefLevelButton(level: Int, modifier: Modifier) {
        Box(modifier = modifier
            .fillMaxWidth()
            .border(
                4.dp,
                if (!collectionObjectiveActivity.isTimerRunning || autoScoreReefLocation == -1 || (autoScoreReefMap[autoScoreReefLocation.absoluteValue]?.value?.get(
                        level
                    ) ?: 0) >= 2) disabledBorder
                else if (allianceColor == AllianceColor.BLUE) blueAllianceBorderColor
                else redAllianceBorderColor
            )
            .background(
                if (!collectionObjectiveActivity.isTimerRunning || autoScoreReefLocation == -1 ||
                    (autoScoreReefMap[autoScoreReefLocation.absoluteValue]?.value?.get(
                        level
                    ) ?: 0) >= 2
                ) disabledBackground
                else if (allianceColor == AllianceColor.BLUE) blueAllianceColor
                else redAllianceColor
            )
            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && autoScoreReefLocation != -1) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        // If match has started
                        if (matchTimer != null) {
                            if ((autoScoreReefMap[autoScoreReefLocation.absoluteValue]?.value?.get(
                                    level
                                ) ?: 0) < 2
                            ) {
                                if (!collectionObjectiveActivity.failing) {
                                    // Tracks both reef location and scoring level on the reef
                                    autoScoreReefMap = autoScoreReefMap
                                        .toMutableMap()
                                        .apply {
                                            set(autoScoreReefLocation,
                                                mutableStateOf(autoScoreReefMap[autoScoreReefLocation]?.value
                                                    ?.toMutableList()
                                                    ?.apply {
                                                        set(
                                                            level,
                                                            autoScoreReefMap[autoScoreReefLocation]?.value
                                                                ?.get(
                                                                    level
                                                                )
                                                                ?.plus(1) ?: 0
                                                        )
                                                    } ?: listOf(0, 0, 0, 0)))
                                        }
                                }

                                // adds AUTO_REEF_FAIL to timeline if failed, otherwise add AUTO_REEF_SUCCESS
                                if (collectionObjectiveActivity.failing) {
                                    collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_REEF_FAIL)
                                } else {
                                    collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_REEF_SUCCESS)
                                }
                                // Adds
                                collectionObjectiveActivity.timelineAddWithStage(action_type = autoScoreFaceReefActionMap[autoScoreReefLocation]!!)
                                collectionObjectiveActivity.timelineAddWithStage(action_type = autoScoreLevelReefActionMap[level]!!)
                                //defaults autoScoreReefLocation to -1
                                autoScoreReefLocation = -1
                                collectionObjectiveActivity.failing = false
                                scoring = !scoring
                                collectionObjectiveActivity.enableButtons()
                            }
                        }
                    }
                }
            }
            .rotate(if (rotated) 0f else 180f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "L${level + 1}:",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                if (autoScoreReefLocation != -1) {
                    Text(
                        text = autoScoreReefMap[autoScoreReefLocation]!!.value[level].toString(),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                }
            }
        }
    }

    /*
    When a intake button is clicked, adds the action to the timeline,
    adds to the count for that action, then switches to scoring and
    enables buttons.
    The background, border, and text colors are set depending on if they
    are incap or not. The contents of the buttons are rotated depending
    on the orientation so that they are not upside down for certain
    orientations.
     */
    @Composable
    // AUTO INTAKE MARK CORAL BUTTON
    fun AutoIntakeMarkCoralButton(
        intakeNum: Int, actionType: Constants.ActionType, modifier: Modifier
    ) {
        val enabled = checkEnabled(actionType)
        Box(modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Checks if the index to autoIntakeList is less than 1
                if (autoIntakeList[intakeNum] < 1 && collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            autoIntakeList = autoIntakeList
                                .toMutableList()
                                //Adds 1 to the corresponding button's position in the autoIntakeList
                                .apply { set(intakeNum, autoIntakeList[intakeNum] + 1) }

                            collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .border(
                4.dp,
                if (autoIntakeList[intakeNum] >= 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                else coralBorderColor
            )
            .background(
                if (autoIntakeList[intakeNum] >= 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else coralColor
            )
            .rotate(if (rotated) 0f else 180f), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (autoIntakeList[intakeNum] == 1) "TAKEN" else "Marked\n Coral ${intakeNum + 1}",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                )
            }
        }
    }

    @Composable
    // AUTO INTAKE MARK ALGAE BUTTON
    fun AutoIntakeMarkAlgaeButton(
        intakeNum: Int, actionType: Constants.ActionType, modifier: Modifier
    ) {
        val enabled = checkEnabled(actionType)
        Box(modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Checks if the index to autoIntakeList is less than 2
                if (autoIntakeList[intakeNum] < 1 && collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            autoIntakeList = autoIntakeList
                                .toMutableList()
                                //Adds 1 to the corresponding button's position in the autoIntakeList
                                .apply {
                                    set(intakeNum, autoIntakeList[intakeNum] + 1)
                                }

                            collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .border(
                4.dp,
                if (autoIntakeList[intakeNum] == 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                else algaeScoreBorderColor
            )
            .background(
                if (autoIntakeList[intakeNum] == 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else algaeScoreColor
            )
            .rotate(if (rotated) 0f else 180f), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (autoIntakeList[intakeNum] == 1) "TAKEN" else "Marked \nAlgae ${intakeNum - 2}",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                )
            }
        }
    }

    // Differentiates between ground and station
    private fun determineStationGround(location: String): String {
        return if ("Ground" in location) "Intake Ground" else "Intake Station"
    }

    // Intake buttons for both station and ground
    @Composable
    fun AutoIntakeStationGroundButton(
        location: String, actionType: Constants.ActionType, modifier: Modifier
    ) {
        val enabled = checkEnabled(actionType)
        Box(modifier = modifier
            .fillMaxHeight()
            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            actionMap = actionMap
                                .toMutableMap()
                                .apply {
                                    set(
                                        "Auto Intake $location",
                                        actionMap["Auto Intake $location"]?.plus(1) ?: 0
                                    )
                                    set(
                                        determineStationGround(location),
                                        actionMap[determineStationGround(location)]?.plus(1) ?: 0
                                    )
                                }
                            collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .border(
                4.dp,
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                else if (location == "Ground 1" || location == "Ground 2") groundBorderColor
                else coralBorderColor
            )
            .background(
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else if (location == "Ground 1" || location == "Ground 2") groundColor
                else coralColor
            )
            .rotate(if (rotated) 0f else 180f), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$location:", style = TextStyle(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${actionMap["Auto Intake $location"]}",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }
    }


    // AUTO SCORE REEF BUTTONS
    @Composable
    fun AutoScoreReefButton(sideNum: Int, modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_REEF_F1)
        Box(
            modifier = modifier
                .size(100.dp)
                .clip(shape = TriangleShape(sideNum))
                .border(
                    BorderStroke(
                        5.dp,
                        if (!collectionObjectiveActivity.isTimerRunning || !enabled || autoScoreReefLocation == sideNum) disabledBorder
                        else {
                            if (allianceColor == AllianceColor.RED) {
                                Color.Red.copy(alpha = 0.8f)
                            } else {
                                Color.Blue.copy(alpha = 0.8f)
                            }
                        }
                    ), shape = TriangleShape(sideNum)
                )
                .background(
                    if (!collectionObjectiveActivity.isTimerRunning || !enabled || autoScoreReefLocation == sideNum) disabledBackground
                    else {
                        if (allianceColor == AllianceColor.RED) {
                            redAllianceColor
                        } else {
                            blueAllianceColor
                        }
                    }
                )
                .clickable {
                    if (collectionObjectiveActivity.isTimerRunning && enabled) {
                        val newPressTime = System.currentTimeMillis()
                        if (buttonPressedTime + 250 < newPressTime) {
                            buttonPressedTime = newPressTime
                            if (matchTimer != null) {
                                autoScoreReefLocation = sideNum
                                collectionObjectiveActivity.enableButtons()

                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            /** Display Face Numbers over Hexagon Triangles*/
            Text(
                text = "F${sideNum + 1}",
                fontSize = 25.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .offset(//offset numbers from center of hexagon
                        x = when (sideNum) {
                            0 -> (-90).dp
                            1 -> (-40).dp
                            2 -> 40.dp
                            3 -> 90.dp
                            4 -> 40.dp
                            5 -> (-40).dp
                            else -> 0.dp
                        },
                        y = when (sideNum) {
                            0 -> 0.dp
                            1 -> 70.dp
                            2 -> 70.dp
                            3 -> 0.dp
                            4 -> (-70).dp
                            5 -> (-70).dp
                            else -> 0.dp
                        }
                    )
                    .rotate(
                        if (rotated) {
                            0F
                        } else {
                            180F
                        }
                    )
            )
        }
    }

    // Intake Reef buttons for Auto
    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AutoIntakeReefButton(intakeNum: Int, modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_INTAKE_REEF_F1)
        BoxWithConstraints(
            modifier = modifier
                .clip(shape = TriangleShape(intakeNum - 6))
                .border(
                    BorderStroke(
                        5.dp,
                        if (autoIntakeList[intakeNum] >= 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                        else {
                            if (allianceColor == AllianceColor.RED) {
                                redAllianceBorderColor
                            } else {
                                blueAllianceBorderColor
                            }
                        }
                    ), shape = TriangleShape(intakeNum - 6)
                )
                .background(
                    if (autoIntakeList[intakeNum] >= 1 || !collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                    else {
                        if (allianceColor == AllianceColor.RED) {
                            redAllianceColor
                        } else {
                            blueAllianceColor
                        }
                    }
                )
                .clickable {
                    if (collectionObjectiveActivity.isTimerRunning && enabled) {
                        val newPressTime = System.currentTimeMillis()
                        if (buttonPressedTime + 250 < newPressTime) {
                            buttonPressedTime = newPressTime
                            if (matchTimer != null) {
                                if (autoIntakeList[intakeNum] < 1) {
                                    autoIntakeList = autoIntakeList
                                        .toMutableList()
                                        // Adds 1 to the corresponding reef button's index in autoIntakeList
                                        .apply {
                                            set(
                                                intakeNum, autoIntakeList[intakeNum] + 1
                                            )
                                        }

                                    updateActionCount("Intake Reef", 1)
                                    // Adds corresponding reef intake location value to the timeline
                                    autoIntakeReefActionMap[intakeNum - 6]?.let { it1 ->
                                        collectionObjectiveActivity.timelineAddWithStage(
                                            action_type = it1
                                        )
                                    }
                                    scoring = !scoring
                                    collectionObjectiveActivity.enableButtons()
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            /** Display Face Numbers over Hexagon Triangles*/
            Text(
                text = "F${intakeNum - 5}",
                fontSize = 25.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .offset(//offset numbers from center of hexagon
                        x = when (intakeNum - 6) {
                            0 -> (-90).dp
                            1 -> (-40).dp
                            2 -> 40.dp
                            3 -> 90.dp
                            4 -> 40.dp
                            5 -> (-40).dp
                            else -> 0.dp
                        },
                        y = when (intakeNum - 6) {
                            0 -> 0.dp
                            1 -> 70.dp
                            2 -> 70.dp
                            3 -> 0.dp
                            4 -> (-70).dp
                            5 -> (-70).dp
                            else -> 0.dp
                        }
                    )
                    .rotate(
                        if (rotated) {
                            0F
                        } else {
                            180F
                        }
                    )
            )
        }
    }

    // AUTO DROP ALGAE BUTTON
    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AutoDropAlgae(sideNum: Int, modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_DROP_ALGAE)
        BoxWithConstraints(modifier = modifier
            .background(
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else algaeScoreColor
            )
            .border(
                BorderStroke(
                    5.dp,
                    if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                    else algaeScoreBorderColor
                )
            )
            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_DROP_ALGAE)
                            // Resets to Default to avoid scoring level buttons activation
                            if (!collectionObjectiveActivity.failing) {
                                autoScoreReefLocation = -1
                                updateActionCount("Auto Drop Algae", 1)
                            }
                            collectionObjectiveActivity.failing = false
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .rotate(if (rotated) 0f else 180f),
            contentAlignment = if (rotated) Alignment.BottomCenter else Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier =
                Modifier.offset(
                    if (rotated) (-50).dp else 50.dp,
                    if (rotated) (-20).dp else 20.dp
                )
            ) {
                Text(
                    text = "DROP ALGAE",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "${actionMap["Auto Drop Algae"]}",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    // AUTO DROP CORAL
    fun AutoDropCoral(sideNum: Int, modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_DROP_CORAL)
        BoxWithConstraints(modifier = modifier
            .background(
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else coralColor
            )
            .border(
                BorderStroke(
                    5.dp,
                    if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                    else coralBorderColor
                )
            )
            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            autoScoreReefLocation = sideNum
                            collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_DROP_CORAL)
                            if (!collectionObjectiveActivity.failing) {
                                autoScoreReefLocation = -1
                                updateActionCount("Auto Drop Coral", 1)
                            }
                            collectionObjectiveActivity.failing = false
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .rotate(if (rotated) 0f else 180f),
            contentAlignment = if (rotated) Alignment.BottomCenter else Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier =
                Modifier.offset(
                    if (rotated) 50.dp else (-50).dp,
                    if (rotated) (-20).dp else 20.dp
                )
            ) {
                Text(
                    text = "DROP CORAL",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "${actionMap["Auto Drop Coral"]}",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }
    }


    // AUTO SCORE NET BUTTON
    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AutoScoreNet(modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_NET)
        BoxWithConstraints(modifier = modifier
            .border(
                BorderStroke(
                    5.dp,
                    if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                    else algaeScoreBorderColor
                ),
            )
            .background(
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else algaeScoreColor
            )
            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_NET)
                            if (!collectionObjectiveActivity.failing) {
                                autoScoreReefLocation = -1
                                updateActionCount("Auto Net", 1)
                            }
                            collectionObjectiveActivity.failing = false
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .rotate(if (rotated) 0f else 180f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset()
            ) {
                Text(
                    text = "NET",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "${actionMap["Auto Net"]}",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // AUTO SCORE PROCESSOR BUTTON
    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AutoScoreProcessor(modifier: Modifier) {
        val enabled = checkEnabled(Constants.ActionType.AUTO_PROCESSOR)
        BoxWithConstraints(modifier = modifier
            .border(
                BorderStroke(
                    5.dp,
                    if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBorder
                    else algaeScoreBorderColor
                ),
            )

            .background(
                if (!collectionObjectiveActivity.isTimerRunning || !enabled) disabledBackground
                else algaeScoreColor
            )

            .clickable {
                if (collectionObjectiveActivity.isTimerRunning && enabled) {
                    val newPressTime = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTime) {
                        buttonPressedTime = newPressTime
                        if (matchTimer != null) {
                            collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.AUTO_PROCESSOR)
                            if (!collectionObjectiveActivity.failing) {
                                autoScoreReefLocation = -1
                                updateActionCount("Auto Processor", 1)
                            }
                            collectionObjectiveActivity.failing = false
                            scoring = !scoring
                            collectionObjectiveActivity.enableButtons()
                        }
                    }
                }
            }
            .rotate(if (rotated) 0f else 180f), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset()
            ) {
                Text(
                    text = "PROCESSOR",
                    style = TextStyle(fontWeight = FontWeight.Bold),

                    )
                Text(
                    text = "${actionMap["Auto Processor"]}",
                    style = TextStyle(fontWeight = FontWeight.Bold),

                    )
            }
        }
    }

    // Draws the Hexagon (Reef) with different buttons depending on if they are scoring or intaking
    @Composable
    fun Hexagon(offsetX: Dp, offsetY: Dp) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val hexagonSize = screenWidth * 0.6f

        if (scoring) {
            repeat(6) { index ->
                AutoScoreReefButton(
                    sideNum = index,
                    modifier = Modifier
                        .size(hexagonSize)
                        .offset(x = offsetX, y = offsetY)
                )
            }
        } else {
            for (index in 6..11) {
                AutoIntakeReefButton(
                    intakeNum = index,
                    modifier = Modifier
                        .size(hexagonSize)
                        .offset(x = offsetX, y = offsetY)
                )
            }
        }
    }

}