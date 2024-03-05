package com.kuwait.showroomz.model.api

import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @GET(value = CATEGORIES_API)// add endpoint
    fun getCategories(
        //@Header("Accept") content_type: String
//        @Query("updatedAt[after]") date: String
    ): Single<BaseListResponse<List<Category>>>

    @GET
    fun getBrandsByCategoryId(
        @Url url: String
    ): Single<BaseListResponse<List<Brand>>>

    @GET
    fun getLocationsByBrandId(
        @Header("Content-Type") content_type: String,
        @Url url: String,
    ): Single<BaseListResponse<java.util.ArrayList<Location>>>

    @GET
    fun getModelsByBrandId(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<List<Model>>

    @GET
    fun getModelsById(
        @Header("Accept") content_type: String,
        @Url url: String,
        @Query("updatedAt[after]") date: String
    ): Single<Model>

    @GET
    fun getModelsBySlug(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Single<List<Model>>

    @GET
    fun getOffersByTrimId(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Observable<BaseListResponse<List<Offer>>>

    @GET
    fun getBrandBySlug(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Single<List<Brand>>

    @GET
    fun getCatBySlug(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Single<List<Category>>

    @GET(value = INCREMENT_MODEL_VIEW)
    fun incrementModelNbViews(@Path("id") id: String): Single<IncrementNViewResponse>

    @GET
    fun getModelTypeByCategory(
        @Header("Content-Type") content_type: String,
        @Url url: String,
        @Query("updatedAt[after]") date: String
    ): Single<BaseListResponse<List<Type>>>
/// change this to post
    @POST
    fun getAllBrandModels(
        @Header("Content-Type") content_type: String,
        @Url url: String,
        @Body userInfo: JsonObject
    ): Observable<BaseListResponse<List<Model>>>

    @GET
    fun filterModels(
        @Header("Accept") content_type: String,
        @Url url: String,
        @Query("updatedAt[after]") date: String
    ): Single<List<Model>>


    @GET(GET_MODEL_GALLERY)
    fun getModelGalleryById(
        @Header("Accept") content_type: String,
        @Path("id") idModel: String
    ): Single<ModelGallery>

    @GET(GET_MODEL_GALLERY)
    fun getModelImagesById(
        @Header("Accept") content_type: String,
        @Path("id") idModel: String
    ): Observable<BaseListResponse<List<Image>>>


    @GET(GET_MODEL_VIDEOS)
    fun getIVideos(
        @Header("Accept") content_type: String,
        @Path("id") idModel: String
    ): Single<VideoGallery>

    @GET(GET_MODEL_VIDEOS)
    fun getModelVideoById(
        @Header("Accept") content_type: String,
        @Path("id") idModel: String
    ): Observable<BaseListResponse<List<Image>>>
    @GET
    fun getModelTrims(
        @Header("Accept") content_type: String,
        @Url url: String
    ): Single<TrimResponse>


    @GET
    fun getTrimByModelId(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Observable<BaseListResponse<List<Trim>>>

    @GET
    fun getOfferByModelId(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Observable<BaseListResponse<List<Offer>>>

    @GET(GET_ALL_BANKS)
    fun getBanks(
        @Header("Content-Type") content_type: String,
        @Query("updatedAt[after]") date: String
    ): Single<BaseListResponse<List<Bank>>>

    @GET(GET_ADS)
    fun getAdvertisements(
        @Header("Accept") content_type: String,
        @Query("updatedAt[after]") date: String
    ): Single<List<Advertisement>>

    @GET
    fun getAdvertisementsWithPagination(
        @Header("Content-Type") content_type: String,
        @Url url: String,
    ): Single<BaseListResponse<List<Advertisement>>>

    @GET(INCREMENT_ADS_VIEW)
    fun incrementAdsNbView(@Path("id") adsId: String): Single<IncrementNViewResponse>

    @GET(INCREMENT_ADS_CLICK)
    fun incrementAdsNbClick(@Path("id") adsId: String): Single<IncrementNViewResponse>

    @GET(SETTINGS)
    fun getSettings(): Single<BaseResponse<Setting>>

    @POST(REGISTER)
    fun registerUser(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: RegisterRequest
    ): Single<BaseResponse<User>>

    @POST(REGISTERSOCIAL)
    fun registerSocial(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body userInfo: JsonObject
    ): Single<BaseResponse<User>>

    @POST(LOGIN)
    fun loginUser(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: LoginRequest
    ): Single<BaseResponse<User>>

    @POST(LOGIN)
    fun loginUserOtp(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: LoginRequest
    ): Single<BaseResponse<String>>

    @POST(LOGINSOCIAL)
    fun loginSocial(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body userInfo: JsonObject
    ): Single<BaseResponse<User>>

    @GET(ME)
    fun getUser(
        @Header("Accept") content_type: String,
        @Header("Authorization") token: String
    ): Single<User>


    @GET
    fun logout(
        @Url url: String,
        @Header("Accept") content_type: String,
        @Header("Authorization") token: String
    ): Single<ResponseBody>

    @PUT(RESET_PASSWORD)
    fun resetPassword(
        @Header("Content-Type") content_type: String,
        @Body resetPasswordRequest: ResetPasswordRequest
    ): Single<ResetPasswordResponse>

    @PUT(CHANGE_PASSWORD)
    fun changePassword(
        @Header("Content-Type") content_type: String,
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Single<BaseResponse<User>>

    @PUT(RESET_PASSWORD_OTP)
    fun resetPasswordOTP(
        @Header("Content-Type") content_type: String,
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ResetPasswordRequestWithOtp
    ): Single<BaseResponse<User>>

    @PUT(UPDATE_PROFILE)
    fun updateProfile(
        @Header("Accept") content_type: String,
        @Path("id") userId: String,
        @Body userInfo: UserRequest
    ): Single<User>

    @PUT(UPDATE_PROFILE)
    fun updateUserInformation(
        @Header("Content-Type") content_type: String,
        @Path("id") userId: String,
        @Body userInfo: JsonObject
    ): Single<BaseResponse<User>>

    @PUT(CALLBACK_APPRAISAL)
    fun callbackAppraisal(
        @Header("Accept") content_type: String,
        @Path("id") userId: String,
        @Body userInfo: JsonObject
    ): Single<AppraisalResp>

    @GET
    fun getCallbacks(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<List<Callback>>>

    @GET
    fun getCallback(
        @Header("Accept") content_type: String,
        @Url url: String
    ): Single<Callback>

    @GET(GET_CALLBACKS_BY_ID)
    fun getCallbackById(
        @Header("Accept") content_type: String,
        @Path("id") userId: String
    ): Single<Callback>
//////


    @POST(GET_CALLBACKS)
    fun sendCallBack(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: JsonObject
    ): Single<BaseResponse<Callback>>

    @POST(GET_CALLBACKSADS)
    fun sendCallBackAds(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: CallbackWithOutUserRequest
    ): Single<BaseResponse<Callback>>

    @POST(CALLBACK_LOCATION)
    fun sendCallbackLocation(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: LocationCallbackWithOutUserRequest
    ): Single<BaseResponse<Callback>>

    @POST(CALLBACK_BANKS)
    fun sendCallBackFinance(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: JsonObject
    ): Single<BaseResponse<Callback>>

    @GET
    fun getCallbacksBanks(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<List<Callback>>>
////////
    @GET
    fun getPreferredTime(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<PreferredTimeTestDrive>>>

    @POST(TEST_DRIVE_AT_SHOWROOM)
    fun requestTestDriveAtShowrooms(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: JsonObject
    ): Single<BaseResponse<TestDrive>>

    @POST(TEST_DRIVE_WITH_DELIVERY)
    fun requestTestDriveWithDelivery(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: JsonObject
    ): Single<TestDriveParamsResponse>

    @GET
    fun getUserAddress(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<UserAddress>>>

    @POST(CLIENT_ADDRESS)
    fun postUserAddress(
        @Header("Content-Type") content_type: String,
        @Body request: JsonObject
    ): Single<BaseResponse<UserAddress>>

    @GET
    fun getTestDrive(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<TestDrive>>>

    @GET
    fun getFavoriteList(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<Favorite>>>

    @POST(FAVORITE_MODELS)
    fun postFavoriteModel(
        @Header("Content-Type") content_type: String,
        @Body request: FavoriteParam
    ): Single<BaseResponse<Favorite>>

    @DELETE
    fun deleteFavoriteModel(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<ResponseBody>

    @Multipart
    @POST(IMAGES)
    fun uploadImage(
        @Header("Accept") content_type: String,
        @Part file: MultipartBody.Part
    ): Single<UploadFileResponse>

    @Multipart
    @POST(MEDIA)
    fun uploadMedia(
        @Header("Accept") content_type: String,
        @Part file: MultipartBody.Part
    ): Single<UploadFileResponse>

    @POST(CALLBACK_BANKS)
    fun requestBankCallback(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body json: JsonObject
    ): Single<CallbackResponse>

    @Streaming
    @GET
    suspend fun downloadFile(
        @Header("Accept") content_type: String,
        @Url fileUrl: String
    ): Response<ResponseBody>

    @POST(CLIENT_PAYMENT_BOOKING)
    fun postPaymentBooking(
        @Header("Content-Type") content_type: String,
        @Body bookNowRequest: BookNowRequest
    ): Single<BaseResponse<Booking>>

    @POST(CLIENT_PAYMENT_BUYING)
    fun postPaymentBuying(
        @Header("Content-Type") content_type: String,
        @Body buyNow: BuyNowRequest
    ): Single<BaseResponse<Booking>>

    @GET
    fun getAddons(@Header("Content-Type") content_type: String, @Url url: String): Single<BaseResponse<List<Addon>>>

    @POST(CLIENT_PAYMENT_RENT)
    fun postPaymentBookingRent(
        @Header("Content-Type") content_type: String,
        @Body bookRent: BookNowRentRequest
    ): Single<BaseResponse<Booking>>

    @GET
    fun getPayments(@Header("Accept") content_type: String, @Url url: String): Single<BaseResponse<List<Booking>>>

    @POST(CLIENT_VEHICLES)
    fun postClientVehicle(
        @Header("Content-Type") content_type: String,
        @Body request: JsonObject
    ): Single<ClientVehicleResponse>

    @POST(CALLBACK_APPRAISAL_CLIENT_VEHICLES)
    fun postCallbackAppraisalClientVehicle(
        @Header("Accept") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: CallbackAppraisalClientVehicleRequest?
    ): Single<CallbackAppraisalClientVehicleResponse>

    @POST(CALLBACK_APPRAISAL_CLIENT_VEHICLES)
    fun postCallbackAppraisalClientVehicleWithoutModel(
        @Header("Content-Type") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Body request: CallbackAppraisalClientVehicleRequestWithoutModel?
    ): Single<CallbackAppraisalClientVehicleResponse>

    @GET
    fun getCallbackAppraisalClientVehicleList(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>>

    @GET
    fun getCallbackAppraisalClientVehicle(
        @Header("Accept") content_type: String,
        @Url url: String
    ): Single<CallbackAppraisalClientVehicle>

    @GET
    fun getClientVehicleList(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<ArrayList<ClientVehicle>>>

    @GET
    fun getDeleted(
        @Header("Content-Type") content_type: String,
        @Url url: String
    ): Single<BaseResponse<Deleted>>

    @GET(INDUSTRIES)
    fun getIndustries(@Header("Accept") content_type: String): Single<ArrayList<Industry>>

    @GET(APPRAISAL_CAR_OPTIONS)
    fun getOptions(@Header("Accept") content_type: String): Single<ArrayList<CarOption>>

    @POST(INCREMENT_NBR_DOWNLOADS)
    fun incrementDownload(
        @Header("Accept") content_type: String,
        @Body json: JsonObject
    ): Single<ResponseBody>

    @GET(DEVICE_ACTIVITY_LOG_START)
    fun getDeviceActivityLogStart(
        @Header("Accept") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Query("isActive") isActive: Boolean,
        @Query("isUnique") isUnique: Boolean,
        @Query("language") _locale: String
    ): Single<ResponseBody>
    @GET(DEVICE_ACTIVITY_LOG_STOP)
    fun getDeviceActivityLogStop(
        @Header("Accept") content_type: String,
        @Query("serialNumber") serialNumber: String?,
        @Query("language") _locale: String
    ): Single<ResponseBody>
    @POST(SEARCH_LOG)
    fun postSearchLog(
        @Header("Accept") content_type: String,
        @Body json: JsonObject
    ): Single<ResponseBody>

    @GET
    fun getDeviceActivityLogProgress(  @Header("Content-Type") content_type: String,@Url url:String): Single<ResponseBody>

    @PUT
    fun registerDevice(
        @Header("Accept") content_type: String,
        @Url url: String,
        @Body json: JsonObject
    ): Single<ResponseBody>

    @GET
    fun getBrandsStock(
        @Header("Content-Type") content_type: String,
        @Url url: String,
    ): Single<BaseListResponse<List<BrandStock>>>

    @GET
    fun getActions(
        @Header("Content-Type") content_type: String,
        @Url url: String

    ): Single<BaseListResponse<List<Action>>>


    @GET
    fun getModelsStock(
        @Header("Content-Type") content_type: String,
        @Url url: String,

    ): Observable<BaseListResponse<List<ModelStock>>>

    @GET
    fun getLocations(
        @Header("Content-Type") content_type: String,
        @Url url: String,

        ): Observable<BaseListResponse<List<Location>>>

    @GET
    fun getCode(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Single<CallbackResponse>

    @GET
    fun verifyCode(
        @Header("Accept") content_type: String,
        @Url url: String

    ): Single<CallbackResponse>
    @GET(value = APPRAISALINFOAPI)
    fun getAppraisalInfo(
        @Header("Content-Type") content_type: String
    ): Single<BaseListResponse<List<AppraisalInfo>>>


}