package pe.edu.upeu.pharmamobilee.data.local

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobilee.db.PharmaDb

class CreadorDeDriverIos : CreadorDeDriver {
    override fun crear() = NativeSqliteDriver(PharmaDb.Schema, "pharma.db")
}
