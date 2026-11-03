package com.adoptme

import android.app.Application
import com.adoptme.core.di.AppContainer
import org.maplibre.android.MapLibre

/**
 * Inicializa el contenedor de dependencias antes de que se cree la Activity.
 * Así la restauración de datos locales (DataStore) empieza lo antes posible.
 */
class AdoptMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        AppContainer.init(this)
    }
}
