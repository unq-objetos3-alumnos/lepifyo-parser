package o3

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class SaludadorSpec extends AnyFunSpec with Matchers {

  describe("Saludador") {
    it("saludar una persona") {
      def saludador = new Saludador()

      saludador.saludar("pepe") should equal("Hola pepe!")
    }
  }

}
