package com.bgnw.locationreminder.api

import android.content.Context
import android.content.SharedPreferences

class AccountDeviceTools {
    companion object Factory {
        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("LRPrefs", Context.MODE_PRIVATE)
        }

        fun saveUsername(context: Context, username: String) {
            getSharedPreferences(context).edit().putString("username", username).apply()
        }

        fun saveDisplayName(context: Context, displayName: String) {
            getSharedPreferences(context).edit().putString("display_name", displayName).apply()
        }

        fun saveDebug(context: Context, enableDebug: Boolean) {
            getSharedPreferences(context).edit().putBoolean("enable_debug", enableDebug).apply()
        }

        fun saveUpdateFreq(context: Context, updateFreq: Int) {
            getSharedPreferences(context).edit().putInt("update_freq", updateFreq).apply()
        }

        fun saveRemindRadius(context: Context, remindRadius: Int) {
            getSharedPreferences(context).edit().putInt("remind_radius", remindRadius).apply()
        }

        fun retrieveUsername(context: Context): String? {
            return getSharedPreferences(context).getString("username", null)
        }

        fun retrieveDisplayName(context: Context): String? {
            return getSharedPreferences(context).getString("display_name", null)
        }

        fun retrieveDebug(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("enable_debug", false)
        }

        fun retrieveUpdateFreq(context: Context): Int {
            return getSharedPreferences(context).getInt("update_freq", 30)
        }

        fun retrieveRemindRadius(context: Context): Int {
            return getSharedPreferences(context).getInt("remind_radius", 50)
        }

        fun eraseData(context: Context): Boolean {
            val sharedPrefsEditor = getSharedPreferences(context).edit()
            sharedPrefsEditor.remove("username")
            sharedPrefsEditor.remove("display_name")
            return sharedPrefsEditor.commit()
        }
    }
}