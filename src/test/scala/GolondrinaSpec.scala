import org.scalatest.{FunSpec, Matchers}
import ar.edu.unq.o3.Golondrina

class GolondrinaSpec extends FunSpec with Matchers {

  describe("Una Golondrina") {
     it("debe inicializarse con energia") {
       val g = new Golondrina()
       g.energia should equal (100)
     }
  }

}
