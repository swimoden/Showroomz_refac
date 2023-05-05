package com.kuwait.showroomz.model.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.API_URL
import com.kuwait.showroomz.extras.LOGOUT
import com.kuwait.showroomz.extras.REGISTER_DEVICE
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit



class ApiService {
    private val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
        .supportsTlsExtensions(true)
        .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
        .cipherSuites(
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA

        ).build()

    private val specs = listOf(
        ConnectionSpec.MODERN_TLS,
        ConnectionSpec.COMPATIBLE_TLS,
        ConnectionSpec.CLEARTEXT
    )


    /* private fun provideOkHttpClient(): OkHttpClient {
         val loggingInterceptor = HttpLoggingInterceptor()
         loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
         val httpClient = OkHttpClient.Builder()
             .connectTimeout(5, TimeUnit.MINUTES)
             .writeTimeout(5, TimeUnit.MINUTES)
             .readTimeout(5, TimeUnit.MINUTES)
 //            .connectionSpecs(specs)
 //            .addInterceptor(loggingInterceptor)
             .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))

 //            .connectionSpecs(Collections.singletonList(spec))
         httpClient.networkInterceptors().add(Interceptor { chain ->
             val requestBuilder = chain.request().newBuilder()
             requestBuilder.header(DEVICE_ID, DeviceManger.getDeviceId())
             chain.proceed(requestBuilder.build())
         })
         httpClient.networkInterceptors().add(loggingInterceptor)
        return httpClient.build()
       /*  return OkHttpClient.Builder()
             .connectTimeout(5, TimeUnit.MINUTES)
             .writeTimeout(5, TimeUnit.MINUTES)
             .readTimeout(5, TimeUnit.MINUTES)
 //            .connectionSpecs(specs)
             .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
 //            .connectionSpecs(Collections.singletonList(spec))
             .addInterceptor(httpClient)
             .build()*/

     }



     private val api = Retrofit.Builder()
         .baseUrl(API_URL)
         //.addConverterFactory(GsonConverterFactory.create(gson))
         .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
         //.client(provideOkHttpClient())
         .build()
         .create(Api::class.java)*/


    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()//.sslSocketFactory(context.socketFactory)
            .connectionSpecs(specs)
            .callTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .connectionSpecs(specs)
            .addInterceptor(loggingInterceptor)
            .build()

    }

    private val api = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(provideOkHttpClient())
        .build()
        .create(Api::class.java)

    private val gson = GsonBuilder()
        .setLenient()
        .create()
    private val paymentApi = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(provideOkHttpClient())
        .build()
        .create(PaymentApi::class.java)

    fun getCategories(): Single<BaseListResponse<List<Category>>> {
        return api.getCategories()
//        return if (prefs.string("cat_updatedAt") == "") {
//            api.getCategories("application/json", "0")
//        } else {
//            val oldTime = prefs.string("cat_updatedAt")
//            api.getCategories("application/json", oldTime!!)
//        }

    }

    fun getBrands(url: String): Single<BaseListResponse<List<Brand>>> {
        return api.getBrandsByCategoryId(url)
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getBrandsByCategoryId("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getBrandsByCategoryId("application/json", url, oldTime!!)
        }*/

    }

    fun getLocations(url: String): Single<BaseListResponse<ArrayList<Location>>> {
        return api.getLocationsByBrandId("application/json-patch+json", url)
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getLocationsByBrandId("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getLocationsByBrandId("application/json", url, oldTime!!)

        }*/
    }

    fun getModelsByBrand(url: String): Single<List<Model>> {
        return  api.getModelsByBrandId("application/json-patch+json", url)
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getModelsByBrandId("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getModelsByBrandId("application/json", url, oldTime!!)

        }*/
    }

    fun getModelsByID(url: String): Single<Model> {
        return api.getModelsById("application/json-patch+json", url, "0")
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getModelsById("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getModelsById("application/json", url, oldTime!!)

        }*/
    }

    fun getModelsBySlug(url: String): Single<List<Model>> {
            return api.getModelsBySlug("application/json", url)
    }

    fun getOffersByTrimId(url: String): Observable<BaseListResponse<List<Offer>>> {
            return api.getOffersByTrimId("application/json-patch+json", url)
    }

    fun getBrandBySlug(url: String): Single<List<Brand>> {
        return api.getBrandBySlug("application/json", url)
    }
    fun getCatBySlug(url: String): Single<List<Category>> {
        return api.getCatBySlug("application/json", url)
    }

    fun incrementModelNbViews(id: String): Single<IncrementNViewResponse> {
        return api.incrementModelNbViews(id)
    }

    fun getModelTypeByCategory(url: String): Single<BaseListResponse<List<Type>>> {
        return api.getModelTypeByCategory("application/json-patch+json", url, "0")
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getModelTypeByCategory("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getModelTypeByCategory("application/json", url, oldTime!!)

        }*/

    }
    /// change this to post
    fun getAllBrandModels(url: String, obj: JsonObject): Observable<BaseListResponse<List<Model>>> {
        return  api.getAllBrandModels("application/json-patch+json", url, obj)
        /*return if (prefs.string("${url}_updatedAt") == "") {
            api.getAllBrandModels("application/json", url, "0")
        } else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getAllBrandModels("application/json", url, oldTime!!)

        }*/
    }

    fun filterModels(url: String): Single<List<Model>> {
        return api.filterModels("application/json", url, "0")
//        return if (prefs.string("${url}_updatedAt") == "") {
//            api.filterModels("application/json", url,"0")
//
//        }else {
//            val oldTime = prefs.string("${url}_updatedAt")
//            return api.filterModels("application/json", url,oldTime!!)
//        }
    }

    fun getImageGallery(idModel: String): Single<ModelGallery> {
        return api.getModelGalleryById("application/json", idModel)
    }

    fun getImageById(idModel: String):Observable<BaseListResponse<List<Image>>>{
        return  api.getModelImagesById("application/json-patch+json",idModel)
    }
    fun getModelVideoById(idModel: String):Observable<BaseListResponse<List<Image>>>{
        return  api.getModelVideoById("application/json-patch+json",idModel)
    }

    fun getIVideos(idModel: String): Single<VideoGallery> {
        return api.getIVideos("application/json", idModel)
    }

    fun getModelTrims(url: String): Single<TrimResponse> {
        return api.getModelTrims("application/json", url)
    }
    fun getTrimByModelId(url: String): Observable<BaseListResponse<List<Trim>>>{
        return api.getTrimByModelId("application/json", url)
    }

    fun getOffersByModelId(url: String): Observable<BaseListResponse<List<Offer>>>{
        return api.getOfferByModelId("application/json", url)
    }

    fun getBanks(): Single<BaseListResponse<List<Bank>>> {
        return   api.getBanks("application/json-patch+json", "0")
        /*return if (prefs.string("banks_updatedAt") == "") {
            api.getBanks("application/json", "0")
        } else {
            val oldTime = prefs.string("bank_updatedAt")
            api.getBanks("application/json", oldTime!!)
        }*/
    }
    fun getAdvertisementsWithPagination(url:String): Single<BaseListResponse<List<Advertisement>>> {
        return api.getAdvertisementsWithPagination("application/json-patch+json", url)
    }

    fun getAdvertisements(): Single<List<Advertisement>> {
        return  api.getAdvertisements("application/json", "0")
       /* return if (prefs.string("ads_updatedAt") == "") {
            api.getAdvertisements("application/json", "0")
        } else {
            val oldTime = prefs.string("ads_updatedAt")
            api.getAdvertisements("application/json", oldTime!!)
        }*/
    }

    fun incrementAdsNbView(ads: Advertisement): Single<IncrementNViewResponse> {
        return api.incrementAdsNbView(ads.id!!)
    }

    fun incrementAdsNbClick(ads: Advertisement): Single<IncrementNViewResponse> {
        return api.incrementAdsNbClick(ads.id!!)
    }

    fun getSettings(): Single<BaseResponse<Setting>> {
        return api.getSettings()
    }

    fun login(loginRequest: LoginRequest): Single<BaseResponse<User>> {
        return api.loginUser("application/json-patch+json", DeviceManger.getDeviceId().toString(), loginRequest)
    }
    fun loginOtp(loginRequest: LoginRequest): Single<BaseResponse<String>> {
        return api.loginUserOtp("application/json-patch+json", DeviceManger.getDeviceId().toString(), loginRequest)
    }
    fun loginSocial(userInfo: JsonObject): Single<BaseResponse<User>> {
        return api.loginSocial("application/json-patch+json", DeviceManger.getDeviceId().toString(), userInfo)
    }

    fun register(request: RegisterRequest): Single<BaseResponse<User>> {
        return api.registerUser("application/json-patch+json",  DeviceManger.getDeviceId().toString(), request)
    }
    fun registerSocial(userInfo: JsonObject): Single<BaseResponse<User>> {
        return api.registerSocial("application/json-patch+json",  DeviceManger.getDeviceId().toString(), userInfo)
    }

    fun getUser(token: String): Single<User> {
        return api.getUser("application/json", "Bearer $token")
    }
    fun logout(token: String): Single<ResponseBody> {
        return api.logout(LOGOUT + DeviceManger.getDeviceId().toString(),"application/json", "Bearer $token")
    }

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): Single<ResetPasswordResponse> {
        return api.resetPassword("application/json-patch+json", resetPasswordRequest)
    }

    fun changePassword(
        token: String?,
        changePasswordRequest: ChangePasswordRequest
    ): Single<BaseResponse<User>> {
        return api.changePassword("application/json-patch+json", "Bearer $token", changePasswordRequest)

    }
    fun changePassword(
        token: String?,
        changePasswordRequest: ResetPasswordRequestWithOtp
    ): Single<BaseResponse<User>> {
        return api.resetPasswordOTP("application/json-patch+json", "Bearer $token", changePasswordRequest)

    }

    fun updateProfile(userId: String, userInfo: UserRequest): Single<User> {
        return api.updateProfile("application/json", userId, userInfo)
    }

    fun updateUserInformation(userId: String, userInfo: JsonObject): Single<BaseResponse<User>> {
        return api.updateUserInformation("application/json-patch+json", userId, userInfo)
    }


    fun callbackAppraisal(userId: String, userInfo: JsonObject): Single<AppraisalResp> {
        return api.callbackAppraisal("application/json", userId, userInfo)
    }

    fun getCallbacks(url: String): Single<BaseResponse<List<Callback>>> {
        return api.getCallbacks("application/json-patch+json", url)
    }
    fun getCallback(url: String): Single<Callback> {
        return api.getCallback("application/json", url)
    }
