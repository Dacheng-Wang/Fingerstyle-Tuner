package com.example.fingerstyleguitartuner.ui.view

interface TunerWidgetView {
    /**
     * Updates the view to display the provided information about a Note.
     *
     * @param noteName      The String to display that represents the current note.
     * @param frequency     The frequency of the current note.
     * @param percentOffset The percent offset used to illustrate the difference between the current
     * frequency and the closest supported frequency values.
     */
    fun updateNote(noteName: String?, frequency: Double, percentOffset: Float)
}
