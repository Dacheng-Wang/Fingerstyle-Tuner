package com.example.fingerstyleguitartuner

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import com.example.fingerstyleguitartuner.ui.fragment.CircleGuitarTunerFragment
import com.example.fingerstyleguitartuner.ui.view.TunerPitchToggleView


class DisplayTuner : AppCompatActivity(), TunerPitchToggleView {
    private val circleGuitarTunerFragment = CircleGuitarTunerFragment.newInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_tuner)

        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

        val pdh = PitchDetectionHandler { result, e ->
            val pitchInHz = result.pitch
            runOnUiThread {
                val text = findViewById<View>(R.id.textView1) as TextView
                text.text = "" + pitchInHz
            }
        }
        val p: AudioProcessor = PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(p)
        showGuitarTuner()
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    override fun showGuitarTuner() {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, circleGuitarTunerFragment).commit()

    }

    override fun showPitchPlayback(note: String?, frequency: Double, x: Float, y: Float, animate: Boolean) {

    }
}