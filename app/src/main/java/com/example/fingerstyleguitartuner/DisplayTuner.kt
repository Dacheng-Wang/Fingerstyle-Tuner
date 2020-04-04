package com.example.fingerstyleguitartuner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.example.fingerstyleguitartuner.fragment.CircleGuitarTunerFragment
import com.example.fingerstyleguitartuner.ui.CircleTunerView

var noteList = ArrayList<String>()
lateinit var frequencyList: FloatArray

class DisplayTuner : AppCompatActivity() {
    private val circleGuitarTunerFragment = CircleGuitarTunerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_tuner)

        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

        val pdh = PitchDetectionHandler { result, e ->
            val pitchInHz = result.pitch
            runOnUiThread {
                val frequencyText = findViewById<TextView>(R.id.capturedFrequency)
                frequencyText.text = getString(R.string.frequency, pitchInHz.toString())
                frequencyText.tag = pitchInHz

                val tuner = findViewById<CircleTunerView>(R.id.circleTunerView)
                val variance = tuner.updateIndicator2Angle(frequencyText, frequencyList[0].toDouble())
                val warningText = findViewById<TextView>(R.id.tuningWarning)
                if (pitchInHz == -1f) warningText.text = null
                else {
                    when (variance) {
                        1 -> warningText.text = getString(R.string.octave_too_high)
                        -1 -> warningText.text = getString(R.string.octave_too_low)
                        else -> warningText.text = null
                    }
                }
            }
        }

        noteList = intent.getStringArrayListExtra("note") as ArrayList<String>
        frequencyList = intent.getFloatArrayExtra("frequency") as FloatArray
        setContentView(R.layout.activity_display_tuner)

        val p: AudioProcessor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(p)
        Thread(dispatcher, "Audio Dispatcher").start()
    }
}