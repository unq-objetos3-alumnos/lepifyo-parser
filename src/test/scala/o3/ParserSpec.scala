package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFunSpec with Matchers {

  implicit def int2Numero: Int => Expresion = Numero

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

  describe("parsea") {
    val parser = new ParserLepifyo[Expresion](
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
      menorIgual = MenorIgual
    )

    it("números literales") {
      val ast = parser.parsear("12")

      ast should equal(Numero(12))
    }

    it("sumas de números") {
      val ast = parser.parsear("12 + 34 + 56")

      ast should equal(Suma(Suma(12, 34), 56))
    }

    it("sumas con multiplicación a la derecha") {
      val ast = parser.parsear("12 + 34 * 56")

      ast should equal(Suma(12, Multiplicacion(34, 56)))
    }

    it("sumas con multiplicación a la izquierda") {
      val ast = parser.parsear("12 * 34 + 56")

      ast should equal(Suma(Multiplicacion(12, 34), 56))
    }

    it("multiplicaciones de números") {
      val ast = parser.parsear("12 * 34 * 56")

      ast should equal(Multiplicacion(Multiplicacion(12, 34), 56))
    }

    it("multiplicaciones de sumas") {
      val ast = parser.parsear("12 * (34 + 56)")

      ast should equal(Multiplicacion(12, Suma(34, 56)))
    }

    it("resta de numeros") {
      val ast = parser.parsear("12 - 34 - 56")

      ast should equal(Resta(Resta(12, 34), 56))
    }

    it("resta de suma a la derecha") {
      val ast = parser.parsear("12 - 34 + 56")

      ast should equal(Suma(Resta(12, 34), 56))
    }

    it("resta de suma a la izquierda") {
      val ast = parser.parsear("12 + 34 - 56")

      ast should equal(Resta(Suma(12, 34), 56))
    }

    it("division de numeros") {
      val ast = parser.parsear("12 / 34 / 56")

      ast should equal(Division(Division(12, 34), 56))
    }

    it("igualdad de numeros") {
      val ast = parser.parsear("12 + 15 == 56 - 21")

      ast should equal(Igual(Suma(12, 15), Resta(56, 21)))
    }

    it("desigualdad de numeros") {
      val ast = parser.parsear("12 + 15 != 56 - 21")

      ast should equal(Distinto(Suma(12, 15), Resta(56, 21)))
    }

    it("comparacion por mayor entre numeros") {
      val ast = parser.parsear("12 + 15 > 56 - 21")

      ast should equal(Mayor(Suma(12, 15), Resta(56, 21)))
    }

    it("comparacion por mayor igual entre numeros") {
      val ast = parser.parsear("12 + 15 >= 56 - 21")

      ast should equal(MayorIgual(Suma(12, 15), Resta(56, 21)))
    }

    it("comparacion por menor entre numeros") {
      val ast = parser.parsear("12 + 15 < 56 - 21")

      ast should equal(Menor(Suma(12, 15), Resta(56, 21)))
    }

    it("comparacion por menor igual entre numeros") {
      val ast = parser.parsear("12 + 15 <= 56 - 21")

      ast should equal(MenorIgual(Suma(12, 15), Resta(56, 21)))
    }

    it("igualdad entre comparaciones") {
      val ast = parser.parsear("12 < 56 == 14 > 13")

      ast should equal(Igual(Menor(12, 56), Mayor(14, 13)))
    }

    it("igualdad entre igualdades") {
      val ast = parser.parsear("12 == 56 == 14")

      ast should equal(Igual(Igual(12, 56), 14))
    }

    it("booleanos literales") {
      val ast = parser.parsear("true == false")

      ast should equal(Igual(Booleano(true), Booleano(false)))
    }

  }
}
