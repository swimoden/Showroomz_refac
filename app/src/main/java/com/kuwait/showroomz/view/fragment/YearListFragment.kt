package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentBrandListBinding
import com.kuwait.showroomz.databinding.FragmentFilterBinding
import com.kuwait.showroomz.databinding.FragmentYearListBinding
import com.kuwait.showroomz.databinding.FragmentYourCarDetailsBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ChooseBrandAdapter
import com.kuwait.showroomz.view.adapters.ChooseYearAdapter
import com.kuwait.showroomz.view.adapters.FilterBrandAdapter
import com.kuwait.showroomz.viewModel.FilterVM
import com.kuwait.showroomz.viewModel.ModelListVM
import java.util.*

class YearListFragment : Fragment() {
    private lateinit var binding: FragmentYearListBinding
    private lateinit var viewModel: ModelListVM
    private var selectedYear: String? = null
    private val yearAdapter: ChooseYearAdapter by lazy {
        ChooseYearAdapter(arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYearListBinding.inflate(inflater, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(ModelListVM::class.java)
//        viewModel.getBrands(" ", " ")
        binding.yearRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = yearAdapter
        }
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        observeData()
        onClickListener()
    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.fab.setOnClickListener {
            Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                "year",
                selectedYear
            )
            Navigation.findNavController(requireView()).popBackStack()
        }
        yearAdapter.setOnItemCLickListener(object : ChooseYearAdapter.OnItemClickListener {
            override fun onItemClick(year: String) {
                selectedYear = year
            }

        })
    }

    private fun observeData() {
        val array = ArrayList<String>()

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (x in currentYear downTo (currentYear - 100) step 1) {
            array.add("$x")
        }
       // array.reverse()
        yearAdapter.refreshData(array)
    }


}