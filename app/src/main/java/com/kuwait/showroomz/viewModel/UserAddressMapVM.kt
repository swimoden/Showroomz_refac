package com.kuwait.showroomz.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.kuwait.showroomz.extras.Shared


class UserAddressMapVM : ViewModel() {
    var from: Int =0
    private val gson = GsonBuilder().create()
    private val prefs = Shared()
    fun  saveAddress(target: LatLng) {
       prefs.setString("LOCATION",gson.toJson(target))
       prefs.setInt("FROM",from)
    }
}