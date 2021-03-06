package com.lkharry.fingerstyletuner

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lkharry.fingerstyletuner.ui.AddingTuneAdapter
import com.lkharry.fingerstyletuner.ui.getItemAtPosition
import com.lkharry.fingerstyletuner.ui.calculateFrequency
import kotlinx.android.synthetic.main.activity_add_tune.*

class AddTune : AppCompatActivity() {
    //List of tunings
    private lateinit var dataList: ArrayList<Array<Any>>
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager
    private var stringCount: Int = 1
    private var isFabOpen = false
    private var fabOpen: Animation? = null
    private var fabClose:Animation? = null
    private var rotateForward:Animation? = null
    private var rotateBackward:Animation? = null
    private var letterList = ArrayList<String>()
    private var numberList = ArrayList<Int>()
    private var sharpList = ArrayList<Int>()
    private var isEdit = false
    private var frequencyList = FloatArray(0)
    private lateinit var mRunnable: Runnable
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tune)

        //set up FAB animation
        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        rotateForward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_backward)
        animateFAB()

        val toolbar = supportActionBar
        if (toolbar != null) {
            toolbar.title = "Add New Tuning (C4 is Middle C)"
        }
        dataList = ArrayList<Array<Any>>()
        //pre-load parameters if any passed through
        if (intent.getStringArrayListExtra("letter") != null) {
            isEdit = true
            tune_name.setText(intent.getStringExtra("name"))
            letterList = intent.getStringArrayListExtra("letter") as ArrayList<String>
            numberList = intent.getIntegerArrayListExtra("number") as ArrayList<Int>
            sharpList = intent.getIntegerArrayListExtra("sharp") as ArrayList<Int>
            frequencyList = intent.getFloatArrayExtra("frequency") as FloatArray
            //initializing listView based on array list
            for (i in 0 until letterList.size) {
                if (i == 0){
                    dataList.add(arrayOf(baseContext.getString(R.string.string_number, (i + 1).toString()) + "(Thickest String)",
                        frequencyList[i], letterList[i], numberList[i], sharpList[i]))
                }
                else {
                    dataList.add(arrayOf(baseContext.getString(R.string.string_number, (i + 1).toString()),
                        frequencyList[i], letterList[i], numberList[i], sharpList[i]))
                }
            }
            stringCount += letterList.size - 1
        }
        else {
            //initializing it with standard tuning
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()) + "(Thickest String)",
                calculateFrequency("E", 2), "E", 2, 0))
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()),
                calculateFrequency("A", 2), "A", 2, 0))
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()),
                calculateFrequency("D", 3), "D", 3, 0))
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()),
                calculateFrequency("G", 3), "G", 3, 0))
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()),
                calculateFrequency("B", 3), "B", 3, 0))
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()),
                calculateFrequency("E", 3), "E", 4, 0))
        }

        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = AddingTuneAdapter(dataList)
        recyclerView = findViewById<RecyclerView>(R.id.addingTuneView).apply {
            layoutManager = recyclerViewLayoutManager
            adapter = recyclerViewAdapter
        }
        fabUnfold.setOnClickListener {
            animateFAB()
        }
        fabAdd.setOnClickListener {
            stringCount += 1
            dataList.add(arrayOf(baseContext.getString(R.string.string_number, stringCount.toString()), calculateFrequency("C", 2), "C", 2, 0))
            recyclerViewAdapter.notifyItemInserted(stringCount - 1)
        }
        fabDelete.setOnClickListener {
            if (stringCount > 1) {
                stringCount -= 1
                dataList.removeAt(stringCount)
                recyclerViewAdapter.notifyItemRemoved(stringCount)
            }
        }
        fabSave.setOnClickListener {
            val returnIntent = Intent()
            //Store all related data in separate array / array list
            val frequencyList = FloatArray(recyclerViewAdapter.itemCount)
            letterList.clear()
            numberList.clear()
            sharpList.clear()
            //refresh adapter
            recyclerViewAdapter.notifyDataSetChanged()
            for (i in 0 until recyclerViewAdapter.itemCount) {
                frequencyList[i] = ((recyclerViewAdapter as AddingTuneAdapter).getItemAtPosition(i))[1] as Float
                var letter = ((recyclerViewAdapter as AddingTuneAdapter).getItemAtPosition(i))[2].toString()
                val number = ((recyclerViewAdapter as AddingTuneAdapter).getItemAtPosition(i))[3].toString().toInt()
                var sharp = ((recyclerViewAdapter as AddingTuneAdapter).getItemAtPosition(i))[4].toString().toInt()
                //Check for non-existing input - B# and E#
                if (letter == "B" && sharp == 1) {
                    letter = "C"
                    sharp = 0
                }
                else if (letter == "E" && sharp == 1) {
                    letter = "F"
                    sharp = 0
                }
                letterList.add(letter)
                numberList.add(number)
                sharpList.add(sharp)
            }
            // add note to the name and pass to main activity
            val name = tune_name.text.toString()
            returnIntent.putExtra("frequency", frequencyList)
            returnIntent.putExtra("name", name)
            returnIntent.putExtra("letter", letterList)
            returnIntent.putExtra("number", numberList)
            returnIntent.putExtra("sharp", sharpList)
            returnIntent.putExtra("isEdit", isEdit)
            if (tune_name.text.toString() == "") {
                val t = Toast.makeText(this, "You Must Enter a Name", Toast.LENGTH_LONG)
                t.show()
            }
            else {
                showDialog("Confirmation", "Do you want to save current tuning as " + tune_name.text + '?', returnIntent)
            }
        }
        tune_name.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                hideKeyboard(v)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun animateFAB() {
        if (isFabOpen) {
            fabUnfold.startAnimation(rotateBackward)
            fabSave.startAnimation(fabClose)
            fabDelete.startAnimation(fabClose)
            fabAdd.startAnimation(fabClose)
            fabSave.isClickable = false
            fabDelete.isClickable = false
            fabAdd.isClickable = false
            isFabOpen = false
        } else {
            fabUnfold.startAnimation(rotateForward)
            fabSave.startAnimation(fabOpen)
            fabDelete.startAnimation(fabOpen)
            fabAdd.startAnimation(fabOpen)
            fabSave.isClickable = true
            fabDelete.isClickable = true
            fabAdd.isClickable = true
            isFabOpen = true
        }
    }

    private fun showDialog(title: String, message: String, returnIntent: Intent){
        // Late initialize an alert dialog object
        lateinit var dialog:AlertDialog


        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle(title)

        // Set a message for alert dialog
        builder.setMessage(message)

        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                DialogInterface.BUTTON_NEUTRAL -> {}
            }
        }

        // Set the alert dialog positive/yes button
        builder.setPositiveButton("SAVE",dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("DISCARD",dialogClickListener)

        // Set the alert dialog neutral/cancel button
        builder.setNeutralButton("CANCEL",dialogClickListener)


        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }
}