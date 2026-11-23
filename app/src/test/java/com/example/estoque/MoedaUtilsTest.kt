package com.example.estoque

import com.example.estoque.util.MoedaUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class MoedaUtilsTest {

    @Test
    fun parsearPrecoDireto_comVirgula_retornaDoubleCorreto() {
        val resultado = MoedaUtils.parsearPrecoDireto("12,50")
        assertEquals(12.50, resultado, 0.001)
    }

    @Test
    fun parsearPrecoDireto_comPonto_retornaDoubleCorreto() {
        val resultado = MoedaUtils.parsearPrecoDireto("12.50")
        assertEquals(12.50, resultado, 0.001)
    }

    @Test
    fun parsearPrecoDireto_vazio_retornaZero() {
        val resultado = MoedaUtils.parsearPrecoDireto("")
        assertEquals(0.0, resultado, 0.001)
    }
}
