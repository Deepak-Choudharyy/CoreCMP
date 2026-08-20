package com.corecmp.shared.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.corecmp.shared.CoreCmp
import kotlinx.coroutines.launch

@Composable
fun AppLockGate(
    manager: AppLockManager = CoreCmp.appLock,
    content: @Composable () -> Unit,
) {
    if (!manager.isEnabled() || manager.isUnlocked) {
        content()
        return
    }

    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("App locked", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            singleLine = true,
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                if (manager.unlockWithPin(pin)) {
                    error = null
                } else {
                    error = "Incorrect PIN"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Unlock")
        }
        if (manager.isBiometricAvailable) {
            Button(
                onClick = {
                    scope.launch {
                        if (!manager.unlockWithBiometric()) {
                            error = "Biometric authentication failed"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("Use biometrics")
            }
        }
    }
}
