package com.example.handyman.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * App language handling.
 *
 * The chosen language is stored locally on the device (not in Firebase) so the
 * very first screen can be shown in the right language before any login, and so
 * it keeps working offline.
 *
 * Every Activity must call [wrap] from `attachBaseContext` for the choice to
 * apply to that screen:
 *
 *     override fun attachBaseContext(newBase: Context) {
 *         super.attachBaseContext(LocaleHelper.wrap(newBase))
 *     }
 *
 * This manual approach is used instead of AppCompatDelegate.setApplicationLocales
 * because the app mixes ComponentActivity and AppCompatActivity, and the
 * AppCompat backport only auto-applies to the latter on API < 33 (minSdk is 29).
 */
object LocaleHelper {

    const val LANG_ENGLISH = "en"
    const val LANG_BANGLA = "bn"

    private const val PREFS_NAME = "handyman_prefs"
    private const val KEY_LANGUAGE = "app_language"

    /** The language the user picked, or null if they have never been asked. */
    fun getSavedLanguage(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)

    /** True once the user has made a choice, so we only ask on first launch. */
    fun hasChosenLanguage(context: Context): Boolean = getSavedLanguage(context) != null

    /** Persist the choice. The caller should recreate the Activity to apply it. */
    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /** Current language, defaulting to English if nothing has been chosen yet. */
    fun currentLanguage(context: Context): String = getSavedLanguage(context) ?: LANG_ENGLISH

    /**
     * Returns a Context configured with the saved language. If the user has not
     * chosen yet, the device default is left untouched.
     */
    fun wrap(context: Context): Context {
        val language = getSavedLanguage(context) ?: return context

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Finds the Activity behind a Context so it can be recreated after a
     * language change. Compose's LocalContext is often a ContextWrapper rather
     * than the Activity itself — a plain `as? Activity` cast returns null there,
     * which would make the language change appear to do nothing until restart.
     */
    fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
