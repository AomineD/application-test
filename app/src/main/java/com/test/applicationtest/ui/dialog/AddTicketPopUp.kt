package com.test.applicationtest.ui.dialog

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.test.applicationtest.R
import com.test.applicationtest.databinding.PopUpAddTicketBinding
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.CoroutinesHelper.main
import com.test.applicationtest.helper.DataConverter.toDateAndTime
import com.test.applicationtest.model.Ticket
import com.wineberryhalley.bclassapp.BottomBaseShet
import kotlinx.coroutines.CoroutineScope

class AddTicketPopUp(val ticketEdit:Ticket? = null,val callback: suspend (CoroutineScope.(Ticket) -> Unit)):BottomBaseShet() {


    /**
     * We validate current form state, changing color and helper text when any input is empty
     */
    private fun validateForm():Boolean = with(binding){
        // --- set helper text
        txtCustomerName.helperText = if(customerName.isEmpty()) requireContext().getString(R.string.customer_empty) else ""
        txtCustomerAddress.helperText = if(customerAddress.isEmpty()) requireContext().getString(R.string.address_empty) else ""
        // --- set helper text
        txtCustomerPhone.helperText = if(customerPhone.isEmpty()) requireContext().getString(R.string.phone_empty) else ""
        datePicker.helperText = if(ticketDateText.isEmpty()) requireContext().getString(R.string.date_empty) else ""
        // return validate form
        return customerPhone.isNotEmpty() && ticketDateText.isNotEmpty() && customerName.isNotEmpty() && customerAddress.isNotEmpty()
    }

    private fun validateButton() = with(binding){
        val notEmpty = customerName.isNotEmpty() && customerAddress.isNotEmpty()
        customerPhone.isNotEmpty() && ticketDateText.isNotEmpty() && ticketNotes.isNotEmpty() && subtitleTicket.isNotEmpty()
        val colorBtn = ContextCompat.getColor(requireContext(), if(notEmpty) R.color.primary else R.color.medium_gray)
        btnAdd.isEnabled = notEmpty
        btnAdd.setCardBackgroundColor(colorBtn)
    }

    override fun layoutID(): Int {
        return R.layout.pop_up_add_ticket
    }

    private lateinit var binding:PopUpAddTicketBinding
    // Default Ticket fields
    var millis:Long = 0
    private var customerName = ""
    private var customerAddress = ""
    private var customerPhone = ""
    private var ticketDateText = ""
    private var subtitleTicket = "No subtitle"
    private var ticketNotes = "No notes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_InputAddTheme)
    }

    override fun OnStart() {
        binding = PopUpAddTicketBinding.bind(requireView())
        binding.txtCustomerName.editText?.text.toString()

        textChangedConfigs()
        configButtons()

        val colorBtn = ContextCompat.getColor(requireContext(), R.color.medium_gray)
        binding.btnAdd.isEnabled = false
        binding.btnAdd.setCardBackgroundColor(colorBtn)

        ticketEdit?.let {
            configEditing(it)
        }
    }

    private fun configEditing(ticket: Ticket) = with(binding){
        val title = String.format(getString(R.string.edit_ticket), ticket.id)
        titlePopUp.text = title
        txtCustomerName.editText!!.setText(ticket.customerName)
        txtSubtitle.editText!!.setText(ticket.subtitle)
        txtNotes.editText!!.setText(ticket.myNote)
        txtCustomerPhone.editText!!.setText(ticket.customerPhone)
        txtCustomerAddress.editText!!.setText(ticket.customerAddress)
        datePicker.editText!!.setText(ticket.ticketDate.toDateAndTime())
    }

    private fun configButtons() = with(binding){
        btnAdd.setOnClickListener {
            val isValid = validateForm()
            if(isValid) {
                ioSafe {
                    callback(getTicket())
                }
                dismiss()
            }
        }
        txtClose.setOnClickListener{
            dismiss()
        }

        datePicker.editText?.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                return@setOnFocusChangeListener
            }
            v.clearFocus()
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() = with(binding){
        activity?.let {
            val popup = DateTimePickerPopUp{ mill ->
                millis = mill
                main {
                    datePicker.editText!!.setText(mill.toDateAndTime())
                    requestNewSize()
                }
            }
            popup.show(it.supportFragmentManager, "dtpcik")
        }
    }


    private fun textChangedConfigs() = with(binding) {
        txtCustomerName.editText?.addTextChangedListener(
            afterTextChanged = {
                customerName = it.toString()
                validateButton()
            }
        )

        txtCustomerAddress.editText?.addTextChangedListener(
            afterTextChanged = {
                customerAddress = it.toString()
                validateButton()
            }
        )

        txtCustomerPhone.editText?.addTextChangedListener(
            afterTextChanged = {
                customerPhone = it.toString()
                validateButton()
            }
        )

        txtSubtitle.editText?.addTextChangedListener(
            afterTextChanged = {
                subtitleTicket = it.toString()
                validateButton()
            }
        )

        txtNotes.editText?.addTextChangedListener(
            afterTextChanged = {
                ticketNotes = it.toString()
                validateButton()
            }
        )

        datePicker.editText?.addTextChangedListener(
            afterTextChanged = {
                ticketDateText = it.toString()
                validateButton()
            }
        )
    }


    private fun getTicket():Ticket{

        return Ticket(
            id = null,
            subtitle = subtitleTicket,
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone,
            myNote = ticketNotes,
            ticketDate = millis,
        )
    }

}