package com.kuwait.showroomz.extras.managers

import android.annotation.SuppressLint
import android.provider.Settings
import com.kuwait.showroomz.extras.MyApplication


class DeviceManger {
    companion object {
        fun getDeviceId(): String? {
            val android_id = Settings.Secure.getString(
                MyApplication.context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            return android_id

        }
    }
}