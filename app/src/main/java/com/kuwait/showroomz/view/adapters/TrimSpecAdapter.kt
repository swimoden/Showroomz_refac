package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.TrimSpecItemBinding
import com.kuwait.showroomz.extras.capitalizeFirstLetter
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Spec
import com.kuwait.showroomz.model.data.Translation
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.SpecSimplifier
import com.kuwait.showroomz.model.simplifier.TrimTranslationSimplifier
import java.util.*


class TrimSpecAdapter(
    private val spec: List<Spec>,
    val context: Context,
    val model: ModelSimplifier
) :
    RecyclerView.Adapter<TrimSpecAdapter.SpecViewHolder>() {
    var isArabic = !isEnglish
    inner class SpecViewHolder(var view: TrimSpecItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(spec: Spec?) {
            view.s = spec?.let { SpecSimplifier(it) }
            if (isArabic)
                view.specType.textAlignment =
                    if (Locale.getDefault().language == "ar") TEXT_ALIGNMENT_VIEW_START else TEXT_ALIGNMENT_VIEW_END
            else if (Locale.getDefault().language == "ar") TEXT_ALIGNMENT_VIEW_END else TEXT_ALIGNMENT_VIEW_START

            spec?.contents?.forEach { labelText ->
                val parent = ConstraintLayout(context)
                parent.layoutParams =
                    ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                parent.layoutDirection =
                    if (isArabic) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

                val guideline = Guideline(context)
                val lp = ConstraintLayout.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT
                )
                guideline.id = View.generateViewId()
                guideline.layoutDirection =
                    if (isArabic) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
                lp.orientation = ConstraintLayout.LayoutParams.VERTICAL
                guideline.layoutParams = lp
                parent.addView(guideline)
                guideline.setGuidelinePercent(0.5f)

                val label =
                    labelText.translations?.let { it1 -> TrimTranslationSimplifier(it1).label }
                val valueTV = TextView(context)
                valueTV.isSingleLine = false

                label?.let { it ->
                    if (it != "" && it != " ") {

                        valueTV.setTextColor(
                            model.brand?.category?.setBgColor()?:ContextCompat.getColor(
                                context, R.color.colorPrimary

                            )

                        )

                        val typeface = ResourcesCompat.getFont(context, R.font.cairo_regular)
                        valueTV.typeface = typeface
                        valueTV.text = it.capitalizeFirstLetter()
                        valueTV.maxLines = 10
                        val lp: ConstraintLayout.LayoutParams =
                            ConstraintLayout.LayoutParams(0, WRAP_CONTENT)
                        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        if (isArabic) {
                            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                            lp.startToEnd = guideline.id
                        } else {
                            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                            lp.endToStart = guideline.id
                        }


                        valueTV.apply {
                            layoutParams = lp
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                        }

                        valueTV.setPadding(15, 15, 15, 0)
                        if ((labelText.isMultiple == true && labelText.isChecked == true) || labelText.isMultiple == false)
                            parent.addView(valueTV)
                    }
                }
                if (labelText.isChoice == true && labelText.isMultiple!!) {

                    valueTV.text = labelText.translations?.let { it1 ->
                        TrimTranslationSimplifier(
                            it1
                        ).value?.capitalizeFirstLetter()
                    }
                    val img = ImageView(context)
                    img.setImageDrawable(
                        if (labelText.isChecked == true) ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_tick
                        ) else ContextCompat.getDrawable(context, R.drawable.ic_cross)
                    )
                    val lp: ConstraintLayout.LayoutParams =
                        ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

                    lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    if (isArabic) {
                        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID

                    } else {
                        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

                    }

                    img.apply {
                        layoutParams = lp
                        setPadding(30, 15, 30, 0)
                    }
                    if (labelText.isChecked == true)
                    parent.addView(img)

                } else {
                    val value = labelText.translations?.let { it1 -> TrimTranslationSimplifier(it1).value }

                    if (value.isNullOrEmpty() || value == "-") {
                        parent.isVisible = false
                    }

                    value?.let { it ->
                        if (it != "" && it != " ") {
                            val valueTV = TextView(context)
                            valueTV.textDirection = if (isEnglish) TEXT_DIRECTION_LTR else TEXT_DIRECTION_RTL
                            //valueTV.textAlignment = TEXT_ALIGNMENT_TEXT_START
                            valueTV.isSingleLine = false
                            valueTV.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colorSlideMenuBackground
                                )
                            )
                            val typeface = ResourcesCompat.getFont(context, R.font.cairo_regular)
                            valueTV.typeface = typeface
                            valueTV.text = it

                            val lp: ConstraintLayout.LayoutParams =  ConstraintLayout.LayoutParams(0, WRAP_CONTENT)
                            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID

                            if (isArabic) {
                                lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                            } else {
                                lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                            }

                            if (label != "" && label != " " && label != null) {
                                if (isArabic) {
                                    lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                                    lp.endToStart = guideline.id
                                } else {
                                    lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                                    lp.startToEnd = guideline.id
                                }
                            } else {
                                if (isArabic) {
                                    lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                                } else {
                                    lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                                }
                            }

                            valueTV.apply {
                                layoutParams = lp
                                if (label != "" && label != " " && label != null)
                                    textAlignment = TEXT_ALIGNMENT_VIEW_END
                                else
                                    TEXT_ALIGNMENT_VIEW_START
                            }
                            valueTV.setPadding(30, 15, 30, 0)
                            parent.addView(valueTV)
                        }
                    }
                }
                view.specContentContainer.addView(parent)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecViewHolder {
        return SpecViewHolder(
            TrimSpecItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun getItemCount(): Int {
        return spec.size
    }
    override fun onBindViewHolder(holder: SpecViewHolder, position: Int) {
          holder.bind(spec[position])
    }
}