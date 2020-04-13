package com.example.fingerstyleguitartuner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fingerstyleguitartuner.ui.MyAdapter
import com.example.fingerstyleguitartuner.ui.RecyclerItemClickListener
import com.example.fingerstyleguitartuner.ui.calculateFrequency
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import net.mabboud.android_tone_player.OneTimeBuzzer
import java.io.Serializable


const val LAUNCH_ADD_TUNE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager
    private var tuneIndex = 0
    private var list = ArrayList<String>()
    private var letterLists = ArrayList<ArrayList<String>>()
    private var numberLists = ArrayList<ArrayList<Int>>()
    private var sharpLists = ArrayList<ArrayList<Int>>()
    private var frequencyLists = ArrayList<FloatArray>()
    private var nameLists = ArrayList<String>()
    private lateinit var pref: SharedPreferences
    private var isReset = false
    private var itemSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json = pref.getString("data", null)
        if (json != null) {
            val dataHelper = gson.fromJson(json, DataHelper::class.java)
            list = dataHelper.getTunes()
            nameLists = dataHelper.getNames()
            letterLists = dataHelper.getLetters()
            numberLists = dataHelper.getNumbers()
            sharpLists = dataHelper.getSharps()
            frequencyLists = dataHelper.getFrequencies()
        }
        else {
            list.add("Standard Tuning (EADGBE)")
            nameLists.add("Standard Tuning")
            letterLists.add(arrayListOf("E", "A", "D", "G", "B", "E"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("E", 2), calculateFrequency("A", 2),
                calculateFrequency("D", 3), calculateFrequency("G", 3),
                calculateFrequency("B", 3), calculateFrequency("E", 4)))

            list.add("Drop D (DADGBE)")
            nameLists.add("Drop D")
            letterLists.add(arrayListOf("D", "A", "D", "G", "B", "E"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("D", 2), calculateFrequency("A", 2),
                calculateFrequency("D", 3), calculateFrequency("G", 3),
                calculateFrequency("B", 3), calculateFrequency("E", 4)))

            list.add("Celtic Tuning (DADGAD)")
            nameLists.add("Celtic Tuning")
            letterLists.add(arrayListOf("D", "A", "D", "G", "A", "D"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("D", 2), calculateFrequency("A", 2),
                calculateFrequency("D", 3), calculateFrequency("G", 3),
                calculateFrequency("A", 3), calculateFrequency("D", 4)))

            list.add("D Standard (DGCFAD)")
            nameLists.add("D Standard")
            letterLists.add(arrayListOf("D", "G", "C", "F", "A", "D"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("D", 2), calculateFrequency("G", 2),
                calculateFrequency("C", 3), calculateFrequency("F", 3),
                calculateFrequency("A", 3), calculateFrequency("D", 4)))

            list.add("Open D (DADF#AD)")
            nameLists.add("Open D")
            letterLists.add(arrayListOf("D", "A", "D", "F", "A", "D"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 1, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("D", 2), calculateFrequency("A", 2),
                calculateFrequency("D", 3), calculateFrequency("F#", 3),
                calculateFrequency("A", 3), calculateFrequency("D", 4)))

            list.add("Open C (CGCGCE)")
            nameLists.add("Open C")
            letterLists.add(arrayListOf("C", "G", "C", "G", "C", "E"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 4, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("C", 2), calculateFrequency("G", 2),
                calculateFrequency("C", 3), calculateFrequency("G", 3),
                calculateFrequency("C", 4), calculateFrequency("E", 4)))

            list.add("Drop C (CGCFAD)")
            nameLists.add("Drop C")
            letterLists.add(arrayListOf("C", "G", "C", "F", "A", "D"))
            numberLists.add(arrayListOf(2, 2, 3, 3, 3, 4))
            sharpLists.add(arrayListOf(0, 0, 0, 0, 0, 0))
            frequencyLists.add(floatArrayOf(calculateFrequency("C", 2), calculateFrequency("G", 2),
                calculateFrequency("C", 3), calculateFrequency("F", 3),
                calculateFrequency("A", 3), calculateFrequency("D", 4)))
        }

        setContentView(R.layout.activity_main)
        toolbar.title = "Fingerstyle Tuner"
        setSupportActionBar(toolbar)
        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = MyAdapter(list)
        recyclerView = findViewById<RecyclerView>(R.id.tuneView).apply {
            layoutManager = recyclerViewLayoutManager
            adapter = recyclerViewAdapter
        }

        registerForContextMenu(recyclerView)
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerView, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    itemSelected = position
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                                Manifest.permission.RECORD_AUDIO)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                            lateinit var dialog: AlertDialog


                            // Initialize a new instance of alert dialog builder object
                            val builder = AlertDialog.Builder(this@MainActivity)

                            // Set a title for alert dialog
                            builder.setTitle(title)

                            // Set a message for alert dialog
                            builder.setMessage("Without this permission, the app is unable to detect the tuning of your instrument.")

                            // On click listener for dialog buttons
                            val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
                                when(which){
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        ActivityCompat.requestPermissions(this@MainActivity,
                                            arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                                    }
                                    DialogInterface.BUTTON_NEUTRAL -> {}
                                }
                            }
                            // Set the alert dialog positive/yes button
                            builder.setPositiveButton("RE-TRY",dialogClickListener)
                            // Set the alert dialog neutral/cancel button
                            builder.setNeutralButton("CANCEL",dialogClickListener)
                            // Initialize the AlertDialog using builder object
                            dialog = builder.create()
                            // Finally, display the alert dialog
                            dialog.show()
                        } else {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                1)
                        }
                    } else {
                        // Permission has already been granted
                        val intent = Intent(this@MainActivity, DisplayTuner::class.java)
                        val noteList = combineLetterAndSharp(letterLists[position], sharpLists[position])
                        intent.putExtra("note", noteList)
                        intent.putExtra("frequency", frequencyLists[position])
                        intent.putExtra("name", nameLists[position])
                        startActivity(intent)
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                    tuneIndex = position
                    openContextMenu(view)
                }
            })
        )

        fab.setOnClickListener {
            if (::buzzer.isInitialized) {
                buzzer.stop()
                timer.cancel()
            }
            val intent = Intent(this@MainActivity, AddTune::class.java)
            startActivityForResult(intent, LAUNCH_ADD_TUNE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(this@MainActivity, DisplayTuner::class.java)
                    val noteList = combineLetterAndSharp(letterLists[itemSelected], sharpLists[itemSelected])
                    intent.putExtra("note", noteList)
                    intent.putExtra("frequency", frequencyLists[itemSelected])
                    intent.putExtra("name", nameLists[itemSelected])
                    startActivity(intent)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ADD_TUNE) {
            if (resultCode == Activity.RESULT_OK) {
                val frequencyResult = data?.getFloatArrayExtra("frequency")
                val name = data?.getStringExtra("name")
                val letter = data?.getStringArrayListExtra("letter")
                val number = data?.getIntegerArrayListExtra("number")
                val sharp = data?.getIntegerArrayListExtra("sharp")
                val isEdit = data?.getBooleanExtra("isEdit", false)
                println(frequencyResult.toString())
                var insertIndex = recyclerViewAdapter.itemCount
                if (name != null && letter != null && isEdit != null) {
                    if (isEdit) {
                        insertIndex = tuneIndex
                        list[insertIndex] = name + " (" + combineLetterAndSharp(letter, sharp as ArrayList<Int>).joinToString("") + ")"
                        nameLists[insertIndex] = name
                        letterLists[insertIndex] = letter
                        if (number != null) {
                            numberLists[insertIndex] = number
                        }
                        sharpLists[insertIndex] = sharp
                        if (frequencyResult != null) {
                            frequencyLists[insertIndex] = frequencyResult
                        }
                        recyclerViewAdapter.notifyItemChanged(insertIndex)
                    }
                    else {
                        list.add(insertIndex, name + " (" + combineLetterAndSharp(letter, sharp as ArrayList<Int>).joinToString("") + ")")
                        nameLists.add(name)
                        letterLists.add(letter)
                        if (number != null) {
                            numberLists.add(number)
                        }
                        sharpLists.add(sharp)
                        if (frequencyResult != null) {
                            frequencyLists.add(frequencyResult)
                        }
                        recyclerViewAdapter.notifyItemInserted(insertIndex)
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                println("Nothing is returned")
            }
        }
    }

    private fun combineLetterAndSharp(letter: ArrayList<String>, sharp: ArrayList<Int>): ArrayList<String> {
        val result = ArrayList<String>()
        for (i in 0 until letter.size) {
            result.add(letter[i] + if (sharp[i] == 1) "#" else "")
        }
        return result
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View?,
        menuInfo: ContextMenuInfo?
    )
    {
        //Context menu
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_context, menu)
    }
    lateinit var buzzer: OneTimeBuzzer
    lateinit var timer: CountDownTimer
    override fun onContextItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.delete ->
            {
                list.removeAt(tuneIndex)
                nameLists.removeAt(tuneIndex)
                letterLists.removeAt(tuneIndex)
                numberLists.removeAt(tuneIndex)
                sharpLists.removeAt(tuneIndex)
                frequencyLists.removeAt(tuneIndex)
                recyclerViewAdapter.notifyItemRemoved(tuneIndex)
                return true
            }
            R.id.edit ->
            {
                val intent = Intent(this@MainActivity, AddTune::class.java)
                intent.putExtra("name", nameLists[tuneIndex])
                intent.putExtra("letter", letterLists[tuneIndex])
                intent.putExtra("number", numberLists[tuneIndex])
                intent.putExtra("sharp", sharpLists[tuneIndex])
                intent.putExtra("frequency", frequencyLists[tuneIndex])
                startActivityForResult(intent, LAUNCH_ADD_TUNE)
                return true
            }
            R.id.play ->
            {
                val interval = 1000L
                val time = interval * frequencyLists[tuneIndex].count()
                var count = 0
                if (::buzzer.isInitialized) {
                    buzzer.stop()
                    timer.cancel()
                }
                timer = object: CountDownTimer(time, interval) {
                    override fun onFinish() {
                    }
                    override fun onTick(millisUntilFinished: Long) {
                        val volume: Int
                        val frequency = frequencyLists[tuneIndex][count]
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
                        buzzer = OneTimeBuzzer()
                        buzzer.duration = interval / 1000.0
                        buzzer.volume = volume
                        buzzer.toneFreqInHz = frequency.toDouble()
                        buzzer.play()
                        count += 1
                    }
                }
                timer.start()
                return true
            }

            else -> super.onContextItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_reset -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Reset Tune List")
                builder.setMessage("Are you sure you want to restore the default tune list? This will delete all saved tunes and restart the app.")

                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val editor = pref.edit()
                            editor.clear()
                            editor.apply()
                            val intent = intent
                            isReset = true
                            finish()
                            startActivity(intent)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                // Set the alert dialog positive/yes button
                builder.setPositiveButton("Yes",dialogClickListener)

                // Set the alert dialog negative/no button
                builder.setNegativeButton("No",dialogClickListener)

                val dialog = builder.create()
                dialog.show()
                return true
            }
            R.id.action_license -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                return true
            }
            R.id.action_source -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Dacheng-Wang/Fingerstyle-Tuner"))
                startActivity(browserIntent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isReset) {
            val editor = pref.edit()
            val gson = Gson()
            val json = gson.toJson(DataHelper(list, nameLists, letterLists, numberLists, sharpLists, frequencyLists))
            editor.putString("data", json)
            editor.apply()
        }
        isReset = false
    }

}

class DataHelper(private val tuneList: ArrayList<String>, private val nameList: ArrayList<String>,
                 private val letterList: ArrayList<ArrayList<String>>, private val numberList: ArrayList<ArrayList<Int>>,
                 private val sharpList: ArrayList<ArrayList<Int>>, private val frequencyList: ArrayList<FloatArray>): Serializable {

    fun getTunes(): ArrayList<String> {
        return this.tuneList
    }

    fun getNames(): ArrayList<String> {
        return this.nameList
    }

    fun getLetters(): ArrayList<ArrayList<String>> {
        return this.letterList
    }
    fun getNumbers(): ArrayList<ArrayList<Int>> {
        return this.numberList
    }
    fun getSharps(): ArrayList<ArrayList<Int>> {
        return this.sharpList
    }
    fun getFrequencies(): ArrayList<FloatArray> {
        return this.frequencyList
    }
}