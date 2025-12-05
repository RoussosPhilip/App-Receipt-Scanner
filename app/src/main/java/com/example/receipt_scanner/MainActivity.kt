package com.example.receipt_scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.receipt_scanner.navigation.AppNavigation
import com.example.receipt_scanner.ui.theme.Receipt_scannerTheme
import com.example.receipt_scanner.ui.screens.DashboardScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Receipt_scannerTheme {
                AppNavigation()
            }
        }
    }
}



//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "Preview")
//@Composable
//fun GreetingPreview() {
//    Receipt_scannerTheme {
//        Greeting("Android")
//    }
//}