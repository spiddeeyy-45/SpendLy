package com.example.spendly

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.spendly.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.navHome.setOnClickListener {
            setNavSelected("home")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        binding.navVehicle.setOnClickListener {
            setNavSelected("vehicle")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, VehicleFragment())
                .commit()
        }

        binding.navAdd.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddFragment())
                .commit()
        }
        binding.navTrip.setOnClickListener {
            setNavSelected("trip")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TripFragment())
                .commit()
        }

        binding.navProfile.setOnClickListener {
            setNavSelected("profile")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }
        setNavSelected("home")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
    }

    private fun setNavSelected(selected: String) {
        val items = listOf(
            Triple(binding.navHome, binding.pillHome, "home"),
            Triple(binding.navVehicle, binding.pillVehicle, "vehicle"),
            Triple(binding.navTrip, binding.pillTrip, "trip"),
            Triple(binding.navProfile, binding.pillProfile, "profile")
        )

        items.forEach { (nav, pill, _) ->
            nav.isSelected = false
            pill.visibility = View.GONE
        }

        items.firstOrNull { it.third == selected }?.let { (nav, pill, _) ->
            nav.isSelected = true
            pill.visibility = View.VISIBLE
            pill.alpha = 0f
            pill.animate().alpha(1f).setDuration(200).start()
        }
    }
}