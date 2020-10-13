package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFunSpec with Matchers {

  implicit def int2Numero: Int => Expresion = Numero

  class Expresion

  case class Numero(valor: Int) extends Expresion

  case class Suma(sumando1: Expresion, sumando2: Expresion) extends Expresion

  case class Multiplicacion(factor1: Expresion, factor2: Expresion) extends Expresion

  describe("parsea") {
    val parser = new ParserLepifyo[Expresion](numero = Numero, suma = Suma, multiplicacion = Multiplicacion)

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
  }
}