//////

    fun sendCallback(request: JsonObject): Single<BaseResponse<Callback>> {
        return api.sendCallBack("application/json-patch+json", DeviceManger.getDeviceId().toString(), request)
    }
    fun sendCallbackAds(request: CallbackWithOutUserRequest): Single<BaseResponse<Callback>> {
        return api.sendCallBackAds("application/json-patch+json", DeviceManger.getDeviceId().toString(), request)
    }

    fun sendCallbackLocation(request: LocationCallbackWithOutUserRequest): Single<BaseResponse<Callback>> {
        return api.sendCallbackLocation("application/json-patch+json", DeviceManger.getDeviceId().toString(), request)
    }

    fun sendFinanceCallback(request: JsonObject): Single<BaseResponse<Callback>> {
        return api.sendCallBackFinance("application/json-patch+json",DeviceManger.getDeviceId().toString(),  request)
    }

    fun getCallbacksBanks(url: String): Single<BaseResponse<List<Callback>>> {
        return api.getCallbacksBanks("application/json-patch+json", url)
    }
    ///////////////

    fun requestTestDriveAtShowrooms(request: JsonObject): Single<BaseResponse<TestDrive>> {
        return api.requestTestDriveAtShowrooms("application/json", DeviceManger.getDeviceId().toString(), request)
    }

    fun requestTestDriveWithDelivery(request: JsonObject): Single<TestDriveParamsResponse> {
        return api.requestTestDriveWithDelivery("application/json-patch+json", DeviceManger.getDeviceId().toString(), request)
    }

    fun getPreferredTime(url: String): Single<BaseResponse<ArrayList<PreferredTimeTestDrive>>> {
        return api.getPreferredTime("application/json-patch+json", url)
    }

    fun getUserAddresses(url: String): Single<BaseResponse<ArrayList<UserAddress>>> {
        return api.getUserAddress("application/json-patch+json", url)
    }

    fun postUserAddress(request: JsonObject): Single<BaseResponse<UserAddress>> {
        return api.postUserAddress("application/json-patch+json", request)
    }

    fun getTestDrive(url: String): Single<BaseResponse<ArrayList<TestDrive>>> {
        return api.getTestDrive("application/json-patch+json", url)
    }

    fun postFavoriteModel(request: FavoriteParam): Single<BaseResponse<Favorite>> {
        return api.postFavoriteModel("application/json-patch+json", request)
    }

    fun getFavoriteModel(url: String): Single<BaseResponse<ArrayList<Favorite>>> {
        return api.getFavoriteList("application/json-patch+json", url)
    }

    fun deleteFavoriteModel(url: String): Single<ResponseBody> {
        return api.deleteFavoriteModel("application/json-patch+json", url)
    }

    fun uploadImage(url: MultipartBody.Part): Single<UploadFileResponse> {
        return api.uploadImage("*/*", url)
    }

    fun uploadMedia(url: MultipartBody.Part): Single<UploadFileResponse> {
        return api.uploadMedia("*/*", url)
    }

    fun requestBankCallback(json: JsonObject): Single<CallbackResponse> {
        return api.requestBankCallback("application/json-patch+json", DeviceManger.getDeviceId().toString(), json)
    }

    fun postPaymentBooking(request: BookNowRequest): Single<BaseResponse<Booking>> {
        return api.postPaymentBooking("application/json-patch+json", request)
    }

    suspend fun downloadFile(fileUrl: String): Response<ResponseBody> {
        return api.downloadFile("application/json", fileUrl)
    }

    fun postPaymentBuying(request: BuyNowRequest): Single<BaseResponse<Booking>> {
        return api.postPaymentBuying("application/json-patch+json", request)
    }

    fun postPaymentBookingRent(request: BookNowRentRequest): Single<BaseResponse<Booking>> {
        return api.postPaymentBookingRent("application/json-patch+json", request)
    }

    fun getAddons(url: String): Single<BaseResponse<List<Addon>>> {
        return api.getAddons("application/json-patch+json", url)

    }

    fun getPayments(url: String): Single<BaseResponse<List<Booking>>> {
        return api.getPayments("application/json-patch+json", url)
    }

    fun postPayment(map: MutableMap<String, RequestBody>): Single<PaymentResponse> {
        return paymentApi.postPayment(
            "application/json",
            "hWFfEkzkYE1X691J4qmcuZHAoet7Ds7ADhL",
            map
        )
    }

    fun postClientVehicle(request: JsonObject): Single<ClientVehicleResponse> {
        return api.postClientVehicle("application/json-patch+json", request)
    }

    fun postCallbackAppraisalClientVehicle(request: CallbackAppraisalClientVehicleRequest?): Single<CallbackAppraisalClientVehicleResponse> {
        return api.postCallbackAppraisalClientVehicle("application/json", DeviceManger.getDeviceId().toString(),request)
    }

    fun postCallbackAppraisalClientVehicleWithoutModel(request: CallbackAppraisalClientVehicleRequestWithoutModel?): Single<CallbackAppraisalClientVehicleResponse> {
        return api.postCallbackAppraisalClientVehicleWithoutModel("application/json-patch+json", DeviceManger.getDeviceId().toString(), request)
    }

    fun getCallbackAppraisalClientVehicleList(url: String): Single<BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>> {
        return api.getCallbackAppraisalClientVehicleList("application/json-patch+json", url)
    }
    fun getCallbackAppraisalClientVehicle(url: String): Single<CallbackAppraisalClientVehicle> {
        return api.getCallbackAppraisalClientVehicle("application/json", url)
    }
    fun getClientVehicleList(url: String): Single<BaseResponse<ArrayList<ClientVehicle>>> {
        return api.getClientVehicleList("application/json-patch+json", url)
    }
    fun getDeletedObj(url: String): Single<BaseResponse<Deleted>> {
        return api.getDeleted("application/json-patch+json", url)
    }

    fun getIndustries(): Single<ArrayList<Industry>> {
        return api.getIndustries("application/json")
    }

    fun getOptions(): Single<ArrayList<CarOption>> {
        return api.getOptions("application/json")
    }

    fun incrementDownload(request: JsonObject): Single<ResponseBody> {
        return api.incrementDownload("application/json", request)
    }

    fun getDeviceActivityLogStart(
        serialNumber: String?,
        isActive: Boolean,
        isUnique: Boolean,
        _locale: String
    ): Single<ResponseBody> {
        return api.getDeviceActivityLogStart(
            "application/json",
            serialNumber,
            isActive,
            isUnique,
            _locale
        )
    }

    fun getDeviceActivityLogStop(serialNumber: String?, _locale: String): Single<ResponseBody> {
        return api.getDeviceActivityLogStop("application/json", serialNumber, _locale)
    }

    fun postSearchLog(request: JsonObject): Single<ResponseBody> {
        return api.postSearchLog("application/json", request)
    }

    fun getDeviceActivityLogProgress(url: String): Single<ResponseBody> {
        return api.getDeviceActivityLogProgress("application/json-patch+json", url)
    }


    fun getBrandsStock(url: String): Single<BaseListResponse<List<BrandStock>>> {

        //return if (prefs.string("${url}_updatedAt") == "") {
        return api.getBrandsStock( "application/json-patch+json", url)
        /*} else {
            val oldTime = prefs.string("${url}_updatedAt")
            api.getBrandsStock("application/json", url, oldTime!!)
        }*/

    }

    fun getModelsStock(url: String): Observable<BaseListResponse<List<ModelStock>>> {
        //  return if (prefs.string("${url}_updatedAt") == "") {
        return api.getModelsStock( "application/json-patch+json", url)
        /* } else {
             val oldTime = prefs.string("${url}_updatedAt")
             api.getModelsStock("application/json", url, oldTime!!)

         }*/
    }

    fun getLoctions(url: String): Observable<BaseListResponse<List<Location>>> {
        //  return if (prefs.string("${url}_updatedAt") == "") {
        return api.getLocations( "application/json-patch+json", url)
        /* } else {
             val oldTime = prefs.string("${url}_updatedAt")
             api.getModelsStock("application/json", url, oldTime!!)

         }*/
    }

    fun getActions(url: String): Single<BaseListResponse<List<Action>>> {
        return api.getActions("application/json-patch+json", url)
    }

    fun getCode(url: String): Single<CallbackResponse> {
        return api.getCode("application/json", url)
    }

    fun verifyCode(url: String): Single<CallbackResponse> {
        return api.verifyCode("application/json", url)
    }

    fun getAppraisalInfo(): Single<BaseListResponse<List<AppraisalInfo>>> {
        return api.getAppraisalInfo("application/json-patch+json")
    }

    fun registerDevice(request: JsonObject): Single<ResponseBody> {
        return api.registerDevice("application/json", "/api/v2$REGISTER_DEVICE${DeviceManger.getDeviceId().toString()}", request)
    }

}



