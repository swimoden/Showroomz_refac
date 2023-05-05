package com.kuwait.showroomz.model.api

import com.kuwait.showroomz.extras.PAYMENT_GATEWAY
import com.kuwait.showroomz.model.simplifier.PaymentResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface PaymentApi {
    @POST(PAYMENT_GATEWAY)
    @Multipart
    fun postPayment(@Header("Accept") content_type: String, @Header("x-Authorization") key: String, @PartMap body: MutableMap<String, RequestBody>) : Single<PaymentResponse>
}