package com.alfan.story.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.alfan.story.R

class PasswordEdittext: TextInputLayout {

    private lateinit var editInputText: TextInputEditText

    private var accListener: ((Boolean) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        editInputText = TextInputEditText(context)
        editInputText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        editInputText.transformationMethod = PasswordTransformationMethod.getInstance()
        editInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                error = if (s.length < 6) {
                    resources.getString(R.string.message_error_typing_password)
                } else {
                    ""
                }

                isErrorEnabled = !error.isNullOrBlank()
                accListener?.invoke(error.isNullOrBlank())
            }
        })

        createEditBox(editInputText)
    }

    fun accept(accListener: (Boolean) -> Unit) {
        this.accListener = accListener
    }

    fun gettext() = editInputText.text

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        hint = resources.getString(R.string.label_password)

    }

    private fun createEditBox(editInputText: TextInputEditText) {
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        editInputText.setPadding(32, 42, 32, 42)
        editInputText.layoutParams = layoutParams
        addView(editInputText)
    }

}