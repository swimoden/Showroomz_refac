package com.kuwait.showroomz.extras

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.material.textfield.TextInputLayout
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.MyApplication.Key.LANG
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.simplifier.*
import com.kuwait.showroomz.view.adapters.*
import com.kuwait.showroomz.viewModel.SearchVM
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


fun String?.isNullOrEmpty(): Boolean = (this == null || this.isEmpty())
fun RealmObject?.isNull(): Boolean = (this == null)

var isEnglish = Shared.prefs.getString(LANG, "") == "EN"

fun ImageView.load(uri: String?) {
    val requestOptions = RequestOptions()
    requestOptions.isMemoryCacheable
    requestOptions.placeholder(R.drawable.place_holder)


    Glide.with(context)
        .setDefaultRequestOptions(requestOptions)
        .load(uri)
        .into(this)
}

fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

fun Context.getStringS(name: String): String {
    return resources.getString(resources.getIdentifier(name, "string", packageName))
}


@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.load(url)
}

@BindingAdapter("android:imageUrlUser")
fun imageUrlUser(view: ImageView, url: String?) {
    val shimmer =
        Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
            .setDuration(1800) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.6f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }
    val requestOptions = RequestOptions()
    requestOptions.placeholder(shimmerDrawable)
    requestOptions.error(R.drawable.avatar_)


    Glide.with(view.context)
        .setDefaultRequestOptions(requestOptions)
        .load(url)
        .into(view)

}

@BindingAdapter("android:imageUrlWithShimmer")
fun loadImageWithShimmer(view: ImageView, url: String?) {
    val shimmer =
        Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
            .setDuration(1800) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.6f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }
    val requestOptions = RequestOptions()
    requestOptions.placeholder(shimmerDrawable)
    requestOptions.error(shimmerDrawable)


    Glide.with(view.context)
        .setDefaultRequestOptions(requestOptions)
        .load(url)
        .into(view)

}

fun has(mask: Int, bit: Int): Int {
    return mask and bit
}

fun Date.getCurrentDayIndex(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val d = LocalDate.now()
    val dow: DayOfWeek = d.dayOfWeek
    dow.value
} else {
    Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
}

fun Date.toStringDate(): String {
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale("en"))
    df.timeZone = TimeZone.getTimeZone("gmt")
    return df.format(Date())
}

fun Date.toStringDate(format: String): String {
    val df: DateFormat = SimpleDateFormat(format, Locale.getDefault())
    df.timeZone = TimeZone.getTimeZone("gmt")
    return df.format(Date())
}

fun getCurrentDateTime(): Date {
    return Date()
}

fun String?.isValidPhoneNumber(): Boolean {
    if (this == null)
        return false
    if (startsWith("1") || startsWith("2") || startsWith("3") || startsWith("4") || startsWith("7")
        || startsWith("8") || startsWith("0")
    )
        return false
    if (hasLettersAndDigits)
        return false
    if (trim().isEmpty() || length < 8 || length > 8)
        return false
    return true
}

@BindingAdapter("android:StringArray")
fun putStringArray(view: TextView, strings: RealmList<String>?) {
    var string = ""
    strings?.let {
        for (index in 0 until strings.size) {
            string += strings[index]
            if (index != strings.size - 1) {
                string += ", "
            }
        }
    }
    view.text = string
}

@BindingAdapter("android:marginStart")
fun setMargin(view: ConstraintLayout, boolean: Boolean) {

    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    if (!boolean) {
        layoutParams.marginStart = -12
    } else layoutParams.marginStart = 0

    view.layoutParams = layoutParams


}

@BindingAdapter("android:actionsAdapter")
fun setActionAdapter(recyclerView: RecyclerView, actions: RealmList<Action>) {
    var adapter =
        ActionsAdapter(actions, 0, recyclerView.context.resources.getColor(R.color.colorPrimary))
    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
    recyclerView.adapter = adapter


}

@BindingAdapter("android:trimAdapter", "android:model")
fun setSpecsAdapter(recyclerView: RecyclerView, specs: List<Spec>, model: ModelSimplifier) {

    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    val array = arrayListOf<Spec>()
    array.addAll(specs.sortedBy { SpecSimplifier(it).position })
    val array2 = arrayListOf<Spec>()
    array.forEach{
        if (SpecSimplifier(it).contents.isNotEmpty()) {
            /*SpecSimplifier(it).contents.forEach{
                SpecContentSimplifier(it).value?.let { it1 -> Log.e("texxxxxt", it1) }
            }*/
            array2.add(it)
        }
    }

    recyclerView.adapter = TrimSpecAdapter(array2, recyclerView.context, model)
}

