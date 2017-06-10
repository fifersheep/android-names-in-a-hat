package uk.lobsterdoodle.namepicker.selection

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckedTextView
import android.widget.FrameLayout

import butterknife.BindView
import butterknife.ButterKnife
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.model.Name

class NameSelectionView : FrameLayout {

    @BindView(R.id.name_selection_view_checkbox)
    lateinit var checkBox: CheckedTextView

    lateinit var listener: CheckedChangeListener

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        App.get(context).component().inject(this)
        ButterKnife.bind(this, LayoutInflater.from(context).inflate(R.layout.name_selection_view, this, true))
        setOnClickListener {
            checkBox.toggle()
            listener.onCheckedChanged(checkBox.isChecked)
        }
    }

    fun bind(name: Name, listener: CheckedChangeListener) {
        this.listener = listener
        checkBox.text = name.name
        checkBox.isChecked = name.toggledOn
    }
}
