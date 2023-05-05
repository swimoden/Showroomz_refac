package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.IndustrySimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ApplyForFinanceStepOneVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val isEligible = MutableLiveData<Boolean>(false)
    val filteredBanks = MutableLiveData<List<Bank>>()
    val industries = MutableLiveData<List<Industry>>()
    val nationality = ObservableInt(-1)
    val retired = ObservableInt(-1)
    val gender = MutableLiveData<Int>()
    val civilNumber = ObservableField<String>()
    var banks = MutableLiveData<List<Bank>>()
    val companyName = ObservableField<String>()
    val dateOfBirth = ObservableField<String>()
    val selectedDateOfBirth = ObservableField<String>()
    val employeeType = ObservableInt(-1)
    val industry = ObservableField<String>()
    val monthlyIncome = ObservableField<String>()
    val name = ObservableField<String>()
    val phone = ObservableField<String>()
    var initIndustry = ObservableField<String>()
    val validCivilNumber = MutableLiveData<Boolean>(true)
    val validName = MutableLiveData<Boolean>(true)
    val validPhone = MutableLiveData<Boolean>(true)
    val validGender = MutableLiveData<Boolean>(true)
    val validDateOFBirth = MutableLiveData<Boolean>(true)
    val validNationality = MutableLiveData<Boolean>(true)
    val validIndustry = MutableLiveData<Boolean>(true)
    val validCompanyName = MutableLiveData<Boolean>(true)
    val validEmployeeType = MutableLiveData<Boolean>(true)
    val validMonthlyIncome = MutableLiveData<Boolean>(true)
    val validEmpStatu = MutableLiveData<Boolean>(true)

    var bank: Bank? = null
    lateinit var user: User

    init {
        getBanks()
    }


    fun getBanks() {

        val auxBank = local.getAll<Bank>()
        auxBank?.let { b ->
            banks.value = b.filter { it.isPartner ?: false }
        }

        if (banks.value?.size == 0) {
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.value = false
                return
            }
            disposable.add(
                service.getBanks()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Bank>>>() {
                        override fun onSuccess(obj: BaseListResponse<List<Bank>>) {
                            obj.Result?.let { t ->
                                if (t.isNotEmpty()){
                                    saveBanks(t)
                                }
                            }

                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false
                        }

                    })
            )
        }
    }

    private fun saveBanks(t: List<Bank>) {
        local.save(t) {
            loading.value = false
            banks.value = t.filter { it.isPartner ?: false }
        }
    }

    fun getUser() {

        user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }!!

        name.set(user.fullName)
        phone.set(user.phone?.number)
        civilNumber.set(user.civilID)
         user.gender?.let{
             gender.value = it
         }
        companyName.set(user.companyName)
        dateOfBirth.set(user.dateOfBirth)
        retired.set(if(user.isRetired == true) 1 else 0)


        initIndustry.set(IndustrySimplifier(user.industry).name ?: "")
        industry.set(IndustrySimplifier(user.industry).id)
        if (!user.dateOfBirth.isNullOrEmpty()) {
            selectedDateOfBirth.set(user.dateOfBirth)
            dateOfBirth.set(
                Utils.instance.dateToString(
                    Utils.instance.stringToDate(
                        user.dateOfBirth!!,
                        "yyyy-MM-dd'T'HH:mm:ss"
                    )!!, "MMM d, ''yy"
                )
            )
        }
        if (user.nationality == 0) {
            nationality.set(0)
        } else nationality.set(1)

        employeeType.set(user.employeType ?: -1)
        monthlyIncome.set(if (user.salary == "-1") "" else user.salary)

    }

    fun checkData() {
        if (name.get().isNullOrEmpty()) {
            validName.value = false
            return
        } else validName.value = true
        if (dateOfBirth.get().isNullOrEmpty()) {
            validDateOFBirth.value = false
            return
        } else validDateOFBirth.value = true

        if (gender.value == -1) {
            validGender.value = false
            return
        } else validGender.value = true

        if (phone.get()?.isValidPhoneNumber() == false) {
            validPhone.value = false
            return
        } else validPhone.value = true

        if (civilNumber.get().isNullOrEmpty() || civilNumber.get()?.length != 12) {
            validCivilNumber.value = false
            return
        } else validCivilNumber.value = true
        if (nationality.get() == -1) {
            validNationality.value = false
            return
        } else validNationality.value = true
        if (retired.get() == -1) {
            validEmpStatu.value = false
            return
        } else validEmpStatu.value = true

        if (retired.get() == 0) {
            if (employeeType.get() == -1) {
                validEmployeeType.value = false
                return
            } else validEmployeeType.value = true
            if (companyName.get().isNullOrEmpty()) {
                validCompanyName.value = false
                return
            } else validCompanyName.value = true
            if (industry.get().isNullOrEmpty()) {
                validIndustry.value = false
                return
            } else validIndustry.value = true
        }

        if (monthlyIncome.get().isNullOrEmpty()) {
            validMonthlyIncome.value = false
            return
        } else validMonthlyIncome.value = true
        checkEligibility()
    }

    private fun checkEligibility() {
        updateProfile()
        if (bank?.allowLoanToExpat == true || (bank?.allowLoanToExpat == false && nationality.get() == 0)) {
            if (bank?.allowLoanToPrivateSector == true || (bank?.allowLoanToPrivateSector == false && employeeType.get() == 0)) {
                if (allowedIndustry()) {
                    if (isValidSalary(bank!!)) {
                        isEligible.value = true
                        success.value = true
//                        filteredBanks.value = filterBankList()
                        return
                    }

                }

            }

        }
        error.value = "1"
        filteredBanks.value = filterBankList()

    }

    private fun filterBankList(): List<Bank>? {
        val filteredArray = arrayListOf<Bank>()
        banks.value?.forEach { bank ->
            if (bank.allowLoanToExpat == true || (bank.allowLoanToExpat == false && nationality.get() == 0)) {
                if (bank.allowLoanToPrivateSector == true || (bank.allowLoanToPrivateSector == false && employeeType.get() == 0)) {
                    if (allowedIndustry()) {
                        if (isValidSalary(bank)) {
                            filteredArray.add(bank)
                        }

                    }

                }
            }
        }

        return filteredArray
    }

    private fun isValidSalary(bank: Bank): Boolean {
        monthlyIncome.get()?.let {
            return it.toInt() >= bank.loanMinimumSalary
        }
        return false
    }

    fun updateProfile() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        } else {
            var json = JsonObject()
            var phoneJson = JsonObject()
            phoneJson.addProperty("code", "+965")
            phoneJson.addProperty("number", user.phone?.number)
            json.add("phone", phoneJson)
            json.addProperty("civilID", civilNumber.get())
            json.addProperty("fullName", user.fullName)
            json.addProperty("gender", gender.value)
            json.addProperty("dateOfBirth", selectedDateOfBirth.get())

            json.addProperty("nationality", if (nationality.get() == 0) 0 else 1)
            json.addProperty("isRetired", retired.get() == 1)
            if (retired.get() == 0) {
                json.addProperty("employeType", employeeType.get())
                json.addProperty("companyName", companyName.get())
                json.addProperty("industry", "${industry.get()}")
            }
            json.addProperty("salary", monthlyIncome.get()?.toInt())
            bank?.let {json.addProperty("bank", "${it.id}")}
            disposable.add(
                service.updateUserInformation(prefs.string(USER_ID)!!, json)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                        override fun onSuccess(obj: BaseResponse<User>) {
                            obj.Result?.let { t->
                                local.realm.executeTransaction {
                                    it.copyToRealmOrUpdate(t)
                                }
//                            updateLocaleUser(t)
                                loading.value = false
                                // success.value = true
                            }
                        }

                        override fun onError(e: Throwable) {
                            loading.value = false
                            Log.e("onErrore", "onError: ${e.localizedMessage} ")
                            // error.value = "error"
                        }

                    })
            )
        }
    }

    private fun allowedIndustry(): Boolean {
        bank?.industries?.forEach {
            if (it.id == industry.get()) {
                return true
            }
        }
        return false
    }

    fun getIndustries() {
         local.getAll<Industry>()?.let{
            industries.value = it
        }


        disposable.add(
            service.getIndustries()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ArrayList<Industry>>() {
                    override fun onSuccess(t: ArrayList<Industry>) {
                        if (t.isNotEmpty()) {
                            industries.value = t
                            saveIndustries(t)
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                })
        )
    }

    private fun saveIndustries(t: ArrayList<Industry>) {
        local.save(t) {

        }
    }
}