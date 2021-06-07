package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFunSpec with Matchers {

  implicit def int2Numero: Int => Expresion = Numero

  case class Programa(expresiones: List[Expresion])

  class Expresion

  case class Numero(valor: Int) extends Expresion
  case class Booleano(valor: Boolean) extends Expresion
  case class Cadena(valor: String) extends Expresion

  case class Suma(sumando1: Expresion, sumando2: Expresion) extends Expresion
  case class Resta(minuendo: Expresion, sustraendo: Expresion) extends Expresion
  case class Multiplicacion(factor1: Expresion, factor2: Expresion) extends Expresion
  case class Division(dividendo: Expresion, divisor: Expresion) extends Expresion

  case class Igual(expresion1: Expresion, expresion2: Expresion) extends Expresion
  case class Distinto(expresion1: Expresion, expresion2: Expresion) extends Expresion
  case class Mayor(expresion1: Expresion, expresion2: Expresion) extends Expresion
  case class MayorIgual(expresion1: Expresion, expresion2: Expresion) extends Expresion
  case class Menor(expresion1: Expresion, expresion2: Expresion) extends Expresion
  case class MenorIgual(expresion1: Expresion, expresion2: Expresion) extends Expresion

  case class DeclaracionVariable(nombre: String, valorInicial: Expresion) extends Expresion
  case class Variable(nombre: String) extends Expresion
  case class Asignacion(nombre: String, valorNuevo: Expresion) extends Expresion

  case class Concatenacion(expresion1: Expresion, expresion2: Expresion) extends Expresion

  case class If(cond: Expresion, pos: List[Expresion], neg: List[Expresion]) extends Expresion

  case class Lambda(parametros: List[String], cuerpo: List[Expresion]) extends Expresion
  case class Aplicacion(funcion: Expresion, argumentos: List[Expresion]) extends Expresion

  describe("función faltante") {
    it("numero") {
      an [ParserLepifyo.MissingFunctionError] should be thrownBy {
        val parser = new ParserLepifyo[Programa, Expresion]()
        parser.parsear("12")
      }
    }
  }

  describe("parsea") {
    val parser = new ParserLepifyo[Programa, Expresion](programa = Programa, numero = Numero, booleano = Booleano, string = Cadena, suma = Suma, resta = Resta, multiplicacion = Multiplicacion, division = Division, igual = Igual, distinto = Distinto, mayor = Mayor, mayorIgual = MayorIgual, menor = Menor, menorIgual = MenorIgual, declaracionVariable = DeclaracionVariable, variable = Variable, asignacion = Asignacion, concatenacion = Concatenacion, si = If, lambda = Lambda, aplicacion = Aplicacion)

    it("números literales") {
      val ast = parser.parsear("12")

      ast should equal(programa(Numero(12)))
    }

    it("sumas de números") {
      val ast = parser.parsear("12 + 34 + 56")

      ast should equal(programa(Suma(Suma(12, 34), 56)))
    }

    it("sumas con multiplicación a la derecha") {
      val ast = parser.parsear("12 + 34 * 56")

      ast should equal(programa(Suma(12, Multiplicacion(34, 56))))
    }

    it("sumas con multiplicación a la izquierda") {
      val ast = parser.parsear("12 * 34 + 56")

      ast should equal(programa(Suma(Multiplicacion(12, 34), 56)))
    }

    it("multiplicaciones de números") {
      val ast = parser.parsear("12 * 34 * 56")

      ast should equal(programa(Multiplicacion(Multiplicacion(12, 34), 56)))
    }

    it("multiplicaciones de sumas") {
      val ast = parser.parsear("12 * (34 + 56)")

      ast should equal(programa(Multiplicacion(12, Suma(34, 56))))
    }

    it("resta de numeros") {
      val ast = parser.parsear("12 - 34 - 56")

      ast should equal(programa(Resta(Resta(12, 34), 56)))
    }

    it("resta de suma a la derecha") {
      val ast = parser.parsear("12 - 34 + 56")

      ast should equal(programa(Suma(Resta(12, 34), 56)))
    }

    it("resta de suma a la izquierda") {
      val ast = parser.parsear("12 + 34 - 56")

      ast should equal(programa(Resta(Suma(12, 34), 56)))
    }

    it("division de numeros") {
      val ast = parser.parsear("12 / 34 / 56")

      ast should equal(programa(Division(Division(12, 34), 56)))
    }

    it("igualdad de numeros") {
      val ast = parser.parsear("12 + 15 == 56 - 21")

      ast should equal(programa(Igual(Suma(12, 15), Resta(56, 21))))
    }

    it("desigualdad de numeros") {
      val ast = parser.parsear("12 + 15 != 56 - 21")

      ast should equal(programa(Distinto(Suma(12, 15), Resta(56, 21))))
    }

    it("comparacion por mayor entre numeros") {
      val ast = parser.parsear("12 + 15 > 56 - 21")

      ast should equal(programa(Mayor(Suma(12, 15), Resta(56, 21))))
    }

    it("comparacion por mayor igual entre numeros") {
      val ast = parser.parsear("12 + 15 >= 56 - 21")

      ast should equal(programa(MayorIgual(Suma(12, 15), Resta(56, 21))))
    }

    it("comparacion por menor entre numeros") {
      val ast = parser.parsear("12 + 15 < 56 - 21")

      ast should equal(programa(Menor(Suma(12, 15), Resta(56, 21))))
    }

    it("comparacion por menor igual entre numeros") {
      val ast = parser.parsear("12 + 15 <= 56 - 21")

      ast should equal(programa(MenorIgual(Suma(12, 15), Resta(56, 21))))
    }

    it("igualdad entre comparaciones") {
      val ast = parser.parsear("12 < 56 == 14 > 13")

      ast should equal(programa(Igual(Menor(12, 56), Mayor(14, 13))))
    }

    it("igualdad entre igualdades") {
      val ast = parser.parsear("12 == 56 == 14")

      ast should equal(programa(Igual(Igual(12, 56), 14)))
    }

    it("booleanos literales") {
      val ast = parser.parsear("true == false")

      ast should equal(programa(Igual(Booleano(true), Booleano(false))))
    }

    it("programas con una expresion por línea") {
      val ast = parser.parsear("1 + 2\n2 + 1")

      ast should equal(programa(Suma(1, 2), Suma(2, 1)))
    }

    it("programas con expresiones en más de una línea") {
      val ast = parser.parsear("1 +\n2")

      ast should equal(programa(Suma(1, 2)))
    }

    it("programas vacíos") {
      val ast = parser.parsear(" \n")

      ast should equal(Programa(List()))
    }

    it("declaraciones de variables") {
      val ast = parser.parsear("let variable = 12")

      ast should equal(programa(DeclaracionVariable("variable", 12)))
    }

    it("identificadores con números, mayúsculas y guiones bajos") {
      val ast = parser.parsear("let _mi_Variable123 = 12")

      ast should equal(programa(DeclaracionVariable("_mi_Variable123", 12)))
    }

    it("los identificadores no pueden empezar con números") {
      an [ParserLepifyo.ParseError] should be thrownBy {
        parser.parsear("let 123 = 12")
      }
    }

    it("los identificadores no pueden empezar con mayúsculas") {
      an [ParserLepifyo.ParseError] should be thrownBy {
        parser.parsear("let Holis = 12")
      }
    }

    it("usos de variables") {
      val ast = parser.parsear("numerador / denominador")

      ast should equal(programa(Division(Variable("numerador"), Variable("denominador"))))
    }

    it("asignaciones de variables") {
      val ast = parser.parsear("numerador = 10")

      ast should equal(programa(Asignacion("numerador", 10)))
    }

    it("if inline") {
      val ast = parser.parsear("if(1 > 3) then 2 + 1 else 3 * 2")

      ast should equal(programa(If(Mayor(1, 3), List(Suma(2, 1)), List(Multiplicacion(3, 2)))))
    }

    it("if multilinea sin espacios") {
      val ast = parser.parsear("if(1 > 3) then{ 2 }else{ 3 }")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(3))))
    }

    it("if multilinea con espacios") {
      val ast = parser.parsear("if(1 > 3) then { 2 } else { 3 }")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(3))))
    }

    it("if con espacio entre if y condición") {
      val ast = parser.parsear("if (1 > 3) then 2 else 4")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(4))))
    }

    it("if sin espacios entre condición y then") {
      val ast = parser.parsear("if (1 > 3)then 2 else 4")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(4))))
    }

    it("if con then inline y else multilinea") {
      val ast = parser.parsear("if(1 > 3) then 2 else { 3 }")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(3))))
    }

    it("if con then multilinea y else inline") {
      val ast = parser.parsear("if(1 > 3) then { 2 } else 3")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(3))))
    }

    it("if solo con then inline") {
      val ast = parser.parsear("if(1 > 3) then 2")

      ast should equal(programa(If(Mayor(1, 3), List(2), List())))
    }

    it("if solo con then multilinea") {
      val ast = parser.parsear("if(1 > 3) then { 2 }")

      ast should equal(programa(If(Mayor(1, 3), List(2), List())))
    }

    it("if dentro de una expresion") {
      val ast = parser.parsear("(if(1 > 3) then 2 else 3) + 4")

      ast should equal(programa(Suma(If(Mayor(1, 3), List(2), List(3)), 4)))
    }

    it("if dentro del else de otro if") {
      val ast = parser.parsear("if(1 > 3) then 2 else if(1 > 3) then 2 else 3")

      ast should equal(programa(If(Mayor(1, 3), List(2), List(If(Mayor(1, 3), List(2), List(3))))))
    }

    it("if dentro del then de otro if") {
      val ast = parser.parsear("if(1 > 3) then if(2 < 1) then 2 else 3 else 4")

      ast should equal(programa(If(Mayor(1, 3), List(If(Menor(2, 1), List(2), List(3))), List(4))))
    }

    it("if dentro del then de otro if sin else") {
      val ast = parser.parsear("if(1 > 3) then if(2 < 1) then 2 else 3")

      ast should equal(programa(If(Mayor(1, 3), List(If(Menor(2, 1), List(2), List(3))), List())))
    }

    it("print numero") {
      val ast = parser.parsear("printLn(1)")

      ast should equal(programa(Aplicacion(Variable("printLn"), List(1))))
    }

    it("print dentro de if") {
      val ast = parser.parsear("if(2 > 1) then printLn(1)")

      ast should equal(programa(If(Mayor(2,1), List(Aplicacion(Variable("printLn"), List(1))), List())))
    }

    it("string vacío") {
      val ast = parser.parsear("\"\"")

      ast should equal(programa(Cadena("")))
    }

    it("string no vacío") {
      val ast = parser.parsear("\"Un String 123\"")

      ast should equal(programa(Cadena("Un String 123")))
    }

    it("string seguido de otro") {
      val ast = parser.parsear(""" "hola" "mundo" """)

      ast should equal(programa(Cadena("hola"), Cadena("mundo")))
    }

    it("string con comillas") {
      val ast = parser.parsear(""" "algo: \"" """)

      ast should equal(programa(Cadena("algo: \"")))
    }

    it("string con barras") {
      val ast = parser.parsear(""" "algo: \\ " """)

      ast should equal(programa(Cadena("algo: \\ ")))
    }

    it("string con barras al final") {
      val ast = parser.parsear(""" "algo: \\" """)

      ast should equal(programa(Cadena("algo: \\")))
    }

    it("string con espacios al principio") {
      val ast = parser.parsear(""" " el string" """)

      ast should equal(programa(Cadena(" el string")))
    }

    it("string con espacios al final") {
      val ast = parser.parsear(""" "el string " """)

      ast should equal(programa(Cadena("el string ")))
    }

    it("print de un string") {
      val ast = parser.parsear("printLn(\"hola mundo\")")

      ast should equal(programa(Aplicacion(Variable("printLn"), List(Cadena("hola mundo")))))
    }

    it("concatenacion de strings") {
      val ast = parser.parsear("\"hola\" ++ \" mundo\"")

      ast should equal(programa(Concatenacion(Cadena("hola"), Cadena(" mundo"))))
    }

    it("concatenacion de sumas de números") {
      val ast = parser.parsear("\"El resultado es: \" ++ 2 + 2")

      ast should equal(programa(
        Concatenacion(Cadena("El resultado es: "), Suma(2, 2))
      ))
    }

    it("prompt int") {
      val ast = parser.parsear("promptInt(\"Ingrese un número: \")")

      ast should equal(programa(Aplicacion(Variable("promptInt"), List(Cadena("Ingrese un número: ")))))
    }

    it("prompt bool") {
      val ast = parser.parsear("promptBool(\"Ingrese un booleano: \")")

      ast should equal(programa(Aplicacion(Variable("promptBool"), List(Cadena("Ingrese un booleano: ")))))
    }

    it("prompt string") {
      val ast = parser.parsear("promptString(\"Ingrese un string: \")")

      ast should equal(programa(Aplicacion(Variable("promptString"), List(Cadena("Ingrese un string: ")))))
    }

    it("funciones con espacios antes de los parámetros") {
      val ast = parser.parsear("promptString (\"Ingrese un string: \")")

      ast should equal(programa(Aplicacion(Variable("promptString"), List(Cadena("Ingrese un string: ")))))
    }

    it("declarar una variable con un prompt") {
      val ast = parser.parsear("let i = promptInt(\"Ingrese un número: \")")

      ast should equal(programa(DeclaracionVariable("i", Aplicacion(Variable("promptInt"), List(Cadena("Ingrese un número: "))))))
    }

    it("lambda sin parámetros que devuelve una expresión") {
      val ast = parser.parsear("() -> 2 + 2")

      ast should equal(programa(
        Lambda(List(), List(Suma(2, 2)))
      ))
    }

    it("lambda con un parámetro que devuelve una expresión") {
      val ast = parser.parsear("(x) -> x")

      ast should equal(programa(
        Lambda(List("x"), List(Variable("x")))
      ))
    }

    it("lambda con parámetros que devuelve una expresión") {
      val ast = parser.parsear("(primero, segundo) -> primero")

      ast should equal(programa(
        Lambda(List("primero", "segundo"), List(Variable("primero")))
      ))
    }

    it("lambda con cuerpo que contiene más de una instrucción") {
      val ast = parser.parsear("() -> {\n\tlet y = 1\n\ty\n}")

      ast should equal(programa(
        Lambda(List(), List(DeclaracionVariable("y", 1), Variable("y")))
      ))
    }

    it("aplicación de lambda") {
      val ast = parser.parsear("(() -> 2)()")

      ast should equal(programa(
        Aplicacion(Lambda(List(), List(2)), List())
      ))
    }

    it("aplicación de variables") {
      val ast = parser.parsear("f(2)")

      ast should equal(programa(
        Aplicacion(Variable("f"), List(2))
      ))
    }
  }

  private def programa(expresiones: Expresion*) = {
    Programa(expresiones.toList)
  }
}
