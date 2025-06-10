package com.example.myapp.network

import com.example.myapp.data.AuthDTO
import com.example.myapp.data.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import okhttp3.ResponseBody

interface ApiService {

    // --- AUTH ---
    @POST("/api/auth/register")
    fun register(@Body request: AuthDTO.RegisterRequest): Call<AuthDTO.AuthResponse>

    @POST("/api/auth/login")
    fun login(@Body request: AuthDTO.LoginRequest): Call<AuthDTO.AuthResponse>

    // --- USER ---
    @GET("/api/user/me")
    fun getProfile(): Call<UserDTO.UserProfileResponse>

    @PUT("/api/user/me")
    fun updateProfile(@Body request: UserDTO.UserUpdateRequest): Call<UserDTO.UserProfileResponse>

    // --- PROMOTIONS ---
    @GET("/api/promotions")
    fun getAllPromotions(): Call<List<PromotionDTO.PromotionResponse>>

    @POST("/api/promotions")
    fun createPromotion(@Body request: PromotionDTO.PromotionCreateRequest): Call<PromotionDTO.PromotionResponse>

    @PUT("/api/promotions/{id}")
    fun updatePromotion(
        @Path("id") id: Long,
        @Body request: PromotionDTO.PromotionUpdateRequest
    ): Call<PromotionDTO.PromotionResponse>

    @DELETE("/api/promotions/{id}")
    fun deletePromotion(@Path("id") id: Long): Call<Void>

    // --- ORDERS ---
    @Multipart
    @POST("/api/orders")
    fun createOrder(
        @Part("address") address: RequestBody,
        @Part("scheduledDateTime") scheduledDateTime: RequestBody,
        @Part("promoCode") promoCode: RequestBody?,
        @Part("services") services: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<OrderDTO.OrderDetailsResponse>

    @GET("/api/orders")
    fun getAllOrders(): Call<List<OrderDTO.OrderSummaryResponse>>

    @GET("/api/orders/{orderId}")
    fun getOrderDetails(@Path("orderId") orderId: String): Call<OrderDTO.OrderDetailsResponse>

    @GET("/api/orders/search")
    fun searchOrders(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<OrderDTO.PageResponse<OrderDTO.OrderSummaryResponse>>


    //доценты и проффесора
    //тесы - посоветоваться с научрук, выбирать самим

    @PUT("/api/orders/{orderId}")
    fun updateOrder(
        @Path("orderId") orderId: String,
        @Body request: OrderDTO.OrderUpdateRequest
    ): Call<OrderDTO.OrderDetailsResponse>

    @DELETE("/api/orders/{orderId}")
    fun deleteOrder(@Path("orderId") orderId: String): Call<Void>

    // --- FILES ---
    @GET("/api/files/orders/{filename}")
    @Streaming
    fun getOrderImage(@Path("filename") filename: String): Call<ResponseBody>

    // --- ANALYTICS ---
    @POST("/api/analytics/events")
    fun logAnalyticsEvent(
        @Body request: AnalyticsDTO.AnalyticsEventRequest
    ): Call<AnalyticsDTO.AnalyticsEventResponse>

    @GET("/api/analytics/events/user/{userId}")
    fun getAnalyticsEventsForUser(@Path("userId") userId: Long): Call<List<AnalyticsDTO.AnalyticsEventResponse>>
}
