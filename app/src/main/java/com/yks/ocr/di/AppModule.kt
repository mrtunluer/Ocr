package com.yks.ocr.di

import android.app.Application
import androidx.room.Room
import com.yks.ocr.db.DocumentDao
import com.yks.ocr.db.DocumentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDb(app: Application): DocumentDatabase {
        return Room.databaseBuilder(app, DocumentDatabase::class.java, DocumentDatabase.DB_NAME).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: DocumentDatabase): DocumentDao {
        return db.getDocumentDao()
    }

}