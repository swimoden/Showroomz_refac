package com.kuwait.showroomz.viewModel

import android.os.Environment
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.FileOutputStream
import java.io.InputStream


class BookingRentItemVm : ViewModel() {
    private val service = ApiService()
    val success = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    val TAG = "BookingRentItemVm"
    var fullAddressPickUp = ObservableField<String>("")
    var fullAddressDropOff = ObservableField<String>("")
    var daysAmountFormula = ObservableField<String>("")
    var startDayNbr = ObservableField<String>("")
    var startMonth = ObservableField<String>("")
    var startDayName = ObservableField<String>("")
    var startTime = ObservableField<String>("")
    var endTime = ObservableField<String>("")
    var endDayNbr = ObservableField<String>("")
    var endMonth = ObservableField<String>("")
    var endDayName = ObservableField<String>("")
    val addonsFormula = MutableLiveData<ArrayList<AddonsResult>>()
    private val local = LocalRepo()
    private val prefs = Shared()
    lateinit var booking: Booking
    private val disposable = CompositeDisposable()
    fun getDates() {

        startDayNbr.set(
            booking.startBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "dd"
                    )
                }
            }
        )
        startMonth.set(
            booking.startBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "MMMM"
                    )
                }
            }
        )
        startDayName.set(
            booking.startBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "EEEE"
                    )
                }
            }
        )
        startTime.set(
            booking.startBookingTime?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "HH:mm"
                    )
                }
            }
        )

        endDayNbr.set(
            booking.endBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "dd"
                    )
                }
            }
        )
        endMonth.set(
            booking.endBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "MMMM"
                    )
                }
            }
        )
        endDayName.set(
            booking.endBookingDate?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "EEEE"
                    )
                }
            }
        )
        endTime.set(
            booking.endBookingTime?.let {
                Utils.instance.stringToDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss"
                )?.let {
                    Utils.instance.dateToString(
                        it, "HH:mm"
                    )
                }
            }
        )
    }

    fun getDeliveryAddress() {
        if (booking.deliveryAddress() != null) {
            fullAddressPickUp.set(UserAddressSimplifier(booking.deliveryAddress()!!).fullAddress)
            fullAddressDropOff.set(booking.dropoutAddress()?.let { UserAddressSimplifier(it).fullAddress })
        } else {
            BookingSimplifier(booking).model?.brand?.id?.let {
                getModelLocations(it)
            }

        }
    }

    private fun getModelLocations(brandID: String) {


        val locs = local.getAllByString<Location>("dealerData.id", brandID)
        if (!locs.isNullOrEmpty()) {
            val location = locs.filter { it.type == 2 }.first()
            fullAddressDropOff.set(LocationSimplifier(location).name)
            fullAddressPickUp.set(LocationSimplifier(location).name)

        } else {
            if (!NetworkUtils.instance.connected) {
                return
            }
            val url = "locations?_page=0&itemsPerPage=200&&brandId=$brandID"
            disposable.add(
                service.getLocations(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseListResponse<ArrayList<Location>>>() {
                        override fun onSuccess(obj: BaseListResponse<ArrayList<Location>>) {

                            obj.Result?.let {list ->
                                if (list.isNotEmpty()) {
                                    setTranslationPrimaryKey(list, brandID)
                                }
                            }
                        }

                        override fun onError(e: Throwable) {


                        }
                    })
            )
        }


    }

    private fun setTranslationPrimaryKey(list: List<Location>, brandID: String) {
        local.save(list) {

            val location = list.filter { it.type == 2 }.first()
            fullAddressDropOff.set(LocationSimplifier(location).name)
            fullAddressPickUp.set(LocationSimplifier(location).name)

            Log.e("setTranslationPrimary", "" + it)

        }
    }

    suspend fun download(file: Image?) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            return
        }
        val url = /*BASE_URL +*/ file?.file
        val responseBody = url?.let {
            service.downloadFile(it).body()
        }
        if (url?.let { service.downloadFile(it).isSuccessful } == true) {
            saveFile(
                responseBody,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/${file?.fileName}"
            )

        } else {
            error.value = true
        }
    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String): String {
        if (body == null)
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
            success.value = true
            return pathWhereYouWantToSaveFile
        } catch (e: Exception) {

            Log.e("saveFile", e.toString())
        } finally {
            input?.close()
        }
        return ""
    }

    fun calculateAmount() {
        var trimsFromLocal = booking.modelData()?.id?.let { local.getOne<TrimResponse>(it) }
        var dailyProgram: Program? = null
        var monthlyProgram: Program? = null
        var weeklyProgram: Program? = null
        var yearlyProgram: Program? = null
        var trim = trimsFromLocal?.trims?.get(0)

        trim?.programs?.forEach {
            Log.e(TAG, "calculateAmount: ${it.contractPeriod}")
            if (it.contractPeriod == 1) dailyProgram = it
            if (it.contractPeriod == 7) weeklyProgram = it
            if (it.contractPeriod == 31) monthlyProgram = it
            if (it.contractPeriod == 365) yearlyProgram = it
        }

        val days = booking.numberOfDays ?: 0


        if (days >= 365) {
            var price: Int? = 0
            if (yearlyProgram != null) {
                addonsFormula.value =arrayListOf<AddonsResult>()
                daysAmountFormula.set("")
                val yearNbr = days.div(365)
                val monthNbr = (days % 365).div(30)
                val weekNbr = ((days % 365) % 30).div(7)
                val daysNbr = ((days % 365) % 30) % 7
                price = yearlyProgram?.monthlyPayment?.toInt()?.times(monthNbr)
                price = monthlyProgram?.monthlyPayment?.toInt()?.times(monthNbr)
                    ?.let { price?.plus(it) }
                price =
                    weeklyProgram?.monthlyPayment?.toInt()?.times(weekNbr)?.let { price?.plus(it) }
                price =
                    dailyProgram?.monthlyPayment?.toInt()?.times(daysNbr)?.let { price?.plus(it) }
                daysAmountFormula.set(daysAmountFormula.get() + "$yearNbr X ${yearlyProgram?.monthlyPayment}")
                if (monthNbr > 0)
                    daysAmountFormula.set(daysAmountFormula.get() + "$monthNbr X ${monthlyProgram?.monthlyPayment}")
                if (weekNbr > 0) {
                    daysAmountFormula.set(daysAmountFormula.get() + " + $weekNbr X ${weeklyProgram?.monthlyPayment}")
                }
                if (daysNbr > 0) {
                    daysAmountFormula.set(daysAmountFormula.get() + " + $daysNbr X ${dailyProgram?.monthlyPayment}")
                }
            } else {
                addonsFormula.value =arrayListOf<AddonsResult>()
                daysAmountFormula.set("")
                val monthNbr = days.div(30)
                val weekNbr = (days % 30).div(7)
                val daysNbr = (days % 30) % 7
                price = monthlyProgram?.monthlyPayment?.toInt()?.times(monthNbr)
                price =
                    weeklyProgram?.monthlyPayment?.toInt()?.times(weekNbr)?.let { price?.plus(it) }
                price =
                    dailyProgram?.monthlyPayment?.toInt()?.times(daysNbr)?.let { price?.plus(it) }
                daysAmountFormula.set(daysAmountFormula.get() + "$monthNbr X ${monthlyProgram?.monthlyPayment}")
                if (weekNbr > 0) {
                    daysAmountFormula.set(daysAmountFormula.get() + " + $weekNbr X ${weeklyProgram?.monthlyPayment}")
                }
                if (daysNbr > 0) {
                    daysAmountFormula.set(daysAmountFormula.get() + " + $daysNbr X ${dailyProgram?.monthlyPayment}")
                }
            }
            daysAmountFormula.set(daysAmountFormula.get() + " = $price" + if (isEnglish) " KWD " else " د.ك ")
            val aux = arrayListOf<AddonsResult>()
            booking.addons?.forEach {

                if (it.isCollection!!) {
                    price = price?.plus(days * it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            "$days X ${it.cost} = ${days * it.cost.toInt()} ${if (isEnglish) " KWD " else " د.ك "}"
                        )
                    )
                } else {
                    price = price?.plus(it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            it.cost + " " + if (isEnglish) " KWD " else " د.ك "
                        )
                    )
                }
            }
            addonsFormula.value = aux
            Log.e(TAG, "calculateAmount: $price")
            Log.e(TAG, "calculateAmount: ${daysAmountFormula.get()}")

        }
        if (days in 30..364) {
            var price: Int? = 0
            addonsFormula.value =arrayListOf<AddonsResult>()
            daysAmountFormula.set("")
            val monthNbr = days.div(30)
            val weekNbr = (days % 30).div(7)
            val daysNbr = (days % 30) % 7
            price = monthlyProgram?.monthlyPayment?.toInt()?.times(monthNbr)
            price = weeklyProgram?.monthlyPayment?.toInt()?.times(weekNbr)?.let { price?.plus(it) }
            price = dailyProgram?.monthlyPayment?.toInt()?.times(daysNbr)?.let { price?.plus(it) }
            daysAmountFormula.set(daysAmountFormula.get() + "$monthNbr X ${monthlyProgram?.monthlyPayment}")
            if (weekNbr > 0) {
                daysAmountFormula.set(daysAmountFormula.get() + " + $weekNbr X ${weeklyProgram?.monthlyPayment}")
            }
            if (daysNbr > 0) {
                daysAmountFormula.set(daysAmountFormula.get() + " + $daysNbr X ${dailyProgram?.monthlyPayment}")
            }
            daysAmountFormula.set(daysAmountFormula.get() + " = $price" + if (isEnglish) " KWD " else " د.ك ")
            val aux = arrayListOf<AddonsResult>()
            booking.addons?.forEach {

                if (it.isCollection!!) {
                    price = price?.plus(days * it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            "$days X ${it.cost} = ${days * it.cost.toInt()} ${if (isEnglish) " KWD " else " د.ك "}"
                        )
                    )
                } else {
                    price = price?.plus(it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            it.cost + " " + if (isEnglish) " KWD " else " د.ك "
                        )
                    )
                }
            }
            addonsFormula.value = aux
            Log.e(TAG, "calculateAmount: $price")
            Log.e(TAG, "calculateAmount: ${daysAmountFormula.get()}")

        }
        if (days in 7..29) {

            var price: Int? = 0
            addonsFormula.value =arrayListOf<AddonsResult>()
            daysAmountFormula.set("")
            val weekNbr = days.div(7)
            val daysNbr = days % 7
            price = weeklyProgram?.monthlyPayment?.toInt()?.times(weekNbr)
            price = dailyProgram?.monthlyPayment?.toInt()?.times(daysNbr)?.let { price?.plus(it) }
            if (weekNbr > 0) {
                daysAmountFormula.set(daysAmountFormula.get() + " $weekNbr X ${weeklyProgram?.monthlyPayment}")
            }
            if (daysNbr > 0) {
                daysAmountFormula.set(daysAmountFormula.get() + " + $daysNbr X ${dailyProgram?.monthlyPayment}")
            }
            daysAmountFormula.set(daysAmountFormula.get() + " = $price" + if (isEnglish) " KWD " else " د.ك ")
            val aux = arrayListOf<AddonsResult>()
            booking.addons?.forEach {

                if (it.isCollection!!) {
                    price = price?.plus(days * it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            "$days X ${it.cost} = ${days * it.cost.toInt()} ${if (isEnglish) " KWD " else " د.ك "}"
                        )
                    )
                } else {
                    price = price?.plus(it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            it.cost + " " + if (isEnglish) " KWD " else " د.ك "
                        )
                    )
                }
            }
            addonsFormula.value = aux
            Log.e(TAG, "calculateAmount: $price")
            Log.e(TAG, "calculateAmount: ${daysAmountFormula.get()}")


        }
        if (days < 7) {
            var price: Int? = 0
            price = dailyProgram?.monthlyPayment?.toInt()?.times(days)
            addonsFormula.value =arrayListOf<AddonsResult>()
            daysAmountFormula.set("$days X ${dailyProgram?.monthlyPayment}")
            daysAmountFormula.set(daysAmountFormula.get() + " = $price" + if (isEnglish) " KWD " else " د.ك ")
            val aux = arrayListOf<AddonsResult>()
            booking.addons?.forEach {

                if (it.isCollection!!) {
                    price = price?.plus(days * it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            "$days X ${it.cost} = ${days * it.cost.toInt()} ${if (isEnglish) " KWD " else " د.ك "}"
                        )
                    )
                } else {
                    price = price?.plus(it.cost.toInt())
                    aux.add(
                        AddonsResult(
                            AddonSimplifier(it).name,
                            it.cost + " " + if (isEnglish) " KWD " else " د.ك "
                        )
                    )
                }
            }
            addonsFormula.value = aux
            Log.e(TAG, "calculateAmount: $price")


        }

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
