package com.example.wellniaryproject

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            navController.navigate("me") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    if (currentUser == null) return

    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val record by viewModel.record.collectAsState()
    val dailyQuote by viewModel.quote.collectAsState()
//    var waterGoal by remember { mutableStateOf(8) }
//    var weightGoal by remember { mutableStateOf(60f) }

    val goalPair by viewModel.userGoals.collectAsState()
    var waterGoal by remember { mutableStateOf(8) }
    var weightGoal by remember { mutableStateOf(60f) }

    LaunchedEffect(goalPair) {
        goalPair?.let {
            waterGoal = it.first
            weightGoal = it.second
        }
    }


    var waterCount by remember { mutableStateOf(0) }
    var confirmedWeight by remember { mutableStateOf("") }

    var showWeightDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadRecordOnce()
        viewModel.loadDailyQuote()
        viewModel.loadUserGoals()
    }

    LaunchedEffect(record) {
        record?.let {
            waterCount = it.waterCount
            confirmedWeight = it.weight.toString()
        }
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Weight Saving Success ✅")
            }
            showSnackbar = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to your Wellness Diary!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.wellness_image),
                contentDescription = "Wellness Image",
                modifier = Modifier.height(240.dp).fillMaxWidth()
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💬 Daily Motivation", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(dailyQuote, fontSize = 16.sp, color = Color(0xFF6D4C41))
                }
            }

            InfoCard(
                title = "Health Goal Setting",
                backgroundColor = Color(0xFFFFF3E0),
                onClick = { showGoalDialog = true },
                content = {
                    Text("• Daily water target：$waterGoal cup")
                    Text("• Target weight：${weightGoal}kg")
                    if (waterCount >= waterGoal) {
                        Text("🎉 Water goal achieved!", color = Color(0xFF388E3C))
                    }
                    val current = confirmedWeight.toFloatOrNull()
                    if (current != null && current <= weightGoal) {
                        Text("💪 Weight goal reached!", color = Color(0xFF00796B))
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    icon = Icons.Filled.LocalDrink,
                    label = "Mug: $waterCount cup",
                    backgroundColor = Color(0xFFB8E0F4),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    extraContent = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {
                                if (waterCount > 0) {
                                    waterCount--
                                    viewModel.saveRecord(waterCount, confirmedWeight.toFloatOrNull() ?: 0f)
                                }
                            }) {
                                Text("-", color = Color.White, fontSize = 18.sp)
                            }
                            Button(onClick = {
                                waterCount++
                                viewModel.saveRecord(waterCount, confirmedWeight.toFloatOrNull() ?: 0f)
                            }) {
                                Text("+", color = Color.White, fontSize = 18.sp)
                            }
                        }
                    }
                )

                FeatureCard(
                    icon = Icons.Filled.MonitorWeight,
                    label = "Weight\nToday weight: $confirmedWeight kg",
                    backgroundColor = Color(0xFFC8E6C9),
                    onClick = { showWeightDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (showWeightDialog) {
            AlertDialog(
                onDismissRequest = { showWeightDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        confirmedWeight = weightInput
                        viewModel.saveRecord(waterCount, weightInput.toFloat())
                        showWeightDialog = false
                        showSnackbar = true
                    }, enabled = weightInput.toFloatOrNull()?.let { it in 30f..300f } == true) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeightDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Enter weight") },
                text = {
                    Column {
                        Text("Please enter today's weight (kg)")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = weightInput,
                            onValueChange = {
                                weightInput = it
                                errorText = if (it.toFloatOrNull() == null || it.toFloat() !in 30f..150f)
                                    "Please enter a legal number in the range of 30-150kg."
                                else ""
                            },
                            isError = errorText.isNotEmpty(),
                            placeholder = { Text("For example：65.5") },
                            singleLine = true
                        )
                        if (errorText.isNotBlank()) {
                            Text(errorText, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            )
        }

        if (showGoalDialog) {
            var newWaterGoal by remember { mutableStateOf(waterGoal.toString()) }
            var newWeightGoal by remember { mutableStateOf(weightGoal.toString()) }

            var waterError by remember { mutableStateOf("") }
            var weightError by remember { mutableStateOf("") }

            val isWaterValid = newWaterGoal.toIntOrNull()?.let { it in 3..15 } == true
            val isWeightValid = newWeightGoal.toFloatOrNull()?.let { it in 30f..150f } == true

            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (isWaterValid && isWeightValid) {
                                waterGoal = newWaterGoal.toInt()
                                weightGoal = newWeightGoal.toFloat()
                                viewModel.updateGoals(waterGoal, weightGoal)
                                showGoalDialog = false
                            }
                        },
                        enabled = isWaterValid && isWeightValid
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Edit Health Goals") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = newWaterGoal,
                            onValueChange = {
                                newWaterGoal = it
                                waterError = if (it.toIntOrNull()?.let { n -> n in 3..15 } != true)
                                    "Please enter 3–15 cups."
                                else ""
                            },
                            label = { Text("Daily water target (cups)") },
                            isError = waterError.isNotEmpty(),
                            singleLine = true
                        )
                        if (waterError.isNotEmpty()) {
                            Text(waterError, color = Color.Red, fontSize = 13.sp)
                        }

                        TextField(
                            value = newWeightGoal,
                            onValueChange = {
                                newWeightGoal = it
                                weightError = if (it.toFloatOrNull()?.let { w -> w in 30f..150f } != true)
                                    "Please enter 30–150 kg."
                                else ""
                            },
                            label = { Text("Target weight (kg)") },
                            isError = weightError.isNotEmpty(),
                            singleLine = true
                        )
                        if (weightError.isNotEmpty()) {
                            Text(weightError, color = Color.Red, fontSize = 13.sp)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    backgroundColor: Color = Color.White,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            content()
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
                Text(
                    label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
            extraContent?.invoke()
        }
    }
}