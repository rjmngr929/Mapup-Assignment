package com.my.mapupassessment.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.request.PermissionRequest
import com.my.mapupassessment.R

internal fun Context.showToast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

//componentId.visible()
fun View.gone() {
    visibility = View.GONE
}

//componentId.invisible()
fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setOnSingleClickListener(interval: Long = 1000, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}

fun View.showSnack(message: String, textColor: Int = 0, bgColor: Int = 0, action: String = "",  actionListener: () -> Unit = {}): Snackbar {
    var snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    // Set background color
//    snackbar.view.setBackgroundColor()
    if(textColor != 0)
        snackbar.setTextColor(ContextCompat.getColor(this.context, textColor))

    if(bgColor != 0){
        snackbar.setBackgroundTint(ContextCompat.getColor(this.context, bgColor))
    }

    if (action != "") {
        snackbar.duration = Snackbar.LENGTH_INDEFINITE

        snackbar.setAction(action) {
            actionListener()
            snackbar.dismiss()
        }
    }
    snackbar.show()
    return snackbar
}

fun String.capitalizeFirstLetter(): String {
    return if (isNotEmpty()) {
        this[0].uppercaseChar() + substring(1)
    } else {
        this
    }
}


internal fun Context.showGrantedToast(permissions: List<PermissionStatus>) {
    val msg = getString(R.string.granted_permissions, permissions.toMessage<PermissionStatus.Granted>())
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

internal fun Context.showRationaleDialog(permissions: List<PermissionStatus>, request: PermissionRequest, msg: String) {

//    AlertDialog.Builder(this)
//        .setTitle(R.string.permissions_required)
//        .setMessage(msg)
//        .setPositiveButton(R.string.request_again) { _, _ ->
//            // Send the request again.
//            request.send()
//        }
//        .setNegativeButton(R.string.cancel, null)
//        .show()

    var dialogBuilder = AlertDialog.Builder(this)
    val layoutView: View = LayoutInflater.from(this).inflate(R.layout.permisison_alert, null)

    val alertTitle : TextView = layoutView.findViewById(R.id.alertdialog_title_permission)
    val alertMessage : TextView = layoutView.findViewById(R.id.alertdialog_desc_permission)
    val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_permission_btn)
    val cancelBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_cancel_permission_btn)


    dialogBuilder.setView(layoutView)
    val alertDialog = dialogBuilder.create()
    alertDialog.setCancelable(false)
    alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    alertDialog.show()

    alertTitle.text = resources.getString(R.string.permissions_required)
    alertMessage.text = msg

    confirmBtn.setOnClickListener {
        request.send()
        alertDialog.dismiss()
    }

    cancelBtn.setOnClickListener {
        alertDialog.dismiss()
    }
}

@SuppressLint("MissingInflatedId")
internal fun Context.showPermanentlyDeniedDialog(permissions: List<PermissionStatus>, msg: String, onButtonClick: () -> Unit = {}) {

//    AlertDialog.Builder(this)
//        .setTitle(R.string.permissions_required)
//        .setMessage(msg)
//        .setPositiveButton(R.string.action_settings) { _, _ ->
//            onButtonClick()
//        }
//        .setNegativeButton(R.string.cancel, null)
//        .show()

    var dialogBuilder = AlertDialog.Builder(this)
    val layoutView: View = LayoutInflater.from(this).inflate(R.layout.permisison_alert, null)

    val alertTitle : TextView = layoutView.findViewById(R.id.alertdialog_title_permission)
    val alertMessage : TextView = layoutView.findViewById(R.id.alertdialog_desc_permission)
    val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_permission_btn)
    val cancelBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_cancel_permission_btn)


    dialogBuilder.setView(layoutView)
    val alertDialog = dialogBuilder.create()
    alertDialog.setCancelable(false)
    alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    alertDialog.show()

    alertTitle.text = resources.getString(R.string.permissions_required)
    alertMessage.text = msg

    confirmBtn.setOnClickListener {
        onButtonClick()
        alertDialog.dismiss()
    }

    cancelBtn.setOnClickListener {
        alertDialog.dismiss()
    }


}

internal fun Context.customAlertDialogAnim(myContext: Context, titleMsg: String, msg: String, btnText : String= "", onButtonClick: () -> Unit = {}){

//    AlertDialog.Builder(this)
//        .setTitle(titleMsg)
//        .setMessage(msg)
//        .setPositiveButton(btnText) { _, _ ->
//            onButtonClick()
//        }
//        .setNegativeButton(R.string.cancel, null)
//        .show()

    var dialogBuilder = AlertDialog.Builder(this)
    val layoutView: View = LayoutInflater.from(this).inflate(R.layout.permisison_alert, null)

    val alertTitle : TextView = layoutView.findViewById(R.id.alertdialog_title_permission)
    val alertMessage : TextView = layoutView.findViewById(R.id.alertdialog_desc_permission)
    val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_permission_btn)
    val cancelBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_cancel_permission_btn)


    dialogBuilder.setView(layoutView)
    val alertDialog = dialogBuilder.create()
    alertDialog.setCancelable(false)
    alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    alertDialog.show()

    alertTitle.text = resources.getString(R.string.permissions_required)
    alertMessage.text = msg

    confirmBtn.setOnClickListener {
        onButtonClick()
        alertDialog.dismiss()
    }

    cancelBtn.setOnClickListener {
        alertDialog.dismiss()
    }
}



//**************************** Loader ************************************************************
//Loader
fun getLoadingDialog(myContext: Context): androidx.appcompat.app.AlertDialog {
    val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(myContext, R.style.MyDialogStyle_transparent)
    alertDialogBuilder.setView(R.layout.layout_loading_alert_dialog)
    alertDialogBuilder.setCancelable(false)

    return alertDialogBuilder.create()
}

//show Loader
fun showLoader(myContext: Context, loader: androidx.appcompat.app.AlertDialog){
    loader.show()
}

//hide loader
fun hideLoader(myContext: Context, loader: androidx.appcompat.app.AlertDialog){
    if (loader.isShowing)
        loader.dismiss()
}
//**************************** Loader ************************************************************

private inline fun <reified T : PermissionStatus> List<PermissionStatus>.toMessage(): String = filterIsInstance<T>()
    .joinToString { it.permission }