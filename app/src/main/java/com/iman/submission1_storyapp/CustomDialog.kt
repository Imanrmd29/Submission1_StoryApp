package com.iman.submission1_storyapp

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.iman.submission1_storyapp.databinding.DialogSuccessCreateAccountBinding

class CustomDialog {
    private var dialog: Dialog? = null
    fun showSuccessCreateAccount(context: Context, desc: String = "", listener: () -> Unit) {
        val binding =
            DialogSuccessCreateAccountBinding.inflate(LayoutInflater.from(context), null, false)
        if (desc.isNotBlank()) {
            binding.dialogDescription.text = desc
        }
        dialog = Dialog(context, R.style.AppTheme_Dark_Dialog)
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setContentView(binding.root)
            show()
        }
        binding.btnNext.setOnClickListener {
            listener()
        }
    }
}