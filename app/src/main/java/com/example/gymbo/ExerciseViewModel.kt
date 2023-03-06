package com.example.gymbo

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExerciseViewModel: ViewModel() {
    var selectedExercise: Exercise? = null

    private val _exercises = MutableLiveData<List<Exercise>>(mutableListOf(
        ResistanceExercise("Bicep curl", 15.0, 3, 10),
        DistanceExercise("Treadmill", 5.0),
        CaloriesExercise("Rowing machine", 200)
    ))

    val exercises: LiveData<List<Exercise>>
        get() = _exercises

    fun add(exercise: Exercise, sharedPreferences: SharedPreferences) {
        _exercises.value = _exercises.value?.plus(exercise)
        saveList(sharedPreferences)
    }

    fun update(exercise: Exercise, sharedPreferences: SharedPreferences) {
        _exercises.value = _exercises.value?.minusElement(exercise)
        add(exercise, sharedPreferences)
    }

    private fun saveList(sharedPreferences: SharedPreferences) {
        with(sharedPreferences.edit()) {
            putStringSet(EXERCISES, exercises.value?.map{ it.serialize() }?.toSet())
            apply()
        }
    }

    fun remove(index: Int) {
        val exercises = _exercises.value

        if (exercises != null) {
            _exercises.value = exercises.subList(0, index).plus(exercises.subList(index + 1, exercises.count()))
        }
    }

    fun setExercises(items: List<Exercise>) {
        _exercises.value = items.toMutableList()
    }

    companion object {
        const val EXERCISES = "Exercises"
    }
}
