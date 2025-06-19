package com.example.myapplication.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.interfaces.QuestionOptionDto

class AnswersAdapter(
    private val options: Array<QuestionOptionDto>,
    private val isMultipleChoice: Boolean,
    private val onSelected: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastCheckedPosition = -1

    companion object {
        private const val VIEW_TYPE_CHECKBOX = 0
        private const val VIEW_TYPE_RADIO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isMultipleChoice) VIEW_TYPE_CHECKBOX else VIEW_TYPE_RADIO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CHECKBOX) {
            val checkBoxParams = TextViewCompat.getTextMetricsParams(CheckBox(parent.context))
            CheckBoxViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_checkbox, parent, false),
                checkBoxParams
            )
        } else {
            val radioButtonParams = TextViewCompat.getTextMetricsParams(RadioButton(parent.context))
            RadioViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_radio, parent, false),
                radioButtonParams
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CheckBoxViewHolder -> holder.bind(options[position])
            is RadioViewHolder -> holder.bind(options[position], position)
        }
    }

    override fun getItemCount(): Int = options.size

    inner class CheckBoxViewHolder(itemView: View, private val textParams: PrecomputedTextCompat.Params) : RecyclerView.ViewHolder(itemView) {
        fun bind(option: QuestionOptionDto) {
            (itemView as CheckBox).apply {
                text = PrecomputedTextCompat.create(option.response_text, textParams)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) onSelected(adapterPosition)
                }
            }
        }
    }

    inner class RadioViewHolder(itemView: View, private val textParams: PrecomputedTextCompat.Params) : RecyclerView.ViewHolder(itemView) {
        fun bind(option: QuestionOptionDto, position: Int) {
            (itemView as RadioButton).apply {
                text = PrecomputedTextCompat.create(option.response_text, textParams)
                isChecked = position == lastCheckedPosition

                setOnClickListener {
                    val prevPosition = lastCheckedPosition
                    lastCheckedPosition = adapterPosition
                    notifyItemChanged(prevPosition)
                    notifyItemChanged(lastCheckedPosition)
                    onSelected(adapterPosition)
                }
            }
        }
    }
}
