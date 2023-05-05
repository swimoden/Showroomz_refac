package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentPaymentWebBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import java.net.URL


class PaymentWebFragment : Fragment() {
    lateinit var binding: FragmentPaymentWebBinding
    lateinit var simplifier: ModelSimplifier
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_web, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Payment_getway_screen")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!NetworkUtils.instance.connected){
            DesignUtils.instance.showErrorDialog(requireContext(), null,null){
                Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                    PAYMENT_RESULT,
                    false
                )
                Navigation.findNavController(requireView()).popBackStack()
            }
            return
        }
        arguments.let {
            var url = it?.let { it1 -> PaymentWebFragmentArgs.fromBundle(it1).url }
            simplifier = ModelSimplifier(PaymentWebFragmentArgs.fromBundle(it!!).model)
            binding.model=simplifier
            url?.let { it1 -> binding.webView.loadUrl(it1) }
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
            binding.webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                    view.loadUrl(url)
                    val uri = URL(
                        "" +
                                url
                    )
                    val baseUrl: String =
                        uri.protocol.toString() + "://" + uri.host + if (uri.port != -1) ":" + uri.port else ""
                    if ("$baseUrl/" == BASE_URL) {
                        if (uri.path == PAYMENT_SUCCESS) {
                            Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                                PAYMENT_RESULT,
                                true
                            )
                            Navigation.findNavController(requireView()).popBackStack()
                        } else {
                            Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                                PAYMENT_RESULT,
                                false
                            )
                            Navigation.findNavController(requireView()).popBackStack()
                        }
                    }
                    Log.e("PaymentWebFragment", "shouldOverrideUrlLoading: ${uri.path}")
                    return true
                }
            }
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            simplifier.brand?.category?.setBgColor()
        )
        onClickListener()

    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }


}