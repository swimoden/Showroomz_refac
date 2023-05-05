package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.ModelResponse
import com.kuwait.showroomz.model.local.LocalRepo

class CallbackSimplifier(val callback: Callback) {
    private val local = LocalRepo()
    var modelData = callback.modelData?.id?.let { local.getOne<Model>(it) }
    var model = modelData?.let { ModelSimplifier(it) }
    var bankData = callback.bank?.id?.let{ local.getOne<Bank>(it)}
    var bank = BankSimplifier(bankData)

    var date = Utils.instance.dateToString(callback.createdAt?.let {
        Utils.instance.stringToDate(
            it,
            "yyyy-MM-dd'T'HH:mm:ss"
        )
    }!!, "dd/MM/yyyy hh:mm a")
    var status = callback.status
    var totalCost=callback.totalCost
    var downpayment=callback.downpayment
    var loanAmount=callback.loanAmount
    var installmentPeriod=callback.installmentPeriod
    var installmentAmount=callback.installmentAmount
    var profit=callback.profit
    var salary_doc = callback.salaryCertificate
    var aka = callback.akamathopia
    var bankStatement = callback.monthsBankStatement
    var bankAccountNumber = callback.bankAccountNumber
    var civilId = if(!callback.civilID.isNullOrEmpty()){
        callback.civilID
    }else{
        callback.client?.civilID
    }
    var isPrivate =callback.isPrivate
    var appliedDate = Utils.instance.dateToString(callback.createdAt?.let {
        Utils.instance.stringToDate(
            it,
            "yyyy-MM-dd'T'HH:mm:ss"
        )
    }!!, "dd MMM yyyy")
    var processStatus= callback.processStatus

}