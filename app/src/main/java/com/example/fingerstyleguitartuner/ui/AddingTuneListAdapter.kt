package com.example.fingerstyleguitartuner.ui

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.fingerstyleguitartuner.R
import net.mabboud.android_tone_player.OneTimeBuzzer
import kotlin.math.pow

class AddingTuneListAdapter(private val dataList: ArrayList<Array<Any>>): BaseAdapter() {
    private lateinit var selectedLetter: String
    private var selectedNumber: Int = 0
    private lateinit var checkBox: CheckBox
    private lateinit var frequencyTextView: TextView
    private lateinit var spinnerLetter: Spinner
    private lateinit var spinnerNumber: Spinner
    private lateinit var tunePlay: ImageButton
    private lateinit var viewGroup: ViewGroup
    private lateinit var timer: CountDownTimer

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        viewGroup = parent
        var view = convertView
        if (view == null) {
            val inflater = LayoutInflater.from(parent.context)
            view = inflater.inflate(R.layout.listview_tuning_items, parent, false)
        }
        if (view != null) {
            val textView = view.findViewById<TextView>(R.id.string_num)
            textView.text = dataList[position][0].toString()
            //get checkbox and TextView
            checkBox = view.findViewById(R.id.tune_sharp_checkBox)
            frequencyTextView = view.findViewById(R.id.tune_frequency)
            //set value for spinners
            spinnerLetter = view.findViewById<Spinner>(R.id.tune_letter)
            val letterAdapter = ArrayAdapter(parent.context,
                R.layout.spinner_item, parent.resources.getStringArray(R.array.tuneLetterList))
            spinnerLetter.adapter = letterAdapter

            spinnerNumber = view.findViewById<Spinner>(R.id.tune_number)
            val numberAdapter = ArrayAdapter(parent.context,
                R.layout.spinner_item, parent.resources.getStringArray(R.array.tuneNumberList))
            spinnerNumber.adapter = numberAdapter

            //check if it's isEdit
            if (dataList[position][2] != "") {
                spinnerLetter.setSelection(viewGroup.resources.getStringArray(R.array.tuneLetterList).indexOf(dataList[position][2]))
                spinnerNumber.setSelection(viewGroup.resources.getStringArray(R.array.tuneNumberList).indexOf(dataList[position][3].toString()))
                checkBox.isChecked = dataList[position][4] == 1
            }

            //Initialize Frequency TextView
            selectedLetter = spinnerLetter.selectedItem.toString()
            selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
            refreshFrequency(parent.context)
        }
        //Update frequency upon selection change
        spinnerLetter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, positionSelected: Int, id: Long) {
                if (view != null) {
                    if (parent != null) {
                        refreshSelected(parent.context, view, false, position)
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

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, positionSelected: Int, id: Long) {
                if (view != null) {
                    if (parent != null) {
                        refreshSelected(parent.context, view, false, position)
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
            refreshSelected(parent.context, it, true, position)
            selectedLetter = spinnerLetter.selectedItem.toString()
            selectedNumber = Integer.parseInt(spinnerNumber.selectedItem.toString())
            refreshFrequency(parent.context)
        }
        if (view != null) {
            tunePlay = view.findViewById<ImageButton>(R.id.tune_play)
        }

        tunePlay.setOnClickListener {
            refreshSelected(parent.context, it, true, position)
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

        return view
    }

    private fun refreshFrequency(context: Context) {
        var noteLetter = selectedLetter
        if (checkBox.isChecked){
            noteLetter += '#'
        }
        val noteNumber = selectedNumber
        val frequency = calculateFrequency(noteLetter, noteNumber)
        frequencyTextView.tag = frequency
        frequencyTextView.text = context.getString(R.string.frequency, context.getString(R.string.rounding).format(frequency))
    }

    fun refreshSelected(context: Context, viewChild: View, isOnClick: Boolean, position: Int) {
        val view: ViewGroup = (if (isOnClick) {
            viewChild.parent
        } else {
            viewChild.parent.parent
        }) as ViewGroup
        checkBox = view.findViewById(R.id.tune_sharp_checkBox)
        frequencyTextView = view.findViewById(R.id.tune_frequency)
        spinnerLetter = view.findViewById(R.id.tune_letter)
        spinnerNumber = view.findViewById(R.id.tune_number)
        tunePlay = view.findViewById(R.id.tune_play)

        dataList[position] = arrayOf(dataList[position][0], frequencyTextView.tag as Float,
            spinnerLetter.selectedItem.toString(), spinnerNumber.selectedItem.toString().toInt(), if(checkBox.isChecked) 1 else 0)
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataList.size
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