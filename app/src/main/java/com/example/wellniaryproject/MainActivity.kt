package com.example.wellniaryproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wellniaryproject.ui.theme.Ass3Theme
//noinspection UsingMaterialAndMaterial3Libraries,UsingMaterialAndMaterial3Librariesht
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


data class NavRoute(val route:String, val icon: ImageVector, val label: String)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ass3Theme {
                BottomNavigationBar()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavigationBar(){
    val navRoutes = listOf(
        NavRoute("home", Icons.Filled.Home,"Home"),
        NavRoute("form", Icons.Filled.Edit,"Intake"),
        NavRoute("report", Icons.Filled.List,"Report"),
        NavRoute("me", Icons.Filled.Person,"Me")
    )
    val navController = rememberNavController()

    Scaffold (
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.padding(bottom = 20.dp),
                backgroundColor = Color(0xFFADD8E6))
            {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navRoutes.forEach { navRoute ->
                    BottomNavigationItem(
                        icon = { Icon(navRoute.icon, contentDescription = navRoute.label) },
                        label = { Text(navRoute.label) },
                        selected = currentDestination?.route == navRoute.route,
                        onClick = {
                            navController.navigate(navRoute.route){
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
    ){paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { Home(navController) }
            composable("form") { Form(navController) }
            composable("report") { Report(navController) }
            composable ("me"){ Me(navController)}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Ass3Theme {
        Greeting("Android")
    }
}