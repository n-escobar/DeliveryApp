package com.example.deliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.deliveryapp.ui.navigation.AppNavigation
import com.example.deliveryapp.ui.theme.DeliveryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeliveryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Role Selection Screen
                    var selectedRole by remember { mutableStateOf<String?>(null) }

                    if (selectedRole == null) {
                        RoleSelectionScreen(
                            onRoleSelected = { role -> selectedRole = role }
                        )
                    } else {
                        AppNavigation(userRole = selectedRole!!)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Select User Type",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRoleSelected("SHOPPER") },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Continue as Shopper", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onRoleSelected("DELIVERER") },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Continue as Deliverer", style = MaterialTheme.typography.titleMedium)
        }
    }
}