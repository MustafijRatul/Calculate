package codestitcher.calcpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import codestitcher.calcpro.navigation.Screen
import codestitcher.calcpro.ui.screens.*
import codestitcher.calcpro.ui.theme.CalcProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            var showSplash by remember { mutableStateOf(true) }

            CalcProTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                        },
                        label = "SplashToMainTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(
                                onSplashFinished = { showSplash = false }
                            )
                        } else {
                            val navController = rememberNavController()

                            NavHost(
                                navController = navController,
                                startDestination = Screen.Calculator.route,
                                enterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
                                exitTransition = { fadeOut(tween(200)) },
                                popEnterTransition = { fadeIn(tween(250)) },
                                popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
                            ) {
                                // Main Calculator
                                composable(Screen.Calculator.route) {
                                    CalculatorScreen(
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                                        onNavigateToRoute = { route ->
                                            navController.navigate(route)
                                        }
                                    )
                                }

                                // Converters
                                composable(Screen.CurrencyConverter.route) {
                                    CurrencyConverterScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.UnitConverter.route) {
                                    UnitConverterScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
