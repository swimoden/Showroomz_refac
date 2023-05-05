package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Location
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.LocationSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.TestDriveDateAdapter
import com.kuwait.showroomz.view.adapters.TestDriveLocationAdapter
import com.kuwait.showroomz.view.adapters.TestDriveTimeAdapter
import com.kuwait.showroomz.viewModel.TestDriveDateReservationVM
import kotlinx.android.synthetic.main.email_update_dialog.*
import kotlinx.android.synthetic.main.fragment_splash.*
import java.util.*


class TestDriveDateReservationFragment : Fragment() {
    private lateinit var binding: FragmentTestDriveDateReservationBinding
    private lateinit var model: Model
    private lateinit var viewModel: TestDriveDateReservationVM
    private lateinit var userInfoDialog: BottomSheetDialog
    private val prefs = Shared()
    private val datesAdapter: TestDriveDateAdapter by lazy {
        TestDriveDateAdapter(listOf(), resources.getColor(R.color.colorPrimary))
    }
    private val locationsAdapter: TestDriveLocationAdapter by lazy {
        TestDriveLocationAdapter(listOf())
    }
    private val timeAdapter: TestDriveTimeAdapter by lazy {
        TestDriveTimeAdapter(resources.getColor(R.color.colorPrimary), arrayListOf(), arrayListOf())
    }
    var dialog = SimpleDialog(null, null)
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress(
            "Book_testDrive_screen",
            category = model.let {
                ModelSimplifier(
                    it
                ).brand?.cat?.id
            }
                ?: "",
            dealerData = model.let { ModelSimplifier(it).brand?.id } ?: "",
            modelData = model.let {
                ModelSimplifier(it).id
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_test_drive_date_reservation,
                container,
                false
            )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        view?.setOnClickListener {
            it.hideKeyboard()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TestDriveDateReservationVM::class.java)
        binding.viewModel = viewModel
        viewModel.toMylocation = false
        timeAdapter.refreshDates(R.color.colorPrimary, arrayListOf())
        arguments.let {
            model = it?.let { it1 -> TestDriveDateReservationFragmentArgs.fromBundle(it1).model }!!
                it.let { it1 ->
                    viewModel.testDriveStatus = TestDriveDateReservationFragmentArgs.fromBundle(it1).atShowroom
                    viewModel.toMylocation =  TestDriveDateReservationFragmentArgs.fromBundle(it1).atShowroom == 0
                }

            viewModel.model = model
            showLocationBottomSheet()
        }
        model.dealerData?.id?.let { viewModel.getModelLocations(it) }
        binding.model = ModelSimplifier(model)
        binding.dateRecycler.apply {
            adapter = datesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.timeRecycler.apply {
            adapter = timeAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
        initDates()

        viewModel.date.set(datesAdapter.dates[1])
        viewModel.selectedDate.set(datesAdapter.dates[1])
        if (viewModel.toMylocation) {
           // viewModel.toMylocation = true
            viewModel.selectedLocation = null
            viewModel.time.set("")
            viewModel.getPreferredTimeList(viewModel.selectedDate.get())
            val day = viewModel.selectedDate.get()?.let { Utils.instance.getDayOfWeek(it) }
            val hours = day?.let { ModelSimplifier(model).getWorkingHoursArray(it) }
            hours?.let { it1 ->
                ModelSimplifier(model).brand?.category?.setBgColor()?.let {
                    timeAdapter.refreshDates(it, it1)
                }
            }
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, false,
            ModelSimplifier(model).brand?.category?.setBgColor()
        )
        onClick()
        observeData()

    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.preferredTimeList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            timeAdapter.refreshPreferredTimeTestDrive(it)

        })
        viewModel.emailError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                showErrorBottomSheet(getString(R.string.invalid_email))
            }
        })
        viewModel.phoneError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                showErrorBottomSheet(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                showErrorBottomSheet(getString(R.string.empty_name))
            }
        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            showErrorBottomSheet(getString(R.string.test_drive_error))

        })


        viewModel.requestTestDriveLoading.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                userInfoDialog.dismiss()
                binding.progressCircula.isVisible = true
                activity?.let { it1 -> DesignUtils.instance.disableUserInteraction(it1) }
            } else {
                binding.progressCircula.isVisible = false
                activity?.let { it1 -> DesignUtils.instance.enableUserInteraction(it1) }
            }
        })

        viewModel.successRequestDrive.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
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
                successDialog.setCanceledOnTouchOutside(false)
                bindingSuccess.titleText.text = getString(R.string.done_successfully)
                bindingSuccess.messageText.text =
                    getString(R.string.test_drive_created_successfully)


                successDialog.show()

                bindingSuccess.exitBtn.setOnClickListener {

                    successDialog.dismiss()
                    view?.let { it1 -> Navigation.findNavController(it1).navigateUp() }

                }
            }
        })
        /*viewModel.verifyPhone.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.phoneNumber.get()?.let { it1 ->

                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.requestTestDriveAtShowroom()
                        dialog.dismiss()
                    }, {
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }

            }

        })*/
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
        bindingSuccess.messageText.text = message
        errorDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            errorDialog.dismiss()
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


    private fun onClick() {
        binding.confirmDeliverToLocation.setOnClickListener {

            if (viewModel.time.get() != "") {
                showUserInformationBottomSheet()
            } else {
                binding.timeRecycler.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
            }
        }
        locationsAdapter.setOnItemCLickListener(object :
            TestDriveLocationAdapter.OnItemClickListener {
            override fun onItemClick(localtion: Location?) {
                if (localtion != null) {
                    viewModel.selectedLocationID = localtion.id
                    viewModel.selectedLocation = localtion
                    //viewModel.toMylocation = false
                    var day = viewModel.selectedDate.get()?.let { Utils.instance.getDayOfWeek(it) }
                    var hours = day?.let { LocationSimplifier(localtion).getWorkingHoursArray(it) }
                    day?.let { LocationSimplifier(localtion).getWorkingHoursArray(it) }?.let {
                        ModelSimplifier(model).brand?.category?.setBgColor()?.let { it1 ->
                            timeAdapter.refreshDates(
                                it1,
                                it
                            )
                        }
                    }
                    binding.testDriveAtShowroomBtn.visibility = View.VISIBLE
                    binding.testDriveAtShowroom.visibility = View.INVISIBLE


                    viewModel.getPreferredTimeList(viewModel.selectedDate.get())
                }

            }

        })

        binding.confirmDeliverToMyLocation.setOnClickListener {
            if (viewModel.time.get() == "") {
                binding.timeRecycler.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
            } else {
                viewModel.savePreferredTime()
                if (viewModel.isConnected())
                    Navigation.findNavController(it)
                        .navigate(TestDriveDateReservationFragmentDirections.actionShowTestDriveAddressFragment())
                else
                    Navigation.findNavController(it)
                        .navigate(TestDriveDateReservationFragmentDirections.showLogin(R.id.testDriveAddressFragment))
            }
        }
        binding.testDriveAtShowroom.setOnClickListener {
//            prefs.string(USER_ID)?.let {
//                val local = LocalRepo()
//                local.getOne<User>(it)?.let {
//                    if (it.email?.startsWith("+") == true) {
//                        showUpdateEmailDialog(requireActivity()) {
//                            viewModel.time.set("")
//                            showLocationBottomSheet()
//                        }
//                    } else {
//                        viewModel.time.set("")
//                        showLocationBottomSheet()
//                    }
//                } ?: run {
//                    viewModel.time.set("")
//                    showLocationBottomSheet()
//                }
//            } ?: run {
                viewModel.time.set("")
                showLocationBottomSheet()
            //}
        }

        binding.testDriveAtShowroomBtn.setOnClickListener {
            showLocationBottomSheet()
        }
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        datesAdapter.setOnItemCLickListener(object : TestDriveDateAdapter.OnItemClickListener {
            override fun onItemClick(date: Date?) {
                viewModel.time.set("")
                viewModel.selectedDate.set(date)
                viewModel.date.set(date)
                if (viewModel.isInitializedLocation()) {
                    viewModel.getPreferredTimeList(date)
                    val day = viewModel.selectedDate.get()?.let { Utils.instance.getDayOfWeek(it) }
                    val hours = day?.let {
                        viewModel.selectedLocation?.let { it1 ->
                            LocationSimplifier(it1).getWorkingHoursArray(
                                it
                            )
                        }
                    }
                    if (hours != null) {

                        ModelSimplifier(model).brand?.category?.setBgColor()?.let {
                            timeAdapter.refreshDates(
                                it, hours
                            )
                        }
                    }
                }
                if (viewModel.toMylocation) {
                    val day = viewModel.selectedDate.get()?.let { Utils.instance.getDayOfWeek(it) }
                    val hours = day?.let { ModelSimplifier(model).getWorkingHoursArray(it) }
                    hours?.let { it1 ->
                        ModelSimplifier(model).brand?.category?.setBgColor()?.let {
                            timeAdapter.refreshDates(
                                it, it1
                            )
                        }
                    }
                    viewModel.getPreferredTimeList(date)
                }
            }

        })
        timeAdapter.setOnItemCLickListener(object : TestDriveTimeAdapter.OnItemClickListener {
            override fun onItemClick(time: String?) {
                viewModel.time.set(time)
                binding.testDriveAtShowroom.visibility = View.INVISIBLE
                binding.testDriveAtShowroomBtn.visibility = View.INVISIBLE
                if (viewModel.toMylocation){
                    binding.confirmDeliverToMyLocation.visibility = View.VISIBLE
                }else{
                    binding.confirmDeliverToLocation.visibility = View.VISIBLE
                }


            }

        })

    }

    private fun showLocationBottomSheet() {
       // viewModel.toMylocation = false
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val testDriveBinding: TestDriveShowroomzLocationsBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.test_drive_showroomz_locations_bottom_sheet,
                null,
                false
            )

        testDriveBinding.viewModel = viewModel
        dialog.setContentView(testDriveBinding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        testDriveBinding.exitBtn.setOnClickListener {
            dialog.dismiss()

        }
        testDriveBinding.locationsRecycler.apply {
            adapter = locationsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.locations.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (locationsAdapter.locations.isEmpty())
                locationsAdapter.refresh(it)

        })


    }

    private fun showUserInformationBottomSheet() {
        userInfoDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var testDriveBinding: TestDriveUserInfoBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.test_drive_user_info_bottom_sheet,
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
        testDriveBinding.container.setOnClickListener {
            it.hideKeyboard()
        }
    }

    private fun initDates() {
        val c: Calendar = Calendar.getInstance(Locale.getDefault())

        c.add(Calendar.DATE, 180)
        val expDate: GregorianCalendar = GregorianCalendar()
        expDate.add(Calendar.DATE, 1)
        expDate.after(Calendar.getInstance(Locale.getDefault()))

        ModelSimplifier(model).brand?.category?.setBgColor()?.let {
            datesAdapter.refreshDates(
                it,
                Utils.instance.dateToString(expDate.time, "yyyy-MM-dd")?.let {
                    Utils.instance.getDates(
                        it,
                        Utils.instance.dateToString(c.time, "yyyy-MM-dd")!!

                    )
                }!!
            )
        }

    }


}