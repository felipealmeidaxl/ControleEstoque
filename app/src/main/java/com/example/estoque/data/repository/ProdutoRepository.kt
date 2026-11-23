package com.example.estoque.data.repository

import com.example.estoque.data.db.ProdutoDao
import com.example.estoque.data.model.Produto
import kotlinx.coroutines.flow.Flow

class ProdutoRepository(private val produtoDao: ProdutoDao) {

    fun buscar(termo: String): Flow<List<Produto>> = produtoDao.buscar(termo)

    fun listarTodos(): Flow<List<Produto>> = produtoDao.listarTodos()

    suspend fun buscarPorId(id: Long): Produto? = produtoDao.buscarPorId(id)

    suspend fun buscarPorCodigoBarras(codigo: String): Produto? = produtoDao.buscarPorCodigoBarras(codigo)

    suspend fun buscarPorCodigoInterno(codigo: String): Produto? = produtoDao.buscarPorCodigoInterno(codigo)

    suspend fun inserir(produto: Produto): Long = produtoDao.inserir(produto)

    suspend fun atualizar(produto: Produto): Int = produtoDao.atualizar(produto)

    suspend fun excluir(produto: Produto): Int = produtoDao.excluir(produto)

    suspend fun ajustarQuantidade(id: Long, delta: Int): Int =
        produtoDao.ajustarQuantidade(id, delta, System.currentTimeMillis())
}
