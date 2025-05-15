// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// map of datapoint to its value
var actionMap by mutableStateOf(
    mapOf(
        "Auto Processor" to 0,
        "Tele Processor" to 0,
        "Tele Coral L1" to 0,
        "Tele Coral L2" to 0,
        "Tele Coral L3" to 0,
        "Tele Coral L4" to 0,
        "Auto Net" to 0,
        "Tele Net" to 0,
        "Fail" to 0,
        "Auto Intake Mark Coral 1" to 0,
        "Auto Intake Mark Coral 2" to 0,
        "Auto Intake Mark Coral 3" to 0,
        "Auto Intake Mark Algae 1" to 0,
        "Auto Intake Mark Algae 2" to 0,
        "Auto Intake Mark Algae 3" to 0,
        "Auto Drop Algae" to 0,
        "Auto Drop Coral" to 0,
        "Tele Drop Algae" to 0,
        "Tele Drop Coral" to 0,
        "Intake Station" to 0,
        "Intake Reef" to 0,
        "Intake Ground" to 0,
        "Intake Poach" to 0,
        "Auto Intake Ground 1" to 0,
        "Auto Intake Ground 2" to 0,
        "Auto Intake Station 1" to 0,
        "Auto Intake Station 2" to 0,
    )
)

// update count of each datapoint
fun updateActionCount(key: String, amount: Int) {
    actionMap = actionMap.toMutableMap().apply {
        set(
            key,
            actionMap[key]?.plus(amount) ?: 0
        )
    }
}

// stores intake counts for all 12 intake locations
var autoIntakeList by mutableStateOf(List(size = 12) { 0 })

// tracks the location of the reef scoring to be used in access values in `autoIntakeReefActionMap`
var autoScoreReefLocation by mutableStateOf(-1)

// used to track values of the auto reef scoring
var autoScoreReefMap by mutableStateOf(
    mapOf(
        0 to mutableStateOf(listOf(0, 0, 0, 0)),
        1 to mutableStateOf(listOf(0, 0, 0, 0)),
        2 to mutableStateOf(listOf(0, 0, 0, 0)),
        3 to mutableStateOf(listOf(0, 0, 0, 0)),
        4 to mutableStateOf(listOf(0, 0, 0, 0)),
        5 to mutableStateOf(listOf(0, 0, 0, 0))
    )
)
var autoIntakeReefActionMap = mapOf(
    0 to Constants.ActionType.AUTO_INTAKE_REEF_F1,
    1 to Constants.ActionType.AUTO_INTAKE_REEF_F2,
    2 to Constants.ActionType.AUTO_INTAKE_REEF_F3,
    3 to Constants.ActionType.AUTO_INTAKE_REEF_F4,
    4 to Constants.ActionType.AUTO_INTAKE_REEF_F5,
    5 to Constants.ActionType.AUTO_INTAKE_REEF_F6
)
var autoScoreFaceReefActionMap = mapOf(
    0 to Constants.ActionType.AUTO_REEF_F1,
    1 to Constants.ActionType.AUTO_REEF_F2,
    2 to Constants.ActionType.AUTO_REEF_F3,
    3 to Constants.ActionType.AUTO_REEF_F4,
    4 to Constants.ActionType.AUTO_REEF_F5,
    5 to Constants.ActionType.AUTO_REEF_F6
)
var autoScoreLevelReefActionMap = mapOf(
    0 to Constants.ActionType.AUTO_REEF_L1,
    1 to Constants.ActionType.AUTO_REEF_L2,
    2 to Constants.ActionType.AUTO_REEF_L3,
    3 to Constants.ActionType.AUTO_REEF_L4
)

val specialActions = listOf(
    Constants.ActionType.FAIL.toString(),
    Constants.ActionType.AUTO_REEF_SUCCESS.toString(),
    Constants.ActionType.AUTO_REEF_FAIL.toString(),
    Constants.ActionType.AUTO_REEF_F1.toString(),
    Constants.ActionType.AUTO_REEF_F2.toString(),
    Constants.ActionType.AUTO_REEF_F3.toString(),
    Constants.ActionType.AUTO_REEF_F4.toString(),
    Constants.ActionType.AUTO_REEF_F5.toString(),
    Constants.ActionType.AUTO_REEF_F6.toString(),
)

// used to decrement/increment values of the `autoScoreReefMap`
var undoReefLevel = -1
var undoReefLocation = -1
var redoReefSuccess = true
var redoReefLocation = -1

