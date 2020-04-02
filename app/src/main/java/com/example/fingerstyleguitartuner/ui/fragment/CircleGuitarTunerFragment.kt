package com.example.fingerstyleguitartuner.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.RequiresApi
import butterknife.BindView
import com.example.fingerstyleguitartuner.R
import com.example.fingerstyleguitartuner.ui.view.TunerView
import com.example.fingerstyleguitartuner.ui.widget.CircleTunerView

class CircleGuitarTunerFragment : BaseFragment(), TunerView, CircleTunerView.OnNotePressedListener {
    @BindView(R.id.circleTunerView)
    var circleTunerView: CircleTunerView? = null

    private var listener: OnPlayNoteListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflateAndBindView(inflater, R.layout.fragment_circle_guitar_tuner, container, false)

        // Set the initial pointer position to zero if this is the first time loading the view
        if (savedInstanceState == null) {
            circleTunerView?.getViewTreeObserver()?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onGlobalLayout() {
                    circleTunerView?.getViewTreeObserver()?.removeOnGlobalLayoutListener(this)
                    circleTunerView?.updateNote(null, 0.0, 0f)
                }
            })
        }
        return v
    }

    interface OnPlayNoteListener {
        fun onPlayNote(noteName: String?, frequency: Double, x: Float, y: Float)
    }

    companion object {
        const val TITLE = "Guitar Tuner"
        fun newInstance(): CircleGuitarTunerFragment {
            return CircleGuitarTunerFragment()
        }
    }

    override fun onShowNote(noteName: String?, frequency: Double, percentOffset: Float) {

    }

    override fun onPlayNote(noteName: String?, frequency: Double, x: Float, y: Float) {

    }

    override fun onNotePressed(notePosition: CircleTunerView.NotePosition?) {

    }
}
