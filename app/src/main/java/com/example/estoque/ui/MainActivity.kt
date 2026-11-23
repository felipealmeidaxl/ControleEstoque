package com.example.estoque.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.estoque.EstoqueApplication
import com.example.estoque.R
import com.example.estoque.databinding.ActivityMainBinding
import com.example.estoque.ui.adapter.ProdutoAdapter
import com.example.estoque.ui.viewmodel.ProdutoViewModel
import com.example.estoque.ui.viewmodel.ProdutoViewModelFactory
import com.example.estoque.util.MoedaUtils
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProdutoViewModel by viewModels {
        ProdutoViewModelFactory((application as EstoqueApplication).repository)
    }

    private lateinit var adapter: ProdutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarBusca()
        configurarBarcodeScanner()
        configurarBotoesInferiores()
        observarDados()
    }

    private fun configurarRecyclerView() {
        adapter = ProdutoAdapter(
            onItemClick = { produto ->
                val intent = Intent(this, ProdutoFormActivity::class.java).apply {
                    putExtra(ProdutoFormActivity.EXTRA_PRODUTO_ID, produto.id)
                }
                startActivity(intent)
            },
            onIncrementarClick = { produto ->
                viewModel.ajustarQuantidade(produto.id, 1)
            },
            onDecrementarClick = { produto ->
                viewModel.ajustarQuantidade(produto.id, -1)
            }
        )

        binding.rvProdutos.layoutManager = LinearLayoutManager(this)
        binding.rvProdutos.adapter = adapter
    }

    private fun configurarBusca() {
        binding.etBusca.addTextChangedListener { editable ->
            val termo = editable?.toString().orEmpty()
            viewModel.setTermoBusca(termo)
        }
    }

    private fun configurarBarcodeScanner() {
        binding.btnEscanearBusca.setOnClickListener {
            val scanner = GmsBarcodeScanning.getClient(this)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val valor = barcode.rawValue
                    if (!valor.isNullOrBlank()) {
                        binding.etBusca.setText(valor)
                        binding.etBusca.setSelection(valor.length)
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

    private fun configurarBotoesInferiores() {
        binding.fabNovoProduto.setOnClickListener {
            val intent = Intent(this, ProdutoFormActivity::class.java)
            startActivity(intent)
        }

        binding.btnGraficos.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observarDados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.produtos.collectLatest { produtos ->
                        adapter.submitList(produtos)
                        if (produtos.isEmpty()) {
                            binding.layoutListaVazia.visibility = View.VISIBLE
                            binding.rvProdutos.visibility = View.GONE
                        } else {
                            binding.layoutListaVazia.visibility = View.GONE
                            binding.rvProdutos.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    viewModel.resumoEstoque.collectLatest { resumo ->
                        binding.tvResumoTotalProdutos.text = resumo.totalProdutos.toString()
                        binding.tvResumoTotalUnidades.text = resumo.totalUnidades.toString()
                        binding.tvResumoValorEstoque.text = MoedaUtils.formatarMoeda(resumo.valorTotalEstoque)
                        binding.tvResumoEstoqueBaixo.text = resumo.quantidadeEstoqueBaixo.toString()
                    }
                }
            }
        }
    }
}
