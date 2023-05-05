package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentSearchBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.SearchResultAdapter
import com.kuwait.showroomz.viewModel.LoginVM
import com.kuwait.showroomz.viewModel.SearchVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private val viewModel by viewModels<SearchVM>()
    private val searchResultAdapter= SearchResultAdapter(listOf())
    private lateinit var category: Category
    private var mBlockCompletion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    var delayy: Long = 500 // 1 seconds after user stops typing
    var searchTxt = ""
    var last_text_edit: Long = 0
    var handler: Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     }
    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delayy - 500) {
            viewModel.searchByName(searchTxt)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.viemModel = viewModel
        arguments.let {
            viewModel.category = it?.let { it1 -> SearchFragmentArgs.fromBundle(it1).cat }
           /* lifecycleScope.launch {
                delay(225)
                viewModel.filterInit()
            }*/
            //
            //viewModel.filterInit()
            it?.let { it1 ->
                category =  SearchFragmentArgs.fromBundle(it1).cat
                LogProgressRepository.logProgress("Search_screen", category = category.id)
            }
        }
        binding.resultRecycler.apply {
            adapter=searchResultAdapter
            layoutManager=LinearLayoutManager(context)
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, true,
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )
        /*binding.filterByBudgetEditText.onTextChanged() { s, start, before, count ->
            viewModel.searchByName(s.toString())
        }*/



        binding.filterByBudgetEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(input_finish_checker)
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.searchByName( s.toString())
                //searchTxt = s.toString()
                //last_text_edit = System.currentTimeMillis();
                //handler.postDelayed(input_finish_checker, delayy)
            }

        })


        observeData()
        onclickListener()
    }

    private fun onclickListener() {
        binding.closeButton.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

    }

    private fun observeData() {

        viewModel.list.observe(viewLifecycleOwner, Observer {
            searchResultAdapter.refresh(it)
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it

        })


    }


}