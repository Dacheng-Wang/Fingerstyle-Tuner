package com.example.fingerstyleguitartuner

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
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
import kotlinx.android.synthetic.main.activity_screen_slide.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


var noteList = ArrayList<String>()
lateinit var frequencyList: FloatArray
var stringList = ArrayList<String>()
class DisplayTuner : AppCompatActivity() {
    var numPages = 0
    private lateinit var warningText: TextView
    private lateinit var tuner: CircleTunerView
    private var isInitailized = false
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
            if (i == 0) stringList.add(baseContext.getString(R.string.string_number, (i + 1).toString()) + "\n(Thickest String)")
            else stringList.add(baseContext.getString(R.string.string_number, (i + 1).toString()))
        }
        setContentView(R.layout.activity_screen_slide)

        // Instantiate a ViewPager2 and a PagerAdapter.
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        val pageListener = PageListener()
        viewPager.registerOnPageChangeCallback(pageListener)
        var frequencyRange = ArrayList<Double>()
        var counter = 0
        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
        val pdh = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch
            runOnUiThread {
                counter += 1
                if (counter > 20) {
                    counter = 0
                    frequencyRange = ArrayList<Double>()
                }
                warningText = findViewById(R.id.tuningWarning)
                tuner = findViewById(R.id.circleTunerView)
                isInitailized = true
                val frequencyText = findViewById<TextView>(R.id.capturedFrequency)
                //frequencyText.text = getString(R.string.frequency, pitchInHz.toString())
                frequencyText.tag = pitchInHz
                val frequency = pitchInHz.toDouble()
                if (frequency != -1.0) {
                    if (frequencyRange.size < 10) frequencyRange.add(frequency)
                    else {
                        val tempList = frequencyRange.clone() as ArrayList<Double>
                        tempList.add(frequency)
                        if (calculateSD(frequencyRange) > calculateSD(tempList)) {
                            frequencyRange = replaceOutlier(frequencyRange, frequency)
                            currentPage = viewPager.currentItem
                            val variance = tuner.updateIndicator2Angle(frequencyText, frequencyList[currentPage].toDouble())
                            if (pitchInHz == -1f) warningText.text = null
                            else {
                                if (pitchInHz > (pitchInHz - variance) * 2 ) {
                                    warningText.text = getString(R.string.octave_too_high)
                                    warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_out_of_tune_color))
                                    tuner.outOfTuneChangeColor()
                                }
                                else if (pitchInHz < (pitchInHz - variance) / 2) {
                                    warningText.text = getString(R.string.octave_too_low)
                                    warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_out_of_tune_color))
                                    tuner.outOfTuneChangeColor()
                                }
                                else {
                                    if (abs(variance) / (pitchInHz - variance) < 0.01) {
                                        warningText.text = getString(R.string.octave_tune_perfect)
                                        warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_in_tune_color))
                                        tuner.inTuneChangeColor()
                                    }
                                    else {
                                        if (variance > 0) warningText.text = getString(R.string.octave_tune_high)
                                        else warningText.text = getString(R.string.octave_tune_low)
                                        warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_inner_circle_color))
                                        tuner.inRangeChangeColor()
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
        var standardDeviation = 0.0

        val sum = numArray.sum()

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

    private inner class PageListener: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (isInitailized) {
                warningText.text = null
                warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_inner_circle_color))
                tuner.inRangeChangeColor()
            }
        }
    }

}