package com.example.wellniaryproject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图
        Image(
            painter = painterResource(id = R.drawable.splash_bg), // 替换为你的图片名
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 文本：居中显示，放在胸口偏上
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp), // 这个值根据实际图像位置可调
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "A Healthier Life Starts with\nWellniary", // 👈 换行写法
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 36.sp, // 行距
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
