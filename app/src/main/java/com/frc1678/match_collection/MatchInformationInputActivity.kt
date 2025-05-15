// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Companion.previousScreen
import com.frc1678.match_collection.objective.StartingPositionObjectiveActivity
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.edit_match_information_activity.et_team_three
import kotlinx.android.synthetic.main.edit_match_information_activity.et_team_two
import kotlinx.android.synthetic.main.id_scout_dialog.lv_scout_id_view
import kotlinx.android.synthetic.main.match_information_input_activity_objective.btn_old_QRs
import kotlinx.android.synthetic.main.match_information_input_activity_objective.btn_proceed_match_start
import kotlinx.android.synthetic.main.match_information_input_activity_objective.btn_scout_id
import kotlinx.android.synthetic.main.match_information_input_activity_objective.et_match_number
import kotlinx.android.synthetic.main.match_information_input_activity_objective.et_team_one
import kotlinx.android.synthetic.main.match_information_input_activity_objective.left_toggle_button
import kotlinx.android.synthetic.main.match_information_input_activity_objective.right_toggle_button
import kotlinx.android.synthetic.main.match_information_input_activity_objective.spinner_assign_mode
import kotlinx.android.synthetic.main.match_information_input_activity_objective.spinner_scout_name
import kotlinx.android.synthetic.main.match_information_input_activity_objective.tv_version_number
import kotlinx.android.synthetic.main.old_qrs_popup.view.iv_old_qr_exit
import kotlinx.android.synthetic.main.old_qrs_popup.view.lv_old_qrs
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.lang.Integer.parseInt

// Activity to input the match information before the start of the match.
class MatchInformationInputActivity : MatchInformationActivity() {
    private var leftToggleButtonColor: Int = 0
    private var rightToggleButtonColor: Int = 0
    private var leftToggleButtonColorDark: Int = 0
    private var rightToggleButtonColorDark: Int = 0

    private lateinit var leftToggleButton: Button
    private lateinit var rightToggleButton: Button

    private var scheduleAlertAlreadySent: Boolean = false
    private var idAlertAlreadySent: Boolean = false

    /** Static storage of the match schedule to avoid retrieving from storage multiple times.
     * Keep in mind that any changes made to the file will require a restart of the app or proceeding
     * to the next match for changes to apply. */
    object MatchSchedule {

        /** The contents of the match schedule. An example of a match schedule can be found
         * [here](https://github.com/frc1678/Cardinal/blob/main/cardinal/api/hardcoded_test_data/match_schedule.json). */
        var contents: JsonObject? = null

        private val file =
            File("/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/match_schedule.json")

        /** Initializes [`contents`][contents] with the JSON data from the [file]. This should only be called once.
         * After calling this, use `MatchSchedule.contents` to access the data.
         * @return Whether the reading was successful. */
        fun read(): Boolean {
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                return false
            }
            return true
        }

