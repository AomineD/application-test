package com.test.applicationtest.module

import android.content.Context
import com.test.applicationtest.db.SQLiteHelper
import com.test.applicationtest.db.TicketProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Ensure to use Dagger Hilt Injections to prevent multiple instances and OOM errors
 */
@Module
@InstallIn(SingletonComponent::class)
object SQLModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): SQLiteHelper {
        return SQLiteHelper(appContext, TicketProvider())
    }
}