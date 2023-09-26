package com.kuwait.showroomz.viewModel

import android.graphics.Paint
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.util.ArrayList


class CategoryVM : ViewModel() {

    val brands = MutableLiveData<List<Brand>>()
    val brandsHasOffer = MutableLiveData<List<Brand>>()
    val trendingModels = MutableLiveData<List<Model>>()
    val recentlyModel = MutableLiveData<List<Model>>()
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val categories = MutableLiveData<List<Category>>()
    val error = MutableLiveData<String>()
    val noConnectionError = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData<Boolean>()
    val financeCallbacks = MutableLiveData<List<Callback>>()
    val financeRequests = MutableLiveData<List<Callback>>()
    val financeRequest = MutableLiveData<List<Callback>>()
    val callbacks = MutableLiveData<List<Callback>>()
    val testDrive = MutableLiveData<List<TestDrive>>()
    val favoriteList = MutableLiveData<List<Favorite>>()
    val empty = MutableLiveData<Boolean>()
    private val local = LocalRepo()
    private val prefs = Shared()
    val isEnableExclusive = ObservableBoolean()
    val isEnableTrending = ObservableBoolean()
    val isEnableRecently = ObservableBoolean()
    val isEnableCallback = ObservableBoolean()
    val isEnableFinanceRequest = ObservableBoolean()
    val isEnableFinanceCallback = ObservableBoolean()
    val isEnableTestDrive = ObservableBoolean()
    var shared = Shared()
    init {

        refresh("0")
        getBrands()
        getModels()
        getRecentlyViewed()



        if (shared.existKey(USER_ID)) {

            fetchFinanceCallbacks()
            fetchCallbacks()
           getTestDrive()
        } else {
            callbacks.value = listOf()
            financeCallbacks.value = listOf()
            financeRequest.value=listOf()
            testDrive.value = listOf()
            favoriteList.value = listOf()
        }
    }
    fun refresh(index: String = "0") {
        fetchCategories(index)
    }

    fun initRecyclersStatus() {
        if (prefs.existKey("ECXLUSIVE")) {
            isEnableExclusive.set(prefs.bool("ECXLUSIVE"))
        } else isEnableExclusive.set(true)
        if (prefs.existKey("TRENDING")) {
            isEnableTrending.set(prefs.bool("TRENDING"))
        } else isEnableTrending.set(true)
        if (prefs.existKey("RECENTLY")) {
            isEnableRecently.set(prefs.bool("RECENTLY"))
        } else isEnableRecently.set(true)
        if (prefs.existKey("CALLBACKS")) {
            isEnableCallback.set(prefs.bool("CALLBACKS"))
        } else isEnableCallback.set(true)
        if (prefs.existKey("FINANCE_REQUEST")) {
            isEnableFinanceRequest.set(prefs.bool("FINANCE_REQUEST"))
        } else isEnableFinanceRequest.set(true)
        if (prefs.existKey("FINANCE_CALLBACK")) {
            isEnableFinanceCallback.set(prefs.bool("FINANCE_CALLBACK"))
        } else isEnableFinanceCallback.set(true)
        if (prefs.existKey("TEST_DRIVE")) {
            isEnableTestDrive.set(prefs.bool("TEST_DRIVE"))
        } else isEnableTestDrive.set(true)
    }

