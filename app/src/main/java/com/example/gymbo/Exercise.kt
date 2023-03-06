package com.example.gymbo

abstract class Exercise(val name: String) {
    abstract fun getSummary(): String
}

class ResistanceExercise(name: String, val weight: Int, val numSets: Int, val numReps: Int):
    Exercise(name) {

    override fun getSummary(): String {
        return "${weight}kg | $numSets sets | $numReps reps"
    }
}

class CaloriesExercise(name: String, val calories: Int):
    Exercise(name) {

    override fun getSummary(): String {
        return "$calories cals"
    }
}

class DistanceExercise(name: String, val distance: Int, val unit: DistanceUnit):
    Exercise(name) {

    override fun getSummary(): String {
        return "$distance ${unit.short}"
    }
}