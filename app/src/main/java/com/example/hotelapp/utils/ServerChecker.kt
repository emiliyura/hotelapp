package com.example.hotelapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

/**
 *
 */
object ServerChecker {
    
    /**
     *
     * 
     * @param host хост сервера
     * @param port порт сервера
     * @param timeout таймаут в миллисекундах
     * @return true, если сервер доступен, false в противном случае
     */
    suspend fun isServerReachable(host: String, port: Int, timeout: Int = 2000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), timeout)
                socket.close()
                true
            } catch (e: SocketTimeoutException) {
                false
            } catch (e: IOException) {
                false
            }
        }
    }
    
    /**
     *
     * 
     * @param timeout таймаут в миллисекундах
     * @return true, если сервер доступен, false в противном случае
     */
    suspend fun isEmulatorServerReachable(timeout: Int = 2000): Boolean {
        return isServerReachable("10.0.2.2", 8080, timeout)
    }
} 