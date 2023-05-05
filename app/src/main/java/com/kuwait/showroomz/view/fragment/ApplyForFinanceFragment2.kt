package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.CacheObjects
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.hideKeyboard
import com.kuwait.showroomz.model.data.Industry
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.IndustrySimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.IndustriesAdapter
import com.kuwait.showroomz.viewModel.ApplyForFinanceStepOneVM
import kotlinx.android.synthetic.main.fragment_apply_for_finance2.*
import java.text.SimpleDateFormat
import java.util.*

class ApplyForFinanceFragment2 : Fragment() {
    private lateinit var binding: FragmentApplyForFinance2Binding
    val industriesDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private lateinit var viewModel: ApplyForFinanceStepOneVM
    private val industriesAdapter: IndustriesAdapter by lazy {
        IndustriesAdapter(arrayListOf())
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_callback_screen_with_semi_approval_step1")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_apply_for_finance2,
                container,
                false
            )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        viewModel = ViewModelProviders.of(this).get(ApplyForFinanceStepOneVM::class.java)
        arguments.let {
            viewModel.bank = ApplyForFinanceFragment2Args.fromBundle(it!!).bank
        }
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.getUser()
        viewModel.getIndustries()
        binding.industryEditText.setText(viewModel.initIndustry.get())
        if (viewModel.gender.value == 0) binding.genderTextInputEditText.setText(getString(R.string.male))
        if (viewModel.gender.value == 1) binding.genderTextInputEditText.setText(getString(R.string.female))
        if (viewModel.nationality.get() == 0) {
            binding.nationalityTextInputEditText.setText(getString(R.string.kuwaiti))
            binding.retiredEmployeeTypeEditText.setText(getString(R.string.employee))
            if (viewModel.retired.get() == 1){
                binding.employeeType.isVisible = false
                binding.companyName.isVisible = false
                binding.industry.isVisible = false
                binding.retiredEmployeeTypeEditText.setText(getString(R.string.retired))
            }
        }
        if (viewModel.nationality.get() == 1) {
            binding.nationalityTextInputEditText.setText(getString(R.string.foreign_nationality))
            binding.retiredEmployeeType.isVisible = false
            viewModel.retired.set(0)
        }
        if (viewModel.employeeType.get() == 0) {
            binding.employeeTypeEditText.setText(getString(R.string.government))
        }
        if (viewModel.employeeType.get() == 1) {
            binding.employeeTypeEditText.setText(getString(R.string.private_txt))
        }

