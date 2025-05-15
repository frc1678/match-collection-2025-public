// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import kotlinx.android.synthetic.main.edit_match_information_activity.et_match_number
import kotlinx.android.synthetic.main.edit_match_information_activity.et_team_one
import kotlinx.android.synthetic.main.edit_match_information_activity.et_team_three
import kotlinx.android.synthetic.main.edit_match_information_activity.et_team_two


// Super class of match information activities (input and edit).
// Used to implement class mechanisms that both match information activities require.
open class MatchInformationActivity : CollectionActivity() {
    // Check if the given text inputs are not empty.
    fun checkInputNotEmpty(vararg views: EditText): Boolean {
        for (view in views) {
            if (view.text.isEmpty()) return false
        }
        return true
    }

    // Allow for inputs to become visible.
    fun makeViewVisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.VISIBLE
        }
    }

    // Allow for inputs to become invisible.
    fun makeViewInvisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.INVISIBLE
        }
    }

    // Read scout names in scouts.txt file and return a list of scout names to populate the scout name spinner.
    private fun populateScoutNameSpinner(context: Context): ArrayList<String> {
        val scoutNameList: ArrayList<String> = ArrayList()
        val bufferedReader = context.resources.openRawResource(R.raw.scouts).bufferedReader()
        var currentLine = bufferedReader.readLine()

        while (currentLine != null) {
            scoutNameList.add(currentLine)
            currentLine = bufferedReader.readLine()
        }
        bufferedReader.close()
        return scoutNameList
    }

    // Initialize scout name spinner.
    fun initScoutNameSpinner(context: Context, spinner: Spinner) {
        val adapter = ArrayAdapter(
            context, R.layout.spinner_text_view, populateScoutNameSpinner(context = context)
        )
        spinner.adapter = adapter

        // If a scout name has been previously inputted, retrieve name from internal storage
        // and set selection to the previously selected name.
        if (context.getSharedPreferences("PREFS", 0).contains("scout_name")) {
            spinner.setSelection(
                populateScoutNameSpinner(context = context).indexOf(
                        retrieveFromStorage(
                            context = context,
                            key = "scout_name"
                        )
                    )
            )
        }

        // If scout name is selected on spinner, set scout name to selected name and save it to internal storage.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    scoutName = populateScoutNameSpinner(context = context)[position]
                    putIntoStorage(context = context, key = "scout_name", value = scoutName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //Checks if the inputted string in the parameter contains at least 1 integer.
    private fun stringContainsNumber(toCheck: String): Boolean {
        val integers = mutableListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        for (i in integers) {
            if (toCheck.contains(i)) {
                return true
            }
        }
        return false
    }

    // Set safeties to create error messages if not all information is inputted.
    fun safetyCheck(view: View, currentScreen: String): Boolean {
        if (checkInputNotEmpty(
                et_match_number
            ) and (allianceColor != Constants.AllianceColor.NONE)
        ) {
            if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
                if (currentScreen == "match_edit_activity_screen") {
                    if (checkInputNotEmpty(et_team_two)) {
                        if ((scoutId != Constants.NONE_VALUE)) {
                            //Ensures the inputted team contains at least 1 integer
                            if (stringContainsNumber(et_team_two.text.toString())) {
                                return true
                            } else {
                                createErrorMessage(
                                    message = getString(
                                        R.string.team_name_missing_integer
                                    ), view = view
                                )
                                return false
                            }
                        } else {
                            createErrorMessage(
                                message = getString(R.string.error_missing_information), view = view
                            )
                            return false
                        }
                    }
                }
                // Check to make sure all objective-related inputs are not empty.
                return if ((scoutId != Constants.NONE_VALUE)) {
                    //Ensures the inputted team contains at least 1 integer
                    if (stringContainsNumber(et_team_one.text.toString())) {
                        true
                    } else {
                        createErrorMessage(
                            message = getString(
                                R.string.team_name_missing_integer
                            ), view = view
                        )
                        false
                    }
                } else {
                    createErrorMessage(
                        message = getString(R.string.error_missing_information), view = view
                    )
                    false
                }
            } else {
                // Check to make sure all subjective-related inputs are not empty.
                if (checkInputNotEmpty(
                        et_team_one, et_team_two, et_team_three
                    )
                ) {
                    // Ensure that inputted team numbers are different.
                    return if ((et_team_one.text.toString() != et_team_two.text.toString()) and (et_team_one.text.toString() != et_team_three.text.toString()) and (et_team_two.text.toString() != et_team_three.text.toString())) {
                        // Ensures the inputted team numbers each contain at least 1 integer
                        if ((!stringContainsNumber(et_team_one.text.toString()) || !stringContainsNumber(
                                et_team_two.text.toString()
                            ) || !stringContainsNumber(et_team_three.text.toString()))
                        ) {
                            createErrorMessage(
                                message = getString(
                                    R.string.team_name_missing_integer
                                ), view = view
                            )
                            return false
                        } else {
                            true
                        }
                    } else {
                        createErrorMessage(
                            message = getString(R.string.error_same_teams), view = view
                        )
                        false
                    }
                } else {
                    createErrorMessage(
                        message = getString(R.string.error_missing_information), view = view
                    )
                    return false
                }
            }
        } else {
            createErrorMessage(
                message = getString(R.string.error_missing_information), view = view
            )
            return false
        }
    }
}

