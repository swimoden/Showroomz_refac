package com.kuwait.showroomz.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.CallbackAppraisalBottomSheetBinding
import com.kuwait.showroomz.databinding.CallbackSuccessBottomSheetBinding
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentApparaisalRequestItemDetailsBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.data.AppraisalRequest
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.CallbackAppraisalClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.adapters.ActionsAdapter
import com.kuwait.showroomz.view.adapters.AppraisalRequestsAdapter
import com.kuwait.showroomz.view.adapters.CarDetailsImageAdapter
import com.kuwait.showroomz.viewModel.AppraisalItemDetailsVM
import io.realm.RealmList


class AppraisalCarItemDetails : Fragment() {
    lateinit var binding: FragmentApparaisalRequestItemDetailsBinding
    private lateinit var simplifier: CallbackAppraisalClientVehicleSimplifier
    private lateinit var viewModel: AppraisalItemDetailsVM
   // private var fromNotif = false
    private val actionsAdapter: ActionsAdapter by lazy {
        ActionsAdapter(arrayListOf(), 0,0)
    }
    private val imageAdapter: CarDetailsImageAdapter by lazy {
        CarDetailsImageAdapter(RealmList())
    }
    private val requestsAdapter: AppraisalRequestsAdapter by lazy {
        AppraisalRequestsAdapter(arrayListOf(), viewModel)
    }

