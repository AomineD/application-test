package com.test.applicationtest.ui.dialog

import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.test.applicationtest.R
import com.test.applicationtest.databinding.PopUpAddTicketBinding
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.model.Ticket
import com.wineberryhalley.bclassapp.BottomBaseShet
import kotlinx.coroutines.CoroutineScope
import java.util.Calendar

class AddTicketPopUp(context: Context, val callback: suspend (CoroutineScope.(Ticket) -> Unit)):BottomBaseShet(), DatePicker.OnDateChangedListener {


    /**
     * We validate current form state, changing color and helper text when any input is empty
     */
    private fun validateForm(){

        val notEmpty = customerName.isNotEmpty() && customerAddress.isNotEmpty()
        val colorBtn = ContextCompat.getColor(requireContext(), if(notEmpty) R.color.primary else R.color.medium_gray)
        binding.btnAdd.isEnabled = notEmpty
        binding.btnAdd.setCardBackgroundColor(colorBtn)
        binding.txtCustomerName.helperText = if(customerName.isEmpty()) requireContext().getString(R.string.customer_empty) else ""
        binding.txtCustomerAddress.helperText = if(customerAddress.isEmpty()) requireContext().getString(R.string.address_empty) else ""

    }

    override fun layoutID(): Int {
        return R.layout.pop_up_add_ticket
    }

    private lateinit var binding:PopUpAddTicketBinding
    var millis:Long = 0
    private var customerName = ""
    private var customerAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_InputAddTheme)
    }

    override fun OnStart() {
        binding = PopUpAddTicketBinding.bind(requireView())
        binding.txtCustomerName.editText?.text.toString()
        val today = Calendar.getInstance()
        millis = System.currentTimeMillis()
        binding.ticketDate.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), this)

        binding.txtCustomerName.editText?.addTextChangedListener(
            afterTextChanged = {
                customerName = it.toString()
                validateForm()
            }
        )

        binding.txtCustomerAddress.editText?.addTextChangedListener(
            afterTextChanged = {
                customerAddress = it.toString()
                validateForm()
            }
        )

        binding.btnAdd.setOnClickListener {
            if(customerName.isEmpty() || customerAddress.isEmpty()){
                return@setOnClickListener
            }
            ioSafe {
                callback(getTicket())
            }
            dismiss()
        }

        val colorBtn = ContextCompat.getColor(requireContext(), R.color.medium_gray)
        binding.btnAdd.isEnabled = false
        binding.btnAdd.setCardBackgroundColor(colorBtn)

        binding.txtClose.setOnClickListener{
            dismiss()
        }

    }





    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, 12)

        millis = calendar.timeInMillis

    }

   private fun getTicket():Ticket{

        return Ticket(
            id = null,
            subtitle = "No subtitle",
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = "3113653547",
            myNote = "No note",
            ticketDate = millis,
        )
    }

}