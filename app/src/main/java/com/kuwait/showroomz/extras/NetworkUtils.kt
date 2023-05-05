package com.kuwait.showroomz.extras

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException


class NetworkUtils {
    var connected = true
    private object HOLDER {
        val INSTANCE = NetworkUtils()
    }
    companion object {
        val instance: NetworkUtils by lazy { HOLDER.INSTANCE }
    }

    fun isNullOrEmpty(word: String):Boolean{
        return word.isNullOrEmpty()
    }
    fun isNetworkAvailable(context: Context):Boolean{
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected


    }

//     fun isConnected() {
//
//         val runtime = Runtime.getRuntime()
//         connected = try {
//             val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
//             val exitValue = ipProcess.waitFor()
//             exitValue == 0
//         } catch (e: IOException) {
//             e.printStackTrace()
//             false
//         } catch (e: InterruptedException) {
//             e.printStackTrace()
//             false
//         }
//
//        //val command = "ping -c 1 google.com"
//       // return Runtime.getRuntime().exec(command).waitFor() == 0
//    }


}

