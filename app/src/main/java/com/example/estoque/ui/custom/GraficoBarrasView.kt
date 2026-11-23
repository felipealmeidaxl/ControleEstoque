package com.example.estoque.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.estoque.R

data class ItemGrafico(
    val id: Long = 0,
    val rotulo: String,
    val subtitulo: String? = null,
    val quantidade: Int,
    val estoqueMinimo: Int = 0,
    val unidade: String = "un",
    val isEstoqueBaixo: Boolean = false
)

class GraficoBarrasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var itens: List<ItemGrafico> = emptyList()
    private var maxQuantidade: Int = 1
    private var animProgress: Float = 0f
    private var animator: ValueAnimator? = null

    // Dimensões em pixels (convertidas de dp)
    private val density = resources.displayMetrics.density
    private fun dp(valor: Float): Float = valor * density
    private fun sp(valor: Float): Float =
        android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP, valor, resources.displayMetrics)

    private val alturaItem = dp(64f)
    private val alturaBarra = dp(14f)
    private val raioCanto = dp(7f)

    // Cores
    private val corPrimaria = ContextCompat.getColor(context, R.color.primary)
    private val corAlerta = ContextCompat.getColor(context, R.color.estoque_baixo)
    private val corFundoBarra = ContextCompat.getColor(context, R.color.surface_variant)
    private val corTextoPrimario = ContextCompat.getColor(context, R.color.texto_primario)
    private val corTextoSecundario = ContextCompat.getColor(context, R.color.texto_secundario)
    private val corAlertaContainer = ContextCompat.getColor(context, R.color.estoque_baixo_container)

    // Paints
    private val paintFundoBarra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corFundoBarra
        style = Paint.Style.FILL
    }

    private val paintBarraNormal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corPrimaria
        style = Paint.Style.FILL
    }

    private val paintBarraAlerta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corAlerta
        style = Paint.Style.FILL
    }

    private val paintTextoRotulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corTextoPrimario
        textSize = sp(14f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintTextoSubtitulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corTextoSecundario
        textSize = sp(11f)
    }

    private val paintTextoValor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corTextoPrimario
        textSize = sp(13f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }

    private val paintBadgeFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corAlertaContainer
        style = Paint.Style.FILL
    }

    private val paintBadgeTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corAlerta
        textSize = sp(10f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val rectF = RectF()

    fun setDados(novosItens: List<ItemGrafico>, animar: Boolean = true) {
        itens = novosItens
        val maior = novosItens.maxOfOrNull { it.quantidade } ?: 0
        maxQuantidade = if (maior > 0) maior else 1

        requestLayout()

        animator?.cancel()
        if (animar) {
            animProgress = 0f
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 750
                interpolator = DecelerateInterpolator()
                addUpdateListener { va ->
                    animProgress = va.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            animProgress = 1f
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val largura = MeasureSpec.getSize(widthMeasureSpec)
        val alturaDesejada = (paddingTop + paddingBottom + (itens.size * alturaItem).toInt())
            .coerceAtLeast(dp(100f).toInt())

        val alturaFinal = resolveSize(alturaDesejada, heightMeasureSpec)
        setMeasuredDimension(largura, alturaFinal)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (itens.isEmpty()) return

        val larguraDisponivel = width - paddingLeft - paddingRight
        var topoAtual = paddingTop.toFloat()

        for (item in itens) {
            val rotuloY = topoAtual + dp(18f)

            // 1. Rótulo (Nome do Produto / Categoria)
            val textoRotuloTratado = paintTextoRotulo.truncarTexto(
                item.rotulo,
                larguraDisponivel - dp(90f) // Reserva espaço para o valor numérico
            )
            canvas.drawText(textoRotuloTratado, paddingLeft.toFloat(), rotuloY, paintTextoRotulo)

            // 2. Valor Numérico à direita (ex: "150 un")
            val textoValor = "${item.quantidade} ${item.unidade}"
            paintTextoValor.color = if (item.isEstoqueBaixo) corAlerta else corTextoPrimario
            canvas.drawText(
                textoValor,
                (width - paddingRight).toFloat(),
                rotuloY,
                paintTextoValor
            )

            // 3. Subtítulo opcional ou Badge de Alerta
            val subY = topoAtual + dp(33f)
            if (item.isEstoqueBaixo) {
                val textoBadge = "Repor Estoque (Mín: ${item.estoqueMinimo})"
                val badgeLargura = paintBadgeTexto.measureText(textoBadge) + dp(12f)
                val badgeAltura = dp(16f)

                rectF.set(
                    paddingLeft.toFloat(),
                    subY - dp(11f),
                    paddingLeft.toFloat() + badgeLargura,
                    subY - dp(11f) + badgeAltura
                )
                canvas.drawRoundRect(rectF, dp(4f), dp(4f), paintBadgeFundo)
                canvas.drawText(
                    textoBadge,
                    paddingLeft.toFloat() + dp(6f),
                    subY,
                    paintBadgeTexto
                )
            } else if (!item.subtitulo.isNullOrBlank()) {
                canvas.drawText(
                    item.subtitulo,
                    paddingLeft.toFloat(),
                    subY,
                    paintTextoSubtitulo
                )
            }

            // 4. Barra de Progresso
            val barraY = topoAtual + dp(40f)
            val barraFim = barraY + alturaBarra

            // Fundo da barra
            rectF.set(
                paddingLeft.toFloat(),
                barraY,
                (width - paddingRight).toFloat(),
                barraFim
            )
            canvas.drawRoundRect(rectF, raioCanto, raioCanto, paintFundoBarra)

            // Barra preenchida proporcional
            val fracao = (item.quantidade.toFloat() / maxQuantidade).coerceIn(0f, 1f)
            val larguraPreenchida = (larguraDisponivel * fracao * animProgress)

            if (larguraPreenchida > 0) {
                val barraPaint = if (item.isEstoqueBaixo) paintBarraAlerta else paintBarraNormal
                rectF.set(
                    paddingLeft.toFloat(),
                    barraY,
                    (paddingLeft.toFloat() + larguraPreenchida).coerceAtLeast(paddingLeft.toFloat() + dp(8f)),
                    barraFim
                )
                canvas.drawRoundRect(rectF, raioCanto, raioCanto, barraPaint)
            }

            topoAtual += alturaItem
        }
    }

    private fun Paint.truncarTexto(texto: String, larguraMaxima: Float): String {
        if (measureText(texto) <= larguraMaxima) return texto
        val reticencias = "…"
        val larguraReticencias = measureText(reticencias)
        val espacoDisponivel = larguraMaxima - larguraReticencias

        var contagem = breakText(texto, true, espacoDisponivel, null)
        if (contagem <= 0) contagem = 1
        return texto.substring(0, contagem) + reticencias
    }
}
