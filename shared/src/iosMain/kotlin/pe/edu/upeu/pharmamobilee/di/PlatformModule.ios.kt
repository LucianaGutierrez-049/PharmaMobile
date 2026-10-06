package pe.edu.upeu.pharmamobilee.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.platform.CompartidorIos

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single(named("urlBase")) { "http://localhost:8080/api/v1/" }
    single<Compartidor> { CompartidorIos() }
}
