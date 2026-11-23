package com.example.estoque.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.estoque.EstoqueApplication
import com.example.estoque.R
import com.example.estoque.data.model.Produto
import com.example.estoque.databinding.ActivityGraficosBinding
import com.example.estoque.ui.custom.ItemGrafico
import com.example.estoque.ui.viewmodel.ProdutoViewModel
import com.example.estoque.ui.viewmodel.ProdutoViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GraficosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraficosBinding
    private val viewModel: ProdutoViewModel by viewModels {
        ProdutoViewModelFactory((application as EstoqueApplication).repository)
    }

    private enum class ModoGrafico { PRODUTOS, CATEGORIAS }
    private enum class Ordenacao { MAIOR_QTD, MENOR_QTD, NOME }

    private var modoAtual = ModoGrafico.PRODUTOS
    private var ordenacaoAtual = Ordenacao.MAIOR_QTD
    private var listaProdutos: List<Produto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraficosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarControles()
        observarProdutos()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarControles() {
        // Seleção de Modo (Por Produto / Por Categoria)
        binding.toggleModoGrafico.check(R.id.btnModoProdutos)
        binding.toggleModoGrafico.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnModoProdutos -> {
                        modoAtual = ModoGrafico.PRODUTOS
                        binding.scrollFiltros.visibility = View.VISIBLE
                        binding.layoutLegenda.visibility = View.VISIBLE
                    }
                    R.id.btnModoCategorias -> {
                        modoAtual = ModoGrafico.CATEGORIAS
                        binding.scrollFiltros.visibility = View.GONE
                        binding.layoutLegenda.visibility = View.GONE
                    }
                }
                atualizarGrafico(animar = true)
            }
        }

        // Filtro de Ordenação
        binding.chipGroupOrdenacao.setOnCheckedStateChangeListener { _, checkedIds ->
            ordenacaoAtual = when {
                checkedIds.contains(R.id.chipMenorQtd) -> Ordenacao.MENOR_QTD
                checkedIds.contains(R.id.chipNome) -> Ordenacao.NOME
                else -> Ordenacao.MAIOR_QTD
            }
            atualizarGrafico(animar = false)
        }
    }

    private fun observarProdutos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa todos os produtos (busca com termo vazio retorna todos)
                val repository = (application as EstoqueApplication).repository
                repository.buscar("").collectLatest { produtos ->
                    listaProdutos = produtos
                    atualizarDestaques(produtos)
                    atualizarGrafico(animar = true)
                }
            }
        }
    }

    private fun atualizarDestaques(produtos: List<Produto>) {
        if (produtos.isEmpty()) {
            binding.layoutDestaques.visibility = View.GONE
            binding.cardGrafico.visibility = View.GONE
            binding.layoutGraficoVazio.visibility = View.VISIBLE
            return
        }

        binding.layoutDestaques.visibility = View.VISIBLE
        binding.cardGrafico.visibility = View.VISIBLE
        binding.layoutGraficoVazio.visibility = View.GONE

        // Maior estoque
        val maior = produtos.maxByOrNull { it.quantidade }
        if (maior != null) {
            binding.tvMaiorEstoqueNome.text = maior.nome
            binding.tvMaiorEstoqueQtd.text = "${maior.quantidade} ${maior.unidade}"
        }

        // Menor estoque
        val menor = produtos.minByOrNull { it.quantidade }
        if (menor != null) {
            binding.tvMenorEstoqueNome.text = menor.nome
            binding.tvMenorEstoqueQtd.text = "${menor.quantidade} ${menor.unidade}"
        }
    }

    private fun atualizarGrafico(animar: Boolean) {
        if (listaProdutos.isEmpty()) {
            binding.graficoBarrasView.setDados(emptyList(), false)
            return
        }

        val itens: List<ItemGrafico> = when (modoAtual) {
            ModoGrafico.PRODUTOS -> {
                val produtosOrdenados = when (ordenacaoAtual) {
                    Ordenacao.MAIOR_QTD -> listaProdutos.sortedByDescending { it.quantidade }
                    Ordenacao.MENOR_QTD -> listaProdutos.sortedBy { it.quantidade }
                    Ordenacao.NOME -> listaProdutos.sortedBy { it.nome.lowercase() }
                }

                produtosOrdenados.map { p ->
                    val categoriaInfo = if (!p.categoria.isNullOrBlank()) " • ${p.categoria}" else ""
                    val subtitulo = "Cód: ${p.codigoInterno}$categoriaInfo"

                    ItemGrafico(
                        id = p.id,
                        rotulo = p.nome,
                        subtitulo = subtitulo,
                        quantidade = p.quantidade,
                        estoqueMinimo = p.estoqueMinimo,
                        unidade = p.unidade,
                        isEstoqueBaixo = p.quantidade <= p.estoqueMinimo
                    )
                }
            }
            ModoGrafico.CATEGORIAS -> {
                // Agrupa por categoria
                listaProdutos
                    .groupBy { it.categoria?.trim()?.ifBlank { null } ?: "Sem Categoria" }
                    .map { (categoria, produtosDoGrupo) ->
                        val totalQuantidade = produtosDoGrupo.sumOf { it.quantidade }
                        val qtdItens = produtosDoGrupo.size
                        ItemGrafico(
                            rotulo = categoria,
                            subtitulo = "$qtdItens item(ns) cadastrado(s)",
                            quantidade = totalQuantidade,
                            unidade = "un",
                            isEstoqueBaixo = false
                        )
                    }
                    .sortedByDescending { it.quantidade }
            }
        }

        binding.graficoBarrasView.setDados(itens, animar)
    }
}
