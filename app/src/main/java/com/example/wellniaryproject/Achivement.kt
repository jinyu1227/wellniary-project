package com.example.wellniaryproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke


@Composable
fun AchievementSection(navController: NavController, viewModel: HealthViewModel) {
    val mugCount = viewModel.currentMugCount
    val mugTarget = viewModel.dailyMugTarget
    val currentWeight = viewModel.currentWeight
    val targetWeight = viewModel.targetWeight

    val waterGoalUnlocked = mugCount >= mugTarget
    val weightGoalUnlocked = currentWeight <= targetWeight
    val streakUnlocked = waterGoalUnlocked && weightGoalUnlocked

    AchievementCard {
        Text(
            text = "🏆 My Achievements",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AchievementIcon(title = "Waater Goal", unlocked = waterGoalUnlocked)
            AchievementIcon(title = "Target Weight Reached", unlocked = weightGoalUnlocked)
            AchievementIcon(title = "30-Day Streak", unlocked = streakUnlocked)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { navController.navigate("achievement_detail") }) {
                Text("View All Achievements")
            }
        }
    }
}

@Composable
fun AchievementDetailScreen(viewModel: HealthViewModel) {
    val mugCount = viewModel.currentMugCount
    val mugTarget = viewModel.dailyMugTarget
    val currentWeight = viewModel.currentWeight
    val targetWeight = viewModel.targetWeight

    val waterGoalUnlocked = mugCount >= mugTarget
    val weightGoalUnlocked = currentWeight <= targetWeight
    val streakUnlocked = waterGoalUnlocked && weightGoalUnlocked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            "Achievement Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AchievementDetailItem("🥤 Reached target amount of water", waterGoalUnlocked)
        AchievementDetailItem("🎯 Reached target weight", weightGoalUnlocked)
        AchievementDetailItem("📆 Logged in 7 days straight", viewModel.consecutiveLoginUnlocked)


    }
}

@Composable
fun AchievementIcon(title: String, unlocked: Boolean) {
    val backgroundColor = if (unlocked) Color(0xFFD1E7DD) else Color.LightGray
    val textColor = if (unlocked) Color(0xFF0F5132) else Color.DarkGray

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun AchievementDetailItem(description: String, unlocked: Boolean) {
    val statusText = if (unlocked) "✅ Unlocked" else "🔒 Locked"
    val statusColor = if (unlocked) Color(0xFF198754) else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FB)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: $statusText",
                fontSize = 14.sp,
                color = statusColor
            )
        }
    }
}

@Composable
fun AchievementCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}