package com.example.estoque.util

import java.text.NumberFormat
import java.util.Locale

object MoedaUtils {

    private val localeBr = Locale("pt", "BR")
    private val formatadorMoeda = NumberFormat.getCurrencyInstance(localeBr)

    fun formatarMoeda(valor: Double): String {
        return formatadorMoeda.format(valor)
    }

    fun parsearPreco(texto: String?): Double {
        if (texto.isNullOrBlank()) return 0.0
        val limpo = texto.trim()
            .replace("R$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()

        return limpo.toDoubleOrNull() ?: try {
            val apenasDigitosEPonto = texto.trim().replace(",", ".")
            apenasDigitosEPonto.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun parsearPrecoDireto(texto: String?): Double {
        if (texto.isNullOrBlank()) return 0.0
        val normalizado = texto.trim().replace(",", ".")
        return normalizado.toDoubleOrNull() ?: 0.0
    }
}
