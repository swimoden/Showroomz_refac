package com.kuwait.showroomz.viewModel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.NetworkUtils
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.Image
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody
import java.io.FileOutputStream
import java.io.InputStream


class BookNowItemVm : ViewModel() {
    private val service = ApiService()
    val success = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    suspend fun download(file: Image?) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            return
        }
        val url = BASE_URL+file?.file
        val responseBody=  service.downloadFile(url).body()
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
        }
        finally {
            input?.close()
        }
        return ""
    }
}
