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
        expEntrada = findViewById(R.id.expEntrada)
        expSaida = findViewById(R.id.expSaida)
    }

    private fun onRemoveParenthesis() {
        parenteses.removeAt(parenteses.size - 1) //para cada ")", remove um "(" que estiver na lista
    }

    private fun onRetornaUltimoChar(tamanho: Int) : CharSequence { //retorna o ultimo char da string contida na TextView
        if(expEntrada.text.isNotEmpty())
            return expEntrada.text.toString().subSequence(tamanho-1, tamanho)
        else
            return ""
    }

    fun onBracket(view : View) {
        val parenthesis = (view as Button).text.toString()
        if(parenthesis == "(") {
            if(!ultimoDigitO && expEntrada.text.isNotEmpty()) { //caso um "(" seja colocado apos um numero, sera indicado explicitamente que se trata de uma multiplicacao
                val tamanho = expEntrada.text.toString().length
                val lastChar = onRetornaUltimoChar(tamanho)

                if(lastChar == ".") {
                    this.alertaErro()

                    return
                }
                else if(lastChar != "(")
                    expEntrada.append("*")
            }

            parenteses.add(parenthesis)
            expEntrada.append((view).text)
        }
        else {
            if(((view).text) == ")" && ultimoDigitO) {
                this.alertaErro()

                return
            }

            if(parenteses.isNotEmpty()) {
                this.onRemoveParenthesis()
                expEntrada.append((view).text)
            }
            else {
                this.alertaErro()

                return
            }

        }

        unicoPonto = false
    }

    fun onBackspace(view : View) { //metodo que seleciona uma substring do tamanho-1 da string principal, para excluir o ultimo digito
        if(expEntrada.text.isEmpty()) {
            this.alertaErro()
        }
        else {
            var tamanho = expEntrada.text.toString().length
            var brckChar = onRetornaUltimoChar(tamanho) //guarda o ultimo digito antes de exclui-lo

            var expressao = this.expEntrada.text.removeRange(tamanho - 1, tamanho).toString()
            expEntrada.text = expressao

            if (brckChar == ")") //caso exclua um parentese fechando, é necessario adicionar novamente o parentese aberto
                parenteses.add("(")
            else if (brckChar == "(")
                this.onRemoveParenthesis() //se um parentese aberto for removido, deve ser retirado da lista tambem
            else if (brckChar == "+" || brckChar == "-" || brckChar == "*" || brckChar == "/")//caso seja removido um digito de operacao, deve ser possivel inseri-lo de novo
                ultimoDigitO = false
            else if (brckChar == ".")
                unicoPonto = false

            //verifico novamente o ultimo digito da expressao apos ter sido apagado, caso seja um operador, mostra o alerta de erro
            tamanho = expEntrada.text.toString().length
            brckChar = onRetornaUltimoChar(tamanho) //guarda o ultimo digito antes de exclui-lo

            if (brckChar == "+" || brckChar == "-" || brckChar == "*" || brckChar == "/")
                ultimoDigitO = true
        }
    }

    fun onNumber(view : View){
        if(expSaida.text.isNotEmpty()){ //se a saida contem algum valor, proximo numero digitado limpa as duas TextViews
            this.expEntrada.text = ""
            this.expSaida.text = ""
        }

        if(ultimoDigitO) //se selecionado um numero apos a operacao, "ultimoDigito_O" é setado como falso para poder escolher mais operacoes se necessario
            ultimoDigitO = false

        expEntrada.append((view as Button).text)
    }

    private fun alertaErro() {
        val alertaErro = AlertDialog.Builder(this@MainActivity)

        alertaErro.setTitle("Operação Inválida")
        alertaErro.setMessage("Por favor digite uma operação válida.")
        alertaErro.setPositiveButton("Ok") { //Gera o botao de "ok" no dialog mas nao mostra a Toast message
                _,_ -> Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
        }

        val alert : AlertDialog = alertaErro.create()
        alert.show()
    }

    fun onOperator(view : View) {
        if(expEntrada.text.isNotEmpty()) {//caso nao esteja vazia a entrada, é preciso saber qual o ultimo digito para validar se pode adicionar um operador
            val tamanho = expEntrada.text.toString().length
            val ultimoChar = onRetornaUltimoChar(tamanho)

            if (ultimoChar == "(") //Caso tenha sido aberto um parentese, nao deve ser possivel adicionar um operador
                this.alertaErro()
            else {
                if (ultimoDigitO || ultimoChar == ".")
                    this.alertaErro()
                else {
                    expEntrada.append((view as Button).text)
                    ultimoDigitO = true //caso ultimo digito tenha sido um numero, pode-se escolher uma operacao apenas uma vez

                    if (unicoPonto)
                        unicoPonto = false //se havia um ".", depois de escolher a operacao pode-se selecionar mais um "." no proximo numero
                }
            }
        }
        else
            this.alertaErro()
    }

    fun onClear(view : View) {//metodo para limpar a tela
        if(unicoPonto)
            unicoPonto = false

        this.expEntrada.text = ""
        this.expSaida.text = ""

        parenteses.clear()
    }

    fun onDot(view : View) {//metodo para impedir que mais de um "." seja inserido no mesmo numero
        if (!unicoPonto){
            val tamanho = expEntrada.text.toString().length
            val lastBrckt = onRetornaUltimoChar(tamanho)

            if(expEntrada.text.isEmpty() || ultimoDigitO || lastBrckt == "(" )
                    expEntrada.append("0")
            else if(expEntrada.text.toString().isNotEmpty()) {
                if(lastBrckt == ")") { //verifica se ultimo digito foi um parentese fechado, para indicar explicitamente a multiplicacao
                    expEntrada.append("*0")

                    ultimoDigitO = true //seta como verdadeiro por ter sido mostrado em tela explicitamente que se trata de uma operacao
                }

            }

            expEntrada.append(".")
            unicoPonto = true
        }
        else
            this.alertaErro()
    }

    fun onEqual(view : View) {
        if(parenteses.isNotEmpty() || expEntrada.text.toString().isEmpty()
            || expEntrada.text.toString().contains("()") || expEntrada.text.toString().contains("..")
            || ultimoDigitO)
            this.alertaErro()
        else {
            val expression = ExpressionBuilder(expEntrada.text.toString()).build()//ExpressionBuilder calcula a expressao contida dentro da TextView

            try {
                val result = expression.evaluate()
                expSaida.text = "" //limpa o resultado para poder exibir o resultado da nova expressao
                expSaida.append(result.toString())
                parenteses.clear()
            } catch (ex : ArithmeticException) {
                this.alertaErro()
            }
        }
    }
}
