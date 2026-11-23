package com.example.estoque.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.estoque.data.model.Produto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {

    @Query("""
        SELECT * FROM produtos 
        WHERE (:termo IS NULL OR :termo = '' 
               OR nome LIKE '%' || :termo || '%' 
               OR codigoInterno LIKE '%' || :termo || '%' 
               OR codigoBarras LIKE '%' || :termo || '%' 
               OR categoria LIKE '%' || :termo || '%')
        ORDER BY nome ASC
    """)
    fun buscar(termo: String): Flow<List<Produto>>

    @Query("SELECT * FROM produtos ORDER BY nome ASC")
    fun listarTodos(): Flow<List<Produto>>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Produto?

    @Query("SELECT * FROM produtos WHERE codigoBarras = :codigo LIMIT 1")
    suspend fun buscarPorCodigoBarras(codigo: String): Produto?

    @Query("SELECT * FROM produtos WHERE codigoInterno = :codigoInterno LIMIT 1")
    suspend fun buscarPorCodigoInterno(codigoInterno: String): Produto?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(produto: Produto): Long

    @Update
    suspend fun atualizar(produto: Produto): Int

    @Delete
    suspend fun excluir(produto: Produto): Int

    @Query("""
        UPDATE produtos 
        SET quantidade = CASE WHEN quantidade + :delta < 0 THEN 0 ELSE quantidade + :delta END,
            dataAtualizacao = :timestamp
        WHERE id = :id
    """)
    suspend fun ajustarQuantidade(id: Long, delta: Int, timestamp: Long = System.currentTimeMillis()): Int
}
