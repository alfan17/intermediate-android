package com.alfan.story.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.alfan.story.R
import com.alfan.story.databinding.ActivityRegisterBinding
import com.alfan.story.viewmodels.RegisterState
import com.alfan.story.viewmodels.UserViewModel
import org.koin.android.ext.android.inject

class RegisterActivity : AppCompatActivity() {

    private lateinit var regBinding: ActivityRegisterBinding

    private val userVM: UserViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        regBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(regBinding.root)

        regBinding.outlinePassword.accept {
            regBinding.register.isEnabled = it
        }

        regBinding.login.setOnClickListener {
            finish()
        }

        regBinding.register.setOnClickListener {
            userVM.register(
                regBinding.name.text.toString(),
                regBinding.email.text.toString(),
                regBinding.outlinePassword.gettext().toString()).observe(this@RegisterActivity) { observer ->
                    when(observer) {
                        RegisterState.Loading -> {
                            regBinding.containerProgressbar.root.visibility = View.VISIBLE
                        }
                        RegisterState.ErrorEmail -> {
                            regBinding.outlineEmail.error = getString(R.string.message_error_email_format)
                        }
                        RegisterState.ErrorFullName -> {
                            regBinding.outlineName.error = resources.getString(R.string.message_error_nama_format)
                        }
                        RegisterState.Success -> {
                            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                                .setTitle(resources.getString(R.string.lebel_informasi))
                                .setMessage(getString(R.string.message_success_register))
                                .setCancelable(false)
                                .setPositiveButton(resources.getString(R.string.label_dialog_positive_action)) { dialog, _ ->
                                    dialog.dismiss()
                                    finish()
                                }.show()
                        }
                        is RegisterState.Error -> {
                            regBinding.containerProgressbar.root.visibility = View.GONE

                            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                                .setTitle(resources.getString(R.string.lebel_informasi))
                                .setMessage(observer.message)
                                .setCancelable(true)
                                .setPositiveButton(resources.getString(R.string.label_dialog_positive_action)) { dialog, _ ->
                                    dialog.dismiss()
                                }.show()
                        }
                    }
            }
        }
    }
}