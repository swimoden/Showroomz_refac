package com.kuwait.showroomz.view.fragment


import android.Manifest
import android.R.attr
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentApplyForFinanceStepFourBinding
import com.kuwait.showroomz.databinding.PickFileBottomSheetBinding
import com.kuwait.showroomz.databinding.SuccessBottomSheetBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.ApplyForFinanceVM
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class ApplyForFinanceStepFourFragment : Fragment() {
    lateinit var binding: FragmentApplyForFinanceStepFourBinding
    private lateinit var viewModel: ApplyForFinanceVM
    val SALARY_CODE = 1
    val AKAKATHOPIA = 2
    val BANK_STATEMENT = 3
    private val PICK_IMAGE = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private var mUri: Uri? = null
    private var fileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_apply_for_finance_step_four, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_callback_screen_step4")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ApplyForFinanceVM::class.java)
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        binding.viewModel = viewModel
        onClickListener()
        observeData()
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
    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.netSalaryConstraint.setOnClickListener {
            showPickFileBottomSheet(SALARY_CODE)
        }
        binding.statementConstraint.setOnClickListener {
            showPickFileBottomSheet(BANK_STATEMENT)
        }
        binding.akamathopiaConstraint.setOnClickListener {
            showPickFileBottomSheet(AKAKATHOPIA)
        }
        binding.requestBtn.setOnClickListener {

            CacheObjects.selectedBanks?.forEach {
                viewModel.requestBankCallback(it)
            }
        }

    }
    private fun pickImage(request: Int) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                clickPicturesThroughCamera(request)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_IMAGE_CAPTURE
                )
            }


    }
    private fun showPickFileBottomSheet(request:Int) {
        val pickFileDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingPickFile: PickFileBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.pick_file_bottom_sheet,
            null,
            false
        )
        pickFileDialog.setContentView(bindingPickFile.root)
        pickFileDialog.setCanceledOnTouchOutside(false)
        pickFileDialog.show()
        bindingPickFile.exitBtn.setOnClickListener {
            pickFileDialog.dismiss()
        }
        bindingPickFile.cameraTxt.setOnClickListener {
            pickImage(request + 10)
            pickFileDialog.dismiss()
        }
        bindingPickFile.galleryTxt.setOnClickListener {

            pickFromGallery(request)
            pickFileDialog.dismiss()
        }
    }
    private fun pickFromGallery(request: Int) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"

            startActivityForResult(intent, request)
        } else {
            if (PermissionUtils.getNBR(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), request
                )
                PermissionUtils.setNBR(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    private fun displayNeverAskAgainDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(
            """
            We need to update your profile. Please permit the permission through Settings screen.
            
            Select Permissions -> Enable permission
            """.trimIndent()
        )
        builder.setCancelable(false)
        builder.setPositiveButton("Permit Manually",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == SALARY_CODE) {
                try {

                    binding.netSalaryCertificateEditText.setText(
                        requireActivity().getString(R.string.net_salary_certificate) + "."
                    )
                    val path =
                        data?.data?.let {
                            context?.let { it1 ->
                                FileUtils.instance.getPath(
                                    it1,
                                    it
                                )
                            }
                        }
                    data?.data?.let {
                        path?.let { it1 ->
                            viewModel.uploadMedia(
                                it1,
                                "*/*",
                                SALARY_CODE
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (requestCode == SALARY_CODE + 10) {
                binding.netSalaryCertificateEditText.setText(
                    requireActivity().getString(R.string.net_salary_certificate) + "."
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        SALARY_CODE
                    )
                }


            }


            if (requestCode == AKAKATHOPIA) {
                try {

                    binding.akamathopiaTextInputEditText.setText(
                        requireActivity().getString(R.string.akamathopia) + "."
                    )
                    val path =
                        data?.data?.let {
                            context?.let { it1 ->
                                FileUtils.instance.getPath(
                                    it1,
                                    it
                                )
                            }
                        }
                    data?.data?.let {
                        path?.let { it1 ->
                            viewModel.uploadMedia(
                                it1,
                                "*/*",
                                AKAKATHOPIA
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == AKAKATHOPIA + 10) {
                binding.akamathopiaTextInputEditText.setText(
                    requireActivity().getString(R.string.akamathopia) + "."
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        AKAKATHOPIA
                    )
                }
            }
            if (requestCode == BANK_STATEMENT) {
                try {

                    binding.statementThreeMonthTextInputEditText.setText(
                        requireActivity().getString(R.string.three_month_bank_statement) + "."
                    )
                    val path =
                        data?.data?.let {
                            context?.let { it1 ->
                                FileUtils.instance.getPath(
                                    it1,
                                    it
                                )
                            }
                        }
                    data?.data?.let {
                        path?.let { it1 ->
                            viewModel.uploadMedia(
                                it1,
                                "*/*",
                                BANK_STATEMENT
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == BANK_STATEMENT + 10) {
                binding.statementThreeMonthTextInputEditText.setText(
                    requireActivity().getString(R.string.three_month_bank_statement) + "."
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        BANK_STATEMENT
                    )
                }
            }
        }
    }
    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.successUploadNetSalaryFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.netSalaryCertificateEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorLightGreen))
            }else{
                binding.netSalaryCertificateEditText.setText("")
                showErrorDialog(getString(R.string.error_uploding))
            }
        })
        viewModel.successUploadAkaFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.akamathopiaTextInputEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorLightGreen))
            }else{
                binding.akamathopiaTextInputEditText.setText("")
                showErrorDialog(getString(R.string.error_uploding))
            }
        })
        viewModel.successUploadBankStatementFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.statementThreeMonthTextInputEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorLightGreen))
            }else{
                binding.statementThreeMonthTextInputEditText.setText("")
                showErrorDialog(getString(R.string.error_uploding))
            }
        })
        viewModel.netSalaryError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog("Net Salary Error")
            }
        })
        viewModel.akaError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog("Aka Error")
            }
        })
        viewModel.bankStatementError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog("Bank Statement Error")
            }
        })
        viewModel.bankNumberError.observe(viewLifecycleOwner, Observer {
            if (!it) {
                showErrorDialog(getString(R.string.invalid_account_number))
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                var binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.success_bottom_sheet,
                    null,
                    false
                )
                dialog.setContentView(binding.root)
                binding.titleText.text = getString(R.string.form_submitted_successfully)
                binding.messageText.text = getString(R.string.form_submitted_successfully_message)
                dialog.show()
                binding.exitBtn.setOnClickListener {
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).popBackStack(R.id.applyForFinanceStepFour,true)
                    Navigation.findNavController(requireView()).popBackStack(R.id.applyForFinanceStepOne,true)
                    Navigation.findNavController(requireView()).popBackStack(R.id.applyForFinanceStepTwo,true)
                    Navigation.findNavController(requireView()).popBackStack(R.id.applyForFinanceStepThree,true)
                    Navigation.findNavController(requireView())
                        .navigateUp()
                }
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.error.value=false
                showErrorDialog("Error")
            }

        })

    }

    fun getPathFromUri(contentUri: Uri?): String? {
        val imagePath: String
        var  cursor: Cursor? = null
        return try {
            cursor= contentUri?.let {
                requireActivity().contentResolver.query(
                    it, null, null,
                    null, null
                )
            }
            val index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            imagePath = index?.let { cursor?.getString(it) }.toString()
            cursor?.close()
            Log.i("imagePath", imagePath)
            imagePath
        } catch (e: java.lang.Exception) {
            cursor?.close()
            null
        }
    }

    private fun initImageUri() {
        val cv = ContentValues()
        val simpleDateFormat = SimpleDateFormat(
            "yyyy-mm-dd-hh-MM-ss",
            Locale.ENGLISH
        )
        val name: String = ("img-"
                + simpleDateFormat.format(Calendar.getInstance().time)
                ).toString() + "_" + Random().nextInt(100).toString() + ".jpg"
        cv.put(MediaStore.Images.Media.TITLE, name)
        fileUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv
        )
    }


    private fun clickPicturesThroughCamera(request:Int) {
        try {
            initImageUri()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(intent, request)
        } catch (e: java.lang.Exception) {
        }
    }


}