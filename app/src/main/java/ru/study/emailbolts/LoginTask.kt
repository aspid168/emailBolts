package ru.study.emailbolts

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.edit
import bolts.Task
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val METHOD_LOGIN = "login"
private const val METHOD_PROFILE = "profile"
private const val METHOD = "method"
private const val STATUS = "status"
private const val TOKEN = "token"
private const val EMAIL = "email"
private const val PASSWORD = "password"
private const val OK_STATUS = "ok"
private const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
private const val DATA_JSON = "DATA_JSON"

private val gson = Gson()
private val client = OkHttpClient()

class LoginTask() : Parcelable {

    var isRunning = false
    private var activity: MainActivity? = null

    constructor(parcel: Parcel) : this() {

    }

    fun unlink() {
        activity = null
    }

    fun link(act: MainActivity?) {
        activity = act
    }

    fun executeTask(email: String, password: String): Task<Void> {
        return Task.callInBackground {
            isRunning = true
            Thread.sleep(3000)

            val loginRequestBody =
                createJsonToGetToken(email, password)
            val request = createRequest(METHOD_LOGIN, loginRequestBody.toRequestBody())
            val result = getResult(request)

            val token: String?
            var userData: String? = null
            if (result != null && JSONObject(result).get(STATUS).equals(OK_STATUS)) {
                token = JSONObject(result).get("token").toString()
                val profileRequestBody = createJsonToGetProfileInfo(token).toRequestBody()
                val req = createRequest(METHOD_PROFILE, profileRequestBody)
                userData = getResult(req)
            }
            gson.fromJson(userData, UserInfo::class.java)
        }.onSuccess { task ->
            Task.UI_THREAD_EXECUTOR.execute {
                activity?.let {
                it.progressDialog.dismiss()
                task.result?.let { result ->
                    result.email = email
                    addToSharedPreferences(gson.toJson(result))
                    UserActivity.startActivity(it)
                    } ?: run {
                        it.error.text =
                            it.resources?.getString(R.string.errorMessageErrorFromServer)
                    }
                }
            }
            isRunning = false
            null
        }
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
        var result: String? = null
        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                result = it.body?.string()
            }
        }
        return result
    }

    private fun addToSharedPreferences(resultJson: String) {
        activity?.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)?.edit {
            putString(DATA_JSON, resultJson)
            commit()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginTask> {
        override fun createFromParcel(parcel: Parcel): LoginTask {
            return LoginTask(parcel)
        }

        override fun newArray(size: Int): Array<LoginTask?> {
            return arrayOfNulls(size)
        }
    }
}
