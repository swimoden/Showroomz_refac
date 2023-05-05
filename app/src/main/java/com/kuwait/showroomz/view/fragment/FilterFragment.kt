package com.kuwait.showroomz.view.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentFilterBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.Shared.Companion.filtredList
import com.kuwait.showroomz.extras.hideKeyboard
import com.kuwait.showroomz.extras.onDone
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.FilterBrandAdapter
import com.kuwait.showroomz.view.adapters.ModelTypesAdapter
import com.kuwait.showroomz.viewModel.FilterVM
import kotlinx.android.synthetic.main.fragment_filter.*


class FilterFragment : Fragment() {
    private lateinit var resultData: FilterModelResultData
    private lateinit var filterAttr: FilterAttributes
    private var types: List<Type> = arrayListOf()
    private var brands: List<Brand> = arrayListOf()
    private lateinit var viewModel: FilterVM
    private lateinit var category: Category
    private lateinit var binding: FragmentFilterBinding
    private var hasModels = true
    private val modelTypesAdapter: ModelTypesAdapter by lazy {
        ModelTypesAdapter(types, viewModel)
    }
    private val filterBrandAdapter: FilterBrandAdapter by lazy {
        FilterBrandAdapter(brands, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("FilterFragment", "onCreate: ")
        viewModel = ViewModelProviders.of(this).get(FilterVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Log.e("FilterFragment", "onCreateView: " )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
       // view?.hideKeyboard()

        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress_circular.isVisible = true
        brands_recycler.requestFocus()
        initializeScreen()
//        viewModel.list?.let {
//
//        } ?: kotlin.run {
//            Handler(Looper.getMainLooper()).postDelayed({
//                initializeScreen()
//            }, 250)
//        }

    }

    private fun initializeScreen() {
        arguments.let {
            it?.let { it1 ->
                category = FilterFragmentArgs.fromBundle(it1).category

                val parent = FilterFragmentArgs.fromBundle(it1).categoryParent
                category.let {
                    hasModels = it.hasModels!!
                    if (!hasModels) {
                        filter_by_budget_txt.isVisible = false
                        filter_by_budget_edit_text.isVisible = false
                        vehicle_type_txt.isVisible = false
                        vehicle_type_recycler.isVisible = false

                    }else{
                        filter_by_budget_edit_text.requestFocus()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        view?.hideKeyboard()
                        viewModel.refreshCate(category, it.id)
                        viewModel.getBrands(it.id)
                        parent.let {
                            it.type?.let { it1 ->
                                viewModel.getModelTypeByCategory(it1)
                            }
                        }
                    }, 250)
                }
            }
        }


        val imm: InputMethodManager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        filter_by_budget_edit_text.onDone {
            startSearch()
        }

        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            true,
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )

        vehicle_type_recycler.apply {
            adapter = modelTypesAdapter
        }

        brands_recycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = filterBrandAdapter
        }

        onclickListener()
        observeData()
    }

    private fun onclickListener() {
        view?.setOnClickListener {
            it.hideKeyboard()
        }

        close_button.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        fab.setOnClickListener {
            startSearch()
        }

    }

    private fun startSearch() {
        if (filter_by_budget_edit_text.text.toString()
                .isEmpty() && viewModel.selectedBrands.isEmpty() && viewModel.selectedType.isEmpty()
        ) {
            Snackbar.make(
                binding.root,
                resources.getString(R.string.filter_error),
                Snackbar.LENGTH_SHORT
            )
                .setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimary
                    )
                )
                .show()
        } else {
            filterAttr = FilterAttributes(
                budget = filter_by_budget_edit_text.text.toString(),
                ids = viewModel.selectedBrands,
                types = viewModel.selectedType,
                category = category
            )
            if (hasModels) {
                val action = FilterFragmentDirections.actionShowFilterModels(filterAttr)
                view?.let { it1 ->
                    progress_circular.isVisible = true
                    Navigation.findNavController(it1).navigate(action)
                }
            } else {
                val idsList = arrayListOf<String>()
                for (item in viewModel.selectedBrands) {
                    idsList.add(item.id)
                }
                val action = FilterFragmentDirections.actionFilterFragmentToMapsFragment(
                    null,
                    null,
                    idsList.toTypedArray()
                )
                view?.let { it1 ->
                    progress_circular.isVisible = true
                    Navigation.findNavController(it1).navigate(action)
                }
            }
        }
    }

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.types.observe(viewLifecycleOwner, Observer { types ->
            run {
                modelTypesAdapter.refreshData(types)
                if (types.size > 1) {
                    vehicle_type_recycler.scrollToPosition(0)
                }

            }

        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            if (hasModels)
                vehicle_type_recycler.isVisible = !isloading
            if (isloading) print("Loading") else print("not loading")
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
        })
        viewModel.brands.observe(viewLifecycleOwner, Observer { brands ->
            run {
                progress_circular.isVisible = false
                filterBrandAdapter.refreshData(brands)
            }

        })
        viewModel.models.observe(viewLifecycleOwner, Observer { models ->

            // filterAttr = FilterAttributes(budget = viewModel.budgetModel, ids = viewModel.selectedBrands, types = viewModel.selectedType)

            /* Log.e("FILTRED_LIST", "" + models.size)
              resultData = if (viewModel.selectedBrands.size > 1) {
                 FilterModelResultData("Result", models)
             } else FilterModelResultData(
                 BrandSimplifier(viewModel.selectedBrands.get(0)).name!!,
                 models
             )*/


            // val action = FilterFragmentDirections.actionShowFilterModels(filterAttr)
            // view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }


        }
        )


    }
}

