package com.kuwait.showroomz.extras

import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Program
import com.kuwait.showroomz.model.data.Trim

object CacheObjects {
    var model: Model?=null
    var trim: Trim?=null
    var program: Program?=null
    var bank: Bank?=null
    var availableBanks: List<Bank>?=null
    var selectedBanks: List<Bank>?=null
    var isEligible:Boolean = true
}