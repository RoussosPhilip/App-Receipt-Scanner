package com.devphilip.receipt_scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devphilip.receipt_scanner.navigation.AppNavigation
import com.devphilip.receipt_scanner.ui.theme.Receipt_scannerTheme


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