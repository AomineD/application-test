package com.test.applicationtest.ui.view

import android.Manifest.permission.READ_CALENDAR
import android.Manifest.permission.WRITE_CALENDAR
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.zawadz88.materialpopupmenu.MaterialPopupMenu
import com.github.zawadz88.materialpopupmenu.popupMenuBuilder
import com.test.applicationtest.R
import com.test.applicationtest.databinding.ActivityMainBinding
import com.test.applicationtest.helper.ActivityUtils.fullScreen
import com.test.applicationtest.helper.CoroutinesHelper.main
import com.test.applicationtest.helper.DataConverter.toDate
import com.test.applicationtest.helper.ViewHelper.playLimit
import com.test.applicationtest.model.Ticket
import com.test.applicationtest.ui.adapter.TicketListAdapter
import com.test.applicationtest.ui.calendar.InteractiveCalendar
import com.test.applicationtest.ui.dialog.AddTicketPopUp
import com.test.applicationtest.ui.dialog.PopUpLoading
import com.test.applicationtest.ui.viewmodel.DashboardViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object{
        const val REQUEST_PERMISSION_CODE = 20183
    }

    private var calendarPopUp:InteractiveCalendar? = null
    private lateinit var popUpLoading: PopUpLoading
    private lateinit var binding:ActivityMainBinding
    private val viewModel:DashboardViewModel by viewModels()

    private var calendarFirstSet = false
    private var ticketList = mutableListOf<Ticket>()

    private var popMenu: MaterialPopupMenu? = null
    private lateinit var adapter:TicketListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        viewModel.onCreate()
        observeData()
        configViews()
        configBtns()
    }

    private fun configBtns() {
        binding.btnAddTicket.setOnClickListener {
            AddTicketPopUp(this@MainActivity) { ticket ->
                viewModel.addTicket(ticket) { result, rowId ->
                    main {
                        if (result) {
                            Toasty.success(this@MainActivity, R.string.added_successfuly)
                                .show()
                            setupCalendar()
                            clickOnView(rowId!!.toInt())
                        } else {
                            Toasty.error(this@MainActivity, R.string.something_went_wrong)
                                .show()
                        }
                    }
                }
            }.show(supportFragmentManager, ",le")
        }

        binding.btnSync.setOnClickListener {
            if(viewModel.getTicketsFromProvider().isNotEmpty()) {
        syncCalendar()
            }else{
                popUpLoading.showError(R.string.empty_tickets_sync)
            }
        }

        binding.btnCalendar.setOnClickListener {
            if(viewModel.getTicketsFromProvider().isNotEmpty()) {
                calendarPopUp?.show(supportFragmentManager, "clander")
            }else{
                popUpLoading.showError(R.string.empty_tickets)
            }
        }

        binding.btnMenu.setOnClickListener {
            openMenu()
        }
    }

    private fun configViews() {
        adapter = TicketListAdapter(ticketList){
            clickOnView(it.id!!)
        }
        binding.rvTickets.layoutManager = LinearLayoutManager(this)
        binding.rvTickets.adapter = adapter

        popUpLoading = PopUpLoading(this)
    }

    private fun setupCalendar(){
        calendarPopUp?.let {
            if(calendarPopUp!!.isVisible) {
                calendarPopUp!!.dismiss()
            }
        }

        calendarPopUp = InteractiveCalendar(viewModel.getTicketsFromProvider()) {
            if (it == null) {
                viewModel.getTickets()
            } else {
                viewModel.getTicketsByDate(it)
            }
            main { _ ->
                binding.tvDate.text = it?.toDate() ?: getString(R.string.all_tickets)
            }

        }

        calendarFirstSet = true
    }

    private fun clickOnView(id:Int) {
        //GO to detail screen
        WorkTicketScreen.openDetailsFor(this, id)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeData() {
        viewModel.ticketList.observe(this){ list ->
            ticketList.clear()
            ticketList.addAll(list)
            main {
                adapter.notifyDataSetChanged()
                if(ticketList.isNotEmpty()) {

                    if(!calendarFirstSet){
                        setupCalendar()
                    }

                }else{
                    Toasty.warning(this@MainActivity, R.string.empty_tickets_list)
                        .show()
                }
            }
        }
    }

    //CALENDAR PERMISSIONS
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_CODE)
    fun syncCalendar() {
        if (EasyPermissions.hasPermissions(this, READ_CALENDAR, WRITE_CALENDAR)) {
            viewModel.syncCalendar(contentResolver){ added, updated ->
                if(added > 0 || updated > 0){
                    popUpLoading.showSuccess(R.string.success_sync, added+updated)
                }else{
                    popUpLoading.showError(R.string.something_went_wrong)
                }
            }
        } else {
            val permissions = arrayOf(READ_CALENDAR, WRITE_CALENDAR)
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                host = this@MainActivity,
                rationale = getString(R.string.permission_calendar_rationale),
                requestCode = REQUEST_PERMISSION_CODE,
                perms = permissions,
            )
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
                            //
                            MapActivity.openDirections(this@MainActivity, "")
                        }
                    }
                    item {
                        label = getString(R.string.work_ticket_txt)
                        callback = {
                            if (viewModel.getTicketsFromProvider().isNotEmpty()) {
                                clickOnView(viewModel.getTicketsFromProvider().last().id!!)
                            } else {
                                Toasty.warning(this@MainActivity, R.string.empty_tickets_list)
                                    .show()
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
        popMenu!!.show(this@MainActivity, binding.menuLocation)
    }
}