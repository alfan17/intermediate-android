package com.alfan.story.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.alfan.story.R
import com.alfan.story.databinding.ActivityLoginBinding
import com.alfan.story.library.PreferencesExt.setObject
import com.alfan.story.viewmodels.LoginState
import com.alfan.story.viewmodels.UserViewModel
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences by inject()

    private val userVM: UserViewModel by inject()

    private lateinit var logBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(logBinding.root)

        logBinding.outlinePassword.accept {
            logBinding.login.isEnabled = it
        }

        logBinding.register.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        logBinding.login.setOnClickListener {
            userVM.login(
                logBinding.email.text.toString(),
                logBinding.outlinePassword.gettext().toString()).observe(this@LoginActivity) { observer ->
                    when(observer) {
                        LoginState.Loading -> {
                            logBinding.containerProgressbar.root.visibility = View.VISIBLE
                        }
                        LoginState.ErrorEmail -> {
                            logBinding.outlineEmail.error = getString(R.string.message_error_email_format)
                        }
                        is LoginState.Error -> {
                            logBinding.containerProgressbar.root.visibility = View.GONE

                            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                                .setTitle(resources.getString(R.string.lebel_informasi))
                                .setMessage(observer.message)
                                .setCancelable(true)
                                .setPositiveButton(resources.getString(R.string.label_dialog_positive_action)) { dialog, _ ->
                                    dialog.dismiss()
                                }.setNeutralButton(resources.getString(R.string.label_daftar)) { dialog, _ ->
                                    dialog.dismiss()
                                    startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                                }.show()
                        }
                        is LoginState.SuccessLogin -> {
                            sharedPreferences.setObject("login", observer.response)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }
            }
        }
    }


}