package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentBrandListBinding
import com.kuwait.showroomz.databinding.FragmentFilterBinding
import com.kuwait.showroomz.databinding.FragmentYourCarDetailsBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.AppraisalInfo
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.AppraisalInfoAdapter
import com.kuwait.showroomz.view.adapters.ChooseBrandAdapter
import com.kuwait.showroomz.view.adapters.FilterBrandAdapter
import com.kuwait.showroomz.viewModel.AppraisalCallbackListVM
import com.kuwait.showroomz.viewModel.FilterVM
import kotlinx.android.synthetic.main.fragment_brand_list.*

class BrandListFragment : Fragment() {
    private lateinit var binding: FragmentBrandListBinding
    private lateinit var viewModel: AppraisalCallbackListVM
    private var selectedBrand: Brand? = null
    private lateinit var category: Category
    private val brandAdapter: ChooseBrandAdapter by lazy {
        ChooseBrandAdapter(arrayListOf())
    }

    private val adapterInfo: AppraisalInfoAdapter by lazy {
        AppraisalInfoAdapter(arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrandListBinding.inflate(inflater, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        viewModel = ViewModelProviders.of(this).get(AppraisalCallbackListVM::class.java)
        viewModel.getAppraisalBrands()
        binding.brandsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterInfo
        }

        observeData()
        onClickListener()
        binding.fab.isVisible = false
    }

    private fun onClickListener() {
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text.toString().isEmpty()){
                    adapterInfo.refreshData(viewModel.infoList.value)
                }else {
                    var array:ArrayList<AppraisalInfo> = arrayListOf()
                    viewModel.infoList.value?.forEach {
                        val lang = if (isEnglish) it.translations?.en else it.translations?.ar
                        val name = lang?.let {
                            it.name
                        } ?: run {
                            ""
                        }
                        if (name.toUpperCase().startsWith(text.toString().toUpperCase())){
                            array.add(it)
                        }
                    }
                    adapterInfo.refreshData(array)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.submit.setOnClickListener {
            val x = binding.otherTxt.text.toString()
            if (x.isNotEmpty()) {
                var obj:AppraisalInfo? = null
                viewModel.infoList.value?.forEach {
                    val langen =  it.translations?.en
                    val langar = it.translations?.ar
                    val nameEn = langen?.let {
                        it.name
                    } ?: run {
                        ""
                    }
                    val namear = langar?.let {
                        it.name
                    } ?: run {
                        ""
                    }
                    if (x == nameEn || x == namear){
                        obj = it
                     return@forEach
                    }
                }
                obj?.let {
                    Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                        "brand",
                        it
                    )
                    Navigation.findNavController(requireView()).popBackStack()
                } ?: kotlin.run {
                    Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                        "brandName",
                        binding.otherTxt.text.toString()
                    )
                    Navigation.findNavController(requireView()).popBackStack()
                }

            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), getString(R.string.error), getString(
                                    R.string.enter_brand_name)) { }
            }
        }

        adapterInfo.setOnItemCLickListener(object : AppraisalInfoAdapter.OnItemClickListener {
             override fun onItemClick(info: AppraisalInfo) {
                 Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                     "brand",
                     info
                 )
                 Navigation.findNavController(requireView()).popBackStack()
            }

        })
        /*brandAdapter.setOnItemCLickListener(object : ChooseBrandAdapter.OnItemClickListener {
            override fun onItemClick(brand: Brand) {
                selectedBrand = brand
            }

        })*/
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) { }
            }
        })
        viewModel.infoList.observe(viewLifecycleOwner, Observer {
            progress_circular.isVisible = false
            if (it.isNotEmpty()) {
                val array = it.filter { s -> !s.appraisalVehicleBrandModels.isEmpty() }
                adapterInfo.refreshData(array)
            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            progress_circular.isVisible = false
            binding.emptyDataTxt.container.isVisible = it
        })
    }


}