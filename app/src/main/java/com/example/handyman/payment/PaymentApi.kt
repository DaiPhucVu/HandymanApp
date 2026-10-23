package com.example.handyman.payment

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Client for our Firebase Functions backend that talks to SSLCommerz.
 * Replace BASE_URL with your deployed function URL, e.g.:
 *   https://us-central1-<your-project-id>.cloudfunctions.net/
 */
object PaymentApi {
    // CHANGE THIS to your project's function URL
    private const val BASE_URL = "https://us-central1-handymanapplicationcos40006.cloudfunctions.net/"

    val service: PaymentService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentService::class.java)
    }
}

interface PaymentService {
    @POST("initPayment")
    suspend fun initPayment(@Body req: InitPaymentRequest): InitPaymentResponse
}

data class InitPaymentRequest(
    val jobId: String,
    val customerId: String,
    val amount: Double,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
)

data class InitPaymentResponse(
    val GatewayPageURL: String,
    val tranId: String,
)
