package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.DateBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentEditProfileBinding
import com.kuwait.showroomz.databinding.PickFileBottomSheetBinding
import com.kuwait.showroomz.databinding.SuccessBottomSheetBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.UserSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.EditProfileVM
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class EditProfileFragment : Fragment() {
    private val PICK_PDF_CODE: Int = 2
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var viewModel: EditProfileVM
    val PICK_IMAGE = 1
    var dialog = SimpleDialog(null, null)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Edit_profile_screen")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EditProfileVM::class.java)
        viewModel.getUser()
        binding.viemModel = viewModel
        binding.user = UserSimplifier(viewModel.user)
        scroll.setOnClickListener {
            it.hideKeyboard()
        }
        if (viewModel.userLicence != "") {

            binding.uploadTextInputEditText.setText(
                viewModel.userLicence?.substring(viewModel.userLicence!!.lastIndexOf("/") + 1)
            )
        }
        val type =
            arrayOf(
                getString(R.string.male), getString(R.string.female)
            )
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            type
        )

        binding.genderTextInputEditText.setAdapter(adapter)
        try {
            binding.genderTextInputEditText.setText(
                adapter.getItem(viewModel.gender.value ?: 0),
                false
            )

        } catch (e: ArrayIndexOutOfBoundsException) {
            binding.genderTextInputEditText.setText(adapter.getItem(0), false)
            viewModel.gender.value = 0
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        observeData()
        onclickListener()
        view.setOnClickListener {
            it.hideKeyboard()
        }
    }

    private fun onclickListener() {
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.dateContainer.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
            val dialogBinding: DateBottomSheetBinding = DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.date_bottom_sheet,
                null,
                false
            )
            dialog.setContentView(dialogBinding.root)

            dialog.show()
            dialogBinding.okBtn.setOnClickListener {
                var dateFormatter = SimpleDateFormat("MMM d, ''yy", Locale.US)
                val newDate = Calendar.getInstance()
                newDate.set(
                    dialogBinding.datePicker.year,
                    dialogBinding.datePicker.month,
                    dialogBinding.datePicker.dayOfMonth
                )
                if (newDate.time < Date()) {
                    binding.dobTextInputEditText.setText(dateFormatter.format(newDate.time))
                    dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    viewModel.selectedDateOfBirth.set(dateFormatter.format(newDate.time))
                    dialog.dismiss()
                } else {
                    val animation = AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                    dialogBinding.datePicker.startAnimation(animation)

                }
            }
            dialogBinding.exitBtn.setOnClickListener {
                dialog.dismiss()
            }

        }
        binding.genderTextInputEditText.setOnItemClickListener { parent, view, position, id ->
            viewModel.gender.value = if (position == 0) 0 else 1
        }
        binding.imageView.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(0)
                //pickImage()
            } else {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }

        }

        binding.uploadImage.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(1)
                // pickPdf()
            } else {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }

        binding.nameTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validName.value =
                binding.nameTextInputEditText.text?.trim().toString().validateLetters()
        }

        binding.emailTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validEmail.value =
                Utils.instance.isValidEmail(binding.emailTextInputEditText.text)
        }
        binding.phoneTextInputEditText.doOnTextChanged { text, _, count, after ->
            viewModel.validPhone.value =
                binding.phoneTextInputEditText.text.toString().isValidPhoneNumber()
        }

        binding.civilIdTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validCivilNumber.value = binding.civilIdTextInputEditText.text.toString()
                .isEmpty() || binding.civilIdTextInputEditText.text.toString().length == 12
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PICK_IMAGE) {
                pickImage()
            }
            if (requestCode == PICK_PDF_CODE) {
                pickPdf()
            }
        } else {
            // PermissionUtils.setShouldShowStatus(requireContext(), READ_EXTERNAL_STORAGE)
            if (PermissionUtils.getNBR(requireContext(), READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(READ_EXTERNAL_STORAGE), PICK_IMAGE
                )
                PermissionUtils.setNBR(requireActivity(), READ_EXTERNAL_STORAGE)
            }
        }

    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                READ_EXTERNAL_STORAGE
            ) != PackageManager
                .PERMISSION_GRANTED
        ) {

            if (PermissionUtils.getNBR(requireContext(), READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(READ_EXTERNAL_STORAGE), PICK_IMAGE
                )
                PermissionUtils.setNBR(requireActivity(), READ_EXTERNAL_STORAGE)
            }

        } else {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }
    }

    private fun pickPdf() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            startActivityForResult(intent, PICK_PDF_CODE)
        } else {
            if (PermissionUtils.getNBR(requireContext(), READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(READ_EXTERNAL_STORAGE), PICK_PDF_CODE
                )
                PermissionUtils.setNBR(requireActivity(), READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                try {
                    val selectedImage = data!!.data
                    val filePath = arrayOf(MediaStore.Images.Media.DATA)
                    val c: Cursor? = context?.contentResolver?.query(
                        selectedImage!!, filePath,
                        null, null, null
                    )
                    c?.moveToFirst()
                    val columnIndex: Int = c?.getColumnIndex(filePath[0])!!
                    val filePathStr = c.getString(columnIndex)
                    c.close()
                    var inputStream: InputStream? =
                        selectedImage?.let { context?.contentResolver?.openInputStream(it) }
                    val selected_img = BitmapFactory.decodeStream(inputStream)
                    binding.imageView.setImageBitmap(selected_img)
                    data.data?.let { viewModel.uploadImage(filePathStr, "image/png") }


                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "An error occured!", Toast.LENGTH_LONG).show()
                }
            }
            if (requestCode == PICK_PDF_CODE) {
                try {
                    binding.uploadTextInputEditText.setText(
                        Utils.instance.getNameFromContentUri(
                            requireContext(),
                            data?.data
                        )
                    )
                    val path = data?.data?.let {
                        context?.let { it1 ->
                            FileUtils.instance.getPath(
                                it1,
                                it
                            )
                        }
                    }
                    data?.data?.let { path?.let { it1 -> viewModel.uploadMedia(it1, "*/*") } }
                    viewModel.userLicencePath = data?.data!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (requestCode == 10){
                val filePath = getPathFromUri(fileUri)

                fileUri?.let { binding.imageView.setImageURI(it) }
                filePath?.let {
                    viewModel.uploadImage(
                        it,
                        "image/png"
                    )
                }
            }
            if (requestCode == 11){
                binding.uploadTextInputEditText.setText(
                    requireActivity().getString(R.string.license_uploaded)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let { viewModel.uploadMedia(it, "*/*") }
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

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.successUploadFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                binding.uploadTextInputEditText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorLightGreen
                    )
                )
            }
        })
        viewModel.validEmail.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                binding.emailTextInputLayout.error = getString(R.string.invalid_email)
            } else binding.emailTextInputLayout.error = null
        })
        viewModel.validName.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (!it) {
                binding.nameTextInputLayout.error =
                    if (binding.nameTextInputEditText.text?.trim()?.length == 0) getString(R.string.empty_name) else getString(
                        R.string.invalid_name
                    )

            } else binding.nameTextInputLayout.error = null

        })
        viewModel.validPhone.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                binding.phoneTextInputLayout.error =
                    if (binding.phoneTextInputEditText.text.toString()
                            .isEmpty()
                    ) getString(R.string.empty_phone) else {
                        getString(R.string.invalid_phone)
                    }
            } else binding.phoneTextInputLayout.error = null

        })
        viewModel.validGender.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                binding.genderTextInputLayout.error = getString(R.string.empty_gender)
            } else binding.genderTextInputLayout.error = null

        })
        viewModel.validCivilNumber.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                binding.civilIdTextInputLayout.error = getString(R.string.invalid_civil_id)
            } else binding.civilIdTextInputLayout.error = null

        })
        viewModel.loading.observe(viewLifecycleOwner, androidx.lifecycle.Observer { loading ->
            if (loading) {
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            } else activity?.let { DesignUtils.instance.enableUserInteraction(it) }
            binding.progressCircular.isVisible = loading


        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer { error ->
            if (error.isNotEmpty() && error == "login_error") {
                if (binding.phoneTextInputEditText.text.toString() != viewModel.user.phone?.number) {
                    binding.phoneTextInputLayout.error = getString(R.string.phone_error)
                    viewModel.verifyPhone.value = false
                } else
                    if (binding.emailTextInputEditText.text.toString() != viewModel.user.email) {
                        binding.emailTextInputLayout.error = getString(R.string.email_error)
                    }
            }
        })
        viewModel.success.observe(viewLifecycleOwner, androidx.lifecycle.Observer { it ->
            if (it) {
                var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                var binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.success_bottom_sheet,
                    null,
                    false
                )
                dialog.setContentView(binding.root)
                binding.titleText.text = getString(R.string.done_successfully)
                binding.messageText.text = getString(R.string.your_profile_is_update)
                dialog.show()
                binding.exitBtn.setOnClickListener {
                    dialog.dismiss()
                    Navigation.findNavController(requireView()).navigateUp()
                }
            }
            binding.user = UserSimplifier(viewModel.user)
        })

        /*viewModel.verifyPhone.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.phone.value?.let { it1 ->
                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.updateProfile()
                        dialog.dismiss()
                    }, {
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }

            }
        })*/
    }

    private var fileUri: Uri? = null
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


    private fun clickPicturesThroughCamera(request: Int) {
        try {
            initImageUri()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(intent, request)
        } catch (e: java.lang.Exception) {
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
                    2
                )
            }

    }

    private fun showPickFileBottomSheet(request: Int) {
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
            if (request == 0) {
                pickImage()
            } else {
                pickPdf()
            }
            // pickFile(request)
            pickFileDialog.dismiss()
        }
    }

}