@BindingAdapter("android:offerContent")
fun setOfferContent(layout: LinearLayout, strings: RealmList<String>) {
  val width = Resources.getSystem().displayMetrics.widthPixels
    layout.removeAllViewsInLayout()
    strings.forEach {
        val valueTV = TextView(layout.context)
        valueTV.width = (width * 0.75).toInt()
        valueTV.setTextColor(ContextCompat.getColor(layout.context, R.color.colorBlack))
        val typeface = ResourcesCompat.getFont(layout.context, R.font.cairo_regular)
        valueTV.typeface = typeface
        valueTV.textSize = 16f
        valueTV.text = "• $it"
        layout.addView(valueTV)
    }

}

@BindingAdapter("android:bankAdapter")
fun setBanks(recyclerView: RecyclerView, banks: RealmList<Bank>) {

    recyclerView.layoutManager =
        LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
    recyclerView.adapter = OffersBanksAdapter(banks, recyclerView.context)
}

@BindingAdapter("android:textContract")
fun setSpecsAdapter(textView: TextView, int: Int) {
    when (int) {
        0 -> {
            textView.text = ""
        }
        1 -> {
            textView.text =
                int.toString() + " " + textView.context.resources.getString(R.string.day_contract)
        }
        7 -> {
            textView.text =
                int.toString() + " " + textView.context.resources.getString(R.string.week_contract)
        }
        30 -> {
            textView.text =
                int.toString() + " " + textView.context.resources.getString(R.string.month_contract)
        }
        365 -> {
            textView.text =
                int.toString() + " " + textView.context.resources.getString(R.string.year_contract)
        }
        else -> {
            run {
                textView.text =
                    int.toString() + " " + textView.context.resources.getString(R.string.month_contract)
            }
        }
    }

}

@BindingAdapter("android:adapter_program")
fun setProgramsAdapter(recyclerView: RecyclerView, programs: RealmList<Program>) {

    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = TrimProgramAdapter(programs, recyclerView.context)
}

@BindingAdapter("android:adapter_service")
fun setServicesAdapter(recyclerView: RecyclerView, services: RealmList<Service>) {

    recyclerView.layoutManager =
        LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
    recyclerView.adapter = TrimServiceAdapter(services)
}

@BindingAdapter("android:colorItem", "android:actions")
fun putActions(linearLayout: LinearLayout, @ColorInt color: Int, actions: List<Action>?) {
    actions?.let{
        linearLayout.removeAllViews()
        actions.forEach {
            if (actions.indexOf(it) < 6) {
                val inflater = LayoutInflater.from(linearLayout.context)
                val view =
                    DataBindingUtil.inflate<com.kuwait.showroomz.databinding.ActionItemBinding>(
                        inflater,
                        R.layout.action_item,
                        linearLayout,
                        false
                    )
                view.actionIconContainer.background = DesignUtils.instance.createDrawableWithStrock(
                    Color.BLACK, color,
                    GradientDrawable.OVAL
                )
                view.action = it.let { action -> ActionSimplifier(action) }
                view.isTheFirst = actions.indexOf(it) == 0

                linearLayout.addView(view.root)
            }
        }
    }
}


@BindingAdapter("android:ImageTint")
fun setImageTing(imageView: ImageView, boolean: Boolean) {
    if (boolean)
        imageView.setColorFilter(ContextCompat.getColor(imageView.context, R.color.colorWhite))
    else imageView.colorFilter = null


}

@BindingAdapter("android:holderBackground")
fun setHolderBackground(view: ConstraintLayout, boolean: Boolean) {
    if (boolean) {
        view.setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                R.color.colorLightGrayRow
            )
        )
    } else view.setBackgroundColor(
        ContextCompat.getColor(
            view.context,
            R.color.colorWhite
        )
    )
}

@BindingAdapter("android:InputError")
fun setInputError(input: TextInputLayout, boolean: Boolean) {
    input.error = if (boolean) " " else null

}

@BindingAdapter("android:toArabic")
fun setText(input: TextView, text: String) {
    input.text = if (!isEnglish) Utils.instance.convertToArabic(text) else text

}

fun String.convertToLocal():String{
    return if (!isEnglish)
        Utils.instance.convertToArabic(this).toString()
    else
        Utils.instance.convertToEnglish(this).toString()
}

@BindingAdapter("android:toArabicDate")
fun setDateText(input: TextView, text: Date) {
    val dateFormatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")

    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
    val dt = dateFormatter.format(text)
    input.text = dt

}

