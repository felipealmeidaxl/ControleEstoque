# 📦 Controle de Estoque - Android

Aplicativo Android nativo completo para **Controle de Estoque** com banco de dados local (Room), interface moderna em Material 3 (XML + ViewBinding) e painel interativo de gráficos.

---

## 📲 Download do Aplicativo (APK)

Você pode baixar e instalar o aplicativo diretamente no seu celular Android:

👉 **[📥 Baixar ControleEstoque.apk (Download Direto)](https://github.com/felipealmeidaxl/ControleEstoque/raw/main/apk/ControleEstoque.apk)**

*(O arquivo também está disponível na pasta [`apk/ControleEstoque.apk`](apk/ControleEstoque.apk) deste repositório).*

---

## ✨ Funcionalidades

### 1. 📊 Painel de Controle e Resumo
- Resumo em tempo real no topo:
  - Total de produtos cadastrados
  - Total de unidades em estoque
  - Valor financeiro total do estoque (R$)
  - Alerta de produtos com estoque baixo/crítico
- Busca em tempo real por nome, código interno, EAN ou categoria.
- Leitor de código de barras integrado ao campo de busca.

### 2. 📋 Gerenciamento de Produtos
- Listagem dinâmica em Cards usando `RecyclerView` + `ListAdapter` e `DiffUtil`.
- Botões de ajuste rápido (`+` e `−`) diretamente nos cards para entrada/saída de 1 unidade sem nunca negativar o saldo.
- Alerta visual em vermelho com etiqueta **"Repor estoque"** quando a quantidade estiver menor ou igual ao estoque mínimo definido.
- Cadastro e Edição completos:
  - Nome (obrigatório)
  - Código Interno (obrigatório, com índice único e validação amigável de duplicidade)
  - Código de Barras (EAN, com scanner via Google Code Scanner sem necessidade de permissão de câmera)
  - Categoria, Fornecedor, Unidade de medida e Localização (prateleira/corredor)
  - Quantidade inicial e Estoque mínimo
  - Preços de Custo e Venda (aceita vírgula ou ponto)
  - Observações adicionais
  - Exclusão com diálogo de confirmação

### 3. 📈 Tela de Gráficos de Estoque
- Acesso rápido por botão dedicado no rodapé da tela principal.
- Gráfico de barras horizontais nativo com animação suave de preenchimento.
- Destaques automáticos dos produtos com **Maior Estoque** e **Menor Estoque**.
- Visão dupla:
  - **Por Produto**: exibe cada item individualmente, com destaque vermelho para produtos críticos.
  - **Por Categoria**: consolida a soma das unidades por grupo/categoria.
- Ordenação instantânea: *Maior Quantidade*, *Menor Quantidade* e *Ordem Alfabética*.

---

## 🛠️ Stack Tecnológica

- **Linguagem:** Kotlin 1.9.24
- **Interface:** Views XML com ViewBinding e componentes Material 3 (suporte a Light e Dark Theme)
- **Banco de Dados Local:** Room 2.6.1 (SQLite) com processamento de anotações via KSP 1.9.24-1.0.20
- **Arquitetura:** MVVM (ViewModel + Coroutines + Flow / StateFlow)
- **Scanner:** Google Code Scanner (`play-services-code-scanner:16.1.0`)
- **Build System:** Gradle 8.7 (Kotlin DSL) com Android Gradle Plugin 8.5.2
- **Configurações:** `minSdk 24`, `compileSdk 34`, `targetSdk 34`, JVM 17

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Android Studio instalado (Ladybug, Koala, Jellyfish ou superior)
- JDK 17 configurado

### Passos
1. Clone ou baixe este repositório.
2. Abra o **Android Studio**.
3. Vá em **File > Open...** e selecione a pasta raiz deste projeto.
4. Aguarde a sincronização automática do Gradle (*Sync Project with Gradle Files*).
5. Selecione um emulador ou dispositivo físico com Android 7.0+ (API 24+).
6. Clique no botão **Run ▶** (`Shift + F10`).

---

## 📄 Licença
Distribuído sob a licença MIT. Sinta-se livre para usar e modificar.
