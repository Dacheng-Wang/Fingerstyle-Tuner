package com.example.fingerstyleguitartuner.ui.view

interface TunerView {
    fun onShowNote(noteName: String?, frequency: Double, percentOffset: Float)
    fun onPlayNote(noteName: String?, frequency: Double, x: Float, y: Float)
}
