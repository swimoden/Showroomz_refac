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
import com.kuwait.showroomz.databinding.FragmentModelListBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.AppraisalInfo
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.AppraisalInfoAdapter
import com.kuwait.showroomz.view.adapters.ChooseModelAdapter
import com.kuwait.showroomz.viewModel.ModelListVM

class ModelListFragment : Fragment() {
    private lateinit var binding: FragmentModelListBinding
    private lateinit var viewModel: ModelListVM
    private var selectedModel: Model? = null
    //private lateinit var simplifier: BrandSimplifier
    private lateinit var models: List<AppraisalInfo>
    private val brandAdapter: ChooseModelAdapter by lazy {
        ChooseModelAdapter(arrayListOf())
    }

    private val adapterInfo: AppraisalInfoAdapter by lazy {
        AppraisalInfoAdapter(arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModelListBinding.inflate(inflater, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(ModelListVM::class.java)
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
        binding.modelsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterInfo
        }
        arguments?.let {
             ModelListFragmentArgs.fromBundle(it).models?.let {
                models = it.appraisalVehicleBrandModels
                 adapterInfo.refreshData(models)
            }
        }
        binding.fab.isVisible = false

        onClickListener()
    }

    private fun onClickListener() {
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
               // viewModel.search(text.toString())
                if (text.toString().isEmpty()){
                    adapterInfo.refreshData(models)
                }else {
                    var array:ArrayList<AppraisalInfo> = arrayListOf()
                    models.forEach {
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
            if (binding.otherTxt.text.toString().isNotEmpty()) {
                Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                    "modelName",
                    binding.otherTxt.text.toString()
                )
                Navigation.findNavController(requireView()).popBackStack()
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), getString(R.string.error), getString(
                    R.string.enter_model_name)) { }
            }
        }


        adapterInfo.setOnItemCLickListener(object : AppraisalInfoAdapter.OnItemClickListener {
            override fun onItemClick(info: AppraisalInfo) {
                Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                    "model",
                    info
                )
                Navigation.findNavController(requireView()).popBackStack()
            }

        })
    }

    /*private fun observeData() {
        viewModel.models.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                brandAdapter.refreshData(it)
            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer{
        binding.emptyDataTxt.container.isVisible=it


        })
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
    }*/


}