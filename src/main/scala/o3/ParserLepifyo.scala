package o3

import o3.ParserLepifyo.{MissingFunctionError, ParseError}

import scala.util.parsing.combinator._

case class ParserLepifyo[TPrograma, TExpresion](
   programa: List[TExpresion] => TPrograma = { _: List[TExpresion] => throw MissingFunctionError("programa") },
   numero: Int => TExpresion = { _: Int => throw MissingFunctionError("numero") },
   booleano: Boolean => TExpresion = { _: Boolean => throw MissingFunctionError("booleano") },
   string: String => TExpresion = { _: String => throw MissingFunctionError("string") },
   suma: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("suma") },
   resta: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("resta") },
   multiplicacion: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("multiplicacion") },
   division: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("division") },
   igual: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("igual") },
   distinto: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("distinto") },
   mayor: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("mayor") },
   mayorIgual: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("mayorIgual") },
   menor: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("menor") },
   menorIgual: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("menorIgual") },
   declaracionVariable: (String, TExpresion) => TExpresion = { (_:String, _:TExpresion) => throw MissingFunctionError("declaracionVariable") },
   variable: String => TExpresion = { _: String => throw MissingFunctionError("variable") },
   asignacion: (String, TExpresion) => TExpresion = { (_:String, _:TExpresion) => throw MissingFunctionError("asignacion") },
   concatenacion: (TExpresion, TExpresion) => TExpresion = { (_:TExpresion, _:TExpresion) => throw MissingFunctionError("concatenacion") },
   si: (TExpresion, List[TExpresion], List[TExpresion]) => TExpresion = { (_:TExpresion, _:List[TExpresion], _:List[TExpresion]) => throw MissingFunctionError("si") },
   lambda: (List[String], List[TExpresion]) => TExpresion = { (_:List[String], _:List[TExpresion]) => throw MissingFunctionError("lambda") },
   aplicacion: (TExpresion, List[TExpresion]) => TExpresion = { (_:TExpresion, _:List[TExpresion]) => throw MissingFunctionError("aplicacion") }
 ) extends RegexParsers {
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

    def parserLiteral = parserString | parserNumero | parserBooleano | parserVariable | "(" ~> parserExpresion <~ ")"

    def parserLambda: Parser[TExpresion] =
      ("(" ~> repsep(parserIdentificador, ",") <~ ")" <~ "->") ~ (("{" ~> parserInstruccion.* <~ "}") | (parserExpresion ^^ (List(_)))) ^^ {
        case parametros ~ cuerpo => lambda(parametros, cuerpo)
      }
    def parserAplicacion: Parser[TExpresion] = parserLiteral ~ ("(" ~> repsep(parserExpresion, ",") <~ ")") ^^ {
      case funcion ~ argumentos => aplicacion(funcion, argumentos)
    }

    def parserFactor: Parser[TExpresion] = parserAplicacion | parserLiteral

    def parserTermino = chainl1(parserFactor, "*" ^^^ multiplicacion | "/" ^^^ division)

    def parserMiembros = chainl1(parserTermino, "+" ^^^ suma | "-" ^^^ resta)

    def parserConcatenacion = chainl1(parserMiembros, "++" ^^^ concatenacion)

    def parserMiembroDesigualdad = chainl1(parserConcatenacion,
      ">=" ^^^ mayorIgual |
      "<=" ^^^ menorIgual |
      ">" ^^^ mayor |
      "<" ^^^ menor
    )
    def parserExpresion = parserLambda | parserIf | chainl1(parserMiembroDesigualdad, "==" ^^^ igual | "!=" ^^^ distinto)
    def parserDeclaracionVariables = ("let " ~> parserIdentificador <~ "=") ~ parserExpresion ^^ {
      case identificador ~ expresion => declaracionVariable(identificador, expresion)
    }
    def parserAsignacion = (parserIdentificador <~ "=") ~ parserExpresion ^^ {
      case identificador ~ expresion => asignacion(identificador, expresion)
    }

    def parserInstruccion = parserDeclaracionVariables | parserAsignacion | parserExpresion
    def parserBloque = "{" ~> parserInstruccion.* <~ "}" | (parserInstruccion ^^ { List(_) })

    def parserIf: Parser[TExpresion] = ("if" ~> parserExpresion <~ "then") ~ parserBloque ~ ("else" ~> parserBloque).? ^^ {
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
  case class MissingFunctionError(fn: String) extends RuntimeException("Falta especificar una función para: "+fn)
}