    private val dialogCallback: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }

    private val successCallbackDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Appraisal_details_screen")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_apparaisal_request_item_details,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AppraisalItemDetailsVM::class.java)
        viewModel.getUser()
        binding.imageRecycler.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.appraisalListRecycler.apply {
            adapter = requestsAdapter
            layoutManager = LinearLayoutManager(context)

        }

        binding.actionRecycler.apply {
            adapter = actionsAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
        arguments?.let {
            val id = AppraisalCarItemDetailsArgs.fromBundle(it).appraisalId
            id?.let{
                viewModel.fetchAppraisal(it) { appr ->
                    appr?.let {
                        simplifier = CallbackAppraisalClientVehicleSimplifier(it)
                        //fromNotif = true
                       updateUI()
                    }
                }
            } ?: run {
                simplifier = AppraisalCarItemDetailsArgs.fromBundle(it).callback!!
               updateUI()
            }
            // binding.txtToBuy.isVisible = simplifier.model != null
        }





        onClickListener()
        observ()
    }
    private fun updateUI(){
        binding.callback = simplifier
        binding.shapeableImageView.isVisible = simplifier.model != null
        binding.callbackBtn.isVisible = false
        binding.backButton.setImageResource(if (simplifier.model != null) R.mipmap.back_btn_white else R.mipmap.back_black)
        simplifier.requests?.let { requestsAdapter.refreshActions(it) }
        simplifier.clientVehicle?.gallery?.let { imageAdapter.refreshImages(it,false) }
        binding.actionsContainer.isVisible = false
        binding.callbackBtn.isVisible = true

        /*val listActions = simplifier.modelData?.actions?.filter { it.identifier == "finance" || it.identifier == "book_now" || it.identifier == "buy_now" || it.identifier == "callback" }
        binding.actionsContainer.isVisible = false
        binding.callbackBtn.isVisible = true

        listActions?.let {listActions ->
            if (listActions.size > 1){
                binding.actionsContainer.isVisible = true
                binding.callbackBtn.isVisible = false
                listActions.let {
                    simplifier.modelData?.brand?.category?.let { it1 ->
                        actionsAdapter.refreshActions(
                            it,
                            Color.parseColor(it1.colorString)
                        )
                        binding.actionsContainer.setBackgroundColor(it1.setBgColor())
                    }
                }
            }else{
                binding.actionsContainer.isVisible = false
                binding.callbackBtn.isVisible = true

            }
        }*/
    }
    private fun observ(){
         viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.callbackError.observe(viewLifecycleOwner, Observer {
            if (it){
                showErrorDialog("callback error")
                viewModel.callbackError.value=false
            }

        })

        viewModel.successCallback.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.successCallback.value = false
                showSuccessCallbackBottomSheet()
            }
        })

        viewModel.callbackLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                dialogCallback.dismiss()
                binding.progressCircular.isVisible = true
                activity?.let { it1 -> DesignUtils.instance.disableUserInteraction(it1) }
            } else {
                binding.progressCircular.isVisible = false
                activity?.let { it1 -> DesignUtils.instance.enableUserInteraction(it1) }
            }
        })

        viewModel.phoneError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
                viewModel.phoneError.value=false
            }
        })

        viewModel.nameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
                viewModel.nameError.value=false
            }
        })

        viewModel.selectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_request))
                viewModel.selectionError.value=false
            }
        })
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
        bindingError.messageText.text =
            message


        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }
    private fun showSuccessCallbackBottomSheet() {
        val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_success_bottom_sheet,
            null,
            false
        )
        successCallbackDialog.setContentView(bindingSuccess.root)
        successCallbackDialog.setCanceledOnTouchOutside(false)
        bindingSuccess.titleText.text = getString(R.string.done_successfully)
        bindingSuccess.messageText.text = getString(R.string.representative_contact)


        successCallbackDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            dialogCallback.dismiss()
            successCallbackDialog.dismiss()

        }
    }
    private fun onClickListener() {
        binding.callbackBtn.setOnClickListener {
            if (viewModel.request?.price != null)
            showCallbackDialog()
        }
        binding.modelDetail.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(AppraisalCarItemDetailsDirections.ShowModelDetail(simplifier.model,
                    null, null))
        }
        binding.backButton.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment){
                val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            }else{
                Navigation.findNavController(it).navigateUp()
            }
        }

        requestsAdapter.setOnItemCLickListener(object :AppraisalRequestsAdapter.OnItemClickListener {
            override fun onItemClick(request: AppraisalRequest?) {
                simplifier.requests?.forEach{
                    it.isSelected = it.id == request?.id
                }

                simplifier.requests?.let { requestsAdapter.refreshActions(it) }
                viewModel.request = request
            }
        })

        actionsAdapter.setOnItemCLickListener(object : ActionsAdapter.OnItemClickListener {
            override fun onItemClick(action: Action?) {
                if (action?.identifier.equals("callback")) {
                    if (viewModel.request?.price != null)
                        showCallbackDialog()
                }else
                if (action?.identifier.equals("finance")) {
                    if (viewModel.request?.price != null) {

                        var trim = Trim()
                        trim.price = viewModel.request!!.price!!.toInt()
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowFinance(
                                TrimSimplifier(trim), null, it
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }
                    } else
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowFinance(
                                null, null,
                                it
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }

                }
                if (action?.identifier.equals("book_now")) {
                    val trim = Trim()
                    if (viewModel.request?.price != null) {

                        trim.price = viewModel.request!!.price!!.toInt()
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowBook(
                                it, trim, null
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }
                    } else {
                        trim.price = simplifier.price!!.toInt()
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowBook(
                                it, trim, null
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }
                    }
                }
                if (action?.identifier.equals("buy_now")) {
                    var trim = Trim()
                    if (viewModel.request?.price != null) {

                        trim.price = viewModel.request!!.price!!.toInt()
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowBuyNow(
                                it, trim
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }
                    } else {
                        simplifier.model?.priceFloat?.toInt()?.let {
                            trim.price = it
                        }
                        simplifier.model?.let {
                            AppraisalCarItemDetailsDirections.actionShowBuyNow(
                                it, trim
                            )
                        }?.let {
                            Navigation.findNavController(requireView())
                                .navigate(it)
                        }
                    }
                }
            }
        })

    }
    private fun showCallbackDialog() {


        val binding: CallbackAppraisalBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_appraisal_bottom_sheet,
            null,
            false
        )

        binding.viewModel = viewModel
        binding.fullNameEdit.setText( viewModel.fullNameField.get().toString())
        binding.phoneEdit.setText(viewModel.phoneNumber.get().toString())

        dialogCallback.setContentView(binding.root)
        dialogCallback.setCanceledOnTouchOutside(false)
        dialogCallback.show()
        binding.exitBtn.setOnClickListener {
            dialogCallback.dismiss()
        }

    }

}