        onclickListener()
        observeData()
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })

        viewModel.isEligible.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.isEligible.value = false
                Navigation.findNavController(requireView())
                    .navigate(ApplyForFinanceFragment2Directions.actionShowFinanceStepTwo(viewModel.bank,true))
            }
        })

        viewModel.filteredBanks.observe(viewLifecycleOwner, androidx.lifecycle.Observer {


            CacheObjects.availableBanks = viewModel.filteredBanks.value

        })
        viewModel.validName.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.empty_name))
            }
        })
        viewModel.validPhone.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.invalid_phone))
            }
        })
        viewModel.validCompanyName.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.empty_company_name))
            }
        })
        viewModel.validDateOFBirth.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.empty_dob))
            }
        })
        viewModel.validGender.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.empty_gender))
            }
        })
        viewModel.validCivilNumber.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.invalid_civil_id))
            }
        })
        viewModel.validNationality.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.nationality))
            }
        })
        viewModel.validEmpStatu.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.employee_statu))
            }
        })
        viewModel.validEmployeeType.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.employee_type))
            }
        })
        viewModel.validIndustry.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.select_industry))
            }
        })
        viewModel.validMonthlyIncome.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                showErrorDialog(getString(R.string.empty_monthly_income))
            }
        })

        viewModel.industries.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            industriesAdapter.refreshData(it)
        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (it=="1"&& viewModel.filteredBanks.value?.isNotEmpty() == true){
                viewModel.error.postValue("")
                Navigation.findNavController(requireView())
                    .navigate(ApplyForFinanceFragment2Directions.actionShowFinanceStepTwo(viewModel.bank,false))

                return@Observer
            }
            if (it == "1"&& viewModel.filteredBanks.value?.isEmpty() == true) {
                viewModel.error.postValue("")
                Navigation.findNavController(requireView())
                    .navigate(ApplyForFinanceFragment2Directions.actionShowFinanceStepTwo(viewModel.bank,false))

            }
            if (it == "error") {
                showErrorDialog("error")
                viewModel.error.postValue("")
            }


        })

    }

    private fun onclickListener() {
        scroll.setOnClickListener{
            it.hideKeyboard()
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        binding.dobClick.setOnClickListener { showDatePickerDialog() }
        binding.genderConstraint.setOnClickListener { showGenderDialog() }
        binding.nationalityConstraint.setOnClickListener { showNationalityDialog() }
        binding.employeeTypeContainer.setOnClickListener { showEmployeeTypeDialog() }
        binding.retiredEmployeeTypeContainer.setOnClickListener{showRetiredDialog()}

        binding.industryContainer.setOnClickListener { showIndustries() }
        industriesAdapter.setOnItemCLickListener(object : IndustriesAdapter.OnItemClickListener {
            override fun onItemClick(industry: Industry) {
                val simplifier = IndustrySimplifier(industry)
                binding.industryEditText.setText(simplifier.name)
                viewModel.industry.set(simplifier.id)
                industriesDialog.dismiss()
            }

        })


    }


    private fun showGenderDialog() {
        clear()
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var dialogBinding: GenderBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.gender_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(dialogBinding.root)

        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.male.setOnClickListener {
            dialog.dismiss()
            binding.genderTextInputEditText.setText(getString(R.string.male))
            viewModel.gender.value = 0

        }
        dialogBinding.female.setOnClickListener {
            dialog.dismiss()
            binding.genderTextInputEditText.setText(getString(R.string.female))
            viewModel.gender.value = 1
        }
    }

    private fun showNationalityDialog() {
        clear()
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var dialogBinding: NationalityBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.nationality_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(dialogBinding.root)

        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.kuwaiti.setOnClickListener {
            dialog.dismiss()
            binding.nationalityTextInputEditText.setText(getString(R.string.kuwaiti))
            viewModel.nationality.set(0)
            binding.retiredEmployeeType.isVisible = true
            binding.retiredEmployeeTypeEditText.setText(getString(R.string.employee))
            if (viewModel.retired.get() == 1){
                binding.employeeType.isVisible = false
                binding.companyName.isVisible = false
                binding.industry.isVisible = false
                binding.retiredEmployeeTypeEditText.setText(getString(R.string.retired))
            }else{
                binding.employeeType.isVisible = true
                binding.companyName.isVisible = true
                binding.industry.isVisible = true
            }

        }
        dialogBinding.foreignNationality.setOnClickListener {
            dialog.dismiss()
            binding.nationalityTextInputEditText.setText(getString(R.string.foreign_nationality))
            viewModel.nationality.set(1)
            binding.retiredEmployeeType.isVisible = false
            binding.employeeType.isVisible = true
            binding.companyName.isVisible = true
            binding.industry.isVisible = true
        }

    }
    private fun showRetiredDialog() {
        clear()
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var dialogBinding: RetiredBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.retired_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(dialogBinding.root)

        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.employed.setOnClickListener {
            dialog.dismiss()
            binding.retiredEmployeeTypeEditText.setText(getString(R.string.employee))
            viewModel.retired.set(0)
            binding.employeeType.isVisible = true
            binding.companyName.isVisible = true
            binding.industry.isVisible = true
        }
        dialogBinding.retired.setOnClickListener {
            dialog.dismiss()
            binding.retiredEmployeeTypeEditText.setText(getString(R.string.retired))
            viewModel.retired.set(1)
            binding.employeeType.isVisible = false
            binding.companyName.isVisible = false
            binding.industry.isVisible = false
        }

    }

    private fun showEmployeeTypeDialog() {
        clear()
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var dialogBinding: EmployeeTypeBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.employee_type_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(dialogBinding.root)

        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.government.setOnClickListener {
            dialog.dismiss()
            binding.employeeTypeEditText.setText(getString(R.string.government))
            viewModel.employeeType.set(0)
        }
        dialogBinding.privateTxt.setOnClickListener {
            dialog.dismiss()
            binding.employeeTypeEditText.setText(getString(R.string.private_txt))
            viewModel.employeeType.set(1)
        }

    }

    private fun showIndustries() {
        clear()
        var dialogBinding: IndustriesListBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.industries_list_bottom_sheet,
            null,
            false
        )
        industriesDialog.setContentView(dialogBinding.root)
        dialogBinding.industries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = industriesAdapter
        }

        industriesDialog.show()
        industriesDialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            industriesDialog.dismiss()
        }

    }

    private fun showDatePickerDialog() {
        clear()
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var dialogBinding: DateBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.date_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(dialogBinding.root)

        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.okBtn.setOnClickListener {
            var dateFormatter = SimpleDateFormat("MMM d, ''yy", Locale.getDefault())
            val newDate = Calendar.getInstance()

            newDate.set(
                dialogBinding.datePicker.year,
                dialogBinding.datePicker.month,
                dialogBinding.datePicker.dayOfMonth
            )

            binding.dobTextInputEditText.setText(dateFormatter.format(newDate.time))
            dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            if (Date().after(newDate.time)){
                viewModel.selectedDateOfBirth.set(dateFormatter.format(newDate.time))
                dialog.dismiss()
            }else{
                showErrorDialog("wrong birth date")
            }

        }
        dialogBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun clear(){
        nameTextInputEditText.clearFocus()
        phoneEditText.clearFocus()
        civilIdTextInputEditText.clearFocus()
        company_nameEditText.clearFocus()
        monthly_incomeEditText.clearFocus()
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
}