package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFunSpec with Matchers {

  implicit def int2Numero: Int => Expresion = Numero

  case class Programa(expresiones: List[Expresion])

  class Expresion

  case class Numero(valor: Int) extends Expresion
  case class Booleano(valor: Boolean) extends Expresion

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

  describe("parsea") {
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = Programa,
      numero = Numero,
      booleano = Booleano,
      suma = Suma,
      resta = Resta,
      multiplicacion = Multiplicacion,
      division = Division,
      igual = Igual,
      distinto = Distinto,
      mayor = Mayor,
      mayorIgual = MayorIgual,
      menor = Menor,
      menorIgual = MenorIgual,
      declaracionVariable = DeclaracionVariable,
      variable = Variable,
      asignacion = Asignacion
    )

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
  }

  private def programa(expresiones: Expresion*) = {
    Programa(expresiones.toList)
  }
}
