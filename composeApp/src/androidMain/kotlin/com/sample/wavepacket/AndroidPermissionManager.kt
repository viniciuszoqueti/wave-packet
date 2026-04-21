package com.sample.wavepacket

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class AndroidPermissionManager {
    private var launcher: ActivityResultLauncher<String>? = null
    private val permissionResult = MutableSharedFlow<Boolean>()

    fun register(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            activity.lifecycle.run {
                permissionResult.tryEmit(isGranted)
            }
        }
    }

    suspend fun requestPermission(permission: String): Boolean {
        launcher?.launch(permission) ?: return false
        return permissionResult.first()
    }
}
