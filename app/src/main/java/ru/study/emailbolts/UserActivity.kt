package ru.study.emailbolts

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class UserActivity : AppCompatActivity() {
    companion object {
        private const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
        private const val DATA_JSON = "DATA_JSON"

        fun startActivity(context: Context) {
            context.startActivity(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, UserActivity::class.java)
        }
    }

    private lateinit var emailDetails: TextView
    private lateinit var firstNameDetails: TextView
    private lateinit var lastNameDetails: TextView
    private lateinit var birthDateDetails: TextView
    private lateinit var notesDetails: TextView
    private lateinit var logout: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        emailDetails = findViewById(R.id.emailDetails)
        firstNameDetails = findViewById(R.id.firstNameDetails)
        lastNameDetails = findViewById(R.id.lastNameDetails)
        birthDateDetails = findViewById(R.id.birthDateDetails)
        notesDetails = findViewById(R.id.notesDetails)
        logout = findViewById(R.id.logout)

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)

        sharedPreferences.getString(
            DATA_JSON, null)?.let {
                with(Gson().fromJson(it, UserInfo::class.java)) {
                    emailDetails.text = email
                    firstNameDetails.text = firstName
                    lastNameDetails.text = lastName
                    birthDateDetails.text = getBirthDate(birthDate)
                    notesDetails.text = notes
                }
        }

        logout.setOnClickListener {
            onExit()
            MainActivity.startActivity(this)
            finish()
        }
    }

    private fun onExit() {
        sharedPreferences.edit {
            clear()
            apply()
        }
    }

    private fun getBirthDate(birthDate: String?): String? {
        val sdf = SimpleDateFormat(resources.getString(R.string.simpleDateFormat), Locale.getDefault())
        birthDate?.let{
            val date =  java.util.Date(it.toLong() * 1000)
            return sdf.format(date)
        }
        return null
    }
}