    private fun fetchCategories(index: String = "0") {
        loading.value = true
        val list = local.getAllParents<Category>(index)
        if (list != null && list.size > 0) {
            loading.value = false
            error.value = ""
            categories.value = list.filter { it.isEnabled == true }.sortedBy { it.position }
        }else{
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                return
            }
        }
        disposable.add(
            service.getCategories()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<Category>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Category>>) {
                        obj.Result?.let { list ->
                                setTranslationPrimaryKey(list, index)
                        }
                    }
                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                    }
                })
        )
    }

    fun getBrands() {
        loading.value = true
        val listFromLocal = local.getAll<Brand>()?.filter { it.isEnabled == true }
        if (listFromLocal != null && listFromLocal.size > 0) {
            loading.value = false
            error.value = ""
            brands.value = listFromLocal.filter { BrandSimplifier(it).isexclisive }
            brandsHasOffer.value = listFromLocal.filter { BrandSimplifier(it).isoffer }
         }
    }

    fun getModels() {
        loading.value = true
        val listFromLocal = local.getAll<Model>()?.filter { it.isEnabled == true }
        if (listFromLocal != null && listFromLocal.size > 0) {
            loading.value = false
            error.value = ""
            var aux =
                listFromLocal.filter { it.isEnabled == true }.sortedByDescending { it.nbViews }
            if (aux.size > 10)
                trendingModels.value = aux.subList(0, 10)
            else {
                trendingModels.value = aux
            }
            //
        }else{
            android.os.Handler().postDelayed({
                getModels()
            }, 1000)

           // trendingModels.value = emptyList()
        }


    }

    fun getRecentlyViewed() {
        if (prefs.existKey(RECENTLY_VIEWED)) {
            val recently = prefs.getList<Model>(RECENTLY_VIEWED)
            val filtredList = arrayListOf<Model>()
            for (i in 0 until recently.size) {
                if (i < 10)
                    filtredList.add(recently[i])
            }
            recentlyModel.value = filtredList
        }
    }

    fun setTranslationPrimaryKey(list: List<Category>, index: String = "0") {

        local.save(list) {
            if (it && list.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("cat_updatedAt", x.toStringDate())
            }
            loading.value = false
            error.value = ""
            val listUpdated = local.getAllParents<Category>(index)
            val list = listUpdated?.filter { it.isEnabled == true }?.sortedBy { it.position }
                //?.filter { it.index == index }
            list?.let {
                categories.value = it
            }


        }
    }

    fun fetchFinanceCallbacks() {


        if (prefs.existKey(USER_ID)) {
            loading.value = true
            empty.value = false
            val listFromLocal = local.getAll<Callback>()?.filter { callback ->
                callback.discr == "callback_bank" && callback.modelData?.id != null && callback.client?.id == prefs.string(
                    USER_ID
                )
            }?.sortedByDescending {
                it.createdAt?.let { it1 ->
                    Utils.instance.stringToDate(
                        it1, "yyyy-MM-dd'T'HH:mm:ss"
                    )?.time
                }
            }
            if (listFromLocal != null && listFromLocal.isNotEmpty()) {
                loading.value = false
                error.value = ""
                financeCallbacks.value = listFromLocal.filter { it.bankAccountNumber == null }
                financeRequest.value = listFromLocal.filter { it.bankAccountNumber != null }
            }else{
                if (!NetworkUtils.instance.connected) {
                    empty.value = true
                    loading.value = false
                    return
                }
            }


           /* val url =
                "$CALLBACK_BANKS?_page=0&itemsPerPage=999&client.id[]=${prefs.string(USER_ID)}"
            disposable.add(
                service.getCallbacksBanks(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<Callback>>() {
                        override fun onSuccess(list: List<Callback>) {
                            loading.value = false
                            if (list.isNotEmpty())
                                saveFinanceCallbacksList(list)

                            loading.value = false
                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false

                        }
                    })
            )*/
        }
    }

    fun saveFinanceCallbacksList(list: List<Callback>) {
        local.save(list) {
            if (prefs.existKey(USER_ID)) {
                financeCallbacks.value =
                    local.getAll<Callback>()
                        ?.filter { callback ->
                            callback.discr == "callback_bank" && callback.modelData?.id != null && callback.bankAccountNumber == null && callback.client?.id == prefs.string(
                                USER_ID
                            )
                        }
                        ?.sortedByDescending {
                            it.createdAt?.let { it1 ->
                                Utils.instance.stringToDate(
                                    it1, "yyyy-MM-dd'T'HH:mm:ss"
                                )?.time
                            }
                        }
                financeRequest.value =
                    local.getAll<Callback>()
                        ?.filter { callback ->
                            callback.discr == "callback_bank" && callback.modelData?.id != null && callback.bankAccountNumber != null && callback.client?.id == prefs.string(
                                USER_ID
                            )
                        }
                        ?.sortedByDescending {
                            it.createdAt?.let { it1 ->
                                Utils.instance.stringToDate(
                                    it1, "yyyy-MM-dd'T'HH:mm:ss"
                                )?.time
                            }
                        }
                error.value = ""
                empty.value = financeCallbacks.value?.isEmpty()
            }
        }
    }

    fun fetchCallbacks() {

        if (prefs.existKey(USER_ID)) {
            loading.value = true
            empty.value = false
            val listFromLocal =
                local.getAll<Callback>()?.filter { callback ->
                    callback.discr == "callback" && callback.modelData?.id != null && callback.client?.id == prefs.string(
                        USER_ID
                    )
                }?.sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
            if (listFromLocal != null && listFromLocal.isNotEmpty()) {
                loading.value = false
                error.value = ""
                listFromLocal.let{
                    callbacks.value = it
                }

            }else{
                if (!NetworkUtils.instance.connected) {
                    loading.value = false
                    return
                }
            }
            /*val url = "$GET_CALLBACKS?_page=0&itemsPerPage=999&client.id[]=${prefs.string(USER_ID)}"
            disposable.add(
                service.getCallbacks(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<Callback>>() {
                        override fun onSuccess(list: List<Callback>) {
                            loading.value = false
                            if (list.isNotEmpty())
                                saveList(list)

                            loading.value = false
                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false

                        }
                    })
            )*/
        }
    }

    fun getTestDrive() {

        if (prefs.existKey(USER_ID)) {
            loading.value = true
            empty.value = false
            val listFromLocal =
                local.getAll<TestDrive>()?.filter { testDrive ->
                    testDrive.client?.id == prefs.string(USER_ID)
                }?.sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
            if (listFromLocal != null && listFromLocal.isNotEmpty()) {
                loading.value = false
                error.value = ""
                listFromLocal.let {
                    testDrive.value = it
                }

            }else{
                if (!NetworkUtils.instance.connected) {
                    loading.value = false
                    return
                }
            }
           /* val url = "$TEST_DRIVE?_page=0&itemsPerPage=999&client.id[]=${prefs.string(USER_ID)}"
            disposable.add(
                service.getTestDrive(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<TestDrive>>() {
                        override fun onSuccess(list: List<TestDrive>) {
                            loading.value = false
                            if (list.isNotEmpty())
                                saveTestDriveList(list)

                            loading.value = false
                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false

                        }
                    })
            )*/
        }
    }

    private fun saveTestDriveList(list: List<TestDrive>) {
        local.save(list) {
            testDrive.value = list
                .filter { testDrive ->
                    testDrive.client?.id == prefs.string(USER_ID)
                }
                .sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
        }
    }

    fun saveList(list: List<Callback>) {
        local.save(list) {
            if (prefs.existKey(USER_ID)) {
                callbacks.value =
                    local.getAll<Callback>()?.filter { callback ->
                        callback.discr == "callback" && callback.modelData?.id != null && callback.client?.id == prefs.string(
                            USER_ID
                        )
                    }
                        ?.sortedByDescending {
                            it.createdAt?.let { it1 ->
                                Utils.instance.stringToDate(
                                    it1, "yyyy-MM-dd'T'HH:mm:ss"
                                )?.time
                            }
                        }
                error.value = ""
                empty.value = callbacks.value?.isEmpty()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getFavorites() {
        if (prefs.existKey(USER_ID)) {
            val listFromLocal =
                local.getAll<Favorite>()?.filter { favorite ->
                    favorite.client()?.id == prefs.string(USER_ID)


                }?.sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
            if (listFromLocal != null && listFromLocal.isNotEmpty()) {
                loading.value = false
                error.value = ""
                listFromLocal.let{
                    favoriteList.value = it
                }



            }

            val url =
                "$FAVORITE_MODELS?_page=0&itemsPerPage=999&customerId=${prefs.string(USER_ID)}"
            disposable.add(
                service.getFavoriteModel(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<Favorite>>>() {
                        override fun onSuccess(obj: BaseResponse<ArrayList<Favorite>>) {
                            loading.value = false
                            obj.Result?.let { list->
                                if (list.isNotEmpty())
                                    saveFavoriteList(list)
                                else favoriteList.value = list
                            }

                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false

                        }
                    })
            )
        } else {
          local.getAllByString<Favorite>("customerId", "")?.let{
              favoriteList.value = it
            }
        }
    }

    private fun saveFavoriteList(list: List<Favorite>) {
        local.save(list) {
            favoriteList.value = list
        }
    }
}

