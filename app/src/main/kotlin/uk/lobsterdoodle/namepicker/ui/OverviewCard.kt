package uk.lobsterdoodle.namepicker.ui

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.PopupMenu
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.util.As
import uk.lobsterdoodle.namepicker.databinding.OverviewCardBinding
import uk.lobsterdoodle.namepicker.overview.OverviewCardActionsCallback
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData

class OverviewCard : CardView {

    private var binding =
        OverviewCardBinding.inflate(LayoutInflater.from(context), this, true)

    private val title: TextView
        get() = binding.overviewCardTitle

    private val count: TextView
        get() = binding.overviewCardCount

    private val overflow: ImageView
        get() = binding.overviewCardOverflow

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setCardBackgroundColor(getColor(context, android.R.color.white))
        useCompatPadding = true
        cardElevation = As.px(context, 2).toFloat()
    }

    fun bind(callback: OverviewCardActionsCallback, data: OverviewCardCellData) {
        title.text = data.listTitle
        count.text = "${data.nameCount} names"
        setOnClickListener { callback.launchSelectionScreen(data.groupId) }

        val popup = PopupMenu(context, overflow)
        popup.inflate(R.menu.menu_overview_card)
        popup.setOnMenuItemClickListener { item ->
            handle(item, data, callback)
            true
        }
        overflow.setOnClickListener { popup.show() }
    }

    private fun handle(
        item: MenuItem,
        data: OverviewCardCellData,
        callback: OverviewCardActionsCallback
    ) {
        when (item.itemId) {
            R.id.overview_card_overflow_edit_names -> callback.launchEditGroupNamesScreen(data.groupId)
            R.id.overview_card_overflow_edit_group -> callback.launchEditGroupDetailsScreen(data.groupId)
            R.id.overview_card_overflow_delete_group -> callback.deleteGroup(data.groupId)
        }
    }
}
