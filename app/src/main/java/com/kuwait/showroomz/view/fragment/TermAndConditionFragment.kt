package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentTermAndConditionBinding
import com.kuwait.showroomz.extras.*


class TermAndConditionFragment : Fragment() {

    private lateinit var binding: FragmentTermAndConditionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_term_and_condition,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!NetworkUtils.instance.connected){
            binding.progressCircular.isVisible=false
            DesignUtils.instance.showErrorDialog(requireContext(), null, null){
                Navigation.findNavController(view).navigateUp()
            }
            return
        }
        val url =  BuildConfig.BASE_URL + (if (isEnglish) "en/" else "ar/") +  TERM_CONDITION_URL//}lang=${if (isEnglish) "en" else "ar"}"
        binding.webView.loadUrl(url)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                binding.progressCircular.isVisible=true
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                binding.progressCircular.isVisible=false
            }
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

}