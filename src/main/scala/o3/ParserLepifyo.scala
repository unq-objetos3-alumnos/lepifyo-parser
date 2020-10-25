package o3

import o3.ParserLepifyo.ParseError

import scala.util.parsing.combinator._

case class ParserLepifyo[TPrograma, TExpresion](
   programa: List[TExpresion] => TPrograma,
   numero: Int => TExpresion,
   booleano: Boolean => TExpresion,
   string: String => TExpresion,
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
   concatenacion: (TExpresion, TExpresion) => TExpresion,
   printLn: TExpresion => TExpresion,
   promptString: TExpresion => TExpresion,
   promptInt: TExpresion => TExpresion,
   promptBool: TExpresion => TExpresion,
   si: (TExpresion, List[TExpresion], List[TExpresion]) => TExpresion
 ) extends RegexParsers {
  private val funciones = Map(
    "PrintLn" -> printLn,
    "PromptInt" -> promptInt,
    "PromptBool" -> promptBool,
    "PromptString" -> promptString,
  )

  def parsear(textoPrograma: String): TPrograma = {
    def parserNumero: Parser[TExpresion] = """[0-9]+""".r ^^ { n => numero(n.toInt) }
    def parserBooleano: Parser[TExpresion] = "true" ^^^ booleano(true) | "false" ^^^ booleano(false)
    def parserString: Parser[TExpresion] = """"\s*""".r ~ """(\\\\|\\"|[^"])*""".r <~ "\"" ^^ {
      // Consumir los espacios del inicio (con la primera regex) es necesario porque si usáramos ~> descartaría
      // los espacios al inicio del string
      case inicioConEspacios ~ restoDelString =>
        string(
          (inicioConEspacios.drop(1) + restoDelString)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
        )
    }

    def parserIdentificador: Parser[String] = """[_a-z][_a-zA-Z0-9]*""".r
    def parserVariable: Parser[TExpresion] = parserIdentificador ^^ variable

    def parserFactor: Parser[TExpresion] = parserString | parserNumero | parserBooleano | parserVariable | "(" ~> parserExpresion <~ ")"

    def parserTermino = chainl1(parserFactor, "*" ^^^ multiplicacion | "/" ^^^ division)

    def parserMiembros = chainl1(parserTermino, "+" ^^^ suma | "-" ^^^ resta)

    def parserConcatenacion = chainl1(parserMiembros, "," ^^^ concatenacion)

    def parserMiembroDesigualdad = chainl1(parserConcatenacion,
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

    def parserFuncion(nombre: String, funcion: TExpresion => TExpresion) = nombre ~> "(" ~> parserExpresion <~ ")" ^^ funcion
    def parserFunciones: Parser[TExpresion] = funciones.map((parserFuncion _).tupled).reduce(_ | _)

    def parserInstruccion = parserDeclaracionVariables | parserAsignacion | parserFunciones | parserExpresion
    def parserBloque = "{" ~> parserInstruccion.* <~ "}" | (parserInstruccion ^^ { List(_) })

    def parserIf: Parser[TExpresion] = ("if" ~> "(" ~> parserExpresion <~ ")" <~ "then") ~ parserBloque ~ ("else" ~> parserBloque).? ^^ {
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