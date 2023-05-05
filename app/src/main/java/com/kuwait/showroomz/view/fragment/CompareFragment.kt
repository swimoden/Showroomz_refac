package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentCompareBinding
import com.kuwait.showroomz.databinding.TrimsCompareBottomSheetBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.CompareResult
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.CompareResultAdapter
import com.kuwait.showroomz.view.adapters.CompareTrimsAdapter
import com.kuwait.showroomz.viewModel.CompareVM
import kotlinx.android.synthetic.main.fragment_compare.*


class CompareFragment : Fragment() {
    private lateinit var dialog1: BottomSheetDialog
    private lateinit var dialog2: BottomSheetDialog
    private var trimOne: Trim? = null
    private var trimTwo: Trim? = null
    private lateinit var binding: FragmentCompareBinding
    private lateinit var viewModel: CompareVM
    private lateinit var model: Model
    private lateinit var model2: Model
    private  var selectedModel: Model? = null
    private val adapter by lazy {
        CompareResultAdapter(arrayListOf<CompareResult>())
    }
    private val compareTrimsAdapter1 by lazy {
        CompareTrimsAdapter(arrayListOf<Trim>())
    }
    private val compareTrimsAdapter2 by lazy {
        CompareTrimsAdapter(arrayListOf<Trim>())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_compare, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CompareVM::class.java)


        arguments.let {

            val model = it?.let { it1 -> CompareFragmentArgs.fromBundle(it1).model }
            if (model != null) {
                this.model = model
                viewModel.getTrimsOneByModelId(model.id)
            }
            binding.modelOne = model?.let { it1 -> ModelSimplifier(it1) }
            if (::model2.isInitialized){
                binding.modelTwo = ModelSimplifier(model2)
            }else {
                binding.modelTwo = ModelSimplifier(Model())
            }
            DesignUtils.instance.setStatusBar(requireActivity() as MainActivity, false,
                model?.let { it1 -> ModelSimplifier(it1).brand?.category?.setBgColor() }
            )


        }
        binding.specRecycler.apply {
            adapter = this@CompareFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
        if (trimOne == null) {
            binding.trimOne.apply {
                isClickable = false
                isEnabled = false
                background = ContextCompat.getDrawable(context, R.drawable.filled_dark_gray)
            }

        } else {
            /*if (compareTrimsAdapter1.trims.size <= 1) {
                binding.trimOne.isVisible = false
            }*/
        }
        if (trimTwo == null) {
            binding.trimTwo.apply {
                isClickable = false
                isEnabled = false
                background = ContextCompat.getDrawable(context, R.drawable.filled_dark_gray)
            }

        }
        observeData()
        onclickListener()
    }

    private fun onclickListener() {
        binding.detailsOne.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(CompareFragmentDirections.aactionShowModelDetail(model,
                    null, null))
        }
        binding.detailsTwo.setOnClickListener {
            if (::model2.isInitialized)
                Navigation.findNavController(it)
                    .navigate(CompareFragmentDirections.aactionShowModelDetail(model2,
                        null, null))
        }
        compareTrimsAdapter1.setOnItemCLickListener(object :
            CompareTrimsAdapter.OnItemClickListener {
            override fun onItemClick(trim: Trim?) {
                this@CompareFragment.trimOne = trim
                this@CompareFragment.trimOne?.let {
                    viewModel.getFormattedSpecToCompare(
                        it,
                        this@CompareFragment.trimTwo
                    )
                }
                if (trim?.price == 0)
                    binding.modelOnePrice.text = ModelSimplifier(model).price
                else
                    binding.modelOnePrice.text =
                        trim.let { it1 -> it1?.let { TrimSimplifier(it).price } }
                dialog1.dismiss()
            }

        })
        compareTrimsAdapter2.setOnItemCLickListener(object :
            CompareTrimsAdapter.OnItemClickListener {
            override fun onItemClick(trim: Trim?) {
                this@CompareFragment.trimTwo = trim
                this@CompareFragment.trimOne?.let {
                    viewModel.getFormattedSpecToCompare(
                        it,
                        this@CompareFragment.trimTwo
                    )
                }
                if (trim?.price == 0)
                    binding.modelTwoPrice.text = ModelSimplifier(model2).price
                else
                    binding.modelTwoPrice.text =
                        trim.let { it1 -> it1?.let { TrimSimplifier(it).price } }
                dialog2.dismiss()
            }

        })
        binding.trimOne.setOnClickListener {
            showModelOneBottomSheet()
        }
        binding.trimTwo.setOnClickListener {
            showModelTwoBottomSheet()
        }
        add_model_btn.setOnClickListener {
            ModelSimplifier(model).dealer?.let { brand ->
                BrandSimplifier(brand).fetchCategory()?.let { it1 ->
                    CompareFragmentDirections.showSearchToCompare(
                        it1
                    )
                }?.let { it2 -> Navigation.findNavController(it).navigate(it2) }
            }
        }
        binding.reloadImg.setOnClickListener {
            binding.modelTwo = ModelSimplifier(Model())
            model2 = Model()
            this@CompareFragment.trimTwo = null
            compareTrimsAdapter2.refreshActions(arrayListOf())
            viewModel.trimTwo.value = null
            //           viewModel.getTrimsForModelTwo("")
            this@CompareFragment.trimOne?.let { it1 ->
                viewModel.getFormattedSpecToCompare(
                    it1,
                    this@CompareFragment.trimTwo
                )
            }

//            model.dealerData?.category?.let { it1 ->
//                CompareFragmentDirections.showSearchToCompare(
//                    it1
//                )
//            }?.let { it2 -> Navigation.findNavController(it).navigate(it2) }
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<Model>(
            "MODEL"
        )?.observe(viewLifecycleOwner,
            Observer { model ->

                if (!::model2.isInitialized || (::model2.isInitialized && model2.id == "")) {
                   // if (model.id != selectedModel?.id) {
                        model2 = model
                        selectedModel = model

                        binding.modelTwo = ModelSimplifier(model)
                        viewModel.getTrimsForModelTwo(model.id)

                   // }
                }
            })
        viewModel.specs.observe(viewLifecycleOwner, Observer {
            adapter.refresh(it)
            if (adapter.list.isNotEmpty()) {
                binding.specRecycler.smoothScrollToPosition(0)
            }
        })
        viewModel.trimOne.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                binding.trimOne.visibility = View.GONE
            }

