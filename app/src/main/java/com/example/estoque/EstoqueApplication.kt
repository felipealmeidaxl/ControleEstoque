package com.example.estoque

import android.app.Application
import com.example.estoque.data.db.AppDatabase
import com.example.estoque.data.repository.ProdutoRepository

class EstoqueApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ProdutoRepository(database.produtoDao()) }
}