var matchTimer: CountDownTimer? = null
var matchTime: String = ""
var isTeleopActivated: Boolean = false
var popupOpen = false
var isMatchTimeEnded: Boolean = false
var collectionMode: Constants.ModeSelection = Constants.ModeSelection.NONE
var assignMode: Constants.AssignmentMode = Constants.AssignmentMode.NONE

// should be a list of list of characters corresponding to pieces.
// 'C' = Coral, 'A' = Algae, 'U' = Unknown/Ambiguous
var robotInvTimeline: MutableList<Array<Char>> = mutableStateListOf()
var actionToPiece = mapOf(
    Constants.ActionType.FAIL.toString() to "",
    Constants.ActionType.AUTO_DROP_ALGAE.toString() to "-A",
    Constants.ActionType.AUTO_DROP_CORAL.toString() to "-C",
    Constants.ActionType.AUTO_INTAKE_GROUND_1.toString() to "+U",
    Constants.ActionType.AUTO_INTAKE_GROUND_2.toString() to "+U",
    Constants.ActionType.AUTO_INTAKE_MARK_1_CORAL.toString() to "+C",
    Constants.ActionType.AUTO_INTAKE_MARK_2_CORAL.toString() to "+C",
    Constants.ActionType.AUTO_INTAKE_MARK_3_CORAL.toString() to "+C",
    Constants.ActionType.AUTO_INTAKE_MARK_1_ALGAE.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_MARK_2_ALGAE.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_MARK_3_ALGAE.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F1.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F2.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F3.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F4.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F5.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_REEF_F6.toString() to "+A",
    Constants.ActionType.AUTO_INTAKE_STATION_1.toString() to "+C",
    Constants.ActionType.AUTO_INTAKE_STATION_2.toString() to "+C",
    Constants.ActionType.AUTO_REEF_FAIL.toString() to "",
    Constants.ActionType.AUTO_REEF_SUCCESS.toString() to "",
    Constants.ActionType.AUTO_REEF_F1.toString() to "-C",
    Constants.ActionType.AUTO_REEF_F2.toString() to "-C",
    Constants.ActionType.AUTO_REEF_F3.toString() to "-C",
    Constants.ActionType.AUTO_REEF_F4.toString() to "-C",
    Constants.ActionType.AUTO_REEF_F5.toString() to "-C",
    Constants.ActionType.AUTO_REEF_F6.toString() to "-C",
    Constants.ActionType.AUTO_REEF_L1.toString() to "",
    Constants.ActionType.AUTO_REEF_L2.toString() to "",
    Constants.ActionType.AUTO_REEF_L3.toString() to "",
    Constants.ActionType.AUTO_REEF_L4.toString() to "",
    Constants.ActionType.AUTO_NET.toString() to "-A",
    Constants.ActionType.AUTO_PROCESSOR.toString() to "-A",
    Constants.ActionType.TELE_DROP_ALGAE.toString() to "-A",
    Constants.ActionType.TELE_DROP_CORAL.toString() to "-C",
    Constants.ActionType.TELE_NET.toString() to "-A",
    Constants.ActionType.TELE_PROCESSOR.toString() to "-A",
    Constants.ActionType.TELE_CORAL_L1.toString() to "-C",
    Constants.ActionType.TELE_CORAL_L2.toString() to "-C",
    Constants.ActionType.TELE_CORAL_L3.toString() to "-C",
    Constants.ActionType.TELE_CORAL_L4.toString() to "-C",
    Constants.ActionType.TELE_INTAKE_GROUND.toString() to "+U",
    Constants.ActionType.TELE_INTAKE_REEF.toString() to "+A",
    Constants.ActionType.TELE_INTAKE_STATION.toString() to "+C",
    Constants.ActionType.TELE_INTAKE_POACH.toString() to "+U",
    Constants.ActionType.START_INCAP_TIME.toString() to "",
    Constants.ActionType.END_INCAP_TIME.toString() to "",
    Constants.ActionType.TO_TELEOP.toString() to "",
    Constants.ActionType.TO_ENDGAME.toString() to ""
)

