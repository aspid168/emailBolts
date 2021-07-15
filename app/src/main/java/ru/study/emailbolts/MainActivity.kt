package ru.study.emailbolts

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.edit
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ERROR_EXTRA = "ERROR_EXTRA"
        private const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
        private const val DATA_JSON = "DATA_JSON"

        fun startActivity(context: Context) {
            context.startActivity(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    lateinit var error: TextView
    private lateinit var login: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    lateinit var progressDialog: ProgressDialog

    private var loginTask: LoginTask? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .getString(DATA_JSON, null)
            ?.let {
                UserActivity.startActivity(this)
                finish()
            }

        error = findViewById(R.id.error)
        login = findViewById(R.id.login)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("In Progress")
            setTitle("Wait")
        }

        savedInstanceState?.getString(ERROR_EXTRA)?.let {
            error.text = it
        }

        loginTask = LoginTask()

        if (LoginTask.isRunning) {
            progressDialog.show()
        }

//        login.setText("john@domain.tld")
//        password.setText("123123")

        loginButton.setOnClickListener {
            val loginText = login.text.toString()
            val passwordText = password.text.toString()
            if (hasEmptyFields(loginText, passwordText)) {
                progressDialog.show()
                loginTask?.addListener(object : Handler {
                    override fun onSuccess(userInfo: String) {
                        addToSharedPreferences(userInfo)
                        progressDialog.dismiss()
                        UserActivity.startActivity(this@MainActivity)
                        finish()
                    }

                    override fun onError() {
                        error.text =
                            it.resources?.getString(R.string.errorMessageErrorFromServer)
                        progressDialog.dismiss()
                    }
                })
                loginTask?.executeTask(loginText, passwordText)
            } else {
                error.text = resources.getString(R.string.errorMessageEmptyField)
            }
        }
    }

    private fun hasEmptyFields(login: String, password: String): Boolean =
        login.isNotEmpty() && password.isNotEmpty()

    private fun addToSharedPreferences(resultJson: String) {
        getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)?.edit {
            putString(DATA_JSON, resultJson)
            apply()
        }
    }

    override fun onPause() {
        super.onPause()
        progressDialog.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ERROR_EXTRA, error.text.toString())
    }
}

interface Handler {
    fun onSuccess(userInfo: String)
    fun onError()
}
