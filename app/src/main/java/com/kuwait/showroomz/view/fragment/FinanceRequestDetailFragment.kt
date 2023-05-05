package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentFinanceRequestDetailBinding
import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.simplifier.CallbackSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.FinanceRequestDetailVm
import kotlinx.android.synthetic.main.fragment_finance_request_detail.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class FinanceRequestDetailFragment : Fragment() {
    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var binding: FragmentFinanceRequestDetailBinding
    private lateinit var viewModel: FinanceRequestDetailVm
    private lateinit var simplifier: CallbackSimplifier
    private var imageUrl = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_finance_request_detail,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinanceRequestDetailVm::class.java)
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            Color.parseColor("#C7112A")
        )
        arguments?.let {
            FinanceRequestDetailFragmentArgs.fromBundle(it).callback?.let {
                simplifier = CallbackSimplifier(it)
                binding.callback = simplifier
                salaryUploadImage.isVisible = simplifier.salary_doc != null
                akamathopia_uploadImage.isVisible = simplifier.aka != null
                statement_three_month_uploadImage.isVisible = simplifier.bankStatement != null
                DesignUtils.instance.setStatusBar(
                    requireActivity() as MainActivity,
                    false,
                    simplifier.model?.brand?.category?.setBgColor()
                )
                checkStatus()
            }?: kotlin.run {
                FinanceRequestDetailFragmentArgs.fromBundle(it).callbackId?.let{
                    viewModel.getFinanceCallBack(it){
                        it?.let {
                            simplifier = CallbackSimplifier(it)
                            binding.callback = simplifier
                            salaryUploadImage.isVisible = simplifier.salary_doc != null
                            akamathopia_uploadImage.isVisible = simplifier.aka != null
                            statement_three_month_uploadImage.isVisible = simplifier.bankStatement != null
                            DesignUtils.instance.setStatusBar(
                                requireActivity() as MainActivity,
                                false,
                                simplifier.model?.brand?.category?.setBgColor()
                            )
                            checkStatus()
                        }
                    }
                }
            }
        }


        onClickListener()
        observeData()

    }

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it){
                Toast.makeText(context,context?.getString(R.string.download_success),Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    context,
                    context?.getString(R.string.download_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }



    private fun onClickListener() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        binding.backBtn.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment){
                val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            }else{
                Navigation.findNavController(it).navigateUp()
            }
        }
        binding.akamathopiaUploadImage.setOnClickListener {
            imageUrl = simplifier.aka?.file.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                askPermissions()
            } else {
                downloadImage(imageUrl)
            }


        }


        binding.salaryUploadImage.setOnClickListener {

            imageUrl = simplifier.salary_doc?.file.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                askPermissions()
            } else {
                downloadImage(imageUrl)
            }


        }
        binding.statementThreeMonthUploadImage.setOnClickListener {
            imageUrl = simplifier.bankStatement?.file.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                askPermissions()
            } else {
                downloadImage(imageUrl)
            }

        }


    }


    private fun checkStatus() {
        when (simplifier.processStatus) {
            10 -> {
                binding.status.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.colorYellow))
                    text = context.getString(R.string.pending)
                }
                binding.statusDesc.text = context?.getString(R.string.finance_request_pending_txt)
            }
            30 -> {
                binding.status.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    text = context.getString(R.string.rejected)
                }
                binding.statusDesc.text = context?.getString(R.string.finance_request_rejected_txt)
            }
            20 -> {
                binding.status.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.colorkfh))
                    text = context.getString(R.string.approved)
                }
                binding.statusDesc.text = context?.getString(R.string.finance_request_approved_txt)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission required")
                    .setMessage("Permission required to save photos from the Web.")
                    .setPositiveButton("Allow") { dialog, id ->
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                        dialog.cancel()
                    }
                    .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {
            // Permission has already been granted
            downloadImage(imageUrl)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    // Download the Image
                    downloadImage(imageUrl)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    private var msg: String? = ""
    private var lastMsg = ""

    private fun downloadImage(url: String) {
        val directory = File(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val downloadManager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread(Runnable {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }).start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }


}
