package pe.edu.upeu.pharmamobilee.data.local

import app.cash.sqldelight.db.SqlDriver

interface CreadorDeDriver {
    fun crear(): SqlDriver
}
