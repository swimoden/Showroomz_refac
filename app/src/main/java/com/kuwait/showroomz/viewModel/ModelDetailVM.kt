package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody


class ModelDetailVM : ViewModel() {
    lateinit var simplifier: ModelSimplifier
    private val TAG = "ModelDetailVM"
    private val service = ApiService()
    private val local = LocalRepo()
    val prefs = Shared()
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var email = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var civilIdError = MutableLiveData<Boolean>(false)
    var acceptConditionError = MutableLiveData<Boolean>(false)
    var civilId = ObservableField<String>()
    var emailError = ObservableBoolean(false)
    lateinit var modelId: String
    private val disposable = CompositeDisposable()
    val model = MutableLiveData<Model>(Model())
    val images = MutableLiveData<List<Image>>()
    val videos = MutableLiveData<List<Image>>()
    val offers = MutableLiveData<List<Offer>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val successCallback = MutableLiveData<Boolean>(false)
    val successFavorite = MutableLiveData<Boolean>(false)
    val errorFavorite = MutableLiveData<Boolean>(false)
    val successTestDriveData = MutableLiveData<Boolean>(false)
    val trimsLoading = MutableLiveData<Boolean>(false)
    val trims = MutableLiveData<TrimResponse>()
    val trimsList = MutableLiveData<List<Trim>>()
    val offersList = MutableLiveData<List<Offer>>()
    val empty = MutableLiveData<Boolean>()
    var callbackLoading = MutableLiveData<Boolean>(false)
    var callbackError = MutableLiveData<Boolean>(false)
    var testDriveLoading = ObservableBoolean(false)
    var isFavorite = ObservableBoolean(false)
    lateinit var user: User
    lateinit var favorite: Favorite
    lateinit var favoriteWithOutUser: Favorite
    var favoritesForUser: MutableList<Favorite>? = mutableListOf<Favorite>()
    var favoritesWithOutUser: MutableList<Favorite>? =
        mutableListOf<Favorite>()
    var favoriteList = ArrayList<Favorite>()
    var isCivilIdMandatory = ObservableBoolean(false)
    var isKfh = ObservableBoolean(false)
    var acceptCondition = ObservableBoolean(true)
    var showVerticalGallery = ObservableBoolean(false)
    fun isConnected() = prefs.existKey(USER_ID)
    val noConectionError = MutableLiveData<Boolean>(false)
    val verifyPhone = MutableLiveData<Boolean>(false)
    val emptyGallery = MutableLiveData<Boolean>()
init {
    if (prefs.existKey(USER_ID)) {
        prefs.string(USER_ID)?.let {
            local.getOne<User>(it)?.let {
                user = it
                fullNameField.set(user.fullName)
                phoneNumber.set(user.phone?.number)
                email.set(user.email)
                civilId.set(user.civilID)

            }
        }
    }
}
    fun getUser() {
        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it
                    fullNameField.set(user.fullName)
                    phoneNumber.set(user.phone?.number)
                    email.set(user.email)
                    civilId.set(user.civilID)

                }
            }



        }
    }
