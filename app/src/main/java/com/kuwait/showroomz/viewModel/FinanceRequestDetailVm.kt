package com.kuwait.showroomz.viewModel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.CALLBACK_BANKS
import com.kuwait.showroomz.extras.NetworkUtils
import com.kuwait.showroomz.extras.USER_ID
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.data.CallbackAppraisalClientVehicle
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.FileOutputStream
import java.io.InputStream


class FinanceRequestDetailVm : ViewModel() {
    private val service = ApiService()
    val success = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<Boolean>(false)
    val noConnectionError = MutableLiveData<Boolean>(false)
    val local = LocalRepo()
    private val disposable = CompositeDisposable()
    suspend fun download(file: Image?) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        val url = BASE_URL+file?.file
        val responseBody =  service.downloadFile(url).body()
        Log.e("download", "onSuccess: ${service.downloadFile(url).isSuccessful}")
        if(service.downloadFile(url).isSuccessful){
            saveFile( responseBody,Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+"/${file?.fileName}")
        }else {
            error.value=true
        }
    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            Log.e("FinanceRequestDetailVm", "saveFile: $pathWhereYouWantToSaveFile")
            success.value=true
            return pathWhereYouWantToSaveFile
        }catch (e:Exception){
            Log.e("saveFile",e.toString())
            input?.close()
        }
        finally {
            input?.close()
        }
        return ""
    }

     fun getFinanceCallBack(id:String, resp:(Callback?)-> Unit) {
        local.getOne<Callback>(id)?.let {
            resp(it)
        }
        disposable.add(
            service.getCallback("$CALLBACK_BANKS/$id")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Callback>() {
                    override fun onSuccess(it:Callback) {
                        resp(it)
                    }

                    override fun onError(e: Throwable) {
                        resp(null)
                    }
                })
        )
    }
}
