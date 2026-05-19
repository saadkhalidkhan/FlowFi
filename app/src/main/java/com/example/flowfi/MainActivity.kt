package com.example.flowfi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flowfi.ui.navigation.FlowFiNavHost
import com.example.flowfi.ui.theme.FlowFiTheme
import com.example.flowfi.viewmodel.TransactionViewModel
import com.example.flowfi.viewmodel.TransactionViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowFiTheme {
                val application = application as FlowFiApplication
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(application.repository)
                )
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlowFiNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
