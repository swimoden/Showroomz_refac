package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentForgetPasswordBinding
import com.kuwait.showroomz.databinding.SuccessBottomSheetBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.ResetPasswordVM


class ForgetPasswordFragment : Fragment() {


    private lateinit var binding: FragmentForgetPasswordBinding
    private lateinit var viewModel: ResetPasswordVM
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_forget_password, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        viewModel = ViewModelProviders.of(this).get(ResetPasswordVM::class.java)
        binding.viewModel = viewModel
        return binding.root

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Forget_password_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        observeData()
        onclickListener()
    }

    private fun onclickListener() {
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.validEmail.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid) {
                binding.emailTextInputLayout.error = getString(R.string.invalid_email)
            } else binding.emailTextInputLayout.error = null


        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if (loading) {
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            } else activity?.let { DesignUtils.instance.enableUserInteraction(it) }
            binding.progressCircular.isVisible = loading


        })
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty() && error == "reset_password_error") {
                binding.emailTextInputLayout.error = getString(R.string.invalid_email)

            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it){
                var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                var binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.success_bottom_sheet,
                    null,
                    false
                )
                dialog.setContentView(binding.root)
                binding.titleText.text=getString(R.string.reset_code_sent)
                binding.messageText.text=getString(R.string.please_check_your_email_to_reset_nyour_password)
                dialog.show()
                binding.exitBtn.setOnClickListener {
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).navigateUp()
                }
            }

        })

    }


}