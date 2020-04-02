package com.example.fingerstyleguitartuner.ui.view

interface TunerPitchToggleView {

    fun showGuitarTuner()

    fun showPitchPlayback(note: String?, frequency: Double, x: Float, y: Float, animate: Boolean)
}