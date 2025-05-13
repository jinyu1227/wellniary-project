package com.example.wellniaryproject

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wellniaryproject.ui.theme.BottomNavigationBarTheme
import androidx.core.view.WindowCompat
import com.example.ass3.Intake
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val viewModel: UserDataViewModel by viewModels()
        setContent {
            BottomNavigationBarTheme {
                BottomNavigationBar(viewModel)
            }
        }
    }
}

data class NavRoute(val route: String, val icon: ImageVector, val label:
String )

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavigationBar(viewModel: UserDataViewModel) {
// Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute("home", Icons.Filled.Home, "Home"),
        NavRoute("intake", Icons.Filled.Edit, "Intake"),
        NavRoute("report", Icons.Filled.List, "Report"),
        NavRoute("me", Icons.Filled.Person, "Me")
    )
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.padding(bottom = 20.dp),
                backgroundColor = Color(0xFFADD8E6)
            ) //light blue color
            {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navRoutes.forEach { navRoute ->
                    BottomNavigationItem(
                        icon = { Icon(navRoute.icon, contentDescription =
                            navRoute.label) },
                        label = { Text(navRoute.label) },
                        selected = currentDestination?.route == navRoute.route,
                        onClick = {
                            navController.navigate(navRoute.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { Home(navController) }
            composable("intake") { Intake(navController) }
            composable("report") { ReportScreen(navController) }
            composable("me") { Me(navController) }
        }
    }
}

class UserDataViewModel : ViewModel() {
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var dateOfBirth by mutableStateOf("")
    var state by mutableStateOf("")
}




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BottomNavigationBarTheme {
        Greeting("Android")
    }
}