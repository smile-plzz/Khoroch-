package app.khoroch.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "khoroch_preferences")

data class UserPreferences(
    val language: String = "en",
    val theme: String = "system",
    val banglaNumerals: Boolean = false,
    val currencyDisplay: String = "Tk",
    val hideAmountsOnHome: Boolean = false,
    val appLockEnabled: Boolean = false,
    val appLockPinHash: String? = null,
    val lockDelaySeconds: Int = 30,
    val onboardingCompleted: Boolean = false,
    val smsTrackingEnabled: Boolean = true
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val THEME = stringPreferencesKey("app_theme")
        val BANGLA_NUMERALS = booleanPreferencesKey("bangla_numerals")
        val CURRENCY_DISPLAY = stringPreferencesKey("currency_display")
        val HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts_home")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val LOCK_DELAY = intPreferencesKey("lock_delay_seconds")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SMS_TRACKING_ENABLED = booleanPreferencesKey("sms_tracking_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val lang = prefs[Keys.LANGUAGE] ?: "en"
        val defaultNumerals = lang == "bn"
        UserPreferences(
            language = lang,
            theme = prefs[Keys.THEME] ?: "system",
            banglaNumerals = prefs[Keys.BANGLA_NUMERALS] ?: defaultNumerals,
            currencyDisplay = prefs[Keys.CURRENCY_DISPLAY] ?: (if (lang == "bn") "৳" else "Tk"),
            hideAmountsOnHome = prefs[Keys.HIDE_AMOUNTS] ?: false,
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            appLockPinHash = prefs[Keys.APP_LOCK_PIN_HASH],
            lockDelaySeconds = prefs[Keys.LOCK_DELAY] ?: 30,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            smsTrackingEnabled = prefs[Keys.SMS_TRACKING_ENABLED] ?: true
        )
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = lang
            if (lang == "bn") {
                prefs[Keys.BANGLA_NUMERALS] = true
                prefs[Keys.CURRENCY_DISPLAY] = "৳"
            } else {
                prefs[Keys.BANGLA_NUMERALS] = false
                prefs[Keys.CURRENCY_DISPLAY] = "Tk"
            }
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME] = theme }
    }

    suspend fun setBanglaNumerals(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BANGLA_NUMERALS] = enabled }
    }

    suspend fun setHideAmounts(hide: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HIDE_AMOUNTS] = hide }
    }

    suspend fun setAppLock(enabled: Boolean, pinHash: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LOCK_ENABLED] = enabled
            if (pinHash != null) {
                prefs[Keys.APP_LOCK_PIN_HASH] = pinHash
            } else if (!enabled) {
                prefs.remove(Keys.APP_LOCK_PIN_HASH)
            }
        }
    }

    suspend fun setLockDelay(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.LOCK_DELAY] = seconds }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSmsTracking(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SMS_TRACKING_ENABLED] = enabled }
    }
}
