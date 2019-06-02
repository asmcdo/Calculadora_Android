package com.example.calculadora_android

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.ArithmeticException

class MainActivity : AppCompatActivity() {
    private lateinit var expEntrada : TextView
    private lateinit var expSaida : TextView

    private var parenteses : MutableList<Any> = mutableListOf() //lista que ira funcionar como uma pilha para verificar validade dos parenteses

    private var unicoPonto : Boolean = false //boolean para verificar se ja existe o indicador de numero decimal
    private var ultimoDigitO : Boolean = false //boolean para verificar se o ultimo digito foi uma operacao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.expEntrada = findViewById(R.id.expEntrada)
        this.expSaida = findViewById(R.id.expSaida)
    }

    private fun onRemoveParenthesis() {
        this.parenteses.removeAt(this.parenteses.size - 1) //para cada ")", remove um "(" que estiver na lista
    }

    private fun onRetornaUltimoChar(tamanho: Int) : CharSequence { //retorna o ultimo char da string contida na TextView
        return if(this.expEntrada.text.isNotEmpty())
            this.expEntrada.text.toString().subSequence(tamanho-1, tamanho)
        else
            ""
    }

    fun onBracket(view : View) {
        val parenthesis = (view as Button).text.toString()
        if(parenthesis == "(") {
            if(!this.ultimoDigitO && this.expEntrada.text.isNotEmpty()) { //caso um "(" seja colocado apos um numero, sera indicado explicitamente que se trata de uma multiplicacao
                val tamanho = this.expEntrada.text.toString().length
                val lastChar = this.onRetornaUltimoChar(tamanho)

                if(lastChar == ".") {
                    this.alertaErro()

                    return
                }
                else if(lastChar != "(")
                    this.expEntrada.append("*")
            }

            this.parenteses.add(parenthesis)
            this.expEntrada.append((view).text)
        }
        else {
            if(((view).text) == ")" && this.ultimoDigitO) {
                this.alertaErro()

                return
            }

            if(this.parenteses.isNotEmpty()) {
                this.onRemoveParenthesis()
                this.expEntrada.append((view).text)
            }
            else {
                this.alertaErro()

                return
            }

        }

        this.unicoPonto = false
    }

    fun onBackspace(view : View) { //metodo que seleciona uma substring do tamanho-1 da string principal, para excluir o ultimo digito
        if(this.expEntrada.text.isEmpty()) {
            this.alertaErro()
        }
        else {
            var tamanho = this.expEntrada.text.toString().length
            var brckChar = this.onRetornaUltimoChar(tamanho) //guarda o ultimo digito antes de exclui-lo

            val expressao = this.expEntrada.text.removeRange(tamanho - 1, tamanho).toString()
            this.expEntrada.text = expressao

            if (brckChar == ")") //caso exclua um parentese fechando, é necessario adicionar novamente o parentese aberto
                this.parenteses.add("(")
            else if (brckChar == "(")
                this.onRemoveParenthesis() //se um parentese aberto for removido, deve ser retirado da lista tambem
            else if (brckChar == "+" || brckChar == "-" || brckChar == "*" || brckChar == "/")//caso seja removido um digito de operacao, deve ser possivel inseri-lo de novo
                this.ultimoDigitO = false
            else if (brckChar == ".")
                this.unicoPonto = false

            //verifico novamente o ultimo digito da expressao apos ter sido apagado, caso seja um operador, mostra o alerta de erro
            tamanho = this.expEntrada.text.toString().length
            brckChar = this.onRetornaUltimoChar(tamanho) //guarda o ultimo digito antes de exclui-lo

            if (brckChar == "+" || brckChar == "-" || brckChar == "*" || brckChar == "/")
                this.ultimoDigitO = true
        }
    }

    fun onNumber(view : View){
        if(this.expSaida.text.isNotEmpty()){ //se a saida contem algum valor, proximo numero digitado limpa as duas TextViews
            this.expEntrada.text = ""
            this.expSaida.text = ""
        }

        if(this.ultimoDigitO) //se selecionado um numero apos a operacao, "ultimoDigito_O" é setado como falso para poder escolher mais operacoes se necessario
            this.ultimoDigitO = false

        this.expEntrada.append((view as Button).text)
    }

    private fun alertaErro() {
        val errorAlert = AlertDialog.Builder(this@MainActivity)

        errorAlert.setTitle("Operação Inválida")
        errorAlert.setMessage("Por favor digite uma operação válida.")
        errorAlert.setPositiveButton("Ok") { //Gera o botao de "ok" no dialog mas nao mostra a Toast message
                _,_ -> Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        }

        val alert : AlertDialog = errorAlert.create()
        alert.show()
    }

    fun onOperator(view : View) {
        if(this.expEntrada.text.isNotEmpty()) {//caso nao esteja vazia a entrada, é preciso saber qual o ultimo digito para validar se pode adicionar um operador
            val tamanho = this.expEntrada.text.toString().length
            val ultimoChar = this.onRetornaUltimoChar(tamanho)

            if (ultimoChar == "(") //Caso tenha sido aberto um parentese, nao deve ser possivel adicionar um operador
                this.alertaErro()
            else {
                if (this.ultimoDigitO || ultimoChar == ".")
                    this.alertaErro()
                else {
                    this.expEntrada.append((view as Button).text)
                    this.ultimoDigitO = true //caso ultimo digito tenha sido um numero, pode-se escolher uma operacao apenas uma vez

                    if (this.unicoPonto)
                        this.unicoPonto = false //se havia um ".", depois de escolher a operacao pode-se selecionar mais um "." no proximo numero
                }
            }
        }
        else
            this.alertaErro()
    }

    fun onClear(view : View) {//metodo para limpar a tela
        if(this.unicoPonto)
            this.unicoPonto = false

        this.expEntrada.text = ""
        this.expSaida.text = ""

        this.parenteses.clear()
    }

    fun onDot(view : View) {//metodo para impedir que mais de um "." seja inserido no mesmo numero
        if (this.expSaida.text.isNotEmpty()) {
            this.expSaida.text = ""
            this.expEntrada.text = "0."
            this.unicoPonto = true

            return
        }

        if (!this.unicoPonto){
            val tamanho = this.expEntrada.text.toString().length
            val lastBrckt = this.onRetornaUltimoChar(tamanho)

            if(this.expEntrada.text.isEmpty() || this.ultimoDigitO || lastBrckt == "(" )
                this.expEntrada.append("0")
            else if(this.expEntrada.text.toString().isNotEmpty()) {
                if(lastBrckt == ")") { //verifica se ultimo digito foi um parentese fechado, para indicar explicitamente a multiplicacao
                    this.expEntrada.append("*0")

                    this.ultimoDigitO = true //seta como verdadeiro por ter sido mostrado em tela explicitamente que se trata de uma operacao
                }

            }

            this.expEntrada.append(".")
            this.unicoPonto = true
        }
        else
            this.alertaErro()
    }

    fun onEqual(view : View) {
        if(this.parenteses.isNotEmpty() || this.expEntrada.text.toString().isEmpty()
            || this.expEntrada.text.toString().contains("()") || this.expEntrada.text.toString().contains("..")
            || this.ultimoDigitO)
            this.alertaErro()
        else {
            val expression = ExpressionBuilder(this.expEntrada.text.toString()).build()//ExpressionBuilder calcula a expressao contida dentro da TextView

            try {
                val result = expression.evaluate()
                this.expSaida.text = "" //limpa o resultado para poder exibir o resultado da nova expressao
                this.expSaida.append(result.toString())
                this.parenteses.clear()
            } catch (ex : ArithmeticException) {
                this.alertaErro()
            }
        }
    }
}
