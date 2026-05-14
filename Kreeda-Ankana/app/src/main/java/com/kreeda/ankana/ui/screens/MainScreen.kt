package com.kreeda.ankana.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kreeda.ankana.R

/**
 * Navigation destinations for the bottom navigation bar.
 */
sealed class Screen(
    val route: String,
    val resourceId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home)
    object Calendar : Screen("calendar", R.string.tab_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Challenges : Screen("challenges", R.string.tab_challenges, Icons.Filled.SportsKabaddi, Icons.Outlined.SportsKabaddi)
    object ScoreWall : Screen("score_wall", R.string.tab_score_wall, Icons.Filled.Scoreboard, Icons.Outlined.Scoreboard)
    object Chat : Screen("chat", R.string.tab_ai_assistant, Icons.Filled.SmartToy, Icons.Outlined.SmartToy)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Challenges,
    Screen.ScoreWall,
    Screen.Chat
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.resourceId)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(screen.resourceId),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Challenges.route) { ChallengeScreen() }
            composable(Screen.ScoreWall.route) { ScoreWallScreen() }
            composable(Screen.Chat.route) { ChatAssistantScreen() }
        }
    }
}
