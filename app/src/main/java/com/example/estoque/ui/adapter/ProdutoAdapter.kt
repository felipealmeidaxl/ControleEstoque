package com.example.estoque.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.estoque.R
import com.example.estoque.data.model.Produto
import com.example.estoque.databinding.ItemProdutoBinding
import com.example.estoque.util.MoedaUtils

class ProdutoAdapter(
    private val onItemClick: (Produto) -> Unit,
    private val onIncrementarClick: (Produto) -> Unit,
    private val onDecrementarClick: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(ProdutoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProdutoViewHolder(
        private val binding: ItemProdutoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: Produto) {
            val context = itemView.context

            binding.tvNome.text = produto.nome
            binding.tvCodigoInterno.text = context.getString(R.string.formato_codigo_interno, produto.codigoInterno)

            // Código de barras / EAN
            if (!produto.codigoBarras.isNullOrBlank()) {
                binding.tvCodigoBarras.visibility = View.VISIBLE
                binding.tvCodigoBarras.text = context.getString(R.string.formato_ean, produto.codigoBarras)
            } else {
                binding.tvCodigoBarras.visibility = View.GONE
            }

            // Categoria
            if (!produto.categoria.isNullOrBlank()) {
                binding.chipCategoria.visibility = View.VISIBLE
                binding.chipCategoria.text = produto.categoria
            } else {
                binding.chipCategoria.visibility = View.GONE
            }

            // Localização
            if (!produto.localizacao.isNullOrBlank()) {
                binding.tvLocalizacao.visibility = View.VISIBLE
                binding.tvLocalizacao.text = context.getString(R.string.formato_localizacao, produto.localizacao)
            } else {
                binding.tvLocalizacao.visibility = View.GONE
            }

            // Preço de venda
            binding.tvPrecoVenda.text = MoedaUtils.formatarMoeda(produto.precoVenda)

            // Quantidade e Unidade
            binding.tvQuantidade.text = context.getString(
                R.string.formato_quantidade_unidade,
                produto.quantidade,
                produto.unidade
            )

            // Alerta de estoque baixo (quantidade <= estoqueMinimo)
            val isEstoqueBaixo = produto.quantidade <= produto.estoqueMinimo
            if (isEstoqueBaixo) {
                binding.tvQuantidade.setTextColor(ContextCompat.getColor(context, R.color.estoque_baixo))
                binding.tvAvisoEstoqueBaixo.visibility = View.VISIBLE
            } else {
                binding.tvQuantidade.setTextColor(ContextCompat.getColor(context, R.color.texto_primario))
                binding.tvAvisoEstoqueBaixo.visibility = View.GONE
            }

            // Botão decrementar desabilitado se quantidade for 0
            binding.btnDecrementar.isEnabled = produto.quantidade > 0

            // Listeners
            binding.root.setOnClickListener {
                onItemClick(produto)
            }

            binding.btnIncrementar.setOnClickListener {
                onIncrementarClick(produto)
            }

            binding.btnDecrementar.setOnClickListener {
                if (produto.quantidade > 0) {
                    onDecrementarClick(produto)
                }
            }
        }
    }

    class ProdutoDiffCallback : DiffUtil.ItemCallback<Produto>() {
        override fun areItemsTheSame(oldItem: Produto, newItem: Produto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Produto, newItem: Produto): Boolean {
            return oldItem == newItem
        }
    }
}
