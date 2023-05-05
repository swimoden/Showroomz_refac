package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentDahboardRecyclersBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.DashboardRecyclersVM


class DashboardRecyclersFragment : Fragment() {
    lateinit var binding: FragmentDahboardRecyclersBinding
    private lateinit var viewModel: DashboardRecyclersVM
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dahboard_recyclers,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )

        return binding.root

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("customize_dashbord_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DashboardRecyclersVM::class.java)
        binding.viewModel = viewModel
        onClickListener()
    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

      /*  binding.exclusiveCnt.setOnClickListener {

            viewModel.changeDashboard("ECXLUSIVE", !viewModel.isEnableExclusive.get())
            binding.exclusiveOffersSwitch.isChecked = !viewModel.isEnableExclusive.get()


        }
        binding.trendingContainer.setOnClickListener {
            viewModel.changeDashboard("TRENDING", !viewModel.isEnableTrending.get())
            binding.trendingSwitch.isChecked = !viewModel.isEnableTrending.get()
        }*/
        binding.recentlyContainer.setOnClickListener {
            viewModel.changeDashboard("RECENTLY", !viewModel.isEnableRecently.get())
            binding.recentlySwitch.isChecked = !viewModel.isEnableRecently.get()
        }
        binding.callbackContainer.setOnClickListener {
            viewModel.changeDashboard("CALLBACKS", !viewModel.isEnableCallback.get())
            binding.callbackSwitch.isChecked = !viewModel.isEnableCallback.get()
        }
        binding.financeRequestContainer.setOnClickListener {
            viewModel.changeDashboard("FINANCE_REQUEST", !viewModel.isEnableFinanceRequest.get())
            binding.financeRequestSwitch.isChecked = !viewModel.isEnableFinanceRequest.get()
        }
        binding.financeCallbackContainer.setOnClickListener {
            viewModel.changeDashboard("FINANCE_CALLBACK", !viewModel.isEnableFinanceCallback.get())
            binding.financeCallbackSwitch.isChecked = !viewModel.isEnableFinanceCallback.get()
        }
        binding.testDriveContainer.setOnClickListener {
            viewModel.changeDashboard("TEST_DRIVE", !viewModel.isEnableTestDrive.get())
            binding.testDriveSwitch.isChecked = !viewModel.isEnableTestDrive.get()
        }

    }


}