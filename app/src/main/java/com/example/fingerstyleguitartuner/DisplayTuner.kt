package com.example.fingerstyleguitartuner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.example.fingerstyleguitartuner.anim.ZoomOutPageTransformer
import com.example.fingerstyleguitartuner.fragment.CircleGuitarTunerFragment
import com.example.fingerstyleguitartuner.fragment.currentPage
import com.example.fingerstyleguitartuner.ui.CircleTunerView
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


var noteList = ArrayList<String>()
lateinit var frequencyList: FloatArray
var stringList = ArrayList<String>()
class DisplayTuner : AppCompatActivity() {
    var numPages = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = supportActionBar
        if (toolbar != null) {
            toolbar.title = intent.getStringExtra("name")
        }

        noteList = intent.getStringArrayListExtra("note") as ArrayList<String>
        frequencyList = intent.getFloatArrayExtra("frequency") as FloatArray
        numPages = noteList.size
        //Prepare items from noteList
        stringList = ArrayList<String>()
        for (i in 0 until numPages) {
            if (i == 0) stringList.add(baseContext.getString(R.string.string_number, (i + 1).toString()) + "(Thickest String)")
            else stringList.add(baseContext.getString(R.string.string_number, (i + 1).toString()))
        }

        setContentView(R.layout.activity_screen_slide)
        // Instantiate a ViewPager2 and a PagerAdapter.
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        var frequencyRange = ArrayList<Double>()
        var counter = 0
        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
        val pdh = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch
            runOnUiThread {
                counter += 1
                if (counter > 10) {
                    counter = 0
                    frequencyRange = ArrayList<Double>()
                }
                val frequencyText = findViewById<TextView>(R.id.capturedFrequency)
                frequencyText.text = getString(R.string.frequency, pitchInHz.toString())
                frequencyText.tag = pitchInHz
                val frequency = (pitchInHz as Float).toDouble()
                if (frequency != -1.0) {
                    if (frequencyRange.size < 5) frequencyRange.add(frequency)
                    else {
                        val tempList = frequencyRange.clone() as ArrayList<Double>
                        tempList.add(frequency)
                        if (calculateSD(frequencyRange) > calculateSD(tempList)) {
                            frequencyRange = replaceOutlier(frequencyRange, frequency)
                            val tuner = findViewById<CircleTunerView>(R.id.circleTunerView)
                            val variance = tuner.updateIndicator2Angle(frequencyText, frequencyList[currentPage].toDouble())
                            val warningText = findViewById<TextView>(R.id.tuningWarning)
                            if (pitchInHz == -1f) warningText.text = null
                            else {
                                when (variance) {
                                    1 -> {
                                        warningText.text = getString(R.string.octave_too_high)
                                        tuner.outOfTuneChangeColor()
                                        /*val colorFade: ObjectAnimator = ObjectAnimator.ofObject(
                                            pager,
                                            "backgroundColor" *//*view attribute name*//*,
                                            ArgbEvaluator(),
                                            Color.argb(255,255,255,255) *//*from color*//*,
                                            Color.argb(0,0,0,0)
                                        )
                                        colorFade.duration = 3500
                                        colorFade.startDelay = 200
                                        colorFade.start()*/
                                    }
                                    -1 -> {
                                        warningText.text = getString(R.string.octave_too_low)
                                        tuner.outOfTuneChangeColor()
                                    }
                                    else -> {
                                        warningText.text = null
                                        tuner.inTuneChangeColor()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //page indicator
        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
        dotsIndicator.setViewPager2(viewPager)
        dotsIndicator.bringToFront()

        val p: AudioProcessor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(p)
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    private fun calculateSD(numArray: ArrayList<Double>): Double {
        var sum = 0.0
        var standardDeviation = 0.0

        sum = numArray.sum()

        val mean = sum / numArray.size

        for (num in numArray) {
            standardDeviation += (num - mean).pow(2.0)
        }

        return sqrt(standardDeviation / numArray.size)
    }

    private fun replaceOutlier(numArray: ArrayList<Double>, check: Double): ArrayList<Double> {
        numArray.add(check)
        val avg = numArray.average()
        var outlier = 0.0
        var variance = 0.0
        for (index in 0 until numArray.size) {
            if (abs(numArray[index] - avg) > variance) {
                outlier = numArray[index]
                variance = abs(numArray[index] - avg)
            }
        }
        numArray.remove(outlier)
        return numArray
    }
    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = numPages

        override fun createFragment(position: Int): Fragment = CircleGuitarTunerFragment(position)
    }

}