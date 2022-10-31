package com.katiearose.sobriety.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.R
import com.katiearose.sobriety.SavingsAdapter
import com.katiearose.sobriety.databinding.ActivitySavingsBinding
import com.katiearose.sobriety.databinding.DialogAddSavingBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.isInputEmpty
import com.katiearose.sobriety.utils.showConfirmDialog
import com.katiearose.sobriety.utils.toggleVisibility
import java.time.LocalTime

class Savings : AppCompatActivity() {

    private lateinit var binding: ActivitySavingsBinding
    private lateinit var addiction: Addiction
    private lateinit var cacheHandler: CacheHandler
    private lateinit var adapter: SavingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cacheHandler = CacheHandler(this)

        val pos = intent.extras!!.getInt(Main.EXTRA_ADDICTION_POSITION)
        addiction = Main.addictions[pos]
        updateSavedTimeDisplay()

        binding.btnEditTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(R.string.select_time_saved)
                .setTimeFormat(CLOCK_24H)
                .build()
            timePicker.addOnPositiveButtonClickListener {
                addiction.timeSaving = LocalTime.of(timePicker.hour, timePicker.minute)
                cacheHandler.writeCache()
                updateSavedTimeDisplay()
            }
            timePicker.show(supportFragmentManager, null)
        }
        binding.btnExpandCollapseTime.setOnClickListener {
            binding.timeSavedCard.toggleVisibility()
            binding.btnExpandCollapseTime.apply {
                setImageResource(if (binding.timeSavedCard.visibility == View.VISIBLE) R.drawable.expand_less_24px else R.drawable.expand_more_24px)
                contentDescription = if (binding.timeSavedCard.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    tooltipText = if (binding.timeSavedCard.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
            }
        }

        binding.btnAddOther.setOnClickListener { showAddSavingDialog(null) }
        binding.btnExpandCollapseOther.setOnClickListener {
            binding.otherSavingsList.toggleVisibility()
            binding.btnExpandCollapseOther.apply {
                setImageResource(if (binding.otherSavingsList.visibility == View.VISIBLE) R.drawable.expand_less_24px else R.drawable.expand_more_24px)
                contentDescription = if (binding.otherSavingsList.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    tooltipText = if (binding.otherSavingsList.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
            }
        }

        adapter = SavingsAdapter(addiction, this)
        adapter.apply {
            setOnButtonEditClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                showAddSavingDialog(addiction.savings.toList()[pos])
            }
            setOnButtonDeleteClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addiction.savings.remove(addiction.savings.toList()[pos].first)
                    updateSavingsList()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_saving_confirm, addiction.savings.toList()[pos].first), action)
            }
        }
        binding.otherSavingsList.layoutManager = LinearLayoutManager(this)
        binding.otherSavingsList.adapter = adapter
    }

    private fun showAddSavingDialog(existingSaving: Pair<String, Pair<Double, String>>?) {
        var dialogViewBinding: DialogAddSavingBinding? = DialogAddSavingBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogViewBinding!!.root)
        if (existingSaving != null) {
            dialogViewBinding.nameStr.visibility = View.GONE
            dialogViewBinding.savingsNameInputLayout.visibility = View.GONE
            dialogViewBinding.savingsAmountInput.setText(existingSaving.second.first.toString())
            dialogViewBinding.unitInput.setText(existingSaving.second.second)
        }
        dialogViewBinding.btnSaveSaving.setOnClickListener {
            if (dialogViewBinding!!.savingsAmountInput.isInputEmpty()) {
                dialogViewBinding!!.savingsAmountInputLayout.error = getString(R.string.error_empty_amount)
            } else if (dialogViewBinding!!.unitInput.isInputEmpty()) {
                dialogViewBinding!!.unitInputLayout.error = getString(R.string.error_empty_unit)
            } else {
                if (existingSaving != null) {
                    addiction.savings[existingSaving.first] = Pair(dialogViewBinding!!.savingsAmountInput.text.toString().toDouble(), dialogViewBinding!!.unitInput.text.toString())
                    updateSavingsList()
                    dialog.dismiss()
                } else {
                    if (dialogViewBinding!!.savingsNameInput.isInputEmpty()) {
                        dialogViewBinding!!.savingsNameInputLayout.error = getString(R.string.error_empty_name)
                    } else {
                        addiction.savings[dialogViewBinding!!.savingsNameInput.text.toString()] = Pair(dialogViewBinding!!.savingsAmountInput.text.toString().toDouble(), dialogViewBinding!!.unitInput.text.toString())
                        updateSavingsList()
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.setOnDismissListener { dialogViewBinding = null }
        dialog.show()
    }

    private fun updateSavingsList() {
        cacheHandler.writeCache()
        adapter.update()
    }

    private fun updateSavedTimeDisplay() {
        binding.timeSaved.text = if (addiction.timeSaving.hour == 0 && addiction.timeSaving.minute == 0) getString(R.string.no_set)
            else getString(R.string.hours_minutes, addiction.timeSaving.hour, addiction.timeSaving.minute)
    }
}