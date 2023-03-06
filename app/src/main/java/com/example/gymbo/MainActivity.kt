package com.example.gymbo

import android.graphics.fonts.FontStyle
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.gymbo.ui.theme.GymboTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val viewModel by viewModels<ExerciseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showDialog by remember { mutableStateOf(false) }
            val scaffoldState: ScaffoldState = rememberScaffoldState()
            val coroutineScope: CoroutineScope = rememberCoroutineScope()

            GymboTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "Add"
                        )
                    } },
                    scaffoldState = scaffoldState
                ) {
                    List(viewModel,
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp, 8.dp, 8.dp, 8.dp)
                            .padding(it))
                }

                if (showDialog) {
                    NewExercise(onDismissRequest = { showDialog = false }, errorSnackBar = {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = it,
                                actionLabel = "OK"
                            )
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun NewExercise(onDismissRequest: () -> Unit, errorSnackBar: (message: String) -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        var selectedIndex by remember { mutableStateOf(0) }
        var continuePressed by remember { mutableStateOf(false) }

        Scaffold {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(it),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var name by remember { mutableStateOf("") }
                    var dropdownVisible by remember { mutableStateOf(false) }

                    val dropdownChoices = listOf("Resistance", "Calories", "Distance")

                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = { Text("Name") },
                        singleLine = true
                    )

                    TextField(
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
                            if (name.isNotBlank())
                                continuePressed = true
                            else
                                errorSnackBar("Name cannot be blank")
                        }) {
                            Text("Continue")
                        }
                    }
                }
            }

            if (continuePressed) {
                when (selectedIndex) {
                    0 -> ResistanceDialog { continuePressed = false }
                }
            }
        }
    }
}

@Composable
fun ResistanceDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var name by remember { mutableStateOf("") }

                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text("Name") },
                    singleLine = true
                )

                Row {
                    Button(onClick = { onDismissRequest() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red,
                        contentColor = Color.White)) {
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = { /*TODO*/ }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun List(viewModel: ExerciseViewModel, modifier: Modifier) {
    val exercises: List<Exercise> by viewModel.exercises.observeAsState(listOf())

    LazyColumn(modifier = modifier) {
        items(exercises) {
            Exercise(it)
        }
    }
}

@Composable
fun Exercise(exercise: Exercise) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(text = exercise.name, style = MaterialTheme.typography.h5)

            Spacer(Modifier.weight(1f))

            Text(text = exercise.getSummary(), style = MaterialTheme.typography.body1)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}