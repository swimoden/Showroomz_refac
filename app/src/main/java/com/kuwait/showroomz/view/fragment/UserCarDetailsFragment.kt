package com.kuwait.showroomz.view.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ApparasialCarContactBottomSheetBinding
import com.kuwait.showroomz.databinding.ApparasialCarSuccessRequestBottomSheetBinding
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentUserCarDetailsBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.data.UserCarDetails
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.CallbackAppraisalClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.ClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.adapters.ActionsAdapter
import com.kuwait.showroomz.view.adapters.AddonsAmountAdapter
import com.kuwait.showroomz.view.adapters.CarDetailsImageAdapter
import com.kuwait.showroomz.view.adapters.USerCarDetailsAdapter
import com.kuwait.showroomz.viewModel.CarDetailsVM
import io.realm.RealmList


class UserCarDetailsFragment : Fragment() {
    lateinit var binding: FragmentUserCarDetailsBinding
    private lateinit var viewModel: CarDetailsVM
    private lateinit var simplifier: ClientVehicleSimplifier
    private var detailsArray:ArrayList<UserCarDetails> = arrayListOf()
    lateinit var model: Model
    private val imageAdapter: CarDetailsImageAdapter by lazy {
        CarDetailsImageAdapter(RealmList())
    }
    private val userCarDetailsAdapter: USerCarDetailsAdapter by lazy {
        USerCarDetailsAdapter(arrayListOf(), context)

    }
    private val contactDialogRequest: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }

    override fun onResume() {
        super.onResume()
      if(::model.isInitialized){
            LogProgressRepository.logProgress("Appraise_existing_client_vehicle",category = ModelSimplifier(model).brand?.cat?.id?:""
                ,dealerData =ModelSimplifier(model).brand?.id?:"",modelData =  ModelSimplifier(model).id)
        } else {
            LogProgressRepository.logProgress("Appraise_existing_client_vehicle")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_car_details,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CarDetailsVM::class.java)
        arguments.let {
            binding.galleryRecycler.apply {
                adapter = imageAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            binding.detailsRecycler.apply {
                adapter = userCarDetailsAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            it?.let { it1 ->
               val usercar = UserCarDetailsFragmentArgs.fromBundle(it1).usercar
                viewModel.clientVehicle = usercar
                simplifier = ClientVehicleSimplifier(usercar)
                binding.model = simplifier
                simplifier.clientVehicle.imageGallery.let { imageAdapter.refreshImages(it, false) }
                val model = UserCarDetailsFragmentArgs.fromBundle(it1).model
                viewModel.modelToBuy = model
                if (!simplifier.engine.isNullOrEmpty()){
                    detailsArray.add(UserCarDetails(key = getString(R.string.engine), value = simplifier.engine))
                }
                if (!simplifier.cylinders.isNullOrEmpty()){
                    detailsArray.add(UserCarDetails(key = getString(R.string.cylinder), value = simplifier.cylinders))
                }
                if (!simplifier.mileage.isNullOrEmpty()){
                    detailsArray.add(UserCarDetails(key = getString(R.string.mileage), value = simplifier.mileage))
                }
                if (!simplifier.condition.isNullOrEmpty()){
                    detailsArray.add(UserCarDetails(key = getString(R.string.condition), value = simplifier.condition))
                }
                simplifier.description?.let { desc ->
                    val x = desc.split(",").toTypedArray()
                    x.forEach {
                        if (it.contains(":")){
                            val y = it.split(":").toTypedArray()
                            detailsArray.add(UserCarDetails(key = y.first(), value = y[1]))
                        }else{
                           if (it != "null")
                            detailsArray.add(UserCarDetails(key = it, value = ""))
                        }
                    }
                }
                userCarDetailsAdapter.refresh(detailsArray)
            } ?: run {
                Navigation.findNavController(view).navigateUp()
            }


        }
        binding.submitBtn.setOnClickListener{
            showRequestBottomSheet()
        }
        binding.backButton.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        viewModel.successCallback.observe(viewLifecycleOwner, Observer {
            if (it) showSuccessDialog()
        })
        viewModel.phoneError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
            }
        })
        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_email))
            }
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.progressCircular.isVisible = true
                DesignUtils.instance.disableUserInteraction(requireActivity())
                contactDialogRequest.dismiss()
            } else {
                binding.progressCircular.isVisible = false
                DesignUtils.instance.enableUserInteraction(requireActivity())
            }
        })
        viewModel.noConnectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
    }
    private fun showRequestBottomSheet() {

        val contactBottomSheetBinding: ApparasialCarContactBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.apparasial_car_contact_bottom_sheet,
                null,
                false
            )
        contactBottomSheetBinding.viewModel = viewModel
        contactDialogRequest.setContentView(contactBottomSheetBinding.root)
        contactDialogRequest.setCancelable(false)
        contactDialogRequest.show()
        contactBottomSheetBinding.exitBtn.setOnClickListener {
            contactDialogRequest.dismiss()
        }
        contactBottomSheetBinding.okBtn.setOnClickListener {
            viewModel.appraisalRequest()
            DesignUtils.instance.hideKeyboard(requireActivity())
        }
    }
    private val actionsAdapter: ActionsAdapter by lazy {
        ActionsAdapter(arrayListOf(), 1, 0)
    }
    private fun showSuccessDialog() {
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var bottomSheetBinding: ApparasialCarSuccessRequestBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.apparasial_car_success_request_bottom_sheet,
                null,
                false
            )

        dialog.setContentView(bottomSheetBinding.root)
        dialog.show()
        dialog.setCancelable(false)
        bottomSheetBinding.actionRecycler.apply {
            adapter = actionsAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
        if (::model.isInitialized) {
            val simplifier = ModelSimplifier(model)
            val listActions = simplifier.actions.filter { it.identifier.contains("with_appraisal") }
            if (simplifier.brand?.color != null) {
                simplifier.brand?.color?.let { actionsAdapter.refreshActions(listActions, Color.parseColor(it.toString())) }
            } else {
                actionsAdapter.refreshActions(listActions, resources.getColor(R.color.colorDialog))
            }
        }


        actionsAdapter.setOnItemCLickListener(object : ActionsAdapter.OnItemClickListener {
            override fun onItemClick(action: Action?) {
                if (action?.identifier.equals("finance_with_appraisal")) {
                    val  trim = Trim()
                    model.priceFloat?.toInt()?.let {
                        trim.price=it
                    }

                    dialog.dismiss()
                    Navigation.findNavController(requireView()).popBackStack(R.id.yourCarDetailsFragment,true)
                    Navigation.findNavController(requireView()).navigate(YourCarDetailsFragmentDirections.actionShowFinance(
                        TrimSimplifier(trim),null,model))
                    return
                }
                if (action?.identifier.equals("book_now_with_appraisal")) {
                    val  trim = Trim()
                    model.priceFloat?.toInt()?.let {
                        trim.price=it
                    }
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).popBackStack(R.id.yourCarDetailsFragment,true)
                    Navigation.findNavController(requireView()).navigate(YourCarDetailsFragmentDirections.actionShowBook(model,trim,null))
                    return
                }

                if (action?.identifier.equals("buy_now_with_appraisal")) {
                    val  trim = Trim()
                    model.priceFloat?.toInt()?.let {
                        trim.price=it
                    }
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).popBackStack(R.id.yourCarDetailsFragment,true)
                    Navigation.findNavController(requireView()).navigate(YourCarDetailsFragmentDirections.actionShowBuyNow(model,trim))
                    return
                }else{
                    showErrorDialog("this feature is not available in current version please update the app")
                }
            }
        })
        bottomSheetBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
            Navigation.findNavController(requireView()).navigateUp()
        }


    }
    private fun showErrorDialog(message: String) {
        val errorDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingError.root)
        errorDialog.setCancelable(false)
        bindingError.titleText.text = getString(R.string.error)
        bindingError.messageText.text = message
        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            errorDialog.dismiss()
        }

    }

}