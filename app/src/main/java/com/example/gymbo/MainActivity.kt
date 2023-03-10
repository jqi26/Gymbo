package com.example.gymbo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymbo.ui.theme.GymboTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ExerciseViewModel>()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        val exerciseStrings = sharedPreferences.getStringSet(ExerciseViewModel.EXERCISES, setOf<String>())
        val exercises = exerciseStrings?.map { Exercise.deserialize(it) }

        if (exercises != null) {
            viewModel.setExercises(exercises.toList())
        }

        setContent {
            var showDialog by remember { mutableStateOf(false) }

            GymboTheme {
                // A surface container using the 'background' color from the theme
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "profile") {
                        composable("profile") { List(viewModel,
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp, 8.dp, 8.dp, 8.dp), navController
                        ) { showDialog = true }
                        }

                        composable("exercise") {
                            SingleExercise(viewModel,
                                Modifier
                                    .fillMaxSize()
                                    .padding(8.dp, 8.dp, 8.dp, 8.dp), navController, sharedPreferences)
                        }
                    }
                }

                if (showDialog) {
                    NewExercise(viewModel, sharedPreferences) { showDialog = false }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExercise(viewModel: ExerciseViewModel, sharedPreferences: SharedPreferences, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        var selectedIndex by remember { mutableStateOf(0) }
        var continuePressed by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var dropdownVisible by remember { mutableStateOf(false) }

                val dropdownChoices = listOf("Resistance", "Calories", "Distance")

                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    supportingText = {
                        Text(errorMessage, color = Color.Red)
                    },
                    isError = isError,
                    shape = RoundedCornerShape(2.dp)
                )

                androidx.compose.material3.OutlinedTextField(
                    value = dropdownChoices[selectedIndex],
                    onValueChange = {
                    },
                    trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) },
                    modifier = Modifier.clickable(onClick = {
                        dropdownVisible = true
                    }),
                    enabled = false,
                    singleLine = true
                )

                DropdownMenu(
                    expanded = dropdownVisible,
                    onDismissRequest = { dropdownVisible = false }) {
                    dropdownChoices.forEachIndexed { index, choice ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            dropdownVisible = false
                        }) {
                            Text(choice)
                        }
                    }
                }

                Row {
                    Button(
                        onClick = { onDismissRequest() }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = {
                        if (name.isNotBlank()) {
                            isError = false
                            continuePressed = true
                        } else {
                            isError = true
                            errorMessage = "Name cannot be empty."
                        }

                    }) {
                        Text("Continue")
                    }
                }
            }
        }

        if (continuePressed) {
            val resistanceSuccess = { weight: Double, sets: Int, reps: Int ->
                viewModel.add(ResistanceExercise(name, weight, sets, reps), sharedPreferences)
                continuePressed = false
                onDismissRequest()
            }

            val failure = {
                continuePressed = false
            }

            when (selectedIndex) {
                0 -> ResistanceDialog(failure, resistanceSuccess)
//                1 -> CaloriesDialog(name, viewModel, failure })
//                2 -> DistanceDialog(name, viewModel, failure)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResistanceDialog(onFailure: () -> Unit, onSuccess: (weight: Double, sets: Int, reps: Int) -> Unit) {
    Dialog(onDismissRequest = onFailure) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ResistanceForm(null, "Back", "Add",
                    onFailure, onSuccess)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResistanceForm(
    exercise: ResistanceExercise?, negativeButtonName: String,
    positiveButtonName: String,
    onFailure: () -> Unit,
    onSuccess: (weight: Double, sets: Int, reps: Int) -> Unit
) {
    var weight by remember { mutableStateOf((exercise?.weight ?: "").toString()) }
    var sets by remember { mutableStateOf((exercise?.numSets ?: "").toString()) }
    var reps by remember { mutableStateOf((exercise?.numReps ?: "").toString()) }

    var weightError: String? by remember { mutableStateOf(null) }
    var setsError: String? by remember { mutableStateOf(null) }
    var repsError: String? by remember { mutableStateOf(null) }

    androidx.compose.material3.OutlinedTextField(
        value = weight,
        onValueChange = {
            weight = it
        },
        label = { Text("Weight (kg)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        supportingText = {
            Text(weightError ?: "", color = Color.Red)
        },
        isError = weightError != null,
    )

    androidx.compose.material3.OutlinedTextField(
        value = sets,
        onValueChange = {
            sets = it
        },
        label = { Text("Number of sets") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        supportingText = {
            Text(setsError ?: "", color = Color.Red)
        },
        isError = setsError != null,
    )

    androidx.compose.material3.OutlinedTextField(
        value = reps,
        onValueChange = {
            reps = it
        },
        label = { Text("Reps per set") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        supportingText = {
            Text(repsError ?: "", color = Color.Red)
        },
        isError = weightError != null,
    )

    Row {
        Button(onClick = onFailure, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red,
            contentColor = Color.White)) {
            Text(negativeButtonName)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            if (weight.isBlank()) {
                weightError = "Weight cannot be empty."
            } else if (sets.isBlank()) {
                setsError = "Number of sets cannot be empty."
            } else if (reps.isBlank()) {
                repsError = "Number of reps cannot be empty."
            } else if (weight.isBlank()) {
                weightError = "Weight cannot be empty."
            } else {
                onSuccess(weight.toDouble(), sets.toInt(), reps.toInt())
            }
        }) {
            Text(positiveButtonName)
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CaloriesDialog(name: String, viewModel: ExerciseViewModel, onDismissRequest: (success: Boolean) -> Unit) {
//    Dialog(onDismissRequest = { onDismissRequest(false) }) {
//        Surface(
//            modifier = Modifier
//                .wrapContentWidth()
//                .wrapContentHeight(),
//            shape = MaterialTheme.shapes.large
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                var calories by remember { mutableStateOf("") }
//                var caloriesError: String? by remember { mutableStateOf("") }
//
//                androidx.compose.material3.OutlinedTextField(
//                    value = calories,
//                    onValueChange = {
//                        calories = it
//                    },
//                    label = { Text("calories (cals)") },
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
//                    supportingText = {
//                        Text(caloriesError ?: "", color = Color.Red)
//                    },
//                    isError = caloriesError != null,
//                )
//
//                Row {
//                    Button(onClick = { onDismissRequest(false) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red,
//                        contentColor = Color.White)) {
//                        Text("Back")
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    Button(onClick = {
//                        if (calories.isBlank()) {
//                            caloriesError = "Calories cannot be empty."
//                        } else {
//                            try {
//                                val caloriesInt = calories.toInt()
//
//                                if (caloriesInt >= 0) {
//                                    viewModel.add(CaloriesExercise(name, caloriesInt))
//                                    onDismissRequest(true)
//                                } else {
//                                    caloriesError = "Calories cannot be negative."
//                                }
//                            } catch (e: java.lang.NumberFormatException) {
//                                caloriesError = "Calories must be an integer."
//                            }
//                        }
//                    }) {
//                        Text("Add")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DistanceDialog(name: String, viewModel: ExerciseViewModel, onDismissRequest: (success: Boolean) -> Unit) {
//    Dialog(onDismissRequest = { onDismissRequest(false) }) {
//        Surface(
//            modifier = Modifier
//                .wrapContentWidth()
//                .wrapContentHeight(),
//            shape = MaterialTheme.shapes.large
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                var distance by remember { mutableStateOf("") }
//                var distanceError: String? by remember { mutableStateOf("") }
//
//                androidx.compose.material3.OutlinedTextField(
//                    value = distance,
//                    onValueChange = {
//                        distance = it
//                    },
//                    label = { Text("distance (km)") },
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
//                    supportingText = {
//                        Text(distanceError ?: "", color = Color.Red)
//                    },
//                    isError = distanceError != null,
//                )
//
//                Row {
//                    Button(onClick = { onDismissRequest(false) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red,
//                        contentColor = Color.White)) {
//                        Text("Back")
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    Button(onClick = {
//                        if (distance.isBlank()) {
//                            distanceError = "Distance cannot be empty."
//                        } else {
//                            try {
//                                val distanceDouble = distance.toDouble()
//
//                                if (distanceDouble >= 0) {
//                                    viewModel.add(DistanceExercise(name, distanceDouble))
//                                    onDismissRequest(true)
//                                } else {
//                                    distanceError = "Distance cannot be negative."
//                                }
//                            } catch (e: java.lang.NumberFormatException) {
//                                distanceError = "Distance must be an integer or decimal."
//                            }
//                        }
//                    }) {
//                        Text("Add")
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun List(viewModel: ExerciseViewModel, modifier: Modifier, navController: NavController, fabAction: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = { FloatingActionButton(onClick = fabAction) {
            Image(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = "Add"
            )
        } },
    ) { padding ->
        val exercises: List<Exercise> by viewModel.exercises.observeAsState(listOf())

        LazyColumn(modifier = modifier.padding(padding)) {
            items(exercises) {
                Exercise(it, viewModel, navController)
            }
        }
    }
}

@Composable
fun SingleExercise(viewModel: ExerciseViewModel, modifier: Modifier, navController: NavController, sharedPreferences: SharedPreferences) {
    Column(modifier = modifier) {
        val selectedExercise = viewModel.selectedExercise
        if (selectedExercise != null) {
            if (selectedExercise is ResistanceExercise) {
                ResistanceForm(
                    selectedExercise,
                    "Delete",
                    "Save",
                    onSuccess = { weight: Double, sets: Int, reps: Int ->
                        selectedExercise.weight = weight
                        selectedExercise.numSets = sets
                        selectedExercise.numReps = reps

                        viewModel.update(selectedExercise, sharedPreferences)
                        navController.popBackStack()
                    },
                    onFailure = {
                        viewModel.remove(selectedExercise, sharedPreferences)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun Exercise(exercise: Exercise, viewModel: ExerciseViewModel, navController: NavController) {
    Column(modifier = Modifier.clickable {
        navController.navigate("exercise")
        viewModel.selectedExercise = exercise
    }) {
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(text = exercise.name, style = MaterialTheme.typography.h5)

            Spacer(Modifier.weight(1f))

            Text(text = exercise.getSummary(), style = MaterialTheme.typography.body1)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}