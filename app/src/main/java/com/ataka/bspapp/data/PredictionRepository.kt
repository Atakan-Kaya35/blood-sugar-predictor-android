package com.ataka.bspapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object PredictionRepository {
    private val _predictions = MutableLiveData<String>() // Example type
    val predictions: LiveData<String> = _predictions

    fun updatePredictions(newData: String) {
        _predictions.postValue(newData)
    }
}
