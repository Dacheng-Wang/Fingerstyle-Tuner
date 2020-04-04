package com.example.fingerstyleguitartuner.ui

import android.content.Context
import android.media.Image
import android.os.CountDownTimer
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import be.tarsos.dsp.AudioGenerator
import be.tarsos.dsp.effects.DelayEffect
import be.tarsos.dsp.effects.FlangerEffect
import be.tarsos.dsp.filters.LowPassFS
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.android.AndroidAudioPlayer
import be.tarsos.dsp.synthesis.AmplitudeLFO
import be.tarsos.dsp.synthesis.NoiseGenerator
import be.tarsos.dsp.synthesis.SineGenerator
import com.example.fingerstyleguitartuner.R
import net.mabboud.android_tone_player.ContinuousBuzzer
import net.mabboud.android_tone_player.OneTimeBuzzer
import kotlin.concurrent.timer
import kotlin.math.log
import kotlin.math.pow


class AddingTuneAdapter(private val tuneList: ArrayList<String>, private val letterList: ArrayList<String>? = null,
                               private val numberList: ArrayList<Int>? = null, private val sharpList: ArrayList<Int>? = null) :
    RecyclerView.Adapter<AddingTuneAdapter.AddingTuneViewHolder>() {
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.

    class AddingTuneViewHolder(val view: View, val textView: TextView) : RecyclerView.ViewHolder(view)
    private lateinit var selectedLetter: String
    private var selectedNumber: Int = 0
    private lateinit var checkBox: CheckBox
    private lateinit var frequencyTextView: TextView
    private lateinit var spinnerLetter: Spinner
    private lateinit var spinnerNumber: Spinner
    private lateinit var tunePlay: ImageButton
    private lateinit var viewGroup: ViewGroup
    private lateinit var timer: CountDownTimer
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AddingTuneViewHolder {
        viewGroup = parent
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_tuning_items, parent, false) as View
        val textView = view.findViewById<TextView>(R.id.string_num)

        //get checkbox and TextView
        checkBox = view.findViewById<CheckBox>(R.id.tune_sharp_checkBox)
        frequencyTextView = view.findViewById<TextView>(R.id.tune_frequency)
        //set value for spinners
        spinnerLetter = view.findViewById<Spinner>(R.id.tune_letter)
        val letterAdapter = ArrayAdapter(parent.context,
            R.layout.spinner_item, parent.resources.getStringArray(R.array.tuneLetterList))
        spinnerLetter.adapter = letterAdapter

        spinnerNumber = view.findViewById<Spinner>(R.id.tune_number)
        val numberAdapter = ArrayAdapter(parent.context,
            R.layout.spinner_item, parent.resources.getStringArray(R.array.tuneNumberList))
        spinnerNumber.adapter = numberAdapter
        //Initialize Frequency TextView
        selectedLetter = spinnerLetter.selectedItem.toString()
        selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
        refreshFrequency(parent.context)

        //Update frequency upon selection change
        spinnerLetter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (view != null) {
                    if (parent != null) {
                        refreshSelected(parent.context, view, false)
                    }
                }
                selectedLetter = spinnerLetter.selectedItem.toString()
                selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
                if (parent != null) {
                    refreshFrequency(parent.context)
                }
            }
        }
        spinnerNumber.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (view != null) {
                    if (parent != null) {
                        refreshSelected(parent.context, view, false)
                    }
                }
                selectedLetter = spinnerLetter.selectedItem.toString()
                selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
                if (parent != null) {
                    refreshFrequency(parent.context)
                }
            }
        }
        checkBox.setOnClickListener {
            refreshSelected(parent.context, it, true)
            selectedLetter = spinnerLetter.selectedItem.toString()
            selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
            refreshFrequency(parent.context)
        }
        tunePlay = view.findViewById<ImageButton>(R.id.tune_play)

        tunePlay.setOnClickListener {
            refreshSelected(parent.context, it, true)
            produceSoundFromFrequency(it as View, frequencyTextView.tag as Float, 5)
            if (::timer.isInitialized) timer.cancel()
            timer = object: CountDownTimer(5000, 5000) {
                override fun onFinish() {
                    if (isPlaying) {
                        buzzer.stop()
                        isPlaying = false
                        (it as ImageButton).setImageResource(R.drawable.ic_play_arrow_orange_24dp)
                    }
                }
                override fun onTick(millisUntilFinished: Long) {
                }
            }
            timer.start()
        }

        // set the view's size, margins, paddings and layout parameters
        return AddingTuneViewHolder(view, textView)
    }

    fun refreshFrequency(context: Context) {
        var noteLetter = selectedLetter
        if (checkBox.isChecked){
            noteLetter += '#'
        }
        val noteNumber = selectedNumber
        val frequency = calculateFrequency(noteLetter, noteNumber)
        frequencyTextView.tag = frequency
        frequencyTextView.text = context.getString(R.string.frequency, context.getString(R.string.rounding).format(frequency))
    }

    fun refreshSelected(context: Context, viewChild: View, isOnClick: Boolean) {
        val view: ViewGroup = (if (isOnClick) {
            viewChild.parent
        } else {
            viewChild.parent.parent
        }) as ViewGroup
        checkBox = view.findViewById<CheckBox>(R.id.tune_sharp_checkBox)
        frequencyTextView = view.findViewById<TextView>(R.id.tune_frequency)
        spinnerLetter = view.findViewById<Spinner>(R.id.tune_letter)
        spinnerNumber = view.findViewById<Spinner>(R.id.tune_number)
        tunePlay = view.findViewById<ImageButton>(R.id.tune_play)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: AddingTuneViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = tuneList[position]
        if (letterList != null && numberList != null && sharpList != null && letterList.size > position) {
            holder.view.findViewById<Spinner>(R.id.tune_letter).setSelection(viewGroup.resources.getStringArray(R.array.tuneLetterList).indexOf(letterList[position]))
            holder.view.findViewById<Spinner>(R.id.tune_number).setSelection(viewGroup.resources.getStringArray(R.array.tuneNumberList).indexOf(numberList[position].toString()))
            holder.view.findViewById<CheckBox>(R.id.tune_sharp_checkBox).isChecked = sharpList[position] == 1
        }
    }
    override fun getItemCount() = tuneList.size
}

