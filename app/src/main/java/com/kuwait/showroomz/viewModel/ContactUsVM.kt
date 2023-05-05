package com.kuwait.showroomz.viewModel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.NetworkUtils

class ContactUsVM:ViewModel() {
    val message =ObservableField<String>("")
    val phone =ObservableField<String>()
    val contactName =ObservableField<String>("")
    val subject =ObservableField<String>("")
    val subjectError =ObservableBoolean()
    val contactNameError =ObservableBoolean()
    val phoneError =ObservableBoolean()
    val messageError =ObservableBoolean()
    val success = MutableLiveData<Boolean>()
    val noConnectionError = MutableLiveData<Boolean>(false)
    fun verifyFields(){
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        if (contactName.get()=="")
            contactNameError.set(true)
        else
            contactNameError.set(false)
        if (phone.get()?.length!=8) phoneError.set(true)else phoneError.set(false)
        if (subject.get()=="")
            subjectError.set(true)
        else
            subjectError.set(false)
        if (message.get()=="") messageError.set(true)else messageError.set(false)
        if (!contactNameError.get()&&!phoneError.get()&&!subjectError.get()&&!messageError.get()){
            success.value=true
        }
    }
}