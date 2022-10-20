package com.test.applicationtest.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.tabs.TabLayout
import com.test.applicationtest.R
import com.test.applicationtest.databinding.ActivityMapBinding
import com.test.applicationtest.helper.ActivityUtils.fullScreen
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import com.test.applicationtest.helper.CoroutinesHelper.main
import com.test.applicationtest.model.Ticket
import com.test.applicationtest.ui.dialog.PopUpSelectMap
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

/**
 * MAPS SDK Implementation and WebView (2 options), until billing is enabled you can use webView full
 */
class MapActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener, OnMapReadyCallback {

    companion object{
        private var addressStr:String? = ""

        fun openDirections(context: Context,ticket: Ticket){
            addressStr = ticket.customerAddress
            context.startActivity(Intent(context, MapActivity::class.java))
        }

        fun openDirections(context: Context,address: String?){
            addressStr = address
            context.startActivity(Intent(context, MapActivity::class.java))
        }

        const val REQUEST_PERMISSION_CODE_MAP = 9350
    }

    override fun onDestroy() {
        super.onDestroy()
        addressStr = ""
    }

    private lateinit var binding:ActivityMapBinding
    private lateinit var mMap:GoogleMap
    private lateinit var mapFragment:SupportMapFragment
    private lateinit var placesClient:PlacesClient
    private val launcherActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
        val data = uri.data
        when (uri.resultCode) {
            Activity.RESULT_OK -> {
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    addressStr = place.address
                    getLocationFromAddress()
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                data?.let {
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e("MAIN", status.statusMessage ?: "no hay ${status.statusCode}")
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.e("MAIN", ": canceled" )
                // The user canceled the operation.
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        configViews()
        configTabLayout()

        // Better dos options than one
        PopUpSelectMap(this){
            main { _ ->
                if(it == 0){
                    //init Maps SDK
                    initMapSDK()
                }else{
                    //or init web
                    initWebView()
                }
            }
        }.show()

    }

    private fun configViews() {
        binding.btnBack.setOnClickListener{
            finish()
        }
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        binding.searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                v.clearFocus()
                binding.searchView.onActionViewCollapsed()
                openPlacesSearch()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        binding.root.findViewById<View>(R.id.mapFragment).isVisible = false
       // binding.root.findViewById<View>(R.id.autocomplete_fragment).isVisible = false
        binding.webViewMap.isVisible = true
        binding.searchView.isVisible = false

        binding.webViewMap.settings.javaScriptEnabled = true
        binding.webViewMap.settings.domStorageEnabled = true

        val queryFormatted = addressStr!!.replace(" ","+").replace("#", "%23")
        val url = "https://www.google.com/maps/search/?api=1&query=$queryFormatted"
        binding.webViewMap.loadUrl(url)
        binding.webViewMap.webViewClient = object:WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                main {
                    //wait 2 sec more
                    delay(2000)
                    binding.loadingView.isVisible = false
                }
            }
        }

    }


    private fun initMapSDK(){
        binding.root.findViewById<View>(R.id.mapFragment).isVisible = true
        binding.webViewMap.isVisible = false
        mapFragment.getMapAsync(this)

        val app = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager
                .getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(
                    PackageManager.GET_META_DATA.toLong()
                ))
        } else {
            packageManager
                .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }

        val bundle = app.metaData

        val  apiKey = bundle.getString("com.google.android.geo.API_KEY")

        apiKey?.let {
            // Initialize the SDK
            Places.initialize(applicationContext, apiKey)

            // Create a new PlacesClient instance
            placesClient = Places.createClient(this)
        }

        main {
            delay(3000)
            // Start the autocomplete intent.
            if (addressStr.isNullOrEmpty()) {
             openPlacesSearch()
            }else{
                binding.searchView.queryHint = addressStr
            }
        }

    }

    private fun openPlacesSearch() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

        val intent = if(addressStr.isNullOrEmpty()) {
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this@MapActivity)
        }else{
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setInitialQuery(addressStr!!)
                .build(this@MapActivity)
        }
        launcherActivity.launch(intent)
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
         //  finish()
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    // Get address lat and long with GeoCode
    @AfterPermissionGranted(REQUEST_PERMISSION_CODE_MAP)
    private fun getLocationFromAddress() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (EasyPermissions.hasPermissions(this,
             perms = permissions
            )) {
            getAddress{
                it?.let {
                   setPointInMap(it)
                }
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                host = this@MapActivity,
                rationale = getString(R.string.permission_map_rationale),
                requestCode = REQUEST_PERMISSION_CODE_MAP,
                perms = permissions,
            )
        }

    }

    private fun setPointInMap(latLng: LatLng) {
        main {
            mMap.addMarker( MarkerOptions().draggable(true).position(latLng).title(addressStr))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
        }
    }

    private fun getAddress(callback: suspend (CoroutineScope.(LatLng?) -> Unit)) {
        ioSafe {
            val coder = Geocoder(this@MapActivity)
            val address: List<Address>?
            var p1: LatLng? = null
            try {
                address = coder.getFromLocationName(addressStr!!, 5)
                if (address == null) {
                    callback(null)
                }
                val location: Address = address!![0]
                p1 = LatLng(location.latitude, location.longitude)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            callback(p1)
        }
    }

    override fun onMapReady(mMap: GoogleMap) {
        //Map is ready
        binding.loadingView.isVisible = false
        this.mMap = mMap
        getLocationFromAddress()
    }


    //MAP PERMISSIONS
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }





}