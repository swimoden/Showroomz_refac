package com.kuwait.showroomz.extras

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi


object PermissionUtils {
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun neverAskAgainSelected(activity: Activity, permission: String?): Boolean {
        val prevShouldShowStatus = getRatinaleDisplayStatus(activity, permission)
        val currShouldShowStatus = activity.shouldShowRequestPermissionRationale(
            permission!!
        )
        return prevShouldShowStatus != currShouldShowStatus
    }

    fun setShouldShowStatus(context: Context, permission: String?) {

            val genPrefs: SharedPreferences =
                context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE)
            val editor = genPrefs.edit()
            editor.putBoolean(permission, true)
            editor.apply()

    }

    fun getRatinaleDisplayStatus(context: Context, permission: String?): Boolean {
        val genPrefs: SharedPreferences =
            context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE)
        return genPrefs.getBoolean(permission, false)
    }
    fun getNBR(context: Context, permission: String?): Int {
        val genPrefs: SharedPreferences =
            context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE)
        return genPrefs.getInt(permission+"_nbr", 0)
    }
    fun setNBR(context: Context, permission: String?) {
        val genPrefs: SharedPreferences =
            context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE)
        val editor = genPrefs.edit()
        editor.putInt(permission+"_nbr", getNBR(context,permission) + 1)
        editor.apply()
    }
}