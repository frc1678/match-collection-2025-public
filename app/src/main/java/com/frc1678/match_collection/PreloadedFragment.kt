package com.frc1678.match_collection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.objective.CollectionObjectiveActivity
import kotlinx.android.synthetic.main.preloaded_fragment.view.toggle_preloaded

class PreloadedFragment : Fragment(R.layout.preloaded_fragment) {

    /**
     * The main view of this fragment.
     */
    private var mainView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainView = super.onCreateView(inflater, container, savedInstanceState)!!
        if (mainView != null && activity != null) with(mainView!!) {
            toggle_preloaded.isChecked = preloaded
        }
        initOnClicks()
        return mainView
    }


    private val collectionObjectiveActivity get() = activity as CollectionObjectiveActivity

    /**
     * Changes the preloaded piece based on the button pressed
     * Changes the screen to scoring/auto depending on the changed preload
     */
    private fun initOnClicks() {
        if (mainView != null && activity != null) with(mainView!!) {
            // Remove previous action from timeline when undo button is clicked.
            toggle_preloaded.setOnClickListener {
                /*
                Toggles if the robot has a preload or not, then sets
                preloaded, scoring, and the color to correspondingly
                 */
                preloaded = !preloaded
                if (preloaded) {
                    robotInvTimeline.add(arrayOf('C'))
                    scoring = true

                } else {
                    robotInvTimeline.clear()
                    scoring = false
                }
                collectionObjectiveActivity.enableButtons()
            }
        }
    }
}