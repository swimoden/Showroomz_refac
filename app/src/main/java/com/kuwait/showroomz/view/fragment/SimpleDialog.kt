package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.convertToLocal
import com.kuwait.showroomz.viewModel.SMSVerificationVM
import kotlinx.android.synthetic.main.sms_verification_dialog.*

class SimpleDialog(val action: (() -> Unit)?,val closeaction: (() -> Unit)?) : DialogFragment() {

    companion object {

        const val TAG = "SimpleDialog"

        private const val KEY_PHONE = "KEY_PHONE"
        private const val KEY_SUBTITLE = "KEY_SUBTITLE"

        /*fun newInstance(title: String, subTitle: String): SimpleDialog {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_SUBTITLE, subTitle)
            val fragment = SimpleDialog()
            fragment.arguments = args
            return fragment
        }*/

        fun emptyInstance(phone:String, action:()-> Unit, closeaction:()-> Unit): SimpleDialog {
            val args = Bundle()
            args.putString(KEY_PHONE, phone)
            val fragment = SimpleDialog(action, closeaction)
            fragment.arguments = args
            return fragment

        }

    }
    lateinit var viewModel: SMSVerificationVM
    var phone:String? = null
    val timer = object: CountDownTimer(300000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            resend_code.text = getFormattedTime(millisUntilFinished / 1000)
            resend_code.isEnabled = false
        }

        override fun onFinish() {
            resend_code.isEnabled = true
            resend_code.text = getString(R.string.resend_code)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sms_verification_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =  ViewModelProviders.of(this).get(SMSVerificationVM::class.java)
        setupView(view)
        setupClickListeners(view)
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

    private fun setupView(view: View) {
        phone = arguments?.getString(KEY_PHONE)
        otp_view.itemCount = 6
        otp_view.setAnimationEnable(true)
        otp_view.setOtpCompletionListener{
            phone?.let { it1 -> viewModel.verifyCode(it1,it) }?: run{  DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.empty_phone),getString(R.string.invalid_phone)){} }
            //action.invoke()
        }
    }

    private fun setupClickListeners(view: View) {
        get_code.setOnClickListener {
            phone?.let {
                    it1 -> viewModel.sendCode(it1)
            } ?: run {
                DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.empty_phone),getString(R.string.invalid_phone)){}
            }

        }
        verify_code.setOnClickListener {
            val s = otp_view.editableText.toString()
            if (s.length < 6){
                otp_view.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
            }else{
                phone?.let { it1 -> viewModel.verifyCode(it1, s) }
            }

           //
        }
        resend_code.setOnClickListener {
            timer.cancel()
            phone?.let {
                    it1 -> viewModel.sendCode(it1)
            } ?: run {
                DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.empty_phone),getString(R.string.invalid_phone)){}
            }

        }
        exit_btn.setOnClickListener {
            timer.cancel()
            closeaction?.invoke()
            dismiss()
        }
    }

    private fun setupObservers(){
        viewModel.noConnectionError.observe(viewLifecycleOwner,  Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){}
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), getString(R.string.error),viewModel.errorMsg.value){}
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
                progress.isVisible = it
        })

        viewModel.successSentCode.observe(viewLifecycleOwner, Observer {
            if (it){
                verification_view.isVisible = true
                title_verification_text.text = viewModel.successMsg.value
                timer()
            }
        })

        viewModel.successVerifyCode.observe(viewLifecycleOwner, Observer {
            if (it){
                dismiss()
                action?.invoke()
                timer.cancel()
            }
        })
    }
    fun timer(){
        timer.start()
    }

    private fun getFormattedTime(totalSeconds: Long): String {

        val min = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60
        return "$min:$seconds".convertToLocal()
        //main_time.text = getString(R.string.time, hour, min)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeaction?.invoke()
        timer.cancel()
    }

}