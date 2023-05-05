package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseListResponse
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.data.TrimResponse
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.CompareResult
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.model.simplifier.TrimTranslationSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlin.text.isNullOrEmpty

class CompareVM : ViewModel() {
    private val TAG = "CompareVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var trimOne = MutableLiveData<List<Trim>>()
    var trimTwo = MutableLiveData<List<Trim>>()
    val specs = MutableLiveData<ArrayList<CompareResult>>()
    val noConnectionError = MutableLiveData<Boolean>(false)

    /*fun getModelOneTrims(model: String) {
        val trims = local.getOne<TrimResponse>(model)
        if (!trims.isNull()) {
            trims.let{
                trimOne.value = it
            }

        } else {
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                return
            }
            val url = "model-data/$model/$TRIMS_OFFERS"
            disposable.add(
                service.getModelTrims(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                        override fun onSuccess(response: TrimResponse) {


                            saveTrims(response)
                            trimOne.value = response

                        }

                        override fun onError(e: Throwable) {
                            Log.e("Throwable", e.localizedMessage)

                        }
                    })
            )
        }

    }*/

    fun getTrimsOneByModelId( modelId: String) {
        val list = local.getAllByInt<Trim>("modelId", modelId.toInt())
        list?.let { it ->
            if (it.size > 0) {
                trimOne.value = it//.value = it
            }
        }
        val url = "Trims/model/$modelId"

        disposable.add(
            service.getTrimByModelId(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Trim>>>() {
                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                    }
                    override fun onNext(t: BaseListResponse<List<Trim>>) {
                        t.Result?.let {trims ->
                            list?.let { it ->
                                trimOne.value = it
                            }/*?: kotlin.run {
                                local.save(trims){}
                            }*/
                        }
                    }
                    override fun onComplete() {

                    }
                })
        )
    }

    private fun saveTrims(response: TrimResponse) {
        local.saveObject(response) {

        }
    }

    fun getFormattedSpecToCompare(trim1: Trim, trim2: Trim?) {
        val listOfCompare = arrayListOf<CompareResult>()

        val spec = trim1.let { TrimSimplifier(it).specs }
        spec.forEach { spec ->
            spec.contents.forEach {
                val translation = it.translations?.let { it1 -> TrimTranslationSimplifier(it1) }
                if (!translation?.label.isNullOrEmpty()) {
                    var result = CompareResult()
                    if (it.isChoice!!&& it.isMultiple!!) {
                        result.isChoice = true
                        result.title = translation?.value?.capitalizeFirstLetter()
                        result.isChecked1 = it.isChecked
                        result.isChoice2 = false


                    } else {
                        result.isChoice = false
                        result.isChoice2 = false
                        result.title = translation?.label?.capitalizeFirstLetter()
                        result.value1 = translation?.value
                    }
                    listOfCompare.add(result)
                }
            }
        }
        if (trim2 != null) {
            val spec = trim2.let { TrimSimplifier(it).specs }
            spec.forEach { spec ->
                spec.contents.forEach { content ->
                    val translation = content.translations?.let { TrimTranslationSimplifier(it) }
                     for (result in listOfCompare) {
                        if (result.isChoice == true) {
                            result.isChoice2 = true
                            if (result.title?.toUpperCase() == translation?.value?.toUpperCase()) {
                                result.isChecked2 = content.isChecked
                                break
                            }

                        } else {
                            if (result.value1 == "Power Foldable Mirrors" ){
                                print("Power Foldable Mirrors")
                            }
                            result.title?.let{
                                if (it.toUpperCase() == translation?.label?.toUpperCase()){
                                    result.value2 = translation.value

                                }
                            }

                        }
                    }
                }
            }
        }
        specs.value = listOfCompare
    }

    fun getTrimsForModelTwo(id: String) {


        val list = local.getAllByInt<Trim>("modelId", id.toInt())
        list?.let { it ->
            if (it.size > 0) {
                trimTwo.value = it
            }else{
                getOnlineTrim(id)
            }
        }?: kotlin.run {
            getOnlineTrim(id)
        }

    }

    private fun getOnlineTrim(id:String){
        val url = "Trims/model/$id"

        disposable.add(
            service.getTrimByModelId(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Trim>>>() {
                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                    }
                    override fun onNext(t: BaseListResponse<List<Trim>>) {
                        t.Result?.let {trims ->
                            //list?.let { it ->
                                trimTwo.value = trims
                            //}/*?: kotlin.run {
                                local.save(trims){}
                           // }*/
                        }
                    }
                    override fun onComplete() {

                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}