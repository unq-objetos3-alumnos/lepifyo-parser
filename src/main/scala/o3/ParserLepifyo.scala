package o3

import o3.ParserLepifyo.ParseError

import scala.util.parsing.combinator._

case class ParserLepifyo[TPrograma, TExpresion](
   programa: List[TExpresion] => TPrograma,
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
   menorIgual: (TExpresion, TExpresion) => TExpresion,
   declaracionVariable: (String, TExpresion) => TExpresion,
   variable: String => TExpresion,
   asignacion: (String, TExpresion) => TExpresion,
   printLn: TExpresion => TExpresion,
   si: (TExpresion, List[TExpresion], List[TExpresion]) => TExpresion
 ) extends RegexParsers {
  def parsear(textoPrograma: String): TPrograma = {
    def parserNumero: Parser[TExpresion] = """[0-9]+""".r ^^ { n => numero(n.toInt) }
    def parserBooleano: Parser[TExpresion] = "true" ^^^ booleano(true) | "false" ^^^ booleano(false)
    def parserIdentificador: Parser[String] = """[_a-z][_a-zA-Z0-9]*""".r
    def parserVariable: Parser[TExpresion] = parserIdentificador ^^ variable

    def parserFactor: Parser[TExpresion] = parserNumero | parserBooleano | parserVariable | "(" ~> parserExpresion <~ ")"

    def parserTermino = chainl1(parserFactor, "*" ^^^ multiplicacion | "/" ^^^ division)

    def parserMiembros = chainl1(parserTermino, "+" ^^^ suma | "-" ^^^ resta)

    def parserMiembroDesigualdad = chainl1(parserMiembros,
      ">=" ^^^ mayorIgual |
      "<=" ^^^ menorIgual |
      ">" ^^^ mayor |
      "<" ^^^ menor
    )
    def parserExpresion = parserIf | chainl1(parserMiembroDesigualdad, "==" ^^^ igual | "!=" ^^^ distinto)
    def parserDeclaracionVariables = ("let " ~> parserIdentificador <~ "=") ~ parserExpresion ^^ {
      case identificador ~ expresion => declaracionVariable(identificador, expresion)
    }
    def parserAsignacion = (parserIdentificador <~ "=") ~ parserExpresion ^^ {
      case identificador ~ expresion => asignacion(identificador, expresion)
    }
    def parserPrint = "PrintLn(" ~> parserExpresion <~ ")" ^^ printLn

    def parserInstruccion = parserDeclaracionVariables | parserAsignacion | parserPrint | parserExpresion
    def parserBloque = "{" ~> parserInstruccion.* <~ "}" | (parserInstruccion ^^ { List(_) })

    def parserIf: Parser[TExpresion] = ("if" ~> "(" ~> parserExpresion <~ ") then") ~ parserBloque ~ ("else" ~> parserBloque).? ^^ {
      case cond ~ pos ~ neg => si(cond, pos, neg.getOrElse(List()))
    }

    def parserPrograma = parserInstruccion.* ^^ programa

    parseAll(parserPrograma, textoPrograma) match {
      case Success(matched, _) => matched
      case Failure(message, rest) => throw ParseError(s"$message: ${rest.source}")
      case Error(message, rest) => throw ParseError(s"$message: ${rest.source}")
    }
  }
}

object ParserLepifyo {
  case class ParseError(message: String) extends RuntimeException(message)
}