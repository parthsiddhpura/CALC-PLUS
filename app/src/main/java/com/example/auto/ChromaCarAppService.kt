package com.example.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Android Auto CarAppService entry point for CALC + (ChromaCalc Pro).
 * Exposes driver-safe calculation tools: Quick Calc, Live Currency, Fuel & Trip Cost,
 * GST Tax Calculator, and Recent Calculation History.
 */
class ChromaCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return ChromaCarSession()
    }
}

class ChromaCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return ChromaCarMainScreen(carContext)
    }
}
