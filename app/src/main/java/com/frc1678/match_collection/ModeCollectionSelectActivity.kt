// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import kotlinx.android.synthetic.main.mode_collection_select_activity.btn_objective_collection_select
import kotlinx.android.synthetic.main.mode_collection_select_activity.btn_playoff_select
import kotlinx.android.synthetic.main.mode_collection_select_activity.btn_subjective_collection_select
import kotlinx.android.synthetic.main.mode_collection_select_activity.tv_event_name
import kotlinx.android.synthetic.main.mode_collection_select_activity.tv_version_number_mode_select
import java.io.File

// Activity for selecting objective or subjective mode.
class ModeCollectionSelectActivity : CollectionActivity() {
    // Initialize button onClickListeners for the objective and subjective mode selection buttons.
    // When clicked, buttons set collection_mode and start MatchInformationInputActivity.kt.
    private fun initButtonOnClicks() {
        btn_objective_collection_select.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                collectionMode = Constants.ModeSelection.OBJECTIVE
                startMatchInformationInputActivity()
            }
        }
        btn_subjective_collection_select.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                collectionMode = Constants.ModeSelection.SUBJECTIVE
                startMatchInformationInputActivity()
            }
        }
        btn_playoff_select.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                startActivity(Intent(this, PlayoffActivity::class.java))
            }
        }
    }

    // Create the intent to start the respective mode activity.
    private fun startMatchInformationInputActivity() {
        putIntoStorage(context = this, key = "collection_mode", value = collectionMode)
        // Start respective mode activity.
        when (collectionMode) {
            Constants.ModeSelection.OBJECTIVE -> {
                finish()
                startActivity(
                    Intent(
                        this,
                        MatchInformationInputActivity::class.java
                    ).putExtra(PREVIOUS_SCREEN, Constants.Screens.MODE_COLLECTION_SELECT)
                )
            }

            Constants.ModeSelection.SUBJECTIVE -> {
                finish()
                startActivity(
                    Intent(
                        this,
                        MatchInformationInputActivity::class.java
                    ).putExtra(PREVIOUS_SCREEN, Constants.Screens.MODE_COLLECTION_SELECT)
                )
            }

            else -> return
        }
    }

    // Continually check storage management permissions to request permissions if not granted.
    // Continue to prompt user to accept usage of permissions until they are accepted.
    override fun onResume() {
        super.onResume()
        // check if the tablet is API 30 (Android 11) or above
        // 'all files access' is only in Android 11+
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            // create intent to open settings
            val intent = Intent().apply {
                action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
            // open settings
            startActivity(intent)
        }
        // if the tablet is a lower version, we use the old permissions
        if (Build.VERSION.SDK_INT < 30) {
            // check read and write permissions
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) or ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    // request permissions if not granted
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 100
                    )
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        File("/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}").mkdirs()
        setContentView(R.layout.mode_collection_select_activity)

        // If the collection mode exists on the device, retrieve it and skip to its match input screen.
        // Otherwise, don't skip and prompt for the mode selection input.
        if (this.getSharedPreferences(
                "PREFS", 0
            ).contains("collection_mode") and (retrieveFromStorage(
                context = this, key = "collection_mode"
            ) != Constants.ModeSelection.NONE.toString())
        ) {
            collectionMode = when (retrieveFromStorage(context = this, key = "collection_mode")) {
                Constants.ModeSelection.SUBJECTIVE.toString() -> Constants.ModeSelection.SUBJECTIVE

                Constants.ModeSelection.OBJECTIVE.toString() -> Constants.ModeSelection.OBJECTIVE

                else -> return
            }
            startMatchInformationInputActivity()
        }
        tv_version_number_mode_select.text =
            getString(R.string.tv_version_num, Constants.VERSION_NUMBER)
        tv_event_name.text = getString(R.string.tv_event_name, Constants.EVENT_KEY)
        initButtonOnClicks()
    }
}
