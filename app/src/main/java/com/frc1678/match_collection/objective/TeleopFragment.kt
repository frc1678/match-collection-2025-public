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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.AllianceColor
import com.frc1678.match_collection.HexagonShape
import com.frc1678.match_collection.R
import com.frc1678.match_collection.actionMap
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.autoScoreReefMap
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.checkEnabled
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.theme.algaeScoreBorderColor
import com.frc1678.match_collection.theme.algaeScoreColor
import com.frc1678.match_collection.theme.blueAllianceBorderColor
import com.frc1678.match_collection.theme.blueAllianceColor
import com.frc1678.match_collection.theme.coralColor
import com.frc1678.match_collection.theme.disabledBackground
import com.frc1678.match_collection.theme.disabledBorder
import com.frc1678.match_collection.theme.groundColor
import com.frc1678.match_collection.theme.redAllianceBorderColor
import com.frc1678.match_collection.theme.redAllianceColor
import com.frc1678.match_collection.updateActionCount
import kotlinx.android.synthetic.main.collection_objective_teleop_fragment.view.teleop_compose_view

/**
 * [Fragment] used for showing intake buttons in [TeleopFragment]
 */
class TeleopFragment : Fragment(R.layout.collection_objective_teleop_fragment) {

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

    /**
     * This is the compose function that creates the layout for the compose view in collection_objective_teleop_fragment.
     */
    @SuppressLint("UnusedBoxWithConstraintsScope")
    private fun setContent() {


        mainView!!.teleop_compose_view.setContent {


            /*
            This box contains all the elements that will be displayed, it is rotated based on your orientation.
            The elements within the box are aligned to the left or the right depending on the alliance color.
             */
            BoxWithConstraints(
                contentAlignment = if (allianceColor == AllianceColor.BLUE) Alignment.TopStart else Alignment.TopEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (orientation) 0f else 180f)
            ) {
                Image(
                    painter = painterResource(
                        when {
                            (allianceColor == AllianceColor.BLUE) -> R.drawable.reefscape_map_auto_blue
                            else -> R.drawable.reefscape_map_auto_red
                        }
                    ),
                    contentDescription = "Field Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize(),
                )
                // SCORING BUTTONS
                /*
                Refer to the comment on scoringButtonPress() for details on pressing the
                buttons.
                The background, border, and text colors are set depending on if they
                are incap and the alliance color. The contents of the buttons are
                rotated depending on the orientation so that they are not upside
                down for certain orientations.
                 */
                if (scoring) {

                    // TELE DROP ALGAE BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight)
                            .width(110 * maxWidth / 220)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 1 * maxWidth / 220 else 1 * maxWidth / -220,
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_DROP_ALGAE)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_DROP_ALGAE)) disabledBorder
                                else algaeScoreBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_DROP_ALGAE)) disabledBackground
                                else algaeScoreColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = if (allianceColor == AllianceColor.RED) {
                            // location of content in the button based off of the orientation
                            if (orientation) {
                                Alignment.BottomCenter
                            } else {
                                Alignment.TopCenter
                            }

                        } else {
                            if (orientation) {
                                Alignment.TopCenter
                            } else {
                                Alignment.BottomCenter
                            }
                        }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = if ((orientation && allianceColor == AllianceColor.RED) || ((allianceColor == AllianceColor.BLUE) && !orientation)) {
                                Modifier.offset(-50.dp, -20.dp)

                            } else {
                                Modifier.offset(50.dp, 20.dp)
                            }
                        ) {
                            Text(
                                text = "Drop Algae:",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            //Displays the sum of Teleop Drop Algae and Auto Drop Algae
                            Text(
                                text = (actionMap["Auto Drop Algae"]?.let {
                                    (actionMap["Tele Drop Algae"])?.plus(
                                        it
                                    )
                                }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // TELE DROP CORAL BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight)
                            .width(118 * maxWidth / 220)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 110 * maxWidth / 220 else 110 * maxWidth / -220,
                            )
                            .clickable {

                                scoringButtonPress(actionType = Constants.ActionType.TELE_DROP_CORAL)

                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_DROP_CORAL)) disabledBorder
                                else if (allianceColor == AllianceColor.BLUE) Color.Blue.copy(alpha = 0.3f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_DROP_CORAL)) disabledBackground
                                else if (allianceColor == AllianceColor.BLUE) coralColor
                                else coralColor.copy(alpha = 0.3f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = if (allianceColor == AllianceColor.BLUE) {
                            if (orientation) {
                                Alignment.TopCenter

                            } else {
                                Alignment.BottomCenter
                            }

                        } else {
                            if (orientation) {
                                Alignment.BottomCenter
                            } else {
                                Alignment.TopCenter
                            }
                        }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = if ((orientation && allianceColor == AllianceColor.RED) || ((allianceColor == AllianceColor.BLUE) && !orientation)) {
                                Modifier.offset(50.dp, -20.dp)
                            } else {
                                Modifier.offset(-50.dp, 20.dp)
                            }
                        ) {
                            Text(
                                text = "Drop Coral:",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            // Displays the sum of Teleop Drop Coral and Auto Drop Coral
                            Text(
                                text = (actionMap["Auto Drop Coral"]?.let {
                                    (actionMap["Tele Drop Coral"])?.plus(
                                        it
                                    )
                                }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    // SCORE L1 BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 4)
                            .width(2 * maxWidth / 13)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 20 else 0 * maxWidth / -20,
                                if (orientation) 3 * maxHeight / 4 else 0 * maxHeight / 4
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_CORAL_L1)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBorder
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceBorderColor
                                else redAllianceBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBackground
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceColor
                                else redAllianceColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "L1:", style = TextStyle(fontWeight = FontWeight.Bold))

                            Text(
                                // Displays the addition of the sum of all L1 values and Tele Coral 1
                                text = (autoScoreReefMap.values.sumOf { it.value[0] }.let {
                                    (actionMap["Tele Coral L1"])?.plus(
                                        it
                                    )
                                }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // SCORE L2 BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 4)
                            .width(2 * maxWidth / 13)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 20 else 0 * maxWidth / -20,
                                if (orientation) 1 * maxHeight / 2 else 1 * maxHeight / 4
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_CORAL_L2)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBorder
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceBorderColor
                                else redAllianceBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBackground
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceColor
                                else redAllianceColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "L2:", style = TextStyle(fontWeight = FontWeight.Bold))
                            // Displays the addition of the sum of all L2 values and Tele Coral 2
                            Text(text = (autoScoreReefMap.values.sumOf { it.value[1] }.let {
                                (actionMap["Tele Coral L2"])?.plus(
                                    it
                                )
                            }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // SCORE L3 BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 4)
                            .width(2 * maxWidth / 13)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 20 else 0 * maxWidth / -20,
                                if (orientation) 1 * maxHeight / 4 else 1 * maxHeight / 2
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_CORAL_L3)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBorder
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceBorderColor
                                else redAllianceBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBackground
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceColor
                                else redAllianceColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "L3:", style = TextStyle(fontWeight = FontWeight.Bold))
                            // Displays the addition of the sum of all L3 values and Tele Coral 3

                            Text(text = (autoScoreReefMap.values.sumOf { it.value[2] }.let {
                                (actionMap["Tele Coral L3"])?.plus(
                                    it
                                )
                            }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // SCORE L4 BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 4)
                            .width(2 * maxWidth / 13)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 20 else 0 * maxWidth / -20,
                                if (orientation) 0 * maxHeight / 4 else 3 * maxHeight / 4
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_CORAL_L4)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBorder
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceBorderColor
                                else redAllianceBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_CORAL_L4)) disabledBackground
                                else if (allianceColor == AllianceColor.BLUE) blueAllianceColor
                                else redAllianceColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "L4:", style = TextStyle(fontWeight = FontWeight.Bold))
                            // Displays the addition of the sum of all L4 values and Tele Coral 4
                            Text(text = (autoScoreReefMap.values.sumOf { it.value.last() }.let {
                                (actionMap["Tele Coral L4"])?.plus(
                                    it
                                )
                            }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // SCORE TELE_NET BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 2)
                            .width(2 * maxWidth / 17)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 1785 * maxWidth / 2000 else 1785 * maxWidth / -2000,
                                if (allianceColor == AllianceColor.BLUE) 0.dp else 2 * maxHeight / 4,
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_NET)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_NET)) disabledBorder
                                else algaeScoreColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_NET)) disabledBackground
                                else algaeScoreColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "NET:", style = TextStyle(fontWeight = FontWeight.Bold))
                            //Displays the sum of Teleop Net and Auto Net
                            Text(
                                text = (actionMap["Tele Net"]?.let {
                                    (actionMap["Auto Net"])?.plus(
                                        it
                                    )
                                }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // SCORE TELE_PROCESSOR BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .height(maxHeight / 5)
                            .width(maxWidth / 4.2f)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 4 * maxWidth / 6.25f else 1 * maxWidth / -1.55f,
                                if (allianceColor == AllianceColor.BLUE) 4 * maxHeight / 5 else maxHeight / 250,
                            )
                            .clickable {
                                scoringButtonPress(actionType = Constants.ActionType.TELE_PROCESSOR)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_PROCESSOR)) disabledBorder
                                else algaeScoreBorderColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_PROCESSOR)) disabledBackground
                                else algaeScoreColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "PROCESSOR:", style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            //Displays the sum of Teleop Processor and Auto Processor
                            Text(
                                text = (actionMap["Tele Processor"]?.let {
                                    (actionMap["Auto Processor"])?.plus(
                                        it
                                    )
                                }).toString(), style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                // INTAKE BUTTONS
                /*
                Refer to the comment on .intakeButtonModifier() for more
                details on the intake buttons.
                The contents of the buttons are rotated depending on the
                orientation so that they are not upside down for certain
                orientations.
                 */
                else {
                    // INTAKE STATION BUTTON
                    Box(
                        modifier = Modifier
                            .height(4 * maxHeight)
                            .width(maxWidth / 2)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 2000 else 1 * maxWidth / -2000,
                                if (allianceColor == AllianceColor.BLUE) 0 * maxHeight / 4 else 0 * maxHeight / 4,
                            )
                            .clickable {
                                intakeButtonPress(actionType = Constants.ActionType.TELE_INTAKE_STATION)
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_INTAKE_STATION)) disabledBorder
                                else coralColor
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !checkEnabled(Constants.ActionType.TELE_INTAKE_STATION)) disabledBackground
                                else coralColor
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if ((orientation && allianceColor == AllianceColor.RED) || ((allianceColor == AllianceColor.BLUE) && !orientation)) {
                            Modifier.offset(50.dp, -20.dp)
                        } else {
                            Modifier.offset(-50.dp, 20.dp)
                        }) {
                            Text(
                                text = "INTAKE \nSTATION: ${actionMap["Intake Station"]}",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }

                    // INTAKE GROUND BUTTON
                    Box(
                        modifier = Modifier.intakeButtonModifier(
                            actionType = Constants.ActionType.TELE_INTAKE_GROUND,
                            borderColor = groundColor,
                            backgroundColor = groundColor,
                            width = maxWidth / 2,
                            height = maxHeight,
                            offsetX = maxWidth / 2,
                            offsetY = 0.dp
                        )
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 2000 else 1 * maxWidth / -2000,
                                if (allianceColor == AllianceColor.BLUE) 0 * maxHeight / 4 else 0 * maxHeight / 4,
                            ),

                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(
                                    if ((orientation && allianceColor == AllianceColor.RED) || ((allianceColor == AllianceColor.BLUE) && !orientation)) {
                                        (-50).dp
                                    } else {
                                        50.dp
                                    }
                                )
                                .padding(40.dp),
                        ) {
                            Text(
                                text = " INTAKE \n GROUND: ${actionMap["Intake Ground"]}",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                    // INTAKE REEF
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val hexagonSize = screenWidth * 0.4f

                    Box(
                        modifier = Modifier.offset(
                            if (allianceColor == AllianceColor.BLUE) 12.5 * maxWidth / 50 else 12.5 * maxWidth / -50,
                            if (allianceColor == AllianceColor.BLUE) 11 * maxHeight / 80 else 10 * maxHeight / 80,
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(HexagonShape())
                                .size(hexagonSize)
                                .border(
                                    4.dp,
                                    if (collectionObjectiveActivity.isIncap || !checkEnabled(
                                            Constants.ActionType.TELE_INTAKE_REEF
                                        ) || (actionMap["Intake Reef"] ?: 0) >= 6
                                    ) disabledBorder
                                    else {
                                        if (allianceColor == AllianceColor.RED) {
                                            Color.Red.copy(alpha = 0.9f)
                                        } else {
                                            Color.Blue.copy(alpha = 0.9f)
                                        }
                                    },
                                    shape = HexagonShape()
                                )
                                .background(
                                    if (collectionObjectiveActivity.isIncap || !checkEnabled(
                                            Constants.ActionType.TELE_INTAKE_REEF
                                        ) || (actionMap["Intake Reef"] ?: 0) >= 6
                                    ) disabledBackground
                                    else {
                                        if (allianceColor == AllianceColor.RED) {
                                            Color.Red.copy(alpha = 0.7f)
                                        } else {
                                            Color.Blue.copy(alpha = 0.7f)
                                        }
                                    }
                                )
                                .clickable {
                                    if ((actionMap["Intake Reef"] ?: 0) < 6) {
                                        intakeButtonPress(actionType = Constants.ActionType.TELE_INTAKE_REEF)
                                    }
                                }
                                .rotate(if (orientation) 0f else 180f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = " INTAKE \n REEF: ${actionMap["Intake Reef"]}",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    /*
    Returns a modifier for the intake buttons that sets the
    position, dimensions, color, and click event depending
    on the given action type, alliance color, and if they
    are incap or not.
    Refer to the comment on intakeButtonPress for more details
    on the click event.
     */
    @SuppressLint("ModifierFactoryUnreferencedReceiver")
    private fun Modifier.intakeButtonModifier(
        actionType: Constants.ActionType,
        borderColor: Color,
        backgroundColor: Color,
        width: Dp,
        height: Dp,
        offsetX: Dp,
        offsetY: Dp
    ): Modifier {
        return size(width, height)
            .offset(
                if (allianceColor == AllianceColor.BLUE) offsetX else -offsetX, offsetY
            )
            .clickable { intakeButtonPress(actionType = actionType); scoring = true }
            .border(
                4.dp,
                if (actionType == Constants.ActionType.TELE_INTAKE_REEF || actionType == Constants.ActionType.TELE_INTAKE_STATION || collectionObjectiveActivity.isIncap || !checkEnabled(
                        actionType
                    )
                ) disabledBorder
                else borderColor.copy(alpha = 0.6f)
            )
            .background(
                if (actionType == Constants.ActionType.TELE_INTAKE_REEF || actionType == Constants.ActionType.TELE_INTAKE_STATION || collectionObjectiveActivity.isIncap || !checkEnabled(
                        actionType
                    )
                ) disabledBackground
                else backgroundColor.copy(alpha = 0.6f)
            )
            .rotate(if (orientation) 0f else 180f)
    }

    /*
    When an intake button is clicked, it calls this function.
    If they are not incap then:
    Adds the given action to the timeline, adds to the count for that action,
    then switches to scoring and enables buttons.
     */
    private fun intakeButtonPress(actionType: Constants.ActionType) {
        // if non incap
        if (!collectionObjectiveActivity.isIncap && checkEnabled(actionType)) {
            val newPressTime = System.currentTimeMillis()
            // if clicked outside of time frame of another button press
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (matchTimer != null) {
                    when (actionType) {
                        Constants.ActionType.TELE_INTAKE_STATION -> updateActionCount(
                            "Intake Station", 1
                        )
                        Constants.ActionType.TELE_INTAKE_REEF -> updateActionCount("Intake Reef", 1)
                        else -> updateActionCount("Intake Ground", 1)
                    }
                    collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                    scoring = !scoring
                    collectionObjectiveActivity.enableButtons()
                }
            }
        }
    }


    /*
    When a scoring button is clicked, it calls this function.
    If they are not incap and if either they aren't failing or they are
    pressing the speaker, or amp button, then:
    Adds the given action to the timeline, adds to the count for that action
    if they are not failing the score, then switches to intaking and
    enables buttons.
     */
    private fun scoringButtonPress(actionType: Constants.ActionType) {
        if (!collectionObjectiveActivity.isIncap && checkEnabled(actionType)) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (matchTimer != null) {
                    collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                    if (!collectionObjectiveActivity.failing) {
                        when (actionType) {
                            Constants.ActionType.TELE_CORAL_L1 -> updateActionCount(
                                "Tele Coral L1", 1
                            )

                            Constants.ActionType.TELE_CORAL_L2 -> updateActionCount(
                                "Tele Coral L2", 1
                            )

                            Constants.ActionType.TELE_CORAL_L3 -> updateActionCount(
                                "Tele Coral L3", 1
                            )

                            Constants.ActionType.TELE_CORAL_L4 -> updateActionCount(
                                "Tele Coral L4", 1
                            )

                            Constants.ActionType.TELE_PROCESSOR -> updateActionCount(
                                "Tele Processor", 1
                            )

                            Constants.ActionType.TELE_DROP_ALGAE -> updateActionCount(
                                "Tele Drop Algae", 1
                            )

                            Constants.ActionType.TELE_DROP_CORAL -> updateActionCount(
                                "Tele Drop Coral", 1
                            )

                            else -> updateActionCount("Tele Net", 1)
                        }
                    }
                    collectionObjectiveActivity.failing = false
                    scoring = !scoring
                    collectionObjectiveActivity.enableButtons()
                }
            }
        }
    }
}
