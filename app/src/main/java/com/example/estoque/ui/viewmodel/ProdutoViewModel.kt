package com.example.estoque.ui.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estoque.data.model.Produto
import com.example.estoque.data.repository.ProdutoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumoEstoque(
    val totalProdutos: Int = 0,
    val totalUnidades: Int = 0,
    val valorTotalEstoque: Double = 0.0,
    val quantidadeEstoqueBaixo: Int = 0
)

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Success : FormState()
    data class Error(val message: String, val isCodigoDuplicado: Boolean = false) : FormState()
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProdutoViewModel(private val repository: ProdutoRepository) : ViewModel() {

    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    val produtos: StateFlow<List<Produto>> = _termoBusca
        .flatMapLatest { termo -> repository.buscar(termo) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resumoEstoque: StateFlow<ResumoEstoque> = repository.listarTodos()
        .map { lista ->
            ResumoEstoque(
                totalProdutos = lista.size,
                totalUnidades = lista.sumOf { it.quantidade },
                valorTotalEstoque = lista.sumOf { it.quantidade * it.precoCusto },
                quantidadeEstoqueBaixo = lista.count { it.quantidade <= it.estoqueMinimo }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResumoEstoque()
        )

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    fun setTermoBusca(termo: String) {
        _termoBusca.value = termo
    }

    fun ajustarQuantidade(id: Long, delta: Int) {
        viewModelScope.launch {
            repository.ajustarQuantidade(id, delta)
        }
    }

    suspend fun carregarProdutoPorId(id: Long): Produto? {
        return repository.buscarPorId(id)
    }

    fun resetFormState() {
        _formState.value = FormState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }

    fun salvarProduto(
        id: Long,
        nome: String,
        codigoInterno: String,
        codigoBarras: String?,
        categoria: String?,
        fornecedor: String?,
        unidade: String,
        localizacao: String?,
        observacoes: String?,
        quantidade: Int,
        estoqueMinimo: Int,
        precoCusto: Double,
        precoVenda: Double,
        dataCadastroOriginal: Long? = null
    ) {
        val nomeNormalizado = nome.trim()
        val codigoNormalizado = codigoInterno.trim()

        if (nomeNormalizado.isBlank()) {
            _formState.value = FormState.Error("O nome do produto é obrigatório")
            return
        }

        if (codigoNormalizado.isBlank()) {
            _formState.value = FormState.Error("O código interno é obrigatório", isCodigoDuplicado = false)
            return
        }

        viewModelScope.launch {
            _formState.value = FormState.Loading
            try {
                // Checa duplicidade do código interno
                val existente = repository.buscarPorCodigoInterno(codigoNormalizado)
                if (existente != null && existente.id != id) {
                    _formState.value = FormState.Error(
                        "O código interno '$codigoNormalizado' já está cadastrado em outro produto.",
                        isCodigoDuplicado = true
                    )
                    return@launch
                }

                val agora = System.currentTimeMillis()
                val produto = Produto(
                    id = id,
                    nome = nomeNormalizado,
                    codigoInterno = codigoNormalizado,
                    codigoBarras = codigoBarras?.trim()?.ifBlank { null },
                    categoria = categoria?.trim()?.ifBlank { null },
                    fornecedor = fornecedor?.trim()?.ifBlank { null },
                    unidade = unidade.trim().ifBlank { "un" },
                    localizacao = localizacao?.trim()?.ifBlank { null },
                    observacoes = observacoes?.trim()?.ifBlank { null },
                    quantidade = maxOf(0, quantidade),
                    estoqueMinimo = maxOf(0, estoqueMinimo),
                    precoCusto = maxOf(0.0, precoCusto),
                    precoVenda = maxOf(0.0, precoVenda),
                    dataCadastro = dataCadastroOriginal ?: agora,
                    dataAtualizacao = agora
                )

                if (id == 0L) {
                    repository.inserir(produto)
                } else {
                    repository.atualizar(produto)
                }

                _formState.value = FormState.Success
            } catch (e: SQLiteConstraintException) {
                _formState.value = FormState.Error(
                    "O código interno informado já está em uso.",
                    isCodigoDuplicado = true
                )
            } catch (e: Exception) {
                _formState.value = FormState.Error("Erro ao salvar produto: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }
    }

    fun excluirProduto(produto: Produto) {
        viewModelScope.launch {
            try {
                repository.excluir(produto)
                _deleteState.value = DeleteState.Success
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error("Erro ao excluir: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }
    }
}