fun setIsFavorite(){
    if (prefs.existKey(USER_ID)) {
        favoritesForUser = local.getAllByString<Favorite>("customerId", prefs.string(USER_ID)!!)
        isFavorite.set(existModelInFavorite(favoritesForUser))
    }else{
        favoritesWithOutUser = local.getAllByString<Favorite>("customerId", "")!!
        isFavorite.set(existModelInFavoriteWithOut(favoritesWithOutUser))
    }
}
    private fun existModelInFavoriteWithOut(list: MutableList<Favorite>?): Boolean {
        list?.forEach {
            if (it.modelData?.id == modelId) {
                favoriteWithOutUser = it
                return true
            }

        }
        return false
    }

    private fun existModelInFavorite(list: MutableList<Favorite>?): Boolean {
        if (::modelId.isInitialized) {
            list?.forEach {
                if (it.modelData?.id == modelId) {
                    favorite = it
                    return true
                }

            }
        }
        return false
    }

    fun getModelById(usedFor: String, id: String) {
        loading.value = true
        val localModel = local.getOne<Model>(id)
        if (localModel != null) {
            loading.value = false
            error.value = ""

            localModel.let {
                model.value  = it
            }
        }

        val url = "$usedFor$BRAND_MODELS/$id"
        disposable.add(
            service.getModelsByID(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Model>() {
                    override fun onSuccess(pModel: Model) {
                        loading.value = false

                        if (model != null) {
                            setTranslationPrimaryKey(pModel, url, id)
                        }

                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("ModelDetailVM", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                    }

                })
        )
 }

    fun getRemoteModelById( id: String,  action: (model: Model) -> (Unit)) {
        loading.value = true

        val url = "$MODEL_DATAS_WITH_ID$id"
        disposable.add(
            service.getModelsByID(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Model>() {
                    override fun onSuccess(pModel: Model) {
                        loading.value = false
                        action.invoke(pModel)
                        local.saveObject(pModel){ }
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("ModelDetailVM", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                    }

                })
        )
    }

    fun getModelBySlug(id: String,  action: (model: Model) -> (Unit)){
        loading.value = true
        val url = "$MODEL_DATAS_WITH_SLUG$id"
        disposable.add(
            service.getModelsBySlug(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Model>>() {
                    override fun onSuccess(list: List<Model>) {
                        loading.value = false
                       if (list.isNotEmpty()){
                           action.invoke(list[0])
                          // model.value = list[0]
                           local.saveObject(list[0]){ }
                       }else{
                           error.value = context.getString(R.string.error_occurred)
                       }
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("ModelDetailVM", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                    }

                })
        )
    }
    fun getOffersByTrimID(id:String){
        val url = "$TRIM_OFFERS/$id"
        disposable.add(
            service.getOffersByTrimId(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Offer>>>() {


                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("ModelDetailVM", it) }
                        //error.value = e.localizedMessage
                       // loading.value = false
                    }

                    override fun onNext(t: BaseListResponse<List<Offer>>) {
                        t.Result?.let {
                            offers.value = it
                        }
                    }

                    override fun onComplete() {
                    }

                })
        )
    }

    fun setTranslationPrimaryKey(pModel: Model, url: String, id: String) {

        local.saveObject(pModel) {
            if (it) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            val modelFromLocal = local.getOne<Model>(id)
            loading.value = false
            error.value = ""
            modelFromLocal.let { local -> model.value = local }
        }

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun incrementModelNbViews(id: String) {
        if (NetworkUtils.instance.connected) {
            disposable.add(
                service.incrementModelNbViews(id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<IncrementNViewResponse>() {
                        override fun onSuccess(t: IncrementNViewResponse) {
                            Log.e("RESPONSE", t.data.toString())
                            updateLocalModel(t, id)
                        }

                        override fun onError(e: Throwable) {
                            e.let { it.message?.let { it1 -> Log.e("RESPONSE_ERROR", it1) } }


                        }
                    })
            )
        }
    }

    private fun updateLocalModel(
        t: IncrementNViewResponse,
        id: String
    ) {
        local.realm.executeTransaction {
            local.getOne<Model>(id)?.nbViews = t.data ?: 0
        }


    }

    fun saveModelToHistory(model: Model) {

        if (prefs.existKey(RECENTLY_VIEWED)) {
            val recently = prefs.getList<Model>(RECENTLY_VIEWED)
            Log.e(TAG, recently.indexOf(model).toString())

            for (i in 0 until recently.size) {
                if (recently[i].id == model.id) {
                    recently.removeAt(i)
                    break
                }
            }
            recently.add(0, model)
            prefs.removeKey(RECENTLY_VIEWED)
            prefs.setList(RECENTLY_VIEWED, recently)
        } else {
            val recently = arrayListOf<Model>()
            recently.add(model)
            prefs.setList(RECENTLY_VIEWED, recently)
        }


    }

    fun getModelVideos(id: String) {
        if (NetworkUtils.instance.connected) {
            loading.value = true
            /*val localImages = local.getOne<VideoGallery>(id)
            if (localImages != null) {
                loading.value = false
                error.value = ""
                videos.value = localImages.images
            } else {*/
                /*disposable.add(
                    service.getIVideos(id)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<VideoGallery>() {
                            override fun onSuccess(gallery: VideoGallery) {
                                loading.value = false
                                if (gallery.images.isNotEmpty())
                                    videos.value = gallery.images
//                                saveGallery(gallery, id)
                            }

                            override fun onError(e: Throwable) {
                                e.message?.let { Log.e("ModelDetailVM_ERROR", it) }
                                error.value = e.localizedMessage
                                loading.value = false
                            }

                        })
                )*/

            disposable.add(
                service.getModelVideoById(id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<BaseListResponse<List<Image>>>() {
                        override fun onNext(t: BaseListResponse<List<Image>>) {
                            loading.value = false
                            t.Result?.let {
                                videos.value = it
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.message?.let { Log.e("ModelDetailVM_ERROR", it) }
                            error.value = e.localizedMessage
                            loading.value = false
                        }

                        override fun onComplete() {

                        }

                    })
            )

           // }
        }
    }

    fun getModelGallery(id: String) {
        loading.value = true
            disposable.add(
                service.getImageById(id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<BaseListResponse<List<Image>>>() {
                        override fun onNext(t: BaseListResponse<List<Image>>) {
                            loading.value = false
                            t.Result?.let {
                                images.value = it
                                emptyGallery.value = it.isEmpty()
                            }?: kotlin.run {
                                emptyGallery.value = true
                            }
                        }
                        override fun onError(e: Throwable) {
                            e.message?.let { Log.e("ModelDetailVM_ERROR", it) }
                            error.value = e.localizedMessage
                            loading.value = false
                            emptyGallery.value = true
                        }
                        override fun onComplete() {}
                    })
            )
    }

    private fun saveGallery(gallery: ModelGallery, id: String) {
        local.saveObject(gallery) {
            var modelFromLocal = local.getOne<ModelGallery>(id)
            loading.value = false
            error.value = ""
            modelFromLocal.let { local ->
                images.value = local?.images
                trimsLoading.value = false
            }
        }

    }

    fun getTrimsByModelId( modelId: String) {
        val list = local.getAllByInt<Trim>("modelId", modelId.toInt())
        list?.let { it ->
            if (it.size > 0) {
                trimsList.value = it
            }else{
                trimsLoading.value = true
            }
        } ?: kotlin.run {
            if (!NetworkUtils.instance.connected) {
                trimsLoading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }
            trimsLoading.value = true
        }
        val url = "Trims/model/$modelId"

        disposable.add(
            service.getTrimByModelId(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Trim>>>() {
                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                        trimsLoading.value = false

                        if (list == null) {
                            empty.value = true
                        }
                    }
                    override fun onNext(t: BaseListResponse<List<Trim>>) {
                        trimsLoading.value = false
                        t.Result?.let {trims ->
                            trimsList.value = trims.filter { s -> s.isEnabled == true }
                            saveTrim(trims, modelId)
                            /*list?.let { it ->

                                saveTrim(trims, modelId)
                                //local.save(trims){}
                                /*if (list.isEmpty()){
                                    saveTrim(trims, modelId)
                                }else{
                                    local.save(trims){}
                                }*/
                            }?: kotlin.run {
                                saveTrim(trims, modelId)

                            }*/
                        }?: kotlin.run {
                            empty.value = true
                        }
                    }
                    override fun onComplete() {
                        getOffersByModelId(modelId)
                    }
                })
        )


    }

    fun getOffersByModelId( modelId: String) {
        val list = local.getAll<Offer>()?.filter { s -> s.modelDataIds.contains(modelId.toInt())  }
        list?.let { it ->
            if (it.isNotEmpty()) {
                offersList.value = it.filter { s -> s.isEnabled == true }
            }
        } ?: kotlin.run {
            if (!NetworkUtils.instance.connected) {
                trimsLoading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }
            trimsLoading.value = true
        }
        val url = "offers/model/$modelId"

        disposable.add(
            service.getOffersByModelId(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Offer>>>() {
                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                        trimsLoading.value = false

                        if (list == null) {
                            empty.value = true
                        }
                    }
                    override fun onNext(t: BaseListResponse<List<Offer>>) {
                        t.Result?.let {offers ->
                           // list?.let { it ->
                                saveOffers(offers, modelId)
                           /* }?: kotlin.run {
                                local.save(offers){}
                            }*/
                        }?: kotlin.run {
                            empty.value = true
                        }
                    }
                    override fun onComplete() { }
                })
        )


    }

    fun getTrims( modelId: String) {
        trimsLoading.value = true
        val trimsFromLocal = local.getOne<TrimResponse>(modelId)
        if (!trimsFromLocal.isNull()) {
            trimsFromLocal?.let {trims.value = it}
            trimsLoading.value = false
        } else {
            if (!NetworkUtils.instance.connected) {
                trimsLoading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }
            trimsLoading.value = true
        }


        val url = "model-data/$modelId/$TRIMS_OFFERS"
        disposable.add(
            service.getModelTrims(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                    override fun onSuccess(response: TrimResponse) {
                        Log.e("RESPONSE", response.toString())
                        trimsLoading.value = false

                        if (!response.isNull()) {
                            GlobalScope.launch {
                                val local2 = LocalRepo()
                                if (trimsFromLocal.isNull())
                                    saveTrims(response, modelId, url)
                                else {
                                    local2.saveObject(response) {
                                        //trims.value = local.getOne(modelId)
                                    }
                                }
                            }

                        } else if (trimsFromLocal.isNull()) {
                            empty.value = true
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                        trimsLoading.value = false
                        if (trimsFromLocal.isNull()) {
                            empty.value = true
                        }
                    }
                })
        )


    }
    fun saveTrim(trims:List<Trim>,id: String ){
        local.save(trims){
           // trimsList.value = local.getAllByInt<Trim>("modelId", id.toInt())//?.filter { s -> s.isEnabled == true }
        }
    }
    fun saveOffers(trims:List<Offer>,id: String ){
        local.save(trims){
            offersList.value = local.getAll<Offer>()?.filter { s -> s.modelDataIds.contains(id.toInt())}?.filter { s -> s.isEnabled == true }
        }
    }
    private fun saveTrims(response: TrimResponse, id: String, url: String) {
        local.saveObject(response) {
            trims.value = local.getOne(id)
        }
    }

    fun requestCallback() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            return
        }
        if (fullNameField.get().isNullOrEmpty()) {
            nameError.value = true
            return
        } else
            nameError.value = false

        if (phoneNumber.get()?.isValidPhoneNumber() == null) {
            phoneError.value = true
            return
        } else if (!phoneNumber.get()!!.isValidPhoneNumber()) {
            phoneError.value = true
            return
        } else {
            phoneError.value = false
        }

        //phoneError.value  = if (phoneNumber.get()?.isValidPhoneNumber() == null) false else phoneNumber.get()?.isValidPhoneNumber()


        if (isCivilIdMandatory.get()) {
            if (civilId.get().toString().length != 12) {
                civilIdError.value = true
                return
            } else
                civilIdError.value = false
            if (!acceptCondition.get()) {
                acceptConditionError.value = true
                return
            } else acceptConditionError.value = false

        }
        /*if (!isConnected() && verifyPhone.value == false) {
            verifyPhone.value = true
            return
        }*/


        if (nameError.value == false && phoneError.value == false && civilIdError.value == false) {
           /* var callback = CallbackWithOutUserRequest(
                fullName = fullNameField.get().toString(),
                phone = Phone("+965", phoneNumber.get().toString())
            )

            if (prefs.existKey(USER_ID)) {
                callback = CallbackRequest(
                    fullName = fullNameField.get().toString(),
                    phone = Phone("+965", phoneNumber.get().toString())
                )
                callback.client = prefs.string(USER_ID)
            }
            if (::modelId.isInitialized) {
                callback.modelData = modelId
            }

            if (isCivilIdMandatory.get()) {
                callback.civilId = civilId.get()
            }
            callback.email = ""
            callback.isKFH = isKfh.get()*/

            val json = JsonObject()
            val phoneJson = JsonObject()
            phoneJson.addProperty("code", "+965")
            phoneJson.addProperty("number", phoneNumber.get())
            json.addProperty("email", email.get())
            json.addProperty("fullName", fullNameField.get())
            json.add("phone", phoneJson)
            json.addProperty("modelData", modelId)
            if (prefs.existKey(USER_ID)) {
                json.addProperty("client", prefs.string(USER_ID))
            }
            if (isCivilIdMandatory.get()) {
                json.addProperty("civilId", civilId.get())
            }
            json.addProperty("email", "")
            json.addProperty("isKFH", isKfh.get())

           // Log.e(TAG, "requestCallback: $callback")
            callbackLoading.value = true
            disposable.add(
                service.sendCallback(json)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<Callback>>() {
                        override fun onSuccess(t: BaseResponse<Callback>) {
                            if (!prefs.existKey(USER_ID)) {
                                t.Result?.client?.id?.let {
                                    if(it != "") {
                                        prefs.setString(USER_ID, it)
                                        prefs.setString(TOKEN, it)
                                    }
                                    LogProgressRepository.refreshMainUserData()
                                }
                            }
                            t.Result?.let { saveCallback(it) }

                            successCallback.value = true
                            callbackLoading.value = false
                        }

                        override fun onError(e: Throwable) {
                            e.let { it.message?.let { it1 -> Log.e("RESPONSE_ERROR", it1) } }
                            callbackLoading.value = false
                            callbackError.value = true
                            verifyPhone.value = false
//                            nameError.set(true)
//                            phoneError.set(true)
                        }

                    })
            )
        }
    }


    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            t.client?.id?.let { prefs.setString(USER_ID, it) }
            Log.e(TAG, "saveCallback: $it")
        }
    }

    fun addToFavorite() {
        if (prefs.existKey(USER_ID)) {
            val x = local.getOneWithPredicate<Favorite>(modelId, "modelData.id")
            if (x != null) {
                return
            }
            callbackLoading.value = true
            val params = FavoriteParam("${user.id}", "$modelId")
            disposable.add(
                service.postFavoriteModel(params)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<Favorite>>() {
                        override fun onSuccess(obj: BaseResponse<Favorite>) {
                            callbackLoading.value = false
                            obj.Result?.let{
                                saveFavorite(it)
                                isFavorite.set(true)
                            }
                        }
                        override fun onError(e: Throwable) {
                            if (!NetworkUtils.instance.connected) {
                                noConectionError.value = true
                                callbackLoading.value = false
                                return
                            }
                            errorFavorite.value = true
                            callbackLoading.value = false
                            Log.e(TAG, "onError: ${e.localizedMessage}")
                        }
                    }
                    )
            )
        } else {
            saveFavoriteWithOutUser()
        }
    }

    private fun saveFavoriteWithOutUser() {
        val localModel = local.getOne<Model>(modelId)
        localModel?.let {
            val favorite = Favorite(id = modelId, modelData = ModelBasic(it.id))
            favoriteWithOutUser = favorite
            local.saveObject(favorite) {
                isFavorite.set(it)
                Log.e(TAG, "saveFavoriteWithOutUser: $it")
            }
        }

    }

    private fun saveFavorite(t: Favorite) {
        local.saveObject(t) {
            isFavorite.set(it)
            Log.e(TAG, "saveFavorite: $it")
        }

    }

    fun removeFromFavoriteList() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            return
        }
        if (prefs.existKey(USER_ID)) {
            val x = local.getOneWithPredicate<Favorite>(modelId, "modelData.id") ?: return
            callbackLoading.value = true
            val url = "$FAVORITE_MODELS/${x.id}"
            deleteFavorite(x)
            disposable.add(
                service.deleteFavoriteModel(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                        override fun onSuccess(t: ResponseBody) {
                            Log.e(TAG, "onSuccess: ${t.string()}")
                            // deleteFavorite(x)
                            callbackLoading.value = false
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, "onError: ${e.localizedMessage}")
                            //deleteFavorite(x)
                            callbackLoading.value = false
                        }

                    })
            )

        } else {
            deleteFavoriteWithOutUser(favoriteWithOutUser)
        }
    }

    private fun deleteFavoriteWithOutUser(favoriteWithOutUser: Favorite) {
        local.realm.executeTransaction { realm ->
            val result: RealmResults<Favorite> =
                realm.where(Favorite::class.java)
                    .equalTo("modelData.id", favoriteWithOutUser.modelData?.id)
                    .equalTo("customerId", "")
                    .findAll()

            result.deleteAllFromRealm()
            isFavorite.set(false)
        }
    }

    private fun deleteFavorite(favorite: Favorite) {
        /*local.deleteObj<Favorite>(favorite){
                isFavorite.set(!it)
        }*/
        local.realm.executeTransaction { realm ->
            val result: RealmResults<Favorite> =
                realm.where(Favorite::class.java).equalTo("id", favorite.id).findAll()
            result.deleteAllFromRealm()
            isFavorite.set(false)
        }
    }
}

