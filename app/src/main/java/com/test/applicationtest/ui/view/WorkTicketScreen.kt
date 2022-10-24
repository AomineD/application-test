package com.test.applicationtest.ui.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.github.zawadz88.materialpopupmenu.MaterialPopupMenu
import com.github.zawadz88.materialpopupmenu.popupMenuBuilder
import com.google.android.material.tabs.TabLayout
import com.test.applicationtest.R
import com.test.applicationtest.databinding.ActivityWorkTicketScreenBinding
import com.test.applicationtest.databinding.LayoutAddressNotesBinding
import com.test.applicationtest.databinding.LayoutCustomerNameAndDateBinding
import com.test.applicationtest.databinding.LayoutDispatchNotesBinding
import com.test.applicationtest.helper.ActivityUtils.fullScreen
import com.test.applicationtest.helper.CoroutinesHelper.main
import com.test.applicationtest.helper.DataConverter.toFormat
import com.test.applicationtest.helper.ViewHelper.playLimit
import com.test.applicationtest.model.Ticket
import com.test.applicationtest.ui.dialog.AddTicketPopUp
import com.test.applicationtest.ui.viewmodel.WorkTViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay

@AndroidEntryPoint
class WorkTicketScreen : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    companion object{
        var ticketId:Int? = null
        fun openDetailsFor(context: Context,id:Int){
            ticketId = id
            context.startActivity(Intent(context, WorkTicketScreen::class.java))
        }
    }

    private val viewModel:WorkTViewModel by viewModels()
    private lateinit var binding: ActivityWorkTicketScreenBinding
    private lateinit var bindingHeader:LayoutCustomerNameAndDateBinding
    private lateinit var bindingAddress:LayoutAddressNotesBinding
    private lateinit var bindingDispatchNote:LayoutDispatchNotesBinding
    private var actualTicket:Ticket? = null
    private var actualNote = ""
    private var popMenu:MaterialPopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkTicketScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()

        if(ticketId == null){
            finish()
        }
        configViews()
        configTabLayout()
        viewModel.onCreate(ticketId!!)
        observeData()
        configNotes()
    }


    /**
     * New Feature, when you write something different to actual ticket note, save button will be appear
     */
    private fun configNotes() {
        bindingDispatchNote.myNotes.addTextChangedListener {
            bindingDispatchNote.saveNote.isVisible = actualTicket!!.myNote != it.toString()
            actualNote = it.toString()
        }
    }

    private fun configViews() {
        bindingHeader = binding.customerLay
        bindingAddress = binding.addressLay
        bindingDispatchNote = binding.notesLay

        binding.menuAnimation.speed = 2f
    }

    private fun observeData() {
        viewModel.ticket.observe(this) {
            if (it != null) {
                configTicketUI(it)
            }else {
               val toast = Toasty.error(this@WorkTicketScreen, R.string.something_went_wrong)
               toast.setGravity(Gravity.TOP, 0, 0)
               toast.show()
            }
        }
    }

    private fun configTicketUI(ticket: Ticket) {
actualTicket = ticket
        main {
            //Set header
            bindingHeader.customerNameText.text = ticket.customerName
            bindingHeader.customerPhoneText.text = ticket.customerPhone
            bindingHeader.dateText.text = ticket.ticketDate.toFormat()

            //Set direction
            bindingAddress.tvAddress.text = ticket.customerAddress
            bindingAddress.getDirectionsBtn.setOnClickListener(clickDirection())
            //Set notes if have

            bindingDispatchNote.myNotes.setText(ticket.myNote)

            bindingDispatchNote.saveNote.setOnClickListener{

              actualTicket =  actualTicket!!.copy(myNote = actualNote)
              updateTicket()
              bindingDispatchNote.myNotes.clearFocus()
            }

            val str = "Ticket #${ticket.id}"
            binding.reasonCallLayout.ticketId.text = str

            binding.btnBack.setOnClickListener{
                finish()
            }

            binding.btnMenu.setOnClickListener {
                openMenu()
            }
        }

    }

    private fun openMenu() {
           binding.menuAnimation.playLimit(0, 85)
           val popupMenu =
               popupMenuBuilder {
                   section {
                       item {
                           label = getString(R.string.get_directions)
                           callback = {
                              clickDirection().onClick(null)
                           }
                       }
                       item {
                           label = getString(R.string.dashboard_txt)
                           callback = {
                               finish()
                           }
                       }
                       item {
                           label = getString(R.string.edit_ticket_txt)
                           callback = {
                               editTicketOn()
                           }
                       }
                       item {
                           label = getString(R.string.delete_ticket)
                           callback = {
                               viewModel.deleteTicket {
                                   main {
                                       val toast = Toasty.info(this@WorkTicketScreen, R.string.ticket_deleted)
                                       toast.setGravity(Gravity.TOP, 0, 30)
                                       toast.show()
                                       finish()
                                   }
                               }
                           }
                       }
                   }
               }
           popMenu =  popupMenu.build()
           popMenu!!.setOnDismissListener {
               popMenu = null
               binding.menuAnimation.playLimit(85, 140)
           }
           popMenu!!.show(this@WorkTicketScreen, binding.menuLocation)
    }

    /**
     * Edit text using #AddTicketPopUp
     */
    private fun editTicketOn() {
        AddTicketPopUp(actualTicket) { ticket ->
           actualTicket = ticket
            updateTicket()
        }.show(supportFragmentManager, "edittkc")
    }

    private fun updateTicket() {
        bindingDispatchNote.saveNote.isVisible = false

        val toast = Toasty.success(this@WorkTicketScreen, R.string.ticket_saved)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()

        viewModel.saveTicket(actualTicket!!)
    }

    private fun clickDirection(): View.OnClickListener {
return View.OnClickListener {
    MapActivity.openDirections(this@WorkTicketScreen, actualTicket!!)
}
    }

    private fun configTabLayout() {
        val tabLayout = binding.tabLayout

        tabLayout.addTab(tabLayout.newTab().setText(R.string.overview))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.work_details))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.purchasing))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.finishing_up))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.camera))

        tabLayout.addOnTabSelectedListener(this)
    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        if(tab!!.position == 0){
            main {
                binding.loadingLayout.isVisible = true
                binding.scrollView.isVisible = false
                delay(2000)
                binding.loadingLayout.isVisible = false
                binding.scrollView.isVisible = true
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }
}