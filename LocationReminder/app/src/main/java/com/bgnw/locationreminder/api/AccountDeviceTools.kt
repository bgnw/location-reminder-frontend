package com.bgnw.locationreminder.api

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences

class AccountDeviceTools {
    companion object Factory {
        private const val ACCOUNT_DOMAIN = "com.bgnw.locationreminder"
        private const val ACCOUNT_NAME = "LocationReminder"

//        fun saveUsername(context: Context, username: String) {
//            val manager: AccountManager = AccountManager.get(context)
//            val account: Account = Account(ACCOUNT_DOMAIN, ACCOUNT_NAME)
//
//            // Add or update the account
//            if (manager.addAccountExplicitly(account, null, null)) {
//                // Successfully added the account
//                manager.setUserData(account, "username", username);
//            } else {
//                // Account already exists, update the username
//                manager.setUserData(account, "username", username);
//            }
//        }

//        fun retrieveUsername(context: Context): String? {
//            val accountManager = AccountManager.get(context)
//            val accounts = accountManager.getAccountsByType(ACCOUNT_DOMAIN)
//
//            return if (accounts.isNotEmpty()) {
//                accountManager.getUserData(accounts[0], "username")
//            } else {
//                null // no account held
//            }
//        }


        fun saveUsername(context: Context, username: String) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("LRPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("username", username).apply()
        }

        fun saveDisplayName(context: Context, displayName: String) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("LRPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("display_name", displayName).apply()
        }

        fun retrieveUsername(context: Context): String? {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("LRPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("username", null)
        }

        fun retrieveDisplayName(context: Context): String? {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("LRPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("display_name", null)
        }


    }
}