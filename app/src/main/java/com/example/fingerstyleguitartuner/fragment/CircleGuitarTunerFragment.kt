package com.example.fingerstyleguitartuner.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fingerstyleguitartuner.R
import com.example.fingerstyleguitartuner.stringList
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var currentPage = 0
/**
 * A simple [Fragment] subclass.
 * Use the [CircleGuitarTunerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CircleGuitarTunerFragment(private val position: Int) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        currentPage = position
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Set string text
        val view = inflater.inflate(R.layout.fragment_circle_guitar_tuner, container, false)
        view.findViewById<TextView>(R.id.string_name).text = stringList[position]
        // Inflate the layout for this fragment
        return view
        /*return if (container != null) {
            CircleTunerView(container.context)
        } else inflater.inflate(R.layout.fragment_circle_guitar_tuner, container, false)*/
    }
}
