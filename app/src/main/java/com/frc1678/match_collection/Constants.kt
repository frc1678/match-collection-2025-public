// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.Activity

// Contains constant values and enum classes.
class Constants {
    companion object {
        const val NONE_VALUE: String = "NONE"
        const val NUMBER_OF_ACTIVE_SCOUTS: Int = 24
        const val COMPRESSED_QR_TAG = "QR"
        const val PREVIOUS_SCREEN = "previous_screen"
        const val VERSION_NUMBER = "1.0.2"
        const val EVENT_KEY = "2025dal"


        /**
         * The previous activity that was visited before this one. This is found by looking for the
         * intent extra with key [PREVIOUS_SCREEN].
         */
        val Activity.previousScreen
            get() = intent.getSerializableExtra(PREVIOUS_SCREEN) as? Screens
    }

    /**
     * Every screen in the app.
     */
    enum class Screens {
        COLLECTION_OBJECTIVE,
        COLLECTION_SUBJECTIVE,
        MATCH_INFORMATION_INPUT,
        MATCH_INFORMATION_EDIT,
        MODE_COLLECTION_SELECT,
        QR_GENERATE,
        STARTING_POSITION_OBJECTIVE
    }

    enum class ModeSelection {
        SUBJECTIVE,
        OBJECTIVE,
        NONE
    }

    enum class AllianceColor {
        RED,
        BLUE,
        NONE
    }

    enum class CageLevel {
        N, // NONE
        FS, // FAILED_SHALLOW
        FD, // FAILED_DEEP
        S, // SHALLOW
        D, // DEEP
    }

    enum class ActionType {
        FAIL,
        AUTO_DROP_ALGAE,
        AUTO_DROP_CORAL,
        AUTO_INTAKE_GROUND_1,
        AUTO_INTAKE_GROUND_2,
        AUTO_INTAKE_MARK_1_CORAL,
        AUTO_INTAKE_MARK_2_CORAL,
        AUTO_INTAKE_MARK_3_CORAL,
        AUTO_INTAKE_MARK_1_ALGAE,
        AUTO_INTAKE_MARK_2_ALGAE,
        AUTO_INTAKE_MARK_3_ALGAE,
        AUTO_INTAKE_REEF_F1,
        AUTO_INTAKE_REEF_F2,
        AUTO_INTAKE_REEF_F3,
        AUTO_INTAKE_REEF_F4,
        AUTO_INTAKE_REEF_F5,
        AUTO_INTAKE_REEF_F6,
        AUTO_INTAKE_STATION_1,
        AUTO_INTAKE_STATION_2,
        AUTO_REEF_FAIL,
        AUTO_REEF_SUCCESS,
        AUTO_REEF_F1,
        AUTO_REEF_F2,
        AUTO_REEF_F3,
        AUTO_REEF_F4,
        AUTO_REEF_F5,
        AUTO_REEF_F6,
        AUTO_REEF_L1,
        AUTO_REEF_L2,
        AUTO_REEF_L3,
        AUTO_REEF_L4,
        AUTO_NET,
        AUTO_PROCESSOR,
        TELE_DROP_ALGAE,
        TELE_DROP_CORAL,
        TELE_NET,
        TELE_PROCESSOR,
        TELE_CORAL_L1,
        TELE_CORAL_L2,
        TELE_CORAL_L3,
        TELE_CORAL_L4,
        TELE_INTAKE_GROUND,
        TELE_INTAKE_REEF,
        TELE_INTAKE_STATION,
        TELE_INTAKE_POACH,
        START_INCAP_TIME,
        END_INCAP_TIME,
        TO_TELEOP,
        TO_ENDGAME
    }

    enum class Stage {
        AUTO,
        TELEOP,
        ENDGAME
    }

    enum class AssignmentMode {
        NONE,
        AUTOMATIC_ASSIGNMENT,
        OVERRIDE
    }
}
