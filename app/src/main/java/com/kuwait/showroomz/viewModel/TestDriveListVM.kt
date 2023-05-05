package com.kuwait.showroomz.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class TestDriveListVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val testDriveList = MutableLiveData<List<TestDrive>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    lateinit var auxList: List<Brand>
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConectionError = MutableLiveData<Boolean>( false)

     fun fetchTestDrive() {
        loading.value = true
        empty.value = false

        val listFromLocal =
            local.getAll<TestDrive>()?.filter { testDrive -> testDrive.client?.id==prefs.string(USER_ID)}?.sortedByDescending { it.createdAt?.let { it1 ->
                Utils.instance.stringToDate(
                    it1,"yyyy-MM-dd'T'HH:mm:ss")?.time
            } }
        if (listFromLocal != null && listFromLocal.isNotEmpty()) {
            loading.value = false
            error.value = ""
            listFromLocal.let {
                testDriveList.value = it
            }

        }else{
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.value = false
                empty.value = true
                return
            }
        }

        val url = "$TEST_DRIVE?_page=0&itemsPerPage=999&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getTestDrive(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<TestDrive>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<TestDrive>>) {
                        loading.value = false
                        obj.Result?.let { list->
                            if (list.isNotEmpty())
                                saveList(list)
                            else if (listFromLocal!!.isEmpty())
                                empty.value = listFromLocal.isEmpty()
                        }

                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        empty.value = listFromLocal!!.isEmpty()
                    }
                })
        )
    }

    fun saveList(list: List<TestDrive>) {
        local.save(list) {
            testDriveList.value = list.sortedByDescending {
                it.createdAt?.let { it1 ->
                    Utils.instance.stringToDate(
                        it1,"yyyy-MM-dd'T'HH:mm:ss")?.time
                }
            }
//                local.getAll<TestDrive>()?.filter { testDrive -> testDrive.client?.id==prefs.string(USER_ID) }?.sortedByDescending { it.createdAt?.let { it1 ->
//                    Utils.instance.stringToDate(
//                        it1,"yyyy-MM-dd'T'HH:mm:ss")?.time
//                } }
            error.value = ""
            empty.value = testDriveList.value?.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}