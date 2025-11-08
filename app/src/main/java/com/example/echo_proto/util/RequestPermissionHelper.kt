package com.example.echo_proto.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun requestPermissionHelper(
    activity: Activity,
    permission: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    requestCode: Int
) {
    if (ContextCompat.checkSelfPermission(activity, permission)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestCode
        )
    }
}