@BindingAdapter("android:onTextChange")
fun setOnTextChanged(view: EditText, viewModel: SearchVM) {
    view.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            viewModel.searchByName(s.toString())
        }

    })
}

fun <T> Parcel.readRealmList(clazz: Class<T>): RealmList<T>?
        where T : RealmModel,
              T : Parcelable = when {
    readInt() > 0 -> RealmList<T>().also { list ->
        repeat(readInt()) {
            list.add(readParcelable(clazz.classLoader))
        }
    }
    else -> null
}

fun <T> Parcel.writeRealmList(realmList: RealmList<T>?, clazz: Class<T>)
        where T : RealmModel,
              T : Parcelable {
    writeInt(
        when {
            realmList == null -> 0
            else -> 1
        }
    )
    if (realmList != null) {
        writeInt(realmList.size)
        for (t in realmList) {
            writeParcelable(t, 0)
        }
    }
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context!!.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun String.validateLetters(): Boolean {
    val regx = "^\\p{L}+(?: \\p{L}+)*\$"
    val pattern: Pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE)
    val matcher: Matcher = pattern.matcher(this)
    return matcher.find()
}

val String.containsLatinLetter: Boolean
    get() = matches(Regex(".*[A-Za-z].*"))

val String.containsDigit: Boolean
    get() = matches(Regex(".*[0-9].*"))

val String.isAlphanumeric: Boolean
    get() = matches(Regex("[A-Za-z0-9]*"))

val String.hasLettersAndDigits: Boolean
    get() = containsLatinLetter && containsDigit

val String.isIntegerNumber: Boolean
    get() = toIntOrNull() != null

val String.toDecimalNumber: Boolean
    get() = toDoubleOrNull() != null

val String.jsonObject: JSONObject?
    get() = try {
        JSONObject(this)
    } catch (e: JSONException) {
        null
    }

val String.jsonArray: JSONArray?
    get() = try {
        JSONArray(this)
    } catch (e: JSONException) {
        null
    }


fun JSONObject.getIntOrNull(name: String): Int? =
    try {
        getInt(name)
    } catch (e: JSONException) {
        val strValue = getStringOrNull(name)
        strValue?.toIntOrNull()
    }

fun JSONObject.getDoubleOrNull(name: String): Double? =
    try {
        getDouble(name)
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getLongOrNull(name: String): Long? =
    try {
        getLong(name)
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getStringOrNull(name: String): String? =
    try {
        getString(name).trim()
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getBooleanOrNull(name: String): Boolean? =
    try {
        getBoolean(name)
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getObjectOrNull(name: String): JSONObject? =
    try {
        getJSONObject(name)
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getArrayOrNull(name: String): JSONArray? =
    try {
        getJSONArray(name)
    } catch (e: JSONException) {
        null
    }

fun JSONObject.getArrayOrEmpty(name: String): JSONArray =
    try {
        getJSONArray(name)
    } catch (e: JSONException) {
        JSONArray()
    }

fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
    return JsonObjectBuilder().json(build)
}

class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.To(value: T) {
        deque.peek().put(this, value)
    }
}
fun Date.getDaysAgo(daysAgo: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

    return calendar.time
}
fun String.ToNonNullLong():Long{
    this.toLong().let {

    }
    return 0
}
fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()
 fun String.delimiter(): String {
     var x = this.replace(",", "").replace(".", "").filter { it.isDigit() }
     var result = this
     if (this.length > 3) {
         result = x.substring(0, x.length - 3) + "," + x.substring(
             x.length - 3,
             x.length
         )
     }
     return result

}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}
fun  ArrayList<Model>.toMSimplifier():ArrayList<ModelSimplifier>{
    val list:ArrayList<ModelSimplifier> = arrayListOf()
    this.forEach{
        list.add(ModelSimplifier(it))
    }
    return list
}

fun  ArrayList<Brand>.toBSimplifier():ArrayList<BrandSimplifier>{
    val list:ArrayList<BrandSimplifier> = arrayListOf()
    this.forEach{
        list.add(BrandSimplifier(it))
    }
    return list
}

fun String.toDate(): Date? {
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale("en"))
    df.parse(this)?.let{
        return it
    }
    return Date()
}

fun View.blockingClickListener(debounceTime: Long = 1200L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val timeNow = SystemClock.elapsedRealtime()
            val elapsedTimeSinceLastClick = timeNow - lastClickTime


            if (elapsedTimeSinceLastClick < debounceTime) {
                return
            }
            else {
                action()
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}