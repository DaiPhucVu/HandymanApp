package com.example.handyman

import android.app.Application
import android.util.Log
import org.osmdroid.config.Configuration
import java.io.File
import androidx.preference.PreferenceManager

class HandymanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = Configuration.getInstance()
        
        // 1. Load preferences first
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        config.load(this, prefs)
        
        // 2. Set a HIGHLY UNIQUE User-Agent (Mandatory for OSM)
        // We avoid generic terms like "example" and use a specific versioned string.
        val uniqueUserAgent = "HandymanICTProject-FinalApp-v7.0"
        config.userAgentValue = uniqueUserAgent
        
        // 3. Force a fresh cache directory to bypass previously blocked tiles
        val freshCache = File(cacheDir, "osmdroid_tiles_v700_fresh")
        if (!freshCache.exists()) freshCache.mkdirs()
        config.osmdroidTileCache = freshCache

        // 4. Set download threads to 2 as per OSM policy
        config.tileDownloadThreads = 2
        config.tileDownloadMaxQueueSize = 2
        
        // 5. Ensure the User-Agent is also set in the HTTP headers explicitly
        config.additionalHttpRequestProperties["User-Agent"] = uniqueUserAgent
        
        // 6. Save settings to ensure persistence
        config.save(this, prefs)

        Log.d("HandymanApp", "OSM Configured - UA: ${config.userAgentValue}")

        try {
            // Cleanup old cache folders to save space
            File(cacheDir, "osmdroid").deleteRecursively()
            File(cacheDir, "osmdroid_tiles_v100_final").deleteRecursively()
        } catch (e: Exception) {
            Log.e("HandymanApp", "Cleanup failed", e)
        }
    }
}
