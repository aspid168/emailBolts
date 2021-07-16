package ru.study.emailbolts

import bolts.Task
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable

class LoginTask : Serializable {
    companion object {
        private const val TOKEN = "token"
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
        private const val OK_STATUS = "ok"

    }

    var isRunning = false
    private var listener: Handler? = null

    fun addListener(handler: Handler) {
        listener = handler
    }

    fun executeTask(email: String, password: String): Task<Void> {
        val q = configureRetrofit()
        return Task.callInBackground {
            isRunning = true
            val loginRequestBody =
                createJsonToGetToken(email, password).toRequestBody()
            Thread.sleep(3000)
            (q.getToken(loginRequestBody).execute().body() as TokenInfo)
        }.onSuccess {
            var userInfo: UserInfo? = null
            it.result?.let { result ->
                if (result.status == OK_STATUS) {
                    val token = result.token
                    val profileRequestBody = createJsonToGetProfileInfo(token).toRequestBody()
                    userInfo = q.getUserInfo(profileRequestBody).execute().body()
                }
            }
            userInfo
        }.continueWith({ task ->
            val userInfo = task.result
            if (userInfo != null) {
                userInfo.email = email
                listener?.onSuccess(Gson().toJson(userInfo))
            } else {
                listener?.onError()
            }
            isRunning = false
            null
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun configureRetrofit(): UserInfoApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pub.zame-dev.org/senla-training-addition/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(UserInfoApi::class.java)
    }

    private fun createJsonToGetToken(email: String, password: String) = JSONObject()
        .put(EMAIL, email)
        .put(PASSWORD, password)
        .toString()

    private fun createJsonToGetProfileInfo(token: String) = JSONObject()
        .put(TOKEN, token)
        .toString()
}
