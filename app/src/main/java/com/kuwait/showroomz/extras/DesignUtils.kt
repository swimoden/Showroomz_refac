package com.kuwait.showroomz.extras

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.view.MainActivity

class DesignUtils private constructor() {


    private object HOLDER {
        val INSTANCE = DesignUtils()
    }
    companion object {
        val instance: DesignUtils by lazy { HOLDER.INSTANCE }
    }
    fun setTransparentStatusBar( activity:Activity,bool: Boolean) {
        if (bool) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )


        }
    }
    fun changeScreenOrientation(activity:Activity,orientation:Int){
        activity.requestedOrientation=orientation
    }
    fun disableUserInteraction(activity:Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

    }

    fun enableUserInteraction(activity:Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    fun setVisibilityWithSlide(view: View,direction:Int){
        val transition: Transition = Slide(direction)
        transition.duration = 400
        TransitionManager.beginDelayedTransition(view.parent as ViewGroup, transition)
        view.visibility = if (!view.isVisible) View.VISIBLE else View.GONE
    }
    fun createDrawable(@ColorInt color:Int,type:Int): GradientDrawable {
        val drawable: GradientDrawable = GradientDrawable()
        drawable.shape = type
        drawable.cornerRadii= FloatArray(8){20f;20f;20f;20f;20f;20f;20f;20f}
        drawable.setColor(color)
        return drawable

    }

    fun createDrawableWithStrock(@ColorInt color:Int, @ColorInt strok:Int, type:Int): GradientDrawable? {
        val drawable: GradientDrawable = GradientDrawable()
        drawable.shape = type
        drawable.setStroke(4,strok)
        drawable.cornerRadii= FloatArray(8){20f;20f;20f;20f;20f;20f;20f;20f}
        drawable.setColor(color)
        return drawable
    }
    fun setStatusBar(context:MainActivity,isDark: Boolean,color: Int?) {
        if (color != null) {
            context.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            context.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val lFlags: Int = context.window.decorView.systemUiVisibility

                context.window.statusBarColor = color
                context.window.decorView.systemUiVisibility =
                    if (!isDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }


    }
    fun hideKeyboard(activity: Activity){
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun showErrorDialog(
        context: Context,
        title:String?, message: String?, dismissListner: () -> Unit
    ) {
        val errorDialog =
            BottomSheetDialog(context, R.style.BottomSheetDialog)
        val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingError.root)
        errorDialog.setCancelable(false)
        bindingError.titleText.text = title ?: context.getString(R.string.no_internet)
        bindingError.messageText.text = message ?: context.getString(R.string.slow_internet)
        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            dismissListner.invoke()
            errorDialog.dismiss()
         }

    }
}