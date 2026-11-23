package com.example.estoque.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.estoque.EstoqueApplication
import com.example.estoque.R
import com.example.estoque.data.model.Produto
import com.example.estoque.databinding.ActivityProdutoFormBinding
import com.example.estoque.ui.viewmodel.DeleteState
import com.example.estoque.ui.viewmodel.FormState
import com.example.estoque.ui.viewmodel.ProdutoViewModel
import com.example.estoque.ui.viewmodel.ProdutoViewModelFactory
import com.example.estoque.util.MoedaUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProdutoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUTO_ID = "extra_produto_id"
    }

    private lateinit var binding: ActivityProdutoFormBinding
    private val viewModel: ProdutoViewModel by viewModels {
        ProdutoViewModelFactory((application as EstoqueApplication).repository)
    }

    private var produtoId: Long = 0L
    private var produtoAtual: Produto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdutoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        produtoId = intent.getLongExtra(EXTRA_PRODUTO_ID, 0L)

        configurarToolbar()
        configurarBarcodeScanner()
        configurarBotoes()
        limparErrosAoDigitar()
        observarEstados()

        if (produtoId > 0) {
            carregarDadosProduto(produtoId)
        } else {
            binding.btnExcluir.visibility = View.GONE
            binding.etUnidade.setText("un")
        }
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (produtoId > 0) {
            getString(R.string.titulo_editar_produto)
        } else {
            getString(R.string.titulo_novo_produto)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarBarcodeScanner() {
        binding.btnEscanearCodigoBarras.setOnClickListener {
            val scanner = GmsBarcodeScanning.getClient(this)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val codigo = barcode.rawValue
                    if (!codigo.isNullOrBlank()) {
                        binding.etCodigoBarras.setText(codigo)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        getString(R.string.erro_leitura_codigo_barras),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun configurarBotoes() {
        binding.btnSalvar.setOnClickListener {
            salvar()
        }

        binding.btnExcluir.setOnClickListener {
            confirmarExclusao()
        }
    }

    private fun limparErrosAoDigitar() {
        binding.etNome.doOnTextChanged { _, _, _, _ ->
            binding.tilNome.error = null
        }
        binding.etCodigoInterno.doOnTextChanged { _, _, _, _ ->
            binding.tilCodigoInterno.error = null
        }
    }

    private fun carregarDadosProduto(id: Long) {
        lifecycleScope.launch {
            val produto = viewModel.carregarProdutoPorId(id)
            if (produto != null) {
                produtoAtual = produto
                preencherCampos(produto)
            } else {
                Toast.makeText(this@ProdutoFormActivity, getString(R.string.produto_nao_encontrado), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun preencherCampos(produto: Produto) {
        binding.etNome.setText(produto.nome)
        binding.etCodigoInterno.setText(produto.codigoInterno)
        binding.etCodigoBarras.setText(produto.codigoBarras.orEmpty())
        binding.etCategoria.setText(produto.categoria.orEmpty())
        binding.etFornecedor.setText(produto.fornecedor.orEmpty())
        binding.etUnidade.setText(produto.unidade.ifBlank { "un" })
        binding.etLocalizacao.setText(produto.localizacao.orEmpty())
        binding.etQuantidade.setText(produto.quantidade.toString())
        binding.etEstoqueMinimo.setText(produto.estoqueMinimo.toString())
        binding.etPrecoCusto.setText(String.format(java.util.Locale.US, "%.2f", produto.precoCusto).replace(".", ","))
        binding.etPrecoVenda.setText(String.format(java.util.Locale.US, "%.2f", produto.precoVenda).replace(".", ","))
        binding.etObservacoes.setText(produto.observacoes.orEmpty())
        binding.btnExcluir.visibility = View.VISIBLE
    }

    private fun salvar() {
        val nome = binding.etNome.text?.toString().orEmpty().trim()
        val codigoInterno = binding.etCodigoInterno.text?.toString().orEmpty().trim()

        var temErro = false
        if (nome.isBlank()) {
            binding.tilNome.error = getString(R.string.erro_campo_obrigatorio)
            temErro = true
        }

        if (codigoInterno.isBlank()) {
            binding.tilCodigoInterno.error = getString(R.string.erro_campo_obrigatorio)
            temErro = true
        }

        if (temErro) return

        val codigoBarras = binding.etCodigoBarras.text?.toString()?.trim()
        val categoria = binding.etCategoria.text?.toString()?.trim()
        val fornecedor = binding.etFornecedor.text?.toString()?.trim()
        val unidade = binding.etUnidade.text?.toString()?.trim().let { if (it.isNullOrBlank()) "un" else it }
        val localizacao = binding.etLocalizacao.text?.toString()?.trim()
        val observacoes = binding.etObservacoes.text?.toString()?.trim()

        val quantidade = binding.etQuantidade.text?.toString()?.toIntOrNull() ?: 0
        val estoqueMinimo = binding.etEstoqueMinimo.text?.toString()?.toIntOrNull() ?: 0

        val precoCusto = MoedaUtils.parsearPrecoDireto(binding.etPrecoCusto.text?.toString())
        val precoVenda = MoedaUtils.parsearPrecoDireto(binding.etPrecoVenda.text?.toString())

        viewModel.salvarProduto(
            id = produtoId,
            nome = nome,
            codigoInterno = codigoInterno,
            codigoBarras = codigoBarras,
            categoria = categoria,
            fornecedor = fornecedor,
            unidade = unidade,
            localizacao = localizacao,
            observacoes = observacoes,
            quantidade = quantidade,
            estoqueMinimo = estoqueMinimo,
            precoCusto = precoCusto,
            precoVenda = precoVenda,
            dataCadastroOriginal = produtoAtual?.dataCadastro
        )
    }

    private fun confirmarExclusao() {
        val produto = produtoAtual ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_excluir_titulo)
            .setMessage(getString(R.string.dialog_excluir_mensagem, produto.nome))
            .setPositiveButton(R.string.excluir) { _, _ ->
                viewModel.excluirProduto(produto)
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collectLatest { state ->
                        when (state) {
                            is FormState.Loading -> {
                                binding.btnSalvar.isEnabled = false
                            }
                            is FormState.Success -> {
                                binding.btnSalvar.isEnabled = true
                                Toast.makeText(
                                    this@ProdutoFormActivity,
                                    getString(R.string.produto_salvo_sucesso),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.resetFormState()
                                finish()
                            }
                            is FormState.Error -> {
                                binding.btnSalvar.isEnabled = true
                                if (state.isCodigoDuplicado) {
                                    binding.tilCodigoInterno.error = state.message
                                    binding.etCodigoInterno.requestFocus()
                                } else {
                                    Toast.makeText(
                                        this@ProdutoFormActivity,
                                        state.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                viewModel.resetFormState()
                            }
                            FormState.Idle -> {
                                binding.btnSalvar.isEnabled = true
                            }
                        }
                    }
                }

                launch {
                    viewModel.deleteState.collectLatest { state ->
                        when (state) {
                            is DeleteState.Success -> {
                                Toast.makeText(
                                    this@ProdutoFormActivity,
                                    getString(R.string.produto_excluido_sucesso),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.resetDeleteState()
                                finish()
                            }
                            is DeleteState.Error -> {
                                Toast.makeText(
                                    this@ProdutoFormActivity,
                                    state.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.resetDeleteState()
                            }
                            DeleteState.Idle -> {}
                        }
                    }
                }
            }
        }
    }
}
