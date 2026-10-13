package pe.edu.upeu.pharmamobilee.di

import android.content.pm.ApplicationInfo
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobilee.platform.CompartidorAndroid

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }
    single(named("esDepuracion")) {
        androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
