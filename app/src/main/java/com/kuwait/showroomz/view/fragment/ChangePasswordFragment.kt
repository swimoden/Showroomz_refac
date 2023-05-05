package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputType.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentChangePaswordBinding
import com.kuwait.showroomz.databinding.SuccessBottomSheetBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.hideKeyboard
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.ChangePasswordVM
import kotlinx.android.synthetic.main.fragment_change_pasword.*


class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePaswordBinding
    private lateinit var viewModel: ChangePasswordVM
    private var email:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_change_pasword, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume() {
        super.onResume()
        LogProgressRepository.logProgress("Update_password_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ChangePasswordVM::class.java)
        binding.viewModel= viewModel
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        observeData()
        onclickListener()
        view.setOnClickListener {
            it.hideKeyboard()
        }
        arguments?.let {
            ChangePasswordFragmentArgs.fromBundle(it).email?.let {
                email = it
                viewModel.email.value = email
                binding.oldPasswordHide.isVisible = false
                binding.oldPasswordTextInputLayout.hint = if (isEnglish) "enter otp" else "ادخل الكود"
                binding.oldTextInputEditText.inputType = TYPE_CLASS_TEXT
            }
        }
    }

    private fun onclickListener() {
        binding.oldTextInputEditText.doOnTextChanged { text, start, count, after ->
            if (email == null)
            viewModel.validOldPassword.value = binding.oldTextInputEditText.text.toString().length > 7
            else
                viewModel.validOldPassword.value = binding.oldTextInputEditText.text.toString().isNotEmpty()
        }
        binding.newPasswordTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validNewPassword.value = binding.newPasswordTextInputEditText.text.toString().length > 7
        }
        binding.confirmPasswordTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validConfirmPassword.value = binding.confirmPasswordTextInputEditText.text.toString().length > 7
        }

        binding.backBnt.setOnClickListener {
            Navigation.findNavController(requireView()).navigateUp()
        }
        binding.oldPasswordHide.setOnClickListener {
            if (binding.oldTextInputEditText.inputType == TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT) {
                binding.oldTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or TYPE_CLASS_TEXT
                oldPasswordHide.text = getText(R.string.hide)
            } else {
                binding.oldTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT
                oldPasswordHide.text = getText(R.string.show)
            }
        }
        binding.newPasswordHide.setOnClickListener {
            if (binding.newPasswordTextInputEditText.inputType == TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT) {
                binding.newPasswordTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or TYPE_CLASS_TEXT
                newPasswordHide.text = getText(R.string.hide)
            } else {
                binding.newPasswordTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT
                newPasswordHide.text = getText(R.string.show)
            }
        }
        binding.confirmPasswordHide.setOnClickListener {
            if (binding.confirmPasswordTextInputEditText.inputType == TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT) {
                binding.confirmPasswordTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or TYPE_CLASS_TEXT
                confirmPasswordHide.text = getText(R.string.hide)
            } else {
                binding.confirmPasswordTextInputEditText.inputType =
                    TYPE_TEXT_VARIATION_PASSWORD or TYPE_CLASS_TEXT
                confirmPasswordHide.text = getText(R.string.show)
            }

        }
    }
    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.validOldPassword.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid && email==null) {
                binding.oldPasswordTextInputLayout.error = if (binding.oldTextInputEditText.text.toString()
                        .isEmpty()
                )getString(R.string.empty_password) else {

                    getString(R.string.invalid_password)

                }
            } else binding.oldPasswordTextInputLayout.error = null


        })
        viewModel.validNewPassword.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid) {
                binding.newPasswordTextInputLayout.error = getString(R.string.invalid_password)
            } else binding.newPasswordTextInputLayout.error = null


        })
        viewModel.validConfirmPassword.observe(viewLifecycleOwner, Observer { valid ->

            //if (!valid) {
                if (binding.confirmPasswordTextInputEditText.text.toString() != binding.newPasswordTextInputEditText.text.toString()){
                    binding.confirmPasswordTextInputLayout.error = getString(R.string.confirmation_pass)

                } else{ binding.confirmPasswordTextInputLayout.error = null}
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if (loading) {
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            }else  activity?.let { DesignUtils.instance.enableUserInteraction(it) }
            binding.progressCircular.isVisible = loading
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error){
                binding.oldPasswordTextInputLayout.error =  if (email == null)
                    getString(R.string.invalid_password)
                else {if (isEnglish) "wrong otp" else "رمز خاطئ"}
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it){
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                val binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.success_bottom_sheet,
                    null,
                    false
                )
                dialog.setContentView(binding.root)
                binding.titleText.text=getString(R.string.done_successfully)
                binding.messageText.text=getString(R.string.your_password_is_changed)
                dialog.show()
                binding.exitBtn.setOnClickListener {
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).navigateUp()
                }
            }

        })
    }


}