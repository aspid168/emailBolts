package ru.study.emailbolts

import bolts.Task
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.Serializable



class LoginTask : Serializable {
    companion object: Serializable {
        private const val METHOD_LOGIN = "login"
        private const val METHOD_PROFILE = "profile"
        private const val METHOD = "method"
        private const val STATUS = "status"
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
        val gson = Gson()
        return Task.callInBackground {
            isRunning = true

            Thread.sleep(3000)

            val loginRequestBody =
                createJsonToGetToken(email, password)
            val request = createRequest(METHOD_LOGIN, loginRequestBody.toRequestBody())
            getResult(request)
        }.onSuccess {
            var userData: String? = null
            it.result?.let { result ->
                if (JSONObject(result).get(STATUS).equals(OK_STATUS)) {
                    val token = JSONObject(result).get(TOKEN).toString()
                    val profileRequestBody = createJsonToGetProfileInfo(token).toRequestBody()
                    val req = createRequest(METHOD_PROFILE, profileRequestBody)
                    userData = getResult(req)
                }
            }
            userData
        }.onSuccess {
            gson.fromJson(it.result, UserInfo::class.java)
        }.continueWith({ task ->
            val userInfo = task.result
            if (userInfo != null) {
                userInfo.email = email
                listener?.onSuccess(gson.toJson(userInfo))
            } else {
                listener?.onError()
            }
            isRunning = false
            null
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun createRequest(method: String, requestBody: RequestBody): Request {
        return Request.Builder()
            .url(createUrl(method))
            .post(requestBody)
            .build()
    }

    private fun createUrl(method: String) =
        "https://pub.zame-dev.org/senla-training-addition/lesson-20.php".toHttpUrl()
            .newBuilder()
            .addQueryParameter(METHOD, method)
            .build()

    private fun createJsonToGetToken(email: String, password: String) = JSONObject()
        .put(EMAIL, email)
        .put(PASSWORD, password)
        .toString()

    private fun createJsonToGetProfileInfo(token: String) = JSONObject()
        .put(TOKEN, token)
        .toString()

    private fun getResult(request: Request): String? {
        val client = OkHttpClient()
        var result: String? = null
        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                result = it.body?.string()
            }
        }
        return result
    }
}
