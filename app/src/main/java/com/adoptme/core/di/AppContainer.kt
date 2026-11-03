package com.adoptme.core.di

import android.content.Context
import com.adoptme.core.session.SessionManager
import com.adoptme.data.datasource.DataStoreLocalStore
import com.adoptme.data.datasource.LocalDataSource
import com.adoptme.data.repository.DogRepository
import com.adoptme.data.repository.DogRepositoryImpl

/**
 * Contenedor manual de dependencias (Fase A).
 * Garantiza una única instancia del repositorio compartida por todos los ViewModels.
 * En la Fase B se reemplaza por Hilt sin cambiar los consumidores.
 */
object AppContainer {

    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    val repository: DogRepository by lazy {
        val context = requireNotNull(applicationContext) {
            "AppContainer.init(context) debe llamarse en Application.onCreate()"
        }
        DogRepositoryImpl(
            localDataSource = LocalDataSource(DataStoreLocalStore(context))
        )
    }

    val sessionManager: SessionManager by lazy {
        val context = requireNotNull(applicationContext) {
            "AppContainer.init(context) debe llamarse en Application.onCreate()"
        }
        SessionManager(context)
    }
}
