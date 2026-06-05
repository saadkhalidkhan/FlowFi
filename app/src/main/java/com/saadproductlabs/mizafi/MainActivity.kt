package com.saadproductlabs.mizafi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saadproductlabs.mizafi.ui.navigation.MizafiNavHost
import com.saadproductlabs.mizafi.ui.theme.MizafiTheme
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModel
import com.saadproductlabs.mizafi.viewmodel.TransactionViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MizafiTheme {
                val application = application as MizafiApplication
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(
                        application.repository,
                        application.savingsGoalRepository
                    )
                )
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MizafiNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
