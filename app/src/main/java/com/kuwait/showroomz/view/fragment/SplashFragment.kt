package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.EmailUpdateDialogBinding
import com.kuwait.showroomz.extras.MyApplication.Key.LANG
import com.kuwait.showroomz.extras.MyApplication.Key.LANGUAGE_SELECTED
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.USER_ID
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.SplashVM
import kotlinx.android.synthetic.main.email_update_dialog.*
import kotlinx.android.synthetic.main.fragment_splash.*


class SplashFragment : Fragment() {
    private lateinit var viewModel: SplashVM
    private var animationDone = false
    lateinit var shared:Shared
    val util = Utils()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Splash_screen")
    }
    val MULTIPLE_PERMISSIONS = 10 // code you want.


    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shared = activity?.let { Shared(it) }!!
        viewModel = ViewModelProviders.of(this).get(SplashVM::class.java)
        checkPermissions()
        val animSet2 = AnimationSet(true)
        val animation = AnimationUtils.loadAnimation(
            context,
            R.anim.zoom_in
        )
        val animation2 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_up
        )
        animSet2.addAnimation(animation)
        animSet2.addAnimation(animation2)
        carImg.startAnimation(animSet2)
        animSet2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val animation1 =  AnimationUtils.loadAnimation(
                    context,
                    R.anim.fade_in
                )
                nameImage.isVisible = true
                nameImage.startAnimation(animation1)
                animation1.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val animation3 =  AnimationUtils.loadAnimation(
                            context,
                            R.anim.shake
                        )
                        animation3.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}
                            override fun onAnimationEnd(animation: Animation) {
                                animationDone = true

                                showNextScreen()
                               // if (loadDone) handler.post(r)
                            }

                            override fun onAnimationRepeat(animation: Animation) {}
                        })
                        nameImage.startAnimation(animation3)
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

    }

    private fun showNextScreen(){
        if (!shared.bool(LANGUAGE_SELECTED)){
            languages_container.isVisible = true
            clicks()
        }else{
//            shared.string(USER_ID)?.let {
//                val local = LocalRepo()
//                local.getOne<User>(it)?.let{
//                    if (it.email?.startsWith("+") == true){
//                       showUpdateEmailDialog(requireActivity()) {
//                           Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
//                       }
//                    }else{
//                        Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
//                    }
//                }?: run{
//                    Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
//                }
//            }?: run {
                Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
           // }
        }
    }

    private fun clicks(){
        arabicBtn.setOnClickListener {
            shared.setBool(LANGUAGE_SELECTED,true)
            isEnglish=false
            shared.setString(LANG,"AR")
            (activity as MainActivity).setLocale("ar")
            Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
        }
        english_btn.setOnClickListener {
            shared.setBool(LANGUAGE_SELECTED,true)
            isEnglish=true
            shared.setString(LANG,"EN")
            (activity as MainActivity).setLocale("en")

            Navigation.findNavController(container).navigate(SplashFragmentDirections.actionShowDashbord())
        }
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }
    private val dialogEmail: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    fun showUpdateEmailDialog(ctx: Context, action: () -> Unit) {
        val binding: EmailUpdateDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(ctx),
            R.layout.email_update_dialog,
            null,
            false
        )

        dialogEmail.setContentView(binding.root)
        dialogEmail.setCancelable(false)
        dialogEmail.ok_btn.setOnClickListener {
            if (Utils.instance.isValidEmail(dialogEmail.email_txt.text)) {
                viewModel.updateEmail(dialogEmail.email_txt.text.toString()) {
                    if (it) {
                        dialogEmail.dismiss()
                        action.invoke()
                    } else {
                        dialogEmail.email_txt.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.shake
                            )
                        )
                    }
                }
            } else {
                dialogEmail.email_txt.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            }
        }

        dialogEmail.exit_btn.setOnClickListener {
            action.invoke()
            dialogEmail.dismiss()
        }
        dialogEmail.show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                } else {
                    // no permissions granted.
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

}


