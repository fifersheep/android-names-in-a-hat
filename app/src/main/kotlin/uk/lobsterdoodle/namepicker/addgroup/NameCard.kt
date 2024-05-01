package uk.lobsterdoodle.namepicker.addgroup

import android.content.Context
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.util.As
import uk.lobsterdoodle.namepicker.databinding.NameCardBinding
import uk.lobsterdoodle.namepicker.edit.NameCardActions
import uk.lobsterdoodle.namepicker.model.Name

class NameCard : CardView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding: NameCardBinding =
        NameCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setCardBackgroundColor(context.resources.getColor(android.R.color.white))
        useCompatPadding = true
        cardElevation = As.px(context, 2).toFloat()
    }

    fun bind(nameCardActions: NameCardActions, name: Name) {
        binding.nameCardName.text = name.name
        binding.nameCardDeleteIcon.setOnClickListener { nameCardActions.deleteName(name.id) }
    }
}
