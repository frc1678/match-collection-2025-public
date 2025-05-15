package com.frc1678.match_collection.objective

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.Constants.CageLevel
import com.frc1678.match_collection.R
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.cageLevel
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.parked
import com.frc1678.match_collection.theme.defaultButton
import com.frc1678.match_collection.theme.cageDefaultBorder
import com.frc1678.match_collection.theme.cageFailBorder
import com.frc1678.match_collection.theme.disabledBackground
import com.frc1678.match_collection.theme.disabledBorder
import com.frc1678.match_collection.theme.cageSuccessBorder
import kotlinx.android.synthetic.main.collection_objective_endgame_fragment.view.endgame_compose_view

/**
 * [Fragment] used for showing intake buttons in [TeleopFragment]
 */
class EndgameFragment : Fragment(R.layout.collection_objective_endgame_fragment) {

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
     * This is the compose function that creates the layout for the compose view in collection_objective_endgame_fragment.
     */
    private fun setContent() {
        mainView!!.endgame_compose_view.setContent {
            /*
            This box contains all the elements that will be displayed, it is rotated based on your orientation.
            The elements within the box are aligned to the left or the right depending on the alliance color.
             */
            BoxWithConstraints(
                contentAlignment = TopStart,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                /*
                This image view is behind everything else in the box
                and displays one of two images based on your alliance color
                */
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // PARK TOGGLE BUTTON
                    /*
                                Disabled if they are onstage on any chain.
                                When clicked, toggles parked between true and false. The border and
                                background colors and text are changed accordingly. The contents
                                of the buttons are rotated depending on the orientation so that
                                they are not upside down for certain orientations.
                                 */
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .clickable {
                                if (
                                    cageLevel != CageLevel.S &&
                                    cageLevel != CageLevel.D
                                ) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) parked = !parked
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (
                                    cageLevel == CageLevel.S ||
                                    cageLevel == CageLevel.D
                                ) {
                                    disabledBorder
                                } else if (parked) cageSuccessBorder
                                else cageDefaultBorder
                            )
                            .background(
                                if (
                                    cageLevel == CageLevel.S ||
                                    cageLevel == CageLevel.D
                                ) {
                                    disabledBackground
                                } else if (parked) Color.Green.copy(alpha = 0.6f)
                                else defaultButton
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (parked) "PARKED" else "NOT PARKED",
                                style = TextStyle(fontWeight = FontWeight.Bold),
                                fontSize = 15.sp,
                                )
                        }
                    }

