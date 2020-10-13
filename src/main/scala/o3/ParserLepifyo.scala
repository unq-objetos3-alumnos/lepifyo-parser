package o3
import scala.util.parsing.combinator._

case class ParserLepifyo[TExpresion](
                                      numero: Int => TExpresion,
                                      suma: (TExpresion, TExpresion) => TExpresion,
                                      multiplicacion: (TExpresion, TExpresion) => TExpresion
                                    ) extends RegexParsers {
  def parsear(textoPrograma: String): TExpresion = {
    def parserNumero: Parser[TExpresion] = """[0-9]+""".r ^^ { n => numero(n.toInt) }

    def parserFactor = parserNumero
    def parserMultiplicacion: Parser[TExpresion] = parserFactor ~ ("*" ~> parserFactor).+ ^^ {
      case primerTermino ~ terminos => (terminos).foldLeft(primerTermino)(multiplicacion)
    }

    def parserTermino = parserMultiplicacion | parserNumero
    def parserSuma: Parser[TExpresion] = parserTermino ~ ("+" ~> parserTermino).+ ^^ {
      case primerTermino ~ terminos => (terminos).foldLeft(primerTermino)(suma)
    }
    def parserExpresion = parserSuma | parserMultiplicacion | parserNumero

    parseAll(parserExpresion, textoPrograma) match {
      case Success(matched, _) => matched
    }
  }
}
