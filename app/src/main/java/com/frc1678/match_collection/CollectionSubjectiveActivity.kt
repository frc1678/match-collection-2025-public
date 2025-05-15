// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.SwitchDefaults
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Screens.COLLECTION_SUBJECTIVE
import com.frc1678.match_collection.theme.blueAllianceColor
import com.frc1678.match_collection.theme.redAllianceColor


/**
 * Activity for Subjective Match Collection to scout the subjective gameplay of an alliance team in a match.
 */
class CollectionSubjectiveActivity : CollectionActivity() {

    private lateinit var teamNumberOne: String
    private lateinit var teamNumberTwo: String
    private lateinit var teamNumberThree: String

    /**
     * Finds the teams that are playing in that match
     */
    private fun getExtras() {
        teamNumberOne = intent.extras?.getString("team_one").toString()
        teamNumberTwo = intent.extras?.getString("team_two").toString()
        teamNumberThree = intent.extras?.getString("team_three").toString()
    }

    // Begin intent used in onKeyLongPress to restart app from StartingGamePieceActivity.kt.
    private fun intentToMatchInput() {
        startActivity(
            Intent(this, MatchInformationInputActivity::class.java).putExtras(intent)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_SUBJECTIVE),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun branded(team: String) : String {
        if (team.contains("1678")) {
            return team + "\uD83C\uDF4B\u200D\uD83D\uDFE9"
        } else {return team}
    }

    // Restart app from StartingGamePieceActivity.kt when back button is long pressed.
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setMessage(R.string.error_back_reset)
                    .setPositiveButton("Yes") { _, _ -> intentToMatchInput() }
                    .show()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubjectiveTeams()
        }
        getExtras()
    }

    @Composable
    fun SubjectiveTeams(
        context: Context = LocalContext.current
    ) {
        //Sets variables so they have default values and are remembered when returning from the match edit screen
        var teamOneAgility by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_agility")?.takeUnless { it == 0 } ?: 2)
        }
        var teamTwoAgility by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_agility")?.takeUnless { it == 0 } ?: 2)
        }
        var teamThreeAgility by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_agility")?.takeUnless { it == 0 } ?: 2)
        }
        var teamOneFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamTwoFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamThreeFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamOneTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_time_left_to_climb")
            )
        }
        var teamTwoTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_time_left_to_climb")
            )
        }
        var teamThreeTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_time_left_to_climb")
            )
        }
        var teamOneDied by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_one_died") ?: false
            )
        }
        var teamTwoDied by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_two_died") ?: false
            )
        }
        var teamThreeDied by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_three_died") ?: false
            )
        }
        var teamOneCanCrossBarge by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_one_can_cross_barge") ?: false
            )
        }
        var teamTwoCanCrossBarge by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_two_can_cross_barge") ?: false
            )
        }
        var teamThreeCanCrossBarge by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_three_can_cross_barge") ?: false
            )
        }
        var teamOneWasTippy by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_one_was_tippy") ?: false
            )
        }
        var teamTwoWasTippy by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_two_was_tippy") ?: false
            )
        }
        var teamThreeWasTippy by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_three_was_tippy") ?: false
            )
        }
        var teamOneHPFromTeam by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_one_hp_from_team") ?: false
            )
        }
        var teamTwoHPFromTeam by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_two_hp_from_team") ?: false
            )
        }
        var teamThreeHPFromTeam by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_three_hp_from_team") ?: false
            )
        }
        // Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val allianceTextColor = if (allianceColor == Constants.AllianceColor.BLUE) blueAllianceColor
                else redAllianceColor
                // Team numbers
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "",
                        color = allianceTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = branded(teamNumberOne),
                        color = allianceTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = branded(teamNumberTwo),
                        color = allianceTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = branded(teamNumberThree),
                        color = allianceTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(10.dp))
                }
                // Died checkboxes
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Died")
                    SwitchButton(
                        setSwitchState = { teamOneDied = it },
                        switchState = teamOneDied
                    )
                    SwitchButton(
                        setSwitchState = { teamTwoDied = it },
                        switchState = teamTwoDied
                    )
                    SwitchButton(
                        setSwitchState = { teamThreeDied = it },
                        switchState = teamThreeDied
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(10.dp))
                }
                // Can cross barge checkboxes
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Can Cross")
                    SwitchButton(
                        setSwitchState = { teamOneCanCrossBarge = it },
                        switchState = teamOneCanCrossBarge
                    )
                    SwitchButton(
                        setSwitchState = { teamTwoCanCrossBarge = it },
                        switchState = teamTwoCanCrossBarge
                    )
                    SwitchButton(
                        setSwitchState = { teamThreeCanCrossBarge = it },
                        switchState = teamThreeCanCrossBarge
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(15.dp))
                }
                // Tippiness checkboxes
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tippy")
                    SwitchButton(
                        setSwitchState = { teamOneWasTippy = it },
                        switchState = teamOneWasTippy
                    )
                    SwitchButton(
                        setSwitchState = { teamTwoWasTippy = it },
                        switchState = teamTwoWasTippy
                    )
                    SwitchButton(
                        setSwitchState = { teamThreeWasTippy = it },
                        switchState = teamThreeWasTippy
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(15.dp))
                }
                // Algae retention checkboxes
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HP from Team")
                    SwitchButton(
                        setSwitchState = { teamOneHPFromTeam = it },
                        switchState = teamOneHPFromTeam
                    )
                    SwitchButton(
                        setSwitchState = { teamTwoHPFromTeam = it },
                        switchState = teamTwoHPFromTeam
                    )
                    SwitchButton(
                        setSwitchState = { teamThreeHPFromTeam = it },
                        switchState = teamThreeHPFromTeam
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(15.dp))
                }
                // Secs Climb at int inputs
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Secs Climb At")
                    CounterButton(
                        setCounterValue = {
                            teamOneTimeLeftToClimb = it
                            timeLeftToClimb[0] = it
                        }, counterValue =
                        teamOneTimeLeftToClimb!!, seconds = true
                    )
                    CounterButton(
                        setCounterValue = {
                            teamTwoTimeLeftToClimb = it
                            timeLeftToClimb[1] = it
                        }, counterValue =
                        teamTwoTimeLeftToClimb!!, seconds = true
                    )
                    CounterButton(
                        setCounterValue = {
                            teamThreeTimeLeftToClimb = it
                            timeLeftToClimb[2] = it
                        }, counterValue =
                        teamThreeTimeLeftToClimb!!, seconds = true
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(15.dp))
                }
                // Agility inputs
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Agility")
                    CounterButton(
                        setCounterValue = { teamOneAgility = it }, counterValue =
                        teamOneAgility
                    )
                    CounterButton(
                        setCounterValue = { teamTwoAgility = it }, counterValue =
                        teamTwoAgility
                    )
                    CounterButton(
                        setCounterValue = { teamThreeAgility = it }, counterValue =
                        teamThreeAgility
                    )
                } }
                item {
                    Spacer(modifier = Modifier.size(10.dp))
                }
                //Field Awareness inputs
                item { Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Field Awareness")
                    CounterButton(
                        setCounterValue = { teamOneFieldAwareness = it }, counterValue =
                        teamOneFieldAwareness
                    )
                    CounterButton(
                        setCounterValue = { teamTwoFieldAwareness = it }, counterValue =
                        teamTwoFieldAwareness
                    )
                    CounterButton(
                        setCounterValue = { teamThreeFieldAwareness = it }, counterValue =
                        teamThreeFieldAwareness
                    )
                } }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeightIn(100.dp)
                    .padding(bottom = 25.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                //Proceed button
                Button(
                    onClick = {
                        val aStopStateList =
                            listOf(teamOneDied, teamTwoDied, teamThreeDied)
                        val canCrossBargeStateList =
                            listOf(teamOneCanCrossBarge, teamTwoCanCrossBarge, teamThreeCanCrossBarge)
                        val wasTippyStateList =
                            listOf(teamOneWasTippy, teamTwoWasTippy, teamThreeWasTippy)
                        val hpFromTeamStateList =
                            listOf(teamOneHPFromTeam, teamTwoHPFromTeam, teamThreeHPFromTeam)
                        val teamList = listOf(teamNumberOne, teamNumberTwo, teamNumberThree)

                        //run through the lists of switch states and add corresponding team to list if datapoint is true
                        for (i in 0..2) {
                            if (aStopStateList[i]) {
                                aStop.add(teamList[i])
                            }
                        }
                        for (i in 0..2) {
                            if (canCrossBargeStateList[i]) {
                                canCrossBarge.add(teamList[i])
                            }
                        }
                        for (i in 0..2) {
                            if (wasTippyStateList[i]) {
                                wasTippy.add(teamList[i])
                            }
                        }
                        for (i in 0..2) {
                            if (hpFromTeamStateList[i]) {
                                hpFromTeam.add(teamList[i])
                            }
                        }

                        //stores datapoints as team ranks
                        agilityScore = SubjectiveTeamRankings(
                            TeamRank(teamNumberOne, teamOneAgility),
                            TeamRank(teamNumberTwo, teamTwoAgility),
                            TeamRank(teamNumberThree, teamThreeAgility)
                        )

                        fieldAwarenessScore = SubjectiveTeamRankings(
                            TeamRank(teamNumberOne, teamOneFieldAwareness),
                            TeamRank(teamNumberTwo, teamTwoFieldAwareness),
                            TeamRank(teamNumberThree, teamThreeFieldAwareness)
                        )

                        val intent = Intent(context, MatchInformationEditActivity::class.java)
                            .putExtra(PREVIOUS_SCREEN, COLLECTION_SUBJECTIVE)
                            .putExtra("team_one", teamNumberOne)
                            .putExtra("team_two", teamNumberTwo)
                            .putExtra("team_three", teamNumberThree)
                            .putExtra("team_one_agility", teamOneAgility)
                            .putExtra("team_two_agility", teamTwoAgility)
                            .putExtra("team_three_agility", teamThreeAgility)
                            .putExtra("team_one_time_left_to_climb", teamOneTimeLeftToClimb)
                            .putExtra("team_two_time_left_to_climb", teamTwoTimeLeftToClimb)
                            .putExtra("team_three_time_left_to_climb", teamThreeTimeLeftToClimb)
                            .putExtra("team_one_field_awareness", teamOneFieldAwareness)
                            .putExtra("team_two_field_awareness", teamTwoFieldAwareness)
                            .putExtra("team_three_field_awareness", teamThreeFieldAwareness)
                            .putExtra("team_one_died", teamOneDied)
                            .putExtra("team_two_died", teamTwoDied)
                            .putExtra("team_three_died", teamThreeDied)
                            .putExtra("team_one_can_cross_barge", teamOneCanCrossBarge)
                            .putExtra("team_two_can_cross_barge", teamTwoCanCrossBarge)
                            .putExtra("team_three_can_cross_barge", teamThreeCanCrossBarge)
                            .putExtra("team_one_was_tippy", teamOneWasTippy)
                            .putExtra("team_two_was_tippy", teamTwoWasTippy)
                            .putExtra("team_three_was_tippy", teamThreeWasTippy)
                            .putExtra("team_one_hp_from_team", teamOneHPFromTeam)
                            .putExtra("team_two_hp_from_team", teamTwoHPFromTeam)
                            .putExtra("team_three_hp_from_team", teamThreeHPFromTeam)

                        //Alert dialog if duplicate rankings
                        if (agilityScore.hasDuplicate() or fieldAwarenessScore.hasDuplicate()) {
                            AlertDialog.Builder(context)
                                .setMessage("Only proceed if the robots were dead for the match. Otherwise, close. ")
                                .setNegativeButton("Close") { dialog, _ -> }
                                .setPositiveButton("Proceed") { _, _ ->
                                    startActivity(intent)
                                }.show()
                        } else {
                            startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .requiredHeight(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                ) {
                    Text("Proceed")
                }
            }
        }
    }

    @Composable
    private fun CounterButton(
        setCounterValue: (Int) -> Unit,
        counterValue: Int,
        seconds: Boolean = false
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (seconds) {
                        if (counterValue > 0) {
                            setCounterValue(counterValue - 10)
                        }
                    } else {
                        if (counterValue > 1) {
                            setCounterValue(counterValue - 1)
                        }
                    }
                },
                modifier = Modifier
                    .requiredWidth(50.dp)
                    .padding(5.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xfff4c7c3)),
            ) {
                Text("-")
            }
            Text(counterValue.toString())
            Button(
                onClick = {
                    if (seconds) {
                        if (counterValue < 60) {
                            setCounterValue(counterValue + 10)
                        }
                    } else {
                        if (counterValue < 3) {
                            setCounterValue(counterValue + 1)
                        }
                    }
                },
                modifier = Modifier
                    .requiredWidth(50.dp)
                    .padding(5.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xffb7e1cd)),
            ) {
                Text("+")
            }
        }
    }

    // The yes/no switches, they swap back and forth between checked or not every time you hit them
    @Composable
    fun SwitchButton(setSwitchState: (Boolean) -> Unit, switchState: Boolean) {
        var checked by remember { mutableStateOf(false) }
        val switchMainColor = if (allianceColor == Constants.AllianceColor.BLUE) blueAllianceColor.copy(alpha = 1.0f)
        else redAllianceColor.copy(alpha = 1.0f)
        val switchTrackColor = if (allianceColor == Constants.AllianceColor.BLUE) blueAllianceColor.copy(alpha = 0.3f)
        else redAllianceColor.copy(alpha = 0.3f)
        checked = switchState
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                setSwitchState(!switchState)
            },
            modifier = Modifier
                .scale(1.5f)
                .padding(5.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = switchMainColor,
                checkedTrackColor = switchTrackColor,
            )
        )
    }
}

