package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentBookNowItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Booking
import com.kuwait.showroomz.model.data.program
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BookingSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimProgramSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.BookNowItemVm
import kotlinx.coroutines.launch


class BookNowItemFragment : Fragment() {
    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var binding: FragmentBookNowItemBinding
    private lateinit var booking: Booking
    private lateinit var viewModel: BookNowItemVm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_book_now_item, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Booking_details_screen",category = BookingSimplifier(booking).model?.let {
            it.brand?.cat?.id
        }
            ?:"", dealerData = BookingSimplifier(booking).model?.let { it.brand?.id } ?:"",modelData =  BookingSimplifier(booking).model?.let {
            it.id
        }
            ?:"")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BookNowItemVm::class.java)
        arguments.let {
            booking = it?.let { it1 -> PaymentItemFragmentArgs.fromBundle(it1).booking }!!
        }
        binding.booking = BookingSimplifier(booking)
        binding.program = TrimProgramSimplifier((booking.program()))
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            BookingSimplifier(booking).model?.brand?.category?.setBgColor()
        )
        observeData()
        onClickListener()
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    context,
                    context?.getString(R.string.download_success),
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun onClickListener() {
        val simplifier = BookingSimplifier(booking)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        if (simplifier.civilID != null) {
            binding.uploadImage.setOnClickListener {
                if (checkPermission()) {
                    lifecycleScope.launch { viewModel.download(simplifier.civilID) }
                } else ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )

            }
        }



    }

}