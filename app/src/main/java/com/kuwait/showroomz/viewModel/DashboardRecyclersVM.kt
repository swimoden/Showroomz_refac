package com.kuwait.showroomz.viewModel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.Shared

class DashboardRecyclersVM : ViewModel() {
    val pref = Shared()
    val isEnableExclusive = ObservableBoolean(true)
    val isEnableTrending = ObservableBoolean(true)
    val isEnableRecently = ObservableBoolean()
    val isEnableCallback = ObservableBoolean()
    val isEnableFinanceRequest = ObservableBoolean()
    val isEnableFinanceCallback = ObservableBoolean()
    val isEnableTestDrive = ObservableBoolean()

    init {
//        if (pref.existKey("ECXLUSIVE")) {
//            isEnableExclusive.set(pref.bool("ECXLUSIVE"))
//        } else isEnableExclusive.set(true)
//        if
//                (pref.existKey("TRENDING")) {
//            isEnableTrending.set(pref.bool("TRENDING"))
//        } else isEnableTrending.set(true)
        if (pref.existKey("RECENTLY")) {
            isEnableRecently.set(pref.bool("RECENTLY"))
        } else isEnableRecently.set(true)
        if (pref.existKey("CALLBACKS")) {
            isEnableCallback.set(pref.bool("CALLBACKS"))
        } else isEnableCallback.set(true)
        if (pref.existKey("FINANCE_REQUEST")) {
            isEnableFinanceRequest.set(pref.bool("FINANCE_REQUEST"))
        } else isEnableFinanceRequest.set(true)
        if (pref.existKey("FINANCE_CALLBACK")) {
            isEnableFinanceCallback.set(pref.bool("FINANCE_CALLBACK"))
        } else isEnableFinanceCallback.set(true)
        if (pref.existKey("TEST_DRIVE")) {
            isEnableTestDrive.set(pref.bool("TEST_DRIVE"))
        } else isEnableTestDrive.set(true)
    }

    fun changeDashboard(key: String, status: Boolean) {
        pref.setBool(key, status)
    }
}