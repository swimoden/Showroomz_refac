package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentRegisterBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.viewModel.MainVM
import com.kuwait.showroomz.viewModel.RegisterVM
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment() {
    lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterVM
    //private lateinit var dataVm: MainVM
    var dialog = SimpleDialog(null, null)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Register_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegisterVM::class.java)
        //dataVm = ViewModelProvider(this).get(MainVM::class.java)

        binding.viewModel = viewModel
        observeData()
        onclickListener()
        container.setOnClickListener {
            it.hideKeyboard()
        }
    }

    private fun onclickListener() {
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.bottomContainer.setOnClickListener {
            arguments?.let { it1 ->
                RegisterFragmentArgs.fromBundle(
                    it1
                ).from
            }?.let { it2 -> RegisterFragmentDirections.showLogin(it2) }?.let { it3 ->
                Navigation.findNavController(it).navigate(
                    it3
                )
            }
        }

        binding.nameTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validName.value = binding.nameTextInputEditText.text?.trim()?.isNotEmpty() == true
                    && binding.nameTextInputEditText.text?.trim().toString().validateLetters()
                    && binding.nameTextInputEditText.text?.length!! < 30
           // binding.nameTextInputLayout.error = null
        }
        binding.emailTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validEmail.value = Utils.instance.isValidEmail(binding.emailTextInputEditText.text)
        }

        binding.passwordTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validPassword.value = binding.passwordTextInputEditText.text.toString().length > 7
            // binding.nameTextInputLayout.error = null
        }

        binding.phoneTextInputEditText.doOnTextChanged{text, start, count, after ->
            viewModel.validPhone.value =  binding.phoneTextInputEditText.text.toString().isValidPhoneNumber()
        }
    }

    private fun observeData() {
        viewModel.validEmail.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid) {
                binding.emailTextInputLayout.error = getString(R.string.invalid_email)
            } else binding.emailTextInputLayout.error = null


        })
        viewModel.validName.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid) {
                binding.nameTextInputLayout.error =
                    if (binding.nameTextInputEditText.text?.trim()?.length == 0) getString(R.string.empty_name) else getString(R.string.invalid_name)

            } else binding.nameTextInputLayout.error = null


        })
        viewModel.validPhone.observe(viewLifecycleOwner, Observer { valid ->
            if (valid != null && !valid) {
                binding.phoneTextInputLayout.error = if (binding.phoneTextInputEditText.text.toString()
                        .isEmpty()
                ) getString(R.string.empty_phone) else getString(R.string.invalid_phone)
            } else binding.phoneTextInputLayout.error = null
        })

        viewModel.validPassword.observe(viewLifecycleOwner, Observer { valid ->

            if (!valid) {
                binding.passwordTextInputLayout.error = getString(R.string.invalid_password)
            } else binding.passwordTextInputLayout.error = null


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
            if (error.isNotEmpty() && error == "500") {
                DesignUtils.instance.showErrorDialog(requireContext(), getString(R.string.server_error),
                    getString(R.string.apologize)){
                }
            } else Toast.makeText(context, R.string.internet_connextion_problem, Toast.LENGTH_LONG)
                .show()
        })
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.existPhone.observe(viewLifecycleOwner, Observer { error ->
            run {
                if (error) {
                    viewModel.verifyPhone.value = false
                    binding.phoneTextInputLayout.error = getString(R.string.phone_exsit)
                }
            }
        })
            viewModel.existEmail.observe(viewLifecycleOwner, Observer { error ->
            run {
                if (error) {
                    binding.emailTextInputLayout.error = getString(R.string.email_exist_already)
                }
            }
            })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                LogProgressRepository.refreshMainUserData()
                if (arguments?.let { it1 -> RegisterFragmentArgs.fromBundle(it1).from } == R.id.profileFragment) {
                    Navigation.findNavController(requireView())
                        .navigate(RegisterFragmentDirections.showProfile())
                }
                if (arguments?.let { it1 -> RegisterFragmentArgs.fromBundle(it1).from }==R.id.testDriveAddressFragment){
                    Navigation.findNavController(requireView()).navigate(RegisterFragmentDirections.showTestDriveAddress())
                }
                if (arguments?.let { it1 -> RegisterFragmentArgs.fromBundle(it1).from }==R.id.applyForFinance){
                    Navigation.findNavController(requireView()).navigate(RegisterFragmentDirections.showCallbackBank())
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from }==R.id.buyNowFragment){
                    val model= CacheObjects.model
                    val trim= CacheObjects.trim
                    CacheObjects.trim=null
                    CacheObjects.model=null
                    trim?.let { it1 ->
                        model?.let { it2 ->
                            RegisterFragmentDirections.actionShowBuyNow(
                                it2,
                                it1
                            )
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from }==R.id.bookNowFragment){
                    val model= CacheObjects.model
                    val trim= CacheObjects.trim
                    val program= CacheObjects.program
                    CacheObjects.trim=null
                    CacheObjects.model=null
                    CacheObjects.program=null
                    model?.let { it1 ->
                        trim?.let { it2 ->
                            RegisterFragmentDirections.actionShowBook(
                                it1, it2,program)
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from }==R.id.bookNowRentFragment){
                    val model= CacheObjects.model
                    val trim= CacheObjects.trim
                    val program = CacheObjects.program
                    CacheObjects.trim=null
                    CacheObjects.model=null
                    CacheObjects.program = null
                    model?.let { it1 ->
                        trim?.let { it2 ->
                            RegisterFragmentDirections.actionShowBookNowForRent(
                                it1, it2, program?.id
                            )
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from }==R.id.yourCarDetailsFragment){
                    val model= CacheObjects.model

                    CacheObjects.trim=null
                    CacheObjects.model=null
                    model?.let { it1 ->

                        RegisterFragmentDirections.actionShowAppraisalCar(
                            it1
                        )

                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
            }
        })

       /* viewModel.verifyPhone.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.phone.value?.let { it1 ->
                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.register()
                        dialog.dismiss()
                    },{
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }

            }
        })*/
    }


}