class AddingTuneItemClickListener(
    context: Context?,
    recyclerView: RecyclerView,
    private val mListener: OnItemClickListener?
) :
    OnItemTouchListener {

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongItemClick(view: View?, position: Int)
    }

    var mGestureDetector: GestureDetector
    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView: View? = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    init {
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child: View? = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }
}
fun calculateFrequency(noteLetter: String, noteNumber: Int): Float{
    val baseFrequency = 440
    val exp = (1.0/12.0).toFloat()
    val step = 2.toFloat().pow(exp)
    val scaleDiff = noteNumber - 4
    //convert letter to number; C -> 1
    var noteLetterNumber = 0
    when (noteLetter.first()){
        'C' -> noteLetterNumber = 1
        'D' -> noteLetterNumber = 3
        'E' -> noteLetterNumber = 5
        'F' -> noteLetterNumber = 6 //only half step from E to F
        'G' -> noteLetterNumber = 8
        'A' -> noteLetterNumber = 10
        'B' -> noteLetterNumber = 12
    }
    var stepDiff = scaleDiff * 12 + noteLetterNumber - 10 //considers the fact that there is only half step difference between B & C
    if (noteLetter.length > 1){
        stepDiff += 1
    }
    return (baseFrequency * (step.pow(stepDiff)))
}

lateinit var buzzer: OneTimeBuzzer
var isPlaying = false
var previousView: View? = null
var firstTime = true
fun produceSoundFromFrequency(view: View, frequency: Float, seconds: Int) {
    var volume: Int
    if (frequency < 100) {
        volume = 150
    }
    else if (frequency < 150) {
        volume = 90
    }
    else if (frequency < 200) {
        volume = 80
    }
    else if (frequency < 250) {
        volume = 70
    }
    else if (frequency < 300) {
        volume = 60
    }
    else if (frequency < 350) {
        volume = 50
    }
    else if (frequency < 400) {
        volume = 40
    }
    else if (frequency < 450) {
        volume = 30
    }
    else if (frequency < 500) {
        volume = 20
    }
    else if (frequency < 550) {
        volume = 10
    }
    else {
        volume = 5
    }

    if (previousView == view || firstTime) {
        if (!isPlaying) {
            buzzer = OneTimeBuzzer()
            buzzer.duration = seconds.toDouble()
            buzzer.volume = volume
            buzzer.toneFreqInHz = frequency.toDouble()
            buzzer.play()
            isPlaying = true
            (view as ImageButton).setImageResource(R.drawable.ic_pause_orange_24dp)
            firstTime = false
        }
        else {
            buzzer.stop()
            isPlaying = false
            (view as ImageButton).setImageResource(R.drawable.ic_play_arrow_orange_24dp)
        }
        previousView = view
    }
    else {
        if (previousView != null) (previousView as ImageButton).setImageResource(R.drawable.ic_play_arrow_orange_24dp)
        previousView = view
        isPlaying = false

        buzzer.stop()
        buzzer = OneTimeBuzzer()
        buzzer.duration = seconds.toDouble()
        buzzer.volume = volume
        buzzer.toneFreqInHz = frequency.toDouble()
        buzzer.play()
        isPlaying = true
        (view as ImageButton).setImageResource(R.drawable.ic_pause_orange_24dp)
    }

}