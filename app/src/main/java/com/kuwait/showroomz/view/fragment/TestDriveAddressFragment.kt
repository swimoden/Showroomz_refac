package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.USER_ID
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.data.UserAddress
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.UserAddressAdapter
import com.kuwait.showroomz.viewModel.TestDriveAddressVM
import kotlinx.android.synthetic.main.email_update_dialog.*


class TestDriveAddressFragment : Fragment() {
    private  val userInfoDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    }
    private lateinit var binding: FragmentTestDriveAddressBinding
    private lateinit var viewModel: TestDriveAddressVM
    val prefs = Shared()
    private val addressAdapter: UserAddressAdapter by lazy {
        UserAddressAdapter(listOf())
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Address_screen",category =viewModel.modelSimplifier.brand?.cat?.id?:"",dealerData =  viewModel.modelSimplifier.brand?.id?:"",modelData = viewModel.modelSimplifier.id)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_test_drive_address,
                container,
                false
            )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TestDriveAddressVM::class.java)
        viewModel.addressType.set("Home")
        viewModel.type.set("10")
        viewModel.getUserAddress()
//        viewModel.getLocation(location)
        binding.viewModel = viewModel
        binding.addressRecycler.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            adapter = addressAdapter
        }
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            viewModel.modelSimplifier.brand?.category?.setBgColor())

        onClickListener()
        observeData()

    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>(
            "LOCATION"
        )?.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { location ->
                if (location!=null){
                    viewModel.getLocation(location)
                    binding.deliverToMyCurrentLocationContainer.visibility = View.VISIBLE
                    binding.addressTypeContainer.visibility = View.VISIBLE
                    binding.inputsContainer.visibility = View.VISIBLE
                }

            })
        viewModel.loadingRequest.observe(viewLifecycleOwner, Observer {
            if (it){
                userInfoDialog.dismiss()
                binding.progress.isVisible=true
                DesignUtils.instance.disableUserInteraction(requireActivity())
            }else {
                binding.progress.isVisible=false
                DesignUtils.instance.enableUserInteraction(requireActivity())
            }
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer {

            binding.progressContainer.isVisible = it
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

        })
        viewModel.addresses.observe(viewLifecycleOwner, Observer {
            addressAdapter.refresh(it)
            binding.addressRecyclerContainer.isVisible = it.isNotEmpty()
            if (it.isNotEmpty()&&viewModel.isNullAddress()) {
                binding.deliverToMyCurrentLocationContainer.visibility = View.GONE
                binding.addressTypeContainer.visibility = View.GONE
                binding.inputsContainer.visibility = View.GONE
            }

        })
        viewModel.successRequestDrive.observe(viewLifecycleOwner, Observer {
            if (it) {
                val successDialog =
                    BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.callback_success_bottom_sheet,
                    null,
                    false
                )
                successDialog.setContentView(bindingSuccess.root)
                bindingSuccess.titleText.text = getString(R.string.done_successfully)
                bindingSuccess.messageText.text =
                    getString(R.string.test_drive_created_successfully)


                successDialog.show()

                bindingSuccess.exitBtn.setOnClickListener {
                    successDialog.dismiss()
                    userInfoDialog.dismiss()
                    Navigation.findNavController(requireView()).popBackStack(R.id.testDriveAddressFragment,true)
                    Navigation.findNavController(requireView())
                        .navigateUp()


                }
            }
        })

        viewModel.emailError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                showErrorBottomSheet(getString(R.string.invalid_email))
            }
        })
        viewModel.phoneBShError.observe(viewLifecycleOwner, androidx.lifecycle.Observer  {
            if (it) {
                showErrorBottomSheet(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(viewLifecycleOwner, androidx.lifecycle.Observer  {
            if (it) {
                showErrorBottomSheet(getString(R.string.empty_name))
            }
        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            showErrorBottomSheet( getString(R.string.test_drive_error))

        })

    }

    private fun showErrorBottomSheet(message: String) {
        val errorDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingSuccess: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingSuccess.root)
        errorDialog.setCanceledOnTouchOutside(false)
        bindingSuccess.titleText.text = getString(R.string.error)
        bindingSuccess.messageText.text =
            message


        errorDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }
    private fun showUserInformationBottomSheet() {
        //userInfoDialog
        val testDriveBinding: TestDriveAddressUserInfoBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.test_drive_address_user_info_bottom_sheet,
                null,
                false
            )

        testDriveBinding.viewModel = viewModel
        userInfoDialog.setContentView(testDriveBinding.root)
        userInfoDialog.setCanceledOnTouchOutside(false)
        userInfoDialog.show()
        testDriveBinding.exitBtn.setOnClickListener {
            userInfoDialog.dismiss()

        }
    }

    private fun onClickListener() {
        binding.addNewAddressTxt.setOnClickListener {
            binding.deliverToMyCurrentLocationContainer.visibility = View.VISIBLE
            binding.addressTypeContainer.visibility = View.VISIBLE
            binding.inputsContainer.visibility = View.VISIBLE
            viewModel.fullAddress.set("")
            viewModel.selectedAddress=null
            checkForAddressType("Home")
        }
        binding.submitBtn.setOnClickListener {
            if (viewModel.validFields()) {

//                prefs.string(USER_ID)?.let {
//                    val local = LocalRepo()
//                    local.getOne<User>(it)?.let {
//                        if (it.email?.startsWith("+") == true) {
//                            showUpdateEmailDialog(requireActivity()) {
//                                showUserInformationBottomSheet()
//                            }
//                        } else {
//                            showUserInformationBottomSheet()
//                        }
//                    } ?: run {
//                        showUserInformationBottomSheet()
//                    }
//                } ?: run {
                    showUserInformationBottomSheet()
               // }
            }
        }
        addressAdapter.setOnItemCLickListener(object : UserAddressAdapter.OnItemClickListener {
            override fun onItemClick(address: UserAddress?) {
                viewModel.putAddress(address)
                viewModel.selectedAddress=address
                checkForAddressType(address?.type)
                binding.deliverToMyCurrentLocationContainer.visibility = View.VISIBLE
                binding.addressTypeContainer.visibility = View.VISIBLE
            }

        })
        binding.findLocationContainer.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(TestDriveAddressFragmentDirections.actionShowTestDriveMapLocation(0))
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.homeContainer.setOnClickListener {
            binding.apartmentCheckbox.isChecked = false
            binding.checkboxHome.isChecked = true
            binding.officeCheckbox.isChecked = false
            viewModel.addressType.set("Home")
            viewModel.type.set("10")
            binding.houseNumberTextInputLayout.hint = getString(R.string.house_number)
        }
        binding.apartmentContainer.setOnClickListener {
            binding.checkboxHome.isChecked = false
            binding.apartmentCheckbox.isChecked = true
            binding.officeCheckbox.isChecked = false
            viewModel.addressType.set("Apartment")
            viewModel.type.set("20")
            binding.houseNumberTextInputLayout.hint = getString(R.string.apartment_number)
        }
        binding.officetContainer.setOnClickListener {
            binding.checkboxHome.isChecked = false
            binding.apartmentCheckbox.isChecked = false
            binding.officeCheckbox.isChecked = true
            viewModel.addressType.set("Office")
            viewModel.type.set("30")
            binding.houseNumberTextInputLayout.hint = getString(R.string.office_number)

        }
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

    private fun checkForAddressType(type: String?) {
        if (type == "Home") {
            binding.apartmentCheckbox.isChecked = false
            binding.checkboxHome.isChecked = true
            binding.officeCheckbox.isChecked = false
            viewModel.addressType.set("Home")
            viewModel.type.set("10")
            return
        }
        if (type == "Apartment") {
            binding.checkboxHome.isChecked = false
            binding.apartmentCheckbox.isChecked = true
            binding.officeCheckbox.isChecked = false
            viewModel.addressType.set("Apartment")
            viewModel.type.set("20")
            return
        }
        if (type == "Office") {
            binding.checkboxHome.isChecked = false
            binding.apartmentCheckbox.isChecked = false
            binding.officeCheckbox.isChecked = true
            viewModel.addressType.set("Office")
            viewModel.type.set("30")
            return
        }

    }


}