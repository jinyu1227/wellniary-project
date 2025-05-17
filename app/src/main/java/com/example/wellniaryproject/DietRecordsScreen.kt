package com.example.wellniaryproject

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DietRecordsScreen(
    navController: NavHostController,
    viewModel: DietLogViewModel = viewModel()
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val userLogs by viewModel.getLogsForUser(uid ?: "").collectAsState(initial = emptyList())
    val groupedRecords = userLogs.groupBy { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diet Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color(0xFFBBDEFB),
                contentColor = Color.Black
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                backgroundColor = Color(0xFFF0F8FF),
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    groupedRecords.forEach { (date, records) ->
                        item {
                            Text(
                                text = "📅 $date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0D47A1),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(records) { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = buildString {
                                        append("⤷ [${record.mealType}] ")
                                        append("Staple: ${record.staple}, ")
                                        append("Meat: ${record.meat}, ")
                                        append("Vegetables: ${record.vegetable}, ")
                                        append("Others: ${record.other}")
                                    },
                                    fontSize = 15.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "🗑️",
                                    fontSize = 16.sp,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.deleteLog(record.id)
                                            Toast
                                                .makeText(context, "Record deleted", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
