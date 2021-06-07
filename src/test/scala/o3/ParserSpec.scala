package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFunSpec with Matchers {
  type Expresion = Any

  case class Programa(expresiones: List[Expresion])

  private def programa(expresiones: Expresion*) = {
    Programa(expresiones.toList)
  }

  describe("el parser") {
    val numero = identity[Int] _
    val booleano = identity[Boolean] _
    val cadena = identity[String] _

    val operacionBinaria = (operador: String) => (operando1: Expresion, operando2: Expresion) => (operador, operando1, operando2)

    val suma = operacionBinaria("+")
    val resta = operacionBinaria("-")
    val multiplicacion = operacionBinaria("*")
    val division = operacionBinaria("/")

    val igual = operacionBinaria("=")
    val distinto = operacionBinaria("/=")
    val mayor = operacionBinaria(">")
    val mayorIgual = operacionBinaria(">=")
    val menor = operacionBinaria("<")
    val menorIgual = operacionBinaria("<=")

    val declaracionVariable = (nombre: String, valorInicial: Expresion) => ("let", nombre, valorInicial)
    val variable = (nombre: String) => ("var", nombre)
    val asignacion = (nombre: String, valorNuevo: Expresion) => ("asign", nombre, valorNuevo)

    val concatenacion = operacionBinaria("++")

    val si = (cond: Expresion, pos: List[Expresion], neg: List[Expresion]) => ("if", cond, pos, neg)

    val lambda = (parametros: List[String], cuerpo: List[Expresion]) => ("lambda", parametros, cuerpo)
    val aplicacion = (funcion: Expresion, argumentos: List[Expresion]) => ("aplicacion", funcion, argumentos)

    implicit def int2Numero: Int => Expresion = numero

    describe("función faltante") {
      it("numero") {
        an [ParserLepifyo.MissingFunctionError] should be thrownBy {
          val parser = new ParserLepifyo[Programa, Expresion]()
          parser.parsear("12")
        }
      }
    }

    describe("parsea") {
      val parser = new ParserLepifyo[Programa, Expresion](
        programa = Programa,
        numero = numero,
        booleano = booleano,
        string = cadena,
        suma = suma,
        resta = resta,
        multiplicacion = multiplicacion,
        division = division,
        igual = igual,
        distinto = distinto,
        mayor = mayor,
        mayorIgual = mayorIgual,
        menor = menor,
        menorIgual = menorIgual,
        declaracionVariable = declaracionVariable,
        variable = variable,
        asignacion = asignacion,
        concatenacion = concatenacion,
        si = si,
        lambda = lambda,
        aplicacion = aplicacion
      )

      it("números literales") {
        val ast = parser.parsear("12")

        ast should equal(programa(numero(12)))
      }

      it("sumas de números") {
        val ast = parser.parsear("12 + 34 + 56")

        ast should equal(programa(suma(suma(12, 34), 56)))
      }

      it("sumas con multiplicación a la derecha") {
        val ast = parser.parsear("12 + 34 * 56")

        ast should equal(programa(suma(12, multiplicacion(34, 56))))
      }

      it("sumas con multiplicación a la izquierda") {
        val ast = parser.parsear("12 * 34 + 56")

        ast should equal(programa(suma(multiplicacion(12, 34), 56)))
      }

      it("multiplicaciones de números") {
        val ast = parser.parsear("12 * 34 * 56")

        ast should equal(programa(multiplicacion(multiplicacion(12, 34), 56)))
      }

      it("multiplicaciones de sumas") {
        val ast = parser.parsear("12 * (34 + 56)")

        ast should equal(programa(multiplicacion(12, suma(34, 56))))
      }

      it("resta de numeros") {
        val ast = parser.parsear("12 - 34 - 56")

        ast should equal(programa(resta(resta(12, 34), 56)))
      }

      it("resta de suma a la derecha") {
        val ast = parser.parsear("12 - 34 + 56")

        ast should equal(programa(suma(resta(12, 34), 56)))
      }

      it("resta de suma a la izquierda") {
        val ast = parser.parsear("12 + 34 - 56")

        ast should equal(programa(resta(suma(12, 34), 56)))
      }

      it("division de numeros") {
        val ast = parser.parsear("12 / 34 / 56")

        ast should equal(programa(division(division(12, 34), 56)))
      }

      it("igualdad de numeros") {
        val ast = parser.parsear("12 + 15 == 56 - 21")

        ast should equal(programa(igual(suma(12, 15), resta(56, 21))))
      }

      it("desigualdad de numeros") {
        val ast = parser.parsear("12 + 15 != 56 - 21")

        ast should equal(programa(distinto(suma(12, 15), resta(56, 21))))
      }

      it("comparacion por mayor entre numeros") {
        val ast = parser.parsear("12 + 15 > 56 - 21")

        ast should equal(programa(mayor(suma(12, 15), resta(56, 21))))
      }

      it("comparacion por mayor igual entre numeros") {
        val ast = parser.parsear("12 + 15 >= 56 - 21")

        ast should equal(programa(mayorIgual(suma(12, 15), resta(56, 21))))
      }

      it("comparacion por menor entre numeros") {
        val ast = parser.parsear("12 + 15 < 56 - 21")

        ast should equal(programa(menor(suma(12, 15), resta(56, 21))))
      }

      it("comparacion por menor igual entre numeros") {
        val ast = parser.parsear("12 + 15 <= 56 - 21")

        ast should equal(programa(menorIgual(suma(12, 15), resta(56, 21))))
      }

      it("igualdad entre comparaciones") {
        val ast = parser.parsear("12 < 56 == 14 > 13")

        ast should equal(programa(igual(menor(12, 56), mayor(14, 13))))
      }

      it("igualdad entre igualdades") {
        val ast = parser.parsear("12 == 56 == 14")

        ast should equal(programa(igual(igual(12, 56), 14)))
      }

      it("booleanos literales") {
        val ast = parser.parsear("true == false")

        ast should equal(programa(igual(booleano(true), booleano(false))))
      }

      it("programas con una expresion por línea") {
        val ast = parser.parsear("1 + 2\n2 + 1")

        ast should equal(programa(suma(1, 2), suma(2, 1)))
      }

      it("programas con expresiones en más de una línea") {
        val ast = parser.parsear("1 +\n2")

        ast should equal(programa(suma(1, 2)))
      }

      it("programas vacíos") {
        val ast = parser.parsear(" \n")

        ast should equal(Programa(List()))
      }

      it("declaraciones de variables") {
        val ast = parser.parsear("let variable = 12")

        ast should equal(programa(declaracionVariable("variable", 12)))
      }

      it("identificadores con números, mayúsculas y guiones bajos") {
        val ast = parser.parsear("let _mi_Variable123 = 12")

        ast should equal(programa(declaracionVariable("_mi_Variable123", 12)))
      }

      it("los identificadores no pueden empezar con números") {
        an [ParserLepifyo.ParseError] should be thrownBy {
          parser.parsear("let 123 = 12")
        }
      }

      it("los identificadores pueden empezar con mayúsculas") {
        val ast = parser.parsear("let MiVariable = 12")

        ast should equal(programa(declaracionVariable("MiVariable", 12)))
      }

      it("usos de variables") {
        val ast = parser.parsear("numerador / denominador")

        ast should equal(programa(division(variable("numerador"), variable("denominador"))))
      }

      it("asignaciones de variables") {
        val ast = parser.parsear("numerador = 10")

        ast should equal(programa(asignacion("numerador", 10)))
      }

      it("if inline") {
        val ast = parser.parsear("if(1 > 3) then 2 + 1 else 3 * 2")

        ast should equal(programa(si(mayor(1, 3), List(suma(2, 1)), List(multiplicacion(3, 2)))))
      }

      it("if multilinea sin espacios") {
        val ast = parser.parsear("if(1 > 3) then{ 2 }else{ 3 }")

        ast should equal(programa(si(mayor(1, 3), List(2), List(3))))
      }

      it("if multilinea con espacios") {
        val ast = parser.parsear("if(1 > 3) then { 2 } else { 3 }")

        ast should equal(programa(si(mayor(1, 3), List(2), List(3))))
      }

      it("if con espacio entre if y condición") {
        val ast = parser.parsear("if (1 > 3) then 2 else 4")

        ast should equal(programa(si(mayor(1, 3), List(2), List(4))))
      }

      it("if sin espacios entre condición y then") {
        val ast = parser.parsear("if (1 > 3)then 2 else 4")

        ast should equal(programa(si(mayor(1, 3), List(2), List(4))))
      }

      it("if con then inline y else multilinea") {
        val ast = parser.parsear("if(1 > 3) then 2 else { 3 }")

        ast should equal(programa(si(mayor(1, 3), List(2), List(3))))
      }

      it("if con then multilinea y else inline") {
        val ast = parser.parsear("if(1 > 3) then { 2 } else 3")

        ast should equal(programa(si(mayor(1, 3), List(2), List(3))))
      }

      it("if solo con then inline") {
        val ast = parser.parsear("if(1 > 3) then 2")

        ast should equal(programa(si(mayor(1, 3), List(2), List())))
      }

      it("if solo con then multilinea") {
        val ast = parser.parsear("if(1 > 3) then { 2 }")

        ast should equal(programa(si(mayor(1, 3), List(2), List())))
      }

      it("if dentro de una expresion") {
        val ast = parser.parsear("(if(1 > 3) then 2 else 3) + 4")

        ast should equal(programa(suma(si(mayor(1, 3), List(2), List(3)), 4)))
      }

      it("if dentro del else de otro if") {
        val ast = parser.parsear("if(1 > 3) then 2 else if(1 > 3) then 2 else 3")

        ast should equal(programa(si(mayor(1, 3), List(2), List(si(mayor(1, 3), List(2), List(3))))))
      }

      it("if dentro del then de otro if") {
        val ast = parser.parsear("if(1 > 3) then if(2 < 1) then 2 else 3 else 4")

        ast should equal(programa(si(mayor(1, 3), List(si(menor(2, 1), List(2), List(3))), List(4))))
      }

      it("if dentro del then de otro if sin else") {
        val ast = parser.parsear("if(1 > 3) then if(2 < 1) then 2 else 3")

        ast should equal(programa(si(mayor(1, 3), List(si(menor(2, 1), List(2), List(3))), List())))
      }

      it("if sin paréntesis en la condición") {
        val ast = parser.parsear("if 1 > 3 then 2 + 1 else 3 * 2")

        ast should equal(programa(si(mayor(1, 3), List(suma(2, 1)), List(multiplicacion(3, 2)))))
      }

      it("print numero") {
        val ast = parser.parsear("printLn(1)")

        ast should equal(programa(aplicacion(variable("printLn"), List(1))))
      }

      it("print dentro de if") {
        val ast = parser.parsear("if(2 > 1) then printLn(1)")

        ast should equal(programa(si(mayor(2,1), List(aplicacion(variable("printLn"), List(1))), List())))
      }

      it("string vacío") {
        val ast = parser.parsear("\"\"")

        ast should equal(programa(cadena("")))
      }

      it("string no vacío") {
        val ast = parser.parsear("\"Un String 123\"")

        ast should equal(programa(cadena("Un String 123")))
      }

      it("string seguido de otro") {
        val ast = parser.parsear(""" "hola" "mundo" """)

        ast should equal(programa(cadena("hola"), cadena("mundo")))
      }

      it("string con comillas") {
        val ast = parser.parsear(""" "algo: \"" """)

        ast should equal(programa(cadena("algo: \"")))
      }

      it("string con barras") {
        val ast = parser.parsear(""" "algo: \\ " """)

        ast should equal(programa(cadena("algo: \\ ")))
      }

      it("string con barras al final") {
        val ast = parser.parsear(""" "algo: \\" """)

        ast should equal(programa(cadena("algo: \\")))
      }

      it("string con espacios al principio") {
        val ast = parser.parsear(""" " el string" """)

        ast should equal(programa(cadena(" el string")))
      }

      it("string con espacios al final") {
        val ast = parser.parsear(""" "el string " """)

        ast should equal(programa(cadena("el string ")))
      }

      it("print de un string") {
        val ast = parser.parsear("printLn(\"hola mundo\")")

        ast should equal(programa(aplicacion(variable("printLn"), List(cadena("hola mundo")))))
      }

      it("concatenacion de strings") {
        val ast = parser.parsear("\"hola\" ++ \" mundo\"")

        ast should equal(programa(concatenacion(cadena("hola"), cadena(" mundo"))))
      }

      it("concatenacion de sumas de números") {
        val ast = parser.parsear("\"El resultado es: \" ++ 2 + 2")

        ast should equal(programa(
          concatenacion(cadena("El resultado es: "), suma(2, 2))
        ))
      }

      it("prompt int") {
        val ast = parser.parsear("promptInt(\"Ingrese un número: \")")

        ast should equal(programa(aplicacion(variable("promptInt"), List(cadena("Ingrese un número: ")))))
      }

      it("prompt bool") {
        val ast = parser.parsear("promptBool(\"Ingrese un booleano: \")")

        ast should equal(programa(aplicacion(variable("promptBool"), List(cadena("Ingrese un booleano: ")))))
      }

      it("prompt string") {
        val ast = parser.parsear("promptString(\"Ingrese un string: \")")

        ast should equal(programa(aplicacion(variable("promptString"), List(cadena("Ingrese un string: ")))))
      }

      it("funciones con espacios antes de los parámetros") {
        val ast = parser.parsear("promptString (\"Ingrese un string: \")")

        ast should equal(programa(aplicacion(variable("promptString"), List(cadena("Ingrese un string: ")))))
      }

      it("declarar una variable con un prompt") {
        val ast = parser.parsear("let i = promptInt(\"Ingrese un número: \")")

        ast should equal(programa(declaracionVariable("i", aplicacion(variable("promptInt"), List(cadena("Ingrese un número: "))))))
      }

      it("lambda sin parámetros que devuelve una expresión") {
        val ast = parser.parsear("() -> 2 + 2")

        ast should equal(programa(
          lambda(List(), List(suma(2, 2)))
        ))
      }

      it("lambda con un parámetro que devuelve una expresión") {
        val ast = parser.parsear("(x) -> x")

        ast should equal(programa(
          lambda(List("x"), List(variable("x")))
        ))
      }

      it("lambda con parámetros que devuelve una expresión") {
        val ast = parser.parsear("(primero, segundo) -> primero")

        ast should equal(programa(
          lambda(List("primero", "segundo"), List(variable("primero")))
        ))
      }

      it("lambda con cuerpo que contiene más de una instrucción") {
        val ast = parser.parsear("() -> {\n\tlet y = 1\n\ty\n}")

        ast should equal(programa(
          lambda(List(), List(declaracionVariable("y", 1), variable("y")))
        ))
      }

      it("aplicación de lambda") {
        val ast = parser.parsear("(() -> 2)()")

        ast should equal(programa(
          aplicacion(lambda(List(), List(2)), List())
        ))
      }

      it("aplicación de variables") {
        val ast = parser.parsear("f(2)")

        ast should equal(programa(
          aplicacion(variable("f"), List(2))
        ))
      }

      it("cadena de aplicaciones") {
        val ast = parser.parsear("f(1)(2)")

        ast should equal(programa(
          aplicacion(aplicacion(variable("f"), List(1)), List(2))
        ))
      }

      it("eñes y tildes como parte de identificadores") {
        val ast = parser.parsear("ñañá(último)")

        ast should equal(programa(
          aplicacion(variable("ñañá"), List(variable("último")))
        ))
      }

      it("emojis como parte de identificadores") {
        val ast = parser.parsear("\uD83D\uDE04\uD83D\uDD96\uD83C\uDFFB")

        ast should equal(programa(
          variable("\uD83D\uDE04\uD83D\uDD96\uD83C\uDFFB")
        ))
      }
    }
  }
}
