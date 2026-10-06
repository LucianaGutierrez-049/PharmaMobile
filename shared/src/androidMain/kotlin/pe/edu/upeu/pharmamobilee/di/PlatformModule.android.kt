package pe.edu.upeu.pharmamobilee.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.platform.CompartidorAndroid

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
