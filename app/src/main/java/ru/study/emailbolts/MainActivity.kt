package ru.study.emailbolts

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import bolts.Task.UI_THREAD_EXECUTOR

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ERROR_EXTRA = "ERROR_EXTRA"
        private const val LOGIN_TASK_EXTRA = "LOGIN_TASK_EXTRA"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        error = findViewById(R.id.error)
        login = findViewById(R.id.login)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("In Progress")
            setTitle("Wait")
        }

        if (savedInstanceState != null) {
            savedInstanceState.getParcelable<LoginTask>(LOGIN_TASK_EXTRA)?.let {
                loginTask = it
                if (loginTask?.isRunning == true) {
                    progressDialog.show()
                }
            }
            savedInstanceState.getString(ERROR_EXTRA)?.let {
                error.text = it
            }
        } else {
            loginTask = LoginTask()
        }
        loginTask?.link(this)

//        login.setText("john@domain.tld")
//        password.setText("123123")

        loginButton.setOnClickListener {
            val loginText = login.text.toString()
            val passwordText = password.text.toString()
            if (checkEmptyFields(loginText, passwordText)) {
                progressDialog.show()
                loginTask?.executeTask(loginText, passwordText)
            } else {
                error.text = resources.getString(R.string.errorMessageEmptyField)
            }

        }
    }

    private fun checkEmptyFields(login: String, password: String): Boolean =
        login.isNotEmpty() && password.isNotEmpty()

    override fun onPause() {
        super.onPause()
        loginTask?.unlink()
        progressDialog.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ERROR_EXTRA, error.text.toString())
        outState.putParcelable(LOGIN_TASK_EXTRA, loginTask)
    }
}
