package uk.lobsterdoodle.namepicker.addgroup

import android.content.Context
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.util.As
import uk.lobsterdoodle.namepicker.edit.NameCardActions
import uk.lobsterdoodle.namepicker.model.Name

class NameCard : CardView {

    @BindView(R.id.name_card_name)
    lateinit var name: TextView

    @BindView(R.id.name_card_delete_icon)
    lateinit var delete: ImageView

    constructor(context: Context) : super(context) { initialise() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialise() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialise() }

    private fun initialise() {
        ButterKnife.bind(this, LayoutInflater.from(context).inflate(R.layout.name_card, this, true))
        setCardBackgroundColor(context.resources.getColor(android.R.color.white))
        useCompatPadding = true
        cardElevation = As.px(context, 2).toFloat()
    }

    fun bind(nameCardActions: NameCardActions, name: Name) {
        this.name.text = name.name
        delete.setOnClickListener { nameCardActions.deleteName(name.id) }
    }
}
