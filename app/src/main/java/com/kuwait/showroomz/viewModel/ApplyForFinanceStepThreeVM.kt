package com.kuwait.showroomz.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Brand

class ApplyForFinanceStepThreeVM :ViewModel() {
    var selectedBanks = ArrayList<Bank>()
    var banks = MutableLiveData<List<Bank>>()
    init {

    }
}