        /** Whether the match schedule file exists. */
        val fileExists: Boolean
            get() = file.exists()
    }

    companion object {
        /*
        * Adjustments for scout assignment methods
        * Currently it prioritizes 1 scout per robot then 3 scouts per robot
        * e.g. if there is 7 scouts it will assign like (2, 1, 1, 1, 1, 1)
        * */
        val OFFSEASON_SCOUT_ASSIGNMENTS = listOf(
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F"
        )

        val COMPETITION_SCOUT_ASSIGNMENTS = listOf(
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F"
        )

    }

    /** The [List] containing the random orderings of the scouts.
     * This is a list of lists of different combinations of A-F denoting the orders.
     * If this object is being accessed for the first time, the file
     * will be read from the raw resources.
     * @see R.raw.scout_orders */
    private val scoutOrders: List<List<String>> by lazy {
        JsonParser.parseReader(
            InputStreamReader(resources.openRawResource(R.raw.scout_orders))
        ).asJsonArray.map { scoutOrdersList ->
            scoutOrdersList.asJsonArray.map { scoutOrderList ->
                scoutOrderList.asJsonPrimitive.asString
            }
        }
    }

    /** Fetches the new robot assignment given the match number and scout ID.
     * @return The assigned robot index in the match, from 0 to 5. */
    private fun getNewScoutAssignment(matchNumber: String, scoutID: Int): Int? {
        // Used for randomization. Should be changed for offseason competitions
        val letter = OFFSEASON_SCOUT_ASSIGNMENTS[scoutID - 1]
        if (matchNumber.isEmpty()) return null
        if (matchNumber.toInt() > scoutOrders.size) return null
        // The if statement makes uses a different random scout order for the scout ids past 6
        val matchOrder = if (scoutID > 6) {
            scoutOrders[matchNumber.toInt() - 1]
        } else {
            scoutOrders.asReversed()[matchNumber.toInt() - 1]
        }
        // Find non-random scout letter in random scout order list
        return matchOrder.indexOf(letter)
    }

    /** Assign team number and alliance color for Objective Scout based on the team index given by
     * [`getNewScoutAssignment()`][getNewScoutAssignment]. */
    private fun assignTeamObjective(
        teamInput: EditText,
        teamIndex: Int?,
        matchNumber: String
    ) {
        val team = MatchSchedule.contents!!
            .getAsJsonObject(matchNumber)
            ?.getAsJsonArray("teams")
            ?.get(teamIndex ?: return)?.asJsonObject
            ?: return
        teamInput.setText(team.get("number")!!.asString)
        allianceColor =
            if (team.get("color")?.asString == "red") {
                switchBorderToRedToggle()
                Constants.AllianceColor.RED
            } else {
                switchBorderToBlueToggle()
                Constants.AllianceColor.BLUE
            }
    }


    // Assign team numbers for Subjective Scout based on alliance color.
    private fun assignTeamsSubjective(
        teamInput: EditText, allianceColor: Constants.AllianceColor,
        matchNumber: String, iterationNumber: Int
    ) {
        val team = MatchSchedule.contents!!
            .get(matchNumber)?.asJsonObject
            ?.get("teams")?.asJsonArray
            ?.get(iterationNumber + if (allianceColor == Constants.AllianceColor.RED) 3 else 0)?.asJsonObject
            ?: return
        teamInput.setText(team.get("number").asString)
    }


    // Automatically assign team number(s) based on collection mode.
    private fun autoAssignTeamInputsGivenMatch() {
        if (assignMode == Constants.AssignmentMode.OVERRIDE) {
            if (previousScreen == Constants.Screens.STARTING_POSITION_OBJECTIVE ||
                previousScreen == Constants.Screens.COLLECTION_SUBJECTIVE ||
                (previousScreen == Constants.Screens.MATCH_INFORMATION_INPUT && intent.extras?.getBoolean(
                    "old_qr"
                ) == true)
            ) {
                if (previousScreen == Constants.Screens.MATCH_INFORMATION_INPUT) {
                    et_match_number.setText(intent.extras?.getString("match_num").toString())
                }
                if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                    et_team_one.setText(intent.extras?.getString("team_one").toString())
                    et_team_two.setText(intent.extras?.getString("team_two").toString())
                    et_team_three.setText(intent.extras?.getString("team_three").toString())
                } else {
                    et_team_one.setText(intent.extras?.getString("team_one").toString())
                }
            }
            return
        }
        if (MatchSchedule.fileExists) {
            if (assignMode == Constants.AssignmentMode.AUTOMATIC_ASSIGNMENT) {
                // Assign three scouts per robot based on the scout order list in Objective
                // Match Collection.
                if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
                    if (scoutId.isNotEmpty() and (scoutId != (Constants.NONE_VALUE))) {
                        assignTeamObjective(
                            teamInput = et_team_one,
                            teamIndex = getNewScoutAssignment(
                                matchNumber = et_match_number.text.toString(),
                                scoutID = scoutId.toInt()
                            ),
                            matchNumber = et_match_number.text.toString()
                        )
                    } else {
                        if (!idAlertAlreadySent) {
                            AlertDialog.Builder(this).setMessage(R.string.error_id_missing).show()
                            idAlertAlreadySent = true
                        }
                    }
                } else {
                    // Assign an alliance to a scout based on alliance color in Subjective Match
                    // Collection.
                    var iterationNumber = 0
                    listOf<EditText>(et_team_one, et_team_two, et_team_three).forEach {
                        assignTeamsSubjective(
                            teamInput = it,
                            allianceColor = allianceColor,
                            matchNumber = et_match_number.text.toString(),
                            iterationNumber = iterationNumber
                        )
                        iterationNumber++
                    }

                }
            }
        } else {
            if (assignMode == Constants.AssignmentMode.AUTOMATIC_ASSIGNMENT && !scheduleAlertAlreadySent) {
                AlertDialog.Builder(this).setMessage(R.string.error_file_missing).show()
                scheduleAlertAlreadySent = true
            }
        }
    }

    // Assign team number based on collection mode when match number is changed.
    private fun initMatchNumberTextChangeListener() {
        et_match_number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (checkInputNotEmpty(et_match_number) && et_match_number.text.toString()
                        .toInt() != 0
                ) {
                    if (et_match_number.text.toString() != "") {
                        if (MatchSchedule.fileExists) {
                            if (parseInt(et_match_number.text.toString()) > MatchSchedule.contents!!.keySet()!!.size) {
                                if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                                    et_team_one.setText("")
                                    et_team_two.setText("")
                                    et_team_three.setText("")
                                } else {
                                    et_team_one.setText("")
                                }
                            } else {
                                if (assignMode != Constants.AssignmentMode.OVERRIDE) {
                                    autoAssignTeamInputsGivenMatch()
                                }
                                matchNumber = parseInt(et_match_number.text.toString())
                            }
                        }
                    }
                } else {
                    if (assignMode != Constants.AssignmentMode.OVERRIDE) {
                        if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                            et_team_one.setText("")
                            et_team_two.setText("")
                            et_team_three.setText("")
                        } else {
                            et_team_one.setText("")
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    // Only lets the user type in numbers and uppercase letters
    private fun initTeamNumberTextChangeListeners() {
        val regex = "[^A-Z0-9]".toRegex()
        et_team_one.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (checkInputNotEmpty(et_team_one)) {
                    if (s.toString().contains(regex)) {
                        val tempString: String = et_team_one.text.toString()
                        et_team_one.setText(regex.replace(tempString, ""))
                    }
                }
            }
        })
        if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
            et_team_two.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (checkInputNotEmpty(et_team_two)) {
                        if (s.toString().contains(regex)) {
                            val tempString: String = et_team_two.text.toString()
                            et_team_two.setText(regex.replace(tempString, ""))
                        }
                    }
                }
            })
            et_team_three.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (checkInputNotEmpty(et_team_three)) {
                        if (s.toString().contains(regex)) {
                            val tempString: String = et_team_three.text.toString()
                            et_team_three.setText(regex.replace(tempString, ""))
                        }
                    }
                }
            })
        }
    }

    // Create an alliance color toggle button given its specifications.
    private fun createToggleButton(
        isBordered: Boolean, toggleButton: Button,
        toggleButtonColor: Int, toggleButtonColorDark: Int
    ) {
        val backgroundDrawable = GradientDrawable()

        if (isBordered) {
            backgroundDrawable.setStroke(10, toggleButtonColorDark)
        }

        backgroundDrawable.setColor(toggleButtonColor)
        backgroundDrawable.cornerRadius = 10f
        toggleButton.background = backgroundDrawable
    }

    // Update alliance color toggle button with unselected backgrounds.
    private fun resetBackground() {
        // Create the unselected alliance color left toggle.
        createToggleButton(
            isBordered = false,
            toggleButton = leftToggleButton,
            toggleButtonColor = leftToggleButtonColor,
            toggleButtonColorDark = leftToggleButtonColorDark
        )

        // Create the unselected alliance color right toggle.
        createToggleButton(
            isBordered = false,
            toggleButton = rightToggleButton,
            toggleButtonColor = rightToggleButtonColor,
            toggleButtonColorDark = rightToggleButtonColorDark
        )
    }

    // Apply border to red alliance toggle when red alliance selected.
    private fun switchBorderToRedToggle() {
        resetBackground()

        // Create selected red toggle.
        createToggleButton(
            isBordered = true,
            toggleButton = rightToggleButton,
            toggleButtonColor = rightToggleButtonColor,
            toggleButtonColorDark = rightToggleButtonColorDark
        )
    }

    // Apply border to blue alliance toggle when blue alliance is selected.
    private fun switchBorderToBlueToggle() {
        resetBackground()

        // Create selected blue toggle.
        createToggleButton(
            isBordered = true,
            toggleButton = leftToggleButton,
            toggleButtonColor = leftToggleButtonColor,
            toggleButtonColorDark = leftToggleButtonColorDark
        )
    }

    // Initialize alliance toggle button onClickListeners.
    private fun initToggleButtons() {
        rightToggleButtonColor = ContextCompat.getColor(this, R.color.alliance_red_light)
        leftToggleButtonColor = ContextCompat.getColor(this, R.color.alliance_blue_light)
        rightToggleButtonColorDark = ContextCompat.getColor(this, R.color.alliance_red_dark)
        leftToggleButtonColorDark = ContextCompat.getColor(this, R.color.alliance_blue_dark)
        leftToggleButton = left_toggle_button
        rightToggleButton = right_toggle_button

        resetBackground()

        when (retrieveFromStorage(context = this, key = "alliance_color")) {
            Constants.AllianceColor.BLUE.toString(), "" -> {
                switchBorderToBlueToggle()
                allianceColor = Constants.AllianceColor.BLUE
            }

            Constants.AllianceColor.RED.toString() -> {
                switchBorderToRedToggle()
                allianceColor = Constants.AllianceColor.RED
            }
        }

        // Set onLongClickListeners to set alliance color when long clicked.
        leftToggleButton.setOnLongClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                    allianceColor = Constants.AllianceColor.BLUE
                    putIntoStorage(context = this, key = "alliance_color", value = allianceColor)
                    autoAssignTeamInputsGivenMatch()
                    switchBorderToBlueToggle()
                } else if ((collectionMode == Constants.ModeSelection.OBJECTIVE) && (assignMode == Constants.AssignmentMode.OVERRIDE)) {
                    allianceColor = Constants.AllianceColor.BLUE
                    switchBorderToBlueToggle()
                }
            }
            return@setOnLongClickListener true
        }
        rightToggleButton.setOnLongClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                    allianceColor = Constants.AllianceColor.RED
                    putIntoStorage(context = this, key = "alliance_color", value = allianceColor)
                    autoAssignTeamInputsGivenMatch()
                    switchBorderToRedToggle()
                } else if ((collectionMode == Constants.ModeSelection.OBJECTIVE) && (assignMode == Constants.AssignmentMode.OVERRIDE)) {
                    allianceColor = Constants.AllianceColor.RED
                    switchBorderToRedToggle()
                }
            }
            return@setOnLongClickListener true
        }
    }

    // Return a list of the scout IDs based on NUMBER_OF_ACTIVE_SCOUTS defined in Constants.kt.
    private fun scoutIdContentsList(): ArrayList<Any> {
        val scoutIdContents = ArrayList<Any>()
        scoutIdContents.add(Constants.NONE_VALUE)
        (1..Constants.NUMBER_OF_ACTIVE_SCOUTS).forEach { scoutIdContents.add(it) }
        return scoutIdContents
    }

    // Initialize onLongClickListener for the scout ID button to prompt for a scout ID input.
    private fun initScoutIdLongClick() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.id_scout_dialog)

        // Set scout ID spinner to previously set scout ID from internal storage.
        if (this.getSharedPreferences("PREFS", 0).contains("scout_id")) {
            btn_scout_id.text = getString(
                R.string.btn_scout_id_message,
                retrieveFromStorage(context = this, key = "scout_id")
            )
            scoutId = retrieveFromStorage(context = this, key = "scout_id")
        }

        // Opens up a spinner with scout id numbers when the scout id button is clicked
        btn_scout_id.setOnLongClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setTitle("Are you sure you want to change Scout ID?")
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }.setPositiveButton("YES") { _: DialogInterface, _: Int ->
                        dialog.show()

                    }.show()
                val adapter = ArrayAdapter(
                    this, R.layout.spinner_text_view,
                    scoutIdContentsList()
                )
                dialog.lv_scout_id_view.adapter = adapter
                dialog.lv_scout_id_view.setOnItemClickListener { _, _, position, _ ->
                    btn_scout_id.text = getString(
                        R.string.btn_scout_id_message,
                        scoutIdContentsList()[position].toString()
                    )
                    // Set scout ID and save it to internal storage.
                    scoutId = scoutIdContentsList()[position].toString()
                    putIntoStorage(context = this, key = "scout_id", value = scoutId)
                    autoAssignTeamInputsGivenMatch()
                    dialog.dismiss()
                    if (scoutId == "NONE") {
                        idAlertAlreadySent = false
                    }
                }
            }
            return@setOnLongClickListener true
        }
    }

    // When given a string it will return Constants.ModeSelection.Subjective if it is the name of a Subjective QR file,
    // Constants.ModeSelection.OBJECTIVE if it is the name of an objective QR file
    private fun differentiateSubjectiveAndObjectiveQRFileNames(file_name: String): Constants.ModeSelection {

        /* When we make the QR files in QRGenerateActivity SubjectiveQR files have 2 variables and 1 underscore between them.
         ObjectiveQR files have 3 variables and 2 underscores, we're searching for underscores because it was the
         easiest way to differentiate between them. */
        return when (file_name.filter { it == '_' }.count()) {
            1 -> Constants.ModeSelection.SUBJECTIVE
            2 -> Constants.ModeSelection.OBJECTIVE
            /* It doesn't really matter what is returned here, this just means that it is not a
             match collection QR file and shouldn't be equal to SUBJECTIVE or OBJECTIVE */
            else -> Constants.ModeSelection.NONE
        }
    }

    private fun initOldQRsLongClick() {
        val matchesPlayed = ArrayList<String>()

        btn_old_QRs.setOnLongClickListener { view ->
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                matchesPlayed.clear()

                /** This will go through the name of every file in downloads and figure out whether
                 * the name is of an objective or subjective file. If it is an objective or subjective
                 * file and you are in that collection mode it will add the match number of the QR
                 * file into matchesPlayed. */
                File("/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/").walkTopDown()
                    .forEach {
                        val name = it.nameWithoutExtension
                        Log.e("fileName", name)
                        if (name != "match_schedule") {
                            Log.e(
                                "mode", "${
                                    differentiateSubjectiveAndObjectiveQRFileNames(
                                        name
                                    )
                                }"
                            )
                            if (collectionMode == differentiateSubjectiveAndObjectiveQRFileNames(
                                    name
                                )
                            ) {
                                // Both subjective and objective QR files start with the match number and
                                // immediately after the match number have an underscore
                                matchesPlayed.add(name.substringBefore("_"))
                            }
                        }
                    }

                //This opens up the Old QR popup
                val popupView = View.inflate(this, R.layout.old_qrs_popup, null)
                val width = LinearLayout.LayoutParams.WRAP_CONTENT
                val height = LinearLayout.LayoutParams.WRAP_CONTENT
                val popupWindow = PopupWindow(popupView, width, height, false)
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, -175)
                popupOpen = true

                // Sets the list view equal to a list matchesPlayed
                val adapter = ArrayAdapter(
                    this, R.layout.old_qrs_popup_cell,
                    matchesPlayed.sortedByDescending { it.toInt() }.map { return@map "Match #$it" })
                popupView.lv_old_qrs.adapter = adapter

                // The Exit button closes the popup
                popupView.iv_old_qr_exit.setOnClickListener {
                    val newPressTimePopup = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTimePopup) {
                        buttonPressedTime = newPressTimePopup
                        popupWindow.dismiss()
                        popupOpen = false
                    }
                }

                popupView.lv_old_qrs.setOnItemClickListener { parent, _, position, _ ->
                    val newPressTimePopup = System.currentTimeMillis()
                    if (buttonPressedTime + 250 < newPressTimePopup) {
                        buttonPressedTime = newPressTimePopup
                        /* This checks every item in downloads and checks if it is the file for the selected match.
                Once found it will read that file and run QRGenerateActivity to display the QR for the file. */
                        val selectedItem =
                            parent.getItemAtPosition(position).toString().substringAfter("#")
                        File("/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/").walkTopDown()
                            .forEach {
                                val fileName = it.name
                                if (
                                    fileName.substringBefore("_") == selectedItem &&
                                    fileName.endsWith(".txt")
                                ) {
                                    val qrContents = it.readText()
                                    val intent = Intent(this, QRGenerateActivity::class.java)
                                        .putExtra(
                                            PREVIOUS_SCREEN,
                                            Constants.Screens.MATCH_INFORMATION_INPUT
                                        )
                                        .putExtra(Constants.COMPRESSED_QR_TAG, qrContents)
                                        .putExtra("match_num", et_match_number.text.toString())
                                        .putExtra("qr_match_num", selectedItem)

                                    if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                                        intent.putExtra("team_one", et_team_one.text.toString())
                                            .putExtra("team_two", et_team_two.text.toString())
                                            .putExtra("team_three", et_team_three.text.toString())
                                    } else {
                                        val teamNum =
                                            fileName.substringAfter("_").substringBefore("_")
                                        intent.putExtra("team_one", et_team_one.text.toString())
                                            .putExtra("qr_team_num", teamNum)
                                    }
                                    startActivity(intent)
                                }
                            }
                    }
                }
            }
            return@setOnLongClickListener true
        }
    }

    // Initialize the adapter and onItemSelectedListener for assignment mode input.
    private fun initAssignModeSpinner() {
        when (retrieveFromStorage(context = this, key = "assignment_mode")) {
            "0" ->
                assignMode = Constants.AssignmentMode.AUTOMATIC_ASSIGNMENT

            "1" ->
                assignMode = Constants.AssignmentMode.OVERRIDE
        }

        val adapter = ArrayAdapter(
            this, R.layout.spinner_text_view,
            arrayOf(getString(R.string.btn_assignment), getString(R.string.btn_override))
        )
        spinner_assign_mode.adapter = adapter

        // Set assignment mode spinner to previously set scout ID from internal storage.
        if (this.getSharedPreferences("PREFS", 0).contains("assignment_mode")) {
            spinner_assign_mode.setSelection(
                parseInt(
                    retrieveFromStorage(
                        context = this,
                        key = "assignment_mode"
                    )
                )
            )
        }

        spinner_assign_mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    // Save selected assignment mode into internal storage.
                    putIntoStorage(
                        context = this@MatchInformationInputActivity,
                        key = "assignment_mode",
                        value = position
                    )

                    // Automatically assign teams if in automatic assignment mode and disable user input.
                    // Otherwise, enable team number edit texts and alliance color toggles.
                    if (position == 0) {
                        assignMode = Constants.AssignmentMode.AUTOMATIC_ASSIGNMENT
                        if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                            et_team_one.setText("")
                            et_team_two.setText("")
                            et_team_three.setText("")

                            et_team_one.isEnabled = false
                            et_team_two.isEnabled = false
                            et_team_three.isEnabled = false
                        } else {
                            et_team_one.setText("")

                            et_team_one.isEnabled = false
                            left_toggle_button.isEnabled = false
                            right_toggle_button.isEnabled = false
                        }
                        autoAssignTeamInputsGivenMatch()
                    } else {
                        assignMode = Constants.AssignmentMode.OVERRIDE
                        if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                            et_team_one.isEnabled = true
                            et_team_two.isEnabled = true
                            et_team_three.isEnabled = true
                        } else {
                            et_team_one.isEnabled = true
                        }
                        if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
                            left_toggle_button.isEnabled = true
                            right_toggle_button.isEnabled = true
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                assignMode = Constants.AssignmentMode.NONE
            }
        }
    }

    // Transition into the next activity and set timestamp for specific match.
    private fun startMatchActivity() {
        matchNumber = parseInt(et_match_number.text.toString())

        putIntoStorage(context = this, key = "match_number", value = matchNumber)
        putIntoStorage(context = this, key = "alliance_color", value = allianceColor)

        if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
            teamNumber = et_team_one.text.toString()
            val intent = Intent(this, StartingPositionObjectiveActivity::class.java)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.MATCH_INFORMATION_INPUT)
                .putExtra("team_one", et_team_one.text.toString())
            startActivity(
                intent, ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    btn_proceed_match_start, "proceed_button"
                ).toBundle()
            )
        } else {
            val intent = Intent(this, CollectionSubjectiveActivity::class.java)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.MATCH_INFORMATION_INPUT)
                .putExtra("team_one", et_team_one.text.toString())
                .putExtra("team_two", et_team_two.text.toString())
                .putExtra("team_three", et_team_three.text.toString())
            startActivity(
                intent, ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    btn_proceed_match_start, "proceed_button"
                ).toBundle()
            )
        }
    }

    // Initialize the onClickListener for the proceed button to go the next screen if inputs pass safety checks.
    private fun initProceedButton() {
        btn_proceed_match_start.setOnClickListener { view ->
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (safetyCheck(view = view, "match_information_input_activity")) {
                    startMatchActivity()
                }
            }
        }
    }

    // Begin intent used in onKeyLongPress to restart app from ModeCollectionSelectActivity.kt.
    // Remove collection mode from internal storage.
    private fun intentToMatchInput() {
        this.getSharedPreferences("PREFS", 0).edit().remove("collection_mode").apply()
        startActivity(
            Intent(this, ModeCollectionSelectActivity::class.java)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.MATCH_INFORMATION_INPUT)
        )
    }

    // Restart app from ModeCollectionSelectActivity.kt when back button is long pressed.
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
        if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
            setContentView(R.layout.match_information_input_activity_objective)
            initScoutIdLongClick()
        } else if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
            setContentView(R.layout.match_information_input_activity_subjective)
        }

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        et_match_number.setText(retrieveFromStorage(context = this, key = "match_number"))
        tv_version_number.text = getString(R.string.tv_version_num, Constants.VERSION_NUMBER)


        resetCollectionReferences()
        resetStartingReferences()

        MatchSchedule.read()

        initOldQRsLongClick()
        initToggleButtons()
        initScoutNameSpinner(context = this, spinner = spinner_scout_name)
        initMatchNumberTextChangeListener()
        initTeamNumberTextChangeListeners()
        initProceedButton()
        initAssignModeSpinner()
        autoAssignTeamInputsGivenMatch()

        //Enables and disables team number edit text based on assignment mode
        if (assignMode != Constants.AssignmentMode.OVERRIDE) {
            et_team_one.isEnabled = false
            if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                et_team_two.isEnabled = false
                et_team_three.isEnabled = false
            }
        } else {
            et_team_one.isEnabled = true
            if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
                et_team_two.isEnabled = true
                et_team_three.isEnabled = true
            }
        }
    }
}