                    // CHAIN TOGGLE BUTTONS
                    /*
                                Disabled if they are onstage on any other chain or if they are incap.
                                When clicked, toggles the respective stage level variable from N
                                (not attempted), to O (onstage), to F (failed), then back to N.
                                The border and background colors and text are changed accordingly.
                                Also sets parked to false if switched to onstage.
                                The contents of the buttons are rotated depending on the orientation
                                so that they are not upside down for certain orientations.
                                 */
                    // Column Containing Shallow and Deep Cage
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Shallow Cage BUTTON
                        Box(
                            modifier = Modifier
                                .size ( this@BoxWithConstraints.maxWidth / 2, this@BoxWithConstraints.maxHeight / 2)
                                .weight(1f)
                                .clickable {
                                    if (
                                        cageLevel != CageLevel.D &&
                                        !collectionObjectiveActivity.isIncap
                                    ) {
                                        // A code that checks whether someone clicked buttons twice
                                        val newPressTime = System.currentTimeMillis()
                                        if (buttonPressedTime + 250 < newPressTime) {
                                            buttonPressedTime = newPressTime
                                            if (matchTimer != null) {
                                                when (cageLevel) {
                                                    CageLevel.N -> {
                                                        cageLevel = CageLevel.S
                                                        parked = false
                                                    }

                                                    CageLevel.S -> cageLevel =
                                                        CageLevel.FS

                                                    else -> cageLevel = CageLevel.N
                                                }
                                                collectionObjectiveActivity.enableButtons()
                                            }
                                        }
                                    }
                                }
                                .border(
                                    4.dp,
                                    if (
                                        cageLevel == CageLevel.D ||
                                        collectionObjectiveActivity.isIncap
                                    ) {
                                        disabledBorder
                                    } else if (cageLevel == CageLevel.S) cageSuccessBorder
                                    else if (cageLevel == CageLevel.FS) cageFailBorder
                                    else cageDefaultBorder
                                )
                                .background(
                                    if (
                                        cageLevel == CageLevel.D ||
                                        collectionObjectiveActivity.isIncap
                                    ) {
                                        disabledBackground
                                    } else if (cageLevel == CageLevel.S) Color.Green.copy(
                                        alpha = 0.6f
                                    )
                                    else if (cageLevel == CageLevel.FS) Color.Red.copy(alpha = 0.6f)
                                    else defaultButton
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
                            {
                                Text(
                                    text = when (cageLevel) {
                                        CageLevel.S -> "SHALLOW : CLIMBED"
                                        CageLevel.FS -> "SHALLOW : FAILED"
                                        else -> "SHALLOW : NOT ATTEMPTED"
                                    },
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (
                                            cageLevel == CageLevel.D ||
                                            cageLevel == CageLevel.S ||
                                            cageLevel == CageLevel.N ||
                                            cageLevel == CageLevel.FD ||
                                            collectionObjectiveActivity.isIncap
                                        ) {
                                            Color.Black
                                        } else Color.White
                                    )
                                )
                            }
                        }
                        //Deep Cage Toggle
                        Box(
                            modifier = Modifier
                                .size ( this@BoxWithConstraints.maxWidth / 2, this@BoxWithConstraints.maxHeight / 2)
                                .weight(1f)
                                .clickable {
                                    if (
                                        cageLevel != CageLevel.S &&
                                        !collectionObjectiveActivity.isIncap
                                    ) {
                                        // A code that checks whether someone clicked buttons at the same time
                                        val newPressTime = System.currentTimeMillis()
                                        if (buttonPressedTime + 250 < newPressTime) {
                                            buttonPressedTime = newPressTime
                                            if (matchTimer != null) {
                                                when (cageLevel) {
                                                    CageLevel.N -> {
                                                        cageLevel = CageLevel.D
                                                        parked = false
                                                    }

                                                    CageLevel.D -> cageLevel =
                                                        CageLevel.FD

                                                    else -> cageLevel = CageLevel.N
                                                }
                                                collectionObjectiveActivity.enableButtons()
                                            }
                                        }
                                    }
                                }
                                .border(
                                    4.dp,
                                    if (
                                        cageLevel == CageLevel.S ||
                                        collectionObjectiveActivity.isIncap
                                    ) {
                                        disabledBorder
                                    } else if (cageLevel == CageLevel.D) cageSuccessBorder
                                    else if (cageLevel == CageLevel.FD) cageFailBorder
                                    else cageDefaultBorder
                                )
                                .background(
                                    if (
                                        cageLevel == CageLevel.S ||
                                        collectionObjectiveActivity.isIncap
                                    ) {
                                        disabledBackground
                                    } else if (cageLevel == CageLevel.D) Color.Green.copy(
                                        alpha = 0.6f
                                    )
                                    else if (cageLevel == CageLevel.FD) Color.Red.copy(alpha = 0.6f)
                                    else defaultButton
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = when (cageLevel) {
                                        CageLevel.D -> "DEEP : CLIMBED"
                                        CageLevel.FD -> "DEEP : FAILED"
                                        else -> "DEEP : NOT ATTEMPTED"
                                    },
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (
                                            cageLevel == CageLevel.S ||
                                            cageLevel == CageLevel.D ||
                                            cageLevel == CageLevel.N ||
                                            cageLevel == CageLevel.FS ||
                                            collectionObjectiveActivity.isIncap
                                        ) {
                                            Color.Black
                                        } else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}