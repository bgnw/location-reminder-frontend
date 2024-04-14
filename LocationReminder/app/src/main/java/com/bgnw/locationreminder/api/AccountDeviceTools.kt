package com.bgnw.locationreminder.api

import android.accounts.Account
import android.accounts.AccountManager
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

        fun retrieveUsername(context: Context): String? {
            return getSharedPreferences(context).getString("username", null)
        }

        fun retrieveDisplayName(context: Context): String? {
            return getSharedPreferences(context).getString("display_name", null)
        }

        fun eraseData(context: Context): Boolean {
            val sharedPrefsEditor = getSharedPreferences(context).edit()
            sharedPrefsEditor.remove("username")
            sharedPrefsEditor.remove("display_name")
            return sharedPrefsEditor.commit()
        }
    }
}