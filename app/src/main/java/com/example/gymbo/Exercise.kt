package com.example.gymbo

abstract class Exercise(val name: String) {
    enum class Type(val string: String) {
        RESISTANCE("RESISTANCE"),
        CALORIES("CALORIES"),
        DISTANCE("DISTANCE")
    }

    abstract val type: Type

    abstract fun getSummary(): String
    abstract fun serialize(): String

    companion object {
        fun deserialize(string: String): Exercise {
            val stringComponents = string.split(", ")
            val typeString = stringComponents[1]

            try {
                val type = Type.valueOf(typeString)

                when (type) {
                    Type.RESISTANCE -> { return resistanceDeserialize(stringComponents) }
                    else -> {}
                }

                return resistanceDeserialize(stringComponents)
            } catch (e: java.lang.IllegalArgumentException) {
                throw e
            }
        }

        fun resistanceDeserialize(stringComponents: List<String>): ResistanceExercise {
            try {
                val name = stringComponents[0]
                val weight = stringComponents[2].toDouble()
                val reps = stringComponents[3].toInt()
                val sets = stringComponents[4].toInt()

                return ResistanceExercise(name, weight, reps, sets)
            } catch (e: java.lang.IndexOutOfBoundsException) {
                throw e
            } catch (e: java.lang.NumberFormatException) {
                throw e
            }
        }
    }
}

class ResistanceExercise(name: String, val weight: Double, val numSets: Int, val numReps: Int):
    Exercise(name) {
    override val type = Type.RESISTANCE

    override fun getSummary(): String {
        return "${weight}kg | $numSets sets | $numReps reps"
    }

    override fun serialize(): String {
        return "$name, $type, $weight, $numSets, $numReps"
    }
}

class CaloriesExercise(name: String, val calories: Int):
    Exercise(name) {
    override val type = Type.CALORIES

    override fun getSummary(): String {
        return "$calories cals"
    }

    override fun serialize(): String {
        return "$name, $type, $calories"
    }
}

class DistanceExercise(name: String, val distance: Double):
    Exercise(name) {
    override val type = Type.DISTANCE

    override fun getSummary(): String {
        return "$distance km"
    }

    override fun serialize(): String {
        return "$name, $type, $distance"
    }
}