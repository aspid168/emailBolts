package ru.study.emailbolts

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserInfoApi {
    @POST("lesson-20.php?method=login")
    fun getToken(@Body body: RequestBody): Call<TokenInfo>

    @POST("lesson-20.php?method=profile")
    fun getUserInfo(@Body body: RequestBody): Call<UserInfo>
}
