package com.test.applicationtest.helper

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


object CoroutinesHelper {

    /**
     * Launch coroutine in secondary thread
     */
    fun <T> T.ioSafe(work: suspend (CoroutineScope.(T) -> Unit)) : Job {
        val value = this

       return CoroutineScope(Dispatchers.IO).launch{
           try {
               work(value)
           }catch (throwable: Throwable){
              logerror(throwable)
           }
        }
    }

    /**
     * Log error orderly
     */
    private fun logerror(throwable: Throwable) {
        Log.d("Safe", "-------------------------------------------------------------------")
        Log.d("Safe", "safeLaunch: " + throwable.localizedMessage)
        Log.d("Safe", "safeLaunch: " + throwable.message)
        throwable.printStackTrace()
        Log.d("Safe", "-------------------------------------------------------------------")
    }

    /**
     * Launch coroutine in Main Thread
     */
    fun <T> T.main(work: suspend (CoroutineScope.(T) -> Unit)) : Job{
        val value = this

       return CoroutineScope(Dispatchers.Main).launch {
           try {
               work(value)
           }catch (throwable: Throwable){
                logerror(throwable)
           }
       }
    }
}