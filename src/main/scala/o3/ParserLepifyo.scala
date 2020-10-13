package o3

import scala.util.parsing.combinator._

case class ParserLepifyo[TExpresion](
                                      numero: Int => TExpresion,
                                      booleano: Boolean => TExpresion,
                                      suma: (TExpresion, TExpresion) => TExpresion,
                                      resta: (TExpresion, TExpresion) => TExpresion,
                                      multiplicacion: (TExpresion, TExpresion) => TExpresion,
                                      division: (TExpresion, TExpresion) => TExpresion,
                                      igual: (TExpresion, TExpresion) => TExpresion,
                                      distinto: (TExpresion, TExpresion) => TExpresion,
                                      mayor: (TExpresion, TExpresion) => TExpresion,
                                      mayorIgual: (TExpresion, TExpresion) => TExpresion,
                                      menor: (TExpresion, TExpresion) => TExpresion,
                                      menorIgual: (TExpresion, TExpresion) => TExpresion
                                    ) extends RegexParsers {
  def parsear(textoPrograma: String): TExpresion = {
    def parserNumero: Parser[TExpresion] = """[0-9]+""".r ^^ { n => numero(n.toInt) }
    def parserBooleano: Parser[TExpresion] = "true" ^^^ booleano(true) | "false" ^^^ booleano(false)

    def parserFactor: Parser[TExpresion] = parserNumero | parserBooleano | "(" ~> parserExpresion <~ ")"

    def parserTermino = chainl1(parserFactor, "*" ^^^ multiplicacion | "/" ^^^ division)

    def parserMiembros = chainl1(parserTermino, "+" ^^^ suma | "-" ^^^ resta)

    def parserMiembroDesigualdad = chainl1(parserMiembros,
      ">=" ^^^ mayorIgual |
      "<=" ^^^ menorIgual |
      ">" ^^^ mayor |
      "<" ^^^ menor
    )
    def parserExpresion = chainl1(parserMiembroDesigualdad, "==" ^^^ igual | "!=" ^^^ distinto)

    parseAll(parserExpresion, textoPrograma) match {
      case Success(matched, _) => matched
    }
  }
}
