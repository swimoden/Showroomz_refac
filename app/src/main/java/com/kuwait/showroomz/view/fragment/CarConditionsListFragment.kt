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
import com.kuwait.showroomz.databinding.FragmentCarConditionListBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ChooseYearAdapter
import com.kuwait.showroomz.viewModel.ModelListVM
import kotlinx.android.synthetic.main.fragment_brand_list.*

class CarConditionsListFragment : Fragment() {
    private lateinit var binding: FragmentCarConditionListBinding
    private lateinit var viewModel: ModelListVM
    private var selectedCondition: String? = null
    private var source = 0
    private val yearAdapter: ChooseYearAdapter by lazy {
        ChooseYearAdapter(arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCarConditionListBinding.inflate(inflater, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(ModelListVM::class.java)
//        viewModel.getBrands(" ", " ")
        arguments?.let {
           source =  CarConditionsListFragmentArgs.fromBundle(it).source
        }
        binding.conditionRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = yearAdapter
        }
        toolbar_text.text = if(source ==0) getString(R.string.car_condition) else getString(R.string.chassis_condition)
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
                if (source == 0) "condition" else  "chassis_condition",
                selectedCondition
            )
            Navigation.findNavController(requireView()).popBackStack()
        }

        yearAdapter.setOnItemCLickListener(object : ChooseYearAdapter.OnItemClickListener {
            override fun onItemClick(year: String) {
                selectedCondition = year
            }

        })
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        val array: Array<String> = resources.getStringArray(R.array.car_conditions)
        val arrayL = ArrayList<String>()
        array.forEach {
            arrayL.add(it)
        }
        yearAdapter.refreshData(arrayL)
    }


}