package com.christianarduino.openweather.screens.HomePage

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.christianarduino.openweather.R
import com.christianarduino.openweather.model.OpenWeatherResponse
import com.christianarduino.openweather.screens.WeeklyPrevisionPage.WeeklyPrevisionActivity
import com.google.android.gms.location.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityModel
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var city: TextView
    private lateinit var weatherImg: ImageView
    private lateinit var temp: TextView
    private lateinit var message: TextView
    private lateinit var info: TextView
    private lateinit var maxTextView: TextView
    private lateinit var minTextView: TextView
    private lateinit var maxValue: TextView
    private lateinit var minValue: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var humidityValue: TextView
    private lateinit var otherPrev: Button
    private lateinit var includeMainInfoLabel: View
    private val permissionId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainActivityModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViews()
        setupObserver()
    }

    override fun onStart() {
        super.onStart()
        viewModel.send(SingleDayEvent.OnRequestLocationPermission)
    }

    private fun setupViews() {
        progressBar = findViewById(R.id.progressBar)
        city = findViewById(R.id.city)
        temp = findViewById(R.id.temp)
        message = findViewById(R.id.message)
        weatherImg = findViewById(R.id.weatherImg)
        info = findViewById(R.id.info)
        maxTextView = findViewById(R.id.maxTextView)
        maxValue = findViewById(R.id.maxValue)
        minTextView = findViewById(R.id.minTextView)
        minValue = findViewById(R.id.minValue)
        humidityTextView = findViewById(R.id.humidityTextView)
        humidityValue = findViewById(R.id.humidityValue)
        otherPrev = findViewById(R.id.otherPrev)
        includeMainInfoLabel = findViewById(R.id.includeMainInfoLabel)
    }

    private fun setupObserver() {
        viewModel.observe(this) { openWeatherState ->
            when (openWeatherState) {
                is SingleDayState.InProgress -> getLastLocation()
                is SingleDayState.Success -> onSuccessView(openWeatherState.weather)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onSuccessView(weather: OpenWeatherResponse) {
        progressBar.visibility = View.GONE

        city.text = weather.name
        city.visibility = View.VISIBLE

        when (weather.clouds.all) {
            in 0..30 -> weatherImg.setImageResource(R.drawable.sun)
            in 31..70 -> weatherImg.setImageResource(R.drawable.cloudy)
            in 71..100 -> weatherImg.setImageResource(R.drawable.raining)

        }
        weatherImg.visibility = View.VISIBLE

        temp.text = weather.main.temp.roundToInt().toString() + "° C"
        temp.visibility = View.VISIBLE
        city.visibility = View.VISIBLE

        when(weather.main.temp.roundToInt()) {
            in Int.MIN_VALUE..11 -> message.text = "Fa molto freddo! Copriti bene."
            in 12..19 -> message.text = "C'è freddo, ma nemmeno troppo.\nVestiti un po' più pesante!"
            in 20..25 -> message.text = "Oggi è una bella giornata!\nNon vestirti troppo pesante"
            in 26..Int.MAX_VALUE -> message.text = "Che caldo! È tempo di\nmaglie a mezze maniche."
        }
        message.visibility = View.VISIBLE

        info.visibility = View.VISIBLE

        maxValue.text = weather.main.temp_max.roundToInt().toString() + "° C"
        maxTextView.visibility = View.VISIBLE
        maxValue.visibility = View.VISIBLE

        minValue.text = weather.main.temp_min.roundToInt().toString() + "° C"
        minTextView.visibility = View.VISIBLE
        minValue.visibility = View.VISIBLE

        humidityValue.text = weather.main.humidity.toString() + "%"
        humidityTextView.visibility = View.VISIBLE
        humidityValue.visibility = View.VISIBLE

        includeMainInfoLabel.visibility = View.VISIBLE

        otherPrev.visibility = View.VISIBLE
        otherPrev.setOnClickListener {
            val intent = Intent(this, WeeklyPrevisionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        viewModel.send(
                            SingleDayEvent.OnPermissionAllow(
                                location.latitude.toString(),
                                location.longitude.toString()
                            )
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Accendi il GPS", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    //controllo dei permessi
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    //richiesta dei permessi
    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, permissionId)
    }

    //metodo chiamato appena l'utente sceglierà l'opzione nella richiesta dei permessi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                //se li ha rifiutati
                TODO("SHOW DIALOG")
            }
        }
    }

    //se l'utente ha accesso la geolocalizzazione
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //nel caso in cui laction == null
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
//            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
//            findViewById<TextView>(R.id.longTextView).text = mLastLocation.longitude.toString()
        }
    }
}
