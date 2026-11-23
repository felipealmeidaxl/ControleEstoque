package com.example.estoque.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "produtos",
    indices = [
        Index(value = ["codigoInterno"], unique = true)
    ]
)
data class Produto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val codigoInterno: String,
    val codigoBarras: String? = null,
    val categoria: String? = null,
    val fornecedor: String? = null,
    val unidade: String = "un",
    val localizacao: String? = null,
    val observacoes: String? = null,
    val quantidade: Int = 0,
    val estoqueMinimo: Int = 0,
    val precoCusto: Double = 0.0,
    val precoVenda: Double = 0.0,
    val dataCadastro: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis()
)
