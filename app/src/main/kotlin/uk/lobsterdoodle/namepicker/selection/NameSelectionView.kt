package uk.lobsterdoodle.namepicker.selection

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckedTextView
import android.widget.FrameLayout
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.databinding.NameSelectionViewBinding
import uk.lobsterdoodle.namepicker.model.Name

class NameSelectionView : FrameLayout {

    private val binding =
        NameSelectionViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val checkBox: CheckedTextView
        get() = binding.nameSelectionViewCheckbox

    lateinit var listener: CheckedChangeListener

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        App.get(context).component().inject(this)
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
