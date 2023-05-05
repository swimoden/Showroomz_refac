package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*

import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.FavoriteRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class ProfileVM : ViewModel() {
    var userImage: String? = ""
    private val local = LocalRepo()
    private val prefs = Shared()
    lateinit var user: User
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var callbacksCount = ObservableField<String>("")
    var testDriveCount = ObservableField<String>("")
    var paymentBookingCount = ObservableField<String>("")
    var financeCallbackCount = ObservableField<String>("")
    var favoriteListCount = ObservableField<String>("")
    var appraisalListCount = ObservableField<String>("")
    var financeRequest = ObservableField<String>("")
    val userModel = MutableLiveData<User>()
init {
    getUser()
    getCounts()
}
    fun getCounts() {

        prefs.string(USER_ID)?.let {
            local.getAllByString<TestDrive>("client.id", it)?.size?.let {
                testDriveCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        }
        prefs.string(USER_ID)?.let {
            local.getAllByString<Booking>("clientId", it)?.size?.let {
                paymentBookingCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        }
        prefs.string(USER_ID)?.let {
            local.getAllByString<Callback>("client.id", it)
                ?.filter { it.discr == "callback" && it.modelData?.id != null }?.size?.let {
                callbacksCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        }
        prefs.string(USER_ID)?.let {
            local.getAllByString<Favorite>("customerId", it)?.size?.let {
                favoriteListCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        }
        prefs.string(USER_ID)?.let {
            local.getAllByString<CallbackAppraisalClientVehicle>("clientId", it)?.size?.let {
                appraisalListCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        }
        local.getAll<Callback>()
            ?.filter { it.discr == "callback_bank" && it.client?.id == prefs.string(USER_ID) && it.bankAccountNumber == null && it.modelData?.id != null }?.size?.let {
                financeCallbackCount.set(
                    if (it == 0) "" else it.toString()
                )
            }
        local.getAll<Callback>()
            ?.filter { it.discr == "callback_bank" && it.client?.id == prefs.string(USER_ID) && it.bankAccountNumber != null && it.modelData?.id != null }?.size?.let {
                financeRequest.set(
                    if (it == 0) "" else it.toString()
                )
            }
        if (NetworkUtils.instance.connected) {
            getUserCars()
            //fetchCallbacks()
            //fetchFinanceCallbacks()
            //fetchTestDrive()
            getFavorite()

        }
    }
    private fun getUserCars() {
        val url = "client-vehicles?clientId=${prefs.string(USER_ID)}&_page=0&itemsPerPage=100"
        disposable.add(
            service.getClientVehicleList(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<ClientVehicle>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<ClientVehicle>>) {
                        obj.Result?.let {
                            local.save(it) {}
                            getAppraisalList()
                        }

                    }

                    override fun onError(e: Throwable) {
                        // getAppraisalList()
                    }
                })
        )
    }

    fun getAppraisalList() {
        val url = "$CALLBACK_APPRAISAL_CLIENT_VEHICLES?_page=0&itemsPerPage=100&clientId=${
            prefs.string(USER_ID)
        }"
        disposable.add(
            service.getCallbackAppraisalClientVehicleList(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                saveAppraisalList(list)
                                appraisalListCount.set(list.size.toString())
                            }
                        }


                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    fun saveAppraisalList(list: List<CallbackAppraisalClientVehicle>) {
        local.save(list) { }
    }

    private fun getFavorite() {

        val url = "$FAVORITE_MODELS?_page=0&itemsPerPage=300&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getFavoriteModel(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<java.util.ArrayList<Favorite>>>() {
                    override fun onSuccess(obj: BaseResponse<java.util.ArrayList<Favorite>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                saveFavoriteList(list)
                                favoriteListCount.set("${list.size}")
                            }
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    private fun saveFavoriteList(list: List<Favorite>) {
        local.save(list) {

        }
    }

    private fun fetchTestDrive() {
        val url = "$TEST_DRIVE?_page=0&itemsPerPage=5&client.id[]=${prefs.string(USER_ID)}"
        disposable.add(
            service.getTestDrive(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<TestDrive>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<TestDrive>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty())
                                saveTestDrive(list)
                            testDriveCount.set(list.size.toString())
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    private fun saveTestDrive(list: List<TestDrive>) {
        local.save(list) {

        }
    }

    fun getUser() {
        prefs.string(USER_ID)?.let {
            local.getOne<User>(it)?.let {
                user = it
                userModel.value = user
            }
        }


        prefs.string(TOKEN)?.let {
            disposable.add(
                service.getUser(it)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<User>() {
                        override fun onSuccess(t: User) {
                            user = t
                            local.saveObject(t) { }
                            userModel.value = t
                        }

                        override fun onError(e: Throwable) { }

                    })
            )
        }

    }

    fun logout() {
        prefs.string(TOKEN)?.let { logout(it) }
        prefs.removeKey(TOKEN)
        prefs.removeKey(USER_ID)

    }

    private fun logout(token: String) {
        disposable.add(
            service.logout(token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        print(t)
                    }

                    override fun onError(e: Throwable) {
                        print(e)
                    }
                })
        )
    }

    fun fetchFinanceCallbacks() {

        val url = "$CALLBACK_BANKS?_page=0&itemsPerPage=100&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getCallbacksBanks(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Callback>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Callback>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty())
                                saveFinanceCallbacksList(list)
                            val aux = list.filter { it.bankAccountNumber == null && it.modelData?.id != null }
                            val aux2 =  list.filter { it.bankAccountNumber != null && it.modelData?.id != null }
                            financeCallbackCount.set(aux.size.toString())
                            financeRequest.set(aux2.size.toString())
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    fun saveFinanceCallbacksList(list: List<Callback>) {
        local.save(list) {

            Log.e("ProfileVM", "saveFinanceCallbacksList: $it")
        }
    }

    private fun fetchCallbacks() {


        val url = "$GET_CALLBACKS?_page=0&itemsPerPage=100&client.id[]=${prefs.string(USER_ID)}"
        disposable.add(
            service.getCallbacks(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Callback>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Callback>>) {
                        obj.Result?.let { list->
                            if (list.isNotEmpty()) {
                                saveList(list)
                                val aux = list.filter { it.modelData != null }
                                callbacksCount.set(aux.size.toString())
                            }
                        }?: kotlin.run {
                            callbacksCount.set("0")
                        }



                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    fun saveList(list: List<Callback>) {
        local.save(list) {

        }
    }
}