/** Checks if a certain action type is legal with the current inventory state */
fun checkEnabled(actionType: Constants.ActionType): Boolean {
    val action = actionToPiece.get(actionType.toString())
    if (action == "") {
        return true
    }
    // if no preload (so empty robotInvTimeline, only activate buttons that are intakes
    if (robotInvTimeline.isEmpty()) {
        return !(action == "-A" || action == "-C")
    }
    // Check different impossible cases
    // Can't intake a coral if you already have a coral
    if (action == "+C" && robotInvTimeline.get(robotInvTimeline.size - 1)
            .contains('C')
    ) return false
    // Can't intake an algae when you already have an algae
    if (action == "+A" && robotInvTimeline.get(robotInvTimeline.size - 1)
            .contains('A')
    ) return false
    // Can't intake a piece (unknown) if you already have 2 pieces
    if (action == "+U" && robotInvTimeline.get(robotInvTimeline.size - 1).size >= 2) return false
    // Can't score/lose a coral if you don't have a coral (unknown piece also works, we'll assume the piece is a coral if you score coral)
    if (action == "-C" && !(robotInvTimeline.get(robotInvTimeline.size - 1)
            .contains('C') || robotInvTimeline.get(robotInvTimeline.size - 1).contains('U'))
    ) return false
    // Can't score/lose an algae if you don't have an algae (unknown piece also works, we'll assume the piece is an algae if you score algae)
    if (action == "-A" && !(robotInvTimeline.get(robotInvTimeline.size - 1)
            .contains('A') || robotInvTimeline.get(robotInvTimeline.size - 1).contains('U'))
    ) return false
    return true
}

var cageLevel by mutableStateOf(Constants.CageLevel.N)
var parked by mutableStateOf(false)
var scoring by mutableStateOf(false)

// Keeps track of what time the last button press was
// Used to add a cooldown for how often you can press buttons, any
// presses within 250 milliseconds of a button press will be ignored
var buttonPressedTime = System.currentTimeMillis()

// Data that is shared between the objective and subjective QRs.
var matchNumber: Int = 1
var allianceColor: Constants.AllianceColor = Constants.AllianceColor.NONE
var timestamp: Long = 0
var scoutName: String = Constants.NONE_VALUE

// Data specific to Objective Match Collection QR.
var teamNumber: String = ""
var scoutId: String = Constants.NONE_VALUE
var orientation by mutableStateOf(true)
var startingPosition: Int? by mutableStateOf(null)
var preloaded by mutableStateOf(false)
var timeline = mutableListOf<Map<String, String>>()

// Data specific to Subjective Match Collection QR.
var agilityScore: SubjectiveTeamRankings = SubjectiveTeamRankings()
var fieldAwarenessScore: SubjectiveTeamRankings = SubjectiveTeamRankings()
var timeLeftToClimb: MutableList<Int> = MutableList(3) { 0 }
var canCrossBarge: ArrayList<String> = ArrayList()
var hpFromTeam: ArrayList<String> = ArrayList()
var wasTippy: ArrayList<String> = ArrayList()
var aStop: ArrayList<String> = ArrayList()

// Function to reset References.kt variables for new match.
fun resetCollectionReferences() {
    actionMap = actionMap.toMutableMap().apply {
        forEach {
            set(it.key, 0)
        }
    }

    autoIntakeList = autoIntakeList.toMutableList().apply {
        indices.forEach {
            set(it, 0)
        }
    }

    autoScoreReefMap = autoScoreReefMap.toMutableMap().apply {
        forEach {
            set(it.key, mutableStateOf(listOf(0, 0, 0, 0)))
        }
    }

    autoScoreReefLocation = -1

    isTeleopActivated = false

    popupOpen = false


    cageLevel = Constants.CageLevel.N
    parked = false
    timestamp = 0

    timeline = ArrayList()

    agilityScore = SubjectiveTeamRankings()
    fieldAwarenessScore = SubjectiveTeamRankings()
    timeLeftToClimb = MutableList(3) { 0 }
}

data class SubjectiveTeamRankings(
    val teamOne: TeamRank? = null, val teamTwo: TeamRank? = null, val teamThree: TeamRank? = null
) {
    private val list: List<TeamRank?>
        get() = listOf(teamOne, teamTwo, teamThree)

    val notNullList: List<TeamRank>
        get() = this.list.filterNotNull()


    fun hasDuplicate(): Boolean {
        val ranks = mutableListOf<Int>()
        for (team in this.notNullList) {
            ranks.add(team.rank)
        }
        return ranks.toSet().toList() != ranks
    }
}

data class TeamRank(var teamNumber: String, val rank: Int)

fun resetStartingReferences() {
    startingPosition = null
    teamNumber = ""
    preloaded = false
}