            if (trimOne != null) {
                return@Observer
            }
            if (it != null && it.isNotEmpty()) {
                binding.trimOne.apply {
                    isClickable = true
                    isEnabled = true
                    background = ContextCompat.getDrawable(context, R.drawable.filled_black)
                }
                it.let { it1 ->

                    compareTrimsAdapter1.refreshActions((it1.filter{s -> s.isEnabled == true }).sortedBy { trim -> trim.position })
                }
                this.trimOne = it[0]
                this.trimOne?.let { it1 -> viewModel.getFormattedSpecToCompare(it1, this.trimTwo) }
                binding.modelTwoPrice.text =
                    it.get(0).let { it1 -> TrimSimplifier(it1).price }

            }
        })
        viewModel.trimTwo.observe(viewLifecycleOwner, Observer {
            if (it == null) {
//                binding.trimTwo.visibility = View.GONE
                binding.trimTwo.apply {
                    isClickable = false
                    isEnabled = false
                    background = ContextCompat.getDrawable(context, R.drawable.filled_dark_gray)
                }
            }
            if (it != null && it.isNotEmpty()) {
                binding.trimTwo.apply {
                    isClickable = true
                    isEnabled = true
                    background = ContextCompat.getDrawable(context, R.drawable.filled_black)
                }
                it.let { it1 -> compareTrimsAdapter2.refreshActions(it1.filter{s -> s.isEnabled == true }.sortedBy { trim -> trim.position }) }
                this.trimTwo = it[0]
                this.trimOne?.let { it1 -> viewModel.getFormattedSpecToCompare(it1, this.trimTwo) }
                binding.modelTwoPrice.text =
                    it[0].let { it1 -> TrimSimplifier(it1).price }
                showModelTwoBottomSheet()
            }

            if (trimTwo == null) {
                binding.trimTwo.apply {
                    isClickable = false
                    isEnabled = false
                    background = ContextCompat.getDrawable(context, R.drawable.filled_dark_gray)
                }

            }

        })
    }


    private fun showModelTwoBottomSheet() {
        dialog2 = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var binding: TrimsCompareBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.trims_compare_bottom_sheet,
            null,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        dialog2.setContentView(binding.root)
        dialog2.setCanceledOnTouchOutside(false)
        binding.trimsRecycler.apply {
            adapter = compareTrimsAdapter2
            layoutManager = LinearLayoutManager(context)
        }
        dialog2.show()
        dialog2.setOnDismissListener {
           // if (compareTrimsAdapter2.trims.size <= 1) trim_two.isVisible = false
        }
        binding.exitBtn.setOnClickListener {
            dialog2.dismiss()
        }
    }

    private fun showModelOneBottomSheet() {
        dialog1 = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var binding: TrimsCompareBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.trims_compare_bottom_sheet,
            null,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        dialog1.setContentView(binding.root)
        dialog1.setCanceledOnTouchOutside(false)
        binding.trimsRecycler.apply {
            adapter = compareTrimsAdapter1
            layoutManager = LinearLayoutManager(context)
        }
        dialog1.show()
        dialog1.setOnDismissListener {
           // if (compareTrimsAdapter1.trims.size <= 1) trim_one.isVisible = false
        }

        binding.exitBtn.setOnClickListener {
            dialog1.dismiss()
        }

    }


}