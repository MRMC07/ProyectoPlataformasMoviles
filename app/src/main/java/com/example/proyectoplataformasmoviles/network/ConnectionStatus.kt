
package com.example.proyectoplataformasmoviles.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ConnectionStatus(
    context: Context
) {
    private val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val isConnected: Flow<Boolean> = callbackFlow {
        fun currentConnection(): Boolean {
            val network =
                connectivityManager.activeNetwork ?: return false

            val capabilities =
                connectivityManager.getNetworkCapabilities(network)
                    ?: return false

            return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
        }

        trySend(currentConnection())

        val callback = object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                trySend(currentConnection())
            }

            override fun onLost(network: Network) {
                trySend(currentConnection())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                trySend(currentConnection())
            }
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                .build(),
            callback
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
