# Lepifyo Parser

## 👷‍♀️ Instalación
1. Crear un token de acceso personal con el permiso `package:read`
    - Obtenerlo desde `configuración` -> `Developer settings` -> `Tokens de acceso personal`. Se puede consultar la [documentación de GitHub](https://docs.github.com/es/enterprise-server@2.20/github/authenticating-to-github/creating-a-personal-access-token).
      > :warning: No olvidar seleccionar el permiso `package:read`.

    - Guardar el token en la configuración de git
      ```bash
      git config --global github.token <token_personal>
      ```
      Donde `<token_personal>` es el token que generaste en GitHub.
    
1. Agregar el plugin `sbt-github-packages` para poder descargar el package
    
    - Crear el archivo `./project/plugins.sbt`
      ```scala 
      addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")
      ```

1. Agregar el package como dependencia
    - Agregar al `./build.sbt`
      ```scala 
      githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource.GitConfig("github.token")
      resolvers += Resolver.githubPackages("unq-objetos3-alumnos", "lepifyo-parser")
      libraryDependencies += "unq-objetos3-alumnos" %% "lepifyo-parser" % "0.3"
      ```

1. Compilar el proyecto con la nueva configuración
    ```bash
    > sbt compile
    ```
    > Cuando termine, es buena idea cerrar y abrir IntelliJ
   
## #️⃣ Uso

La clase `ParserLepifyo` es la encargada de generar instancias del parser. Y se importa como:
```scala
import o3.ParserLepifyo
```

Al crear una instancia del parser hay que indicarle el tipo del objeto que representa un `Programa` y el tipo de los objetos que representan a los elementos del `AST`. Además, tenemos que pasarle las funciones que usará para crear cada elemento del `AST`.

> #### ⚠️ Atención!
> En los siguientes ejemplos se crean algunas clases necesarias,
> pero no significa que deban usar estas mismas para su solución.

Ejemplos:
- Parsear un número
    - Si usan clases:
        ```scala
        import o3.ParserLepifyo
        
        class Programa(elementos: List[Elemento])
        
        class Elemento
        
        class Numero(valor: Int) extends Elemento
        
        val parser = new ParserLepifyo[Programa, Elemento](
          programa = elementos => new Programa(elementos),
          numero = n => new Numero(n)
        )
        
        val ast = parser.parsear("2")
        // `ast` es equivalente a `new Programa(List(new Numero(2)))`
        ```

    - Si usan case clases
        ```scala
        import o3.ParserLepifyo
        
        case class Programa(expresiones: List[Expresion])
        
        class Expresion
        
        case class Numero(valor: Int) extends Expresion
        
        val parser = new ParserLepifyo[Programa, Expresion](
          programa = expresiones => Programa(expresiones),
          numero = n => Numero(n)
        )
        
        val ast = parser.parsear("2")
        // `ast` es equivalente a `Programa(List(Numero(2)))`
        ```
      
- Parsear operaciones
    ```scala
    import o3.ParserLepifyo
    
    case class Programa(expresiones: List[Expresion])
    
    class Expresion
    
    case class Numero(valor: Int) extends Expresion

    case class Suma(sumando1: Expresion, sumando2: Expresion) extends Expresion
    case class Resta(minuendo: Expresion, sustraendo: Expresion) extends Expresion
    case class Multiplicacion(factor1: Expresion, factor2: Expresion) extends Expresion
    case class Division(dividendo: Expresion, divisor: Expresion) extends Expresion
    
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = expresiones => Programa(expresiones),
      numero = n => Numero(n),
      suma = Suma,
      resta = Resta,
      multiplicacion = Multiplicacion,
      division = Division
    )
    
    val ast = parser.parsear(
        """
          | 8 - 3
          | 4 / 1
          | 12 * (34 + 56)
          |""".stripMargin
    )
    // `ast` es equivalente a `Programa(List(
    //    Resta(Numero(8),Numero(3)), 
    //    Division(Numero(4),Numero(1)), 
    //    Multiplicacion(Numero(12),Suma(Numero(34),Numero(56)))
    //  ))`
    ```
  
- Parsear comparaciones y booleanos
    ```scala
    import o3.ParserLepifyo
    
    case class Programa(expresiones: List[Expresion])
    
    class Expresion
  
    case class Numero(valor: Int) extends Expresion
    case class Booleano(valor: Boolean) extends Expresion
  
    case class Igual(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class Distinto(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class Mayor(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class MayorIgual(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class Menor(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class MenorIgual(expresion1: Expresion, expresion2: Expresion) extends Expresion
    
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = Programa,
      numero = Numero,
      booleano = Booleano,
      igual = Igual,
      distinto = Distinto,
      mayor = Mayor,
      mayorIgual = MayorIgual,
      menor = Menor,
      menorIgual = MenorIgual
    )
    
    val ast = parser.parsear(
        """
          | 12 == 56
          | 12 != 10
          | 56 >= 15
          | 12 < 56 == 14 > 13
          | true == false
          |""".stripMargin
    )
    // `ast` es equivalente a `Programa(List(
    //    Igual(Numero(12),Numero(56)), 
    //    Distinto(Numero(12),Numero(10)), 
    //    MayorIgual(Numero(56),Numero(15)), 
    //    Igual(Menor(Numero(12),Numero(56)),Mayor(Numero(14),Numero(13))),
    //    Igual(Booleano(true),Booleano(false))
    //  ))`
    ```
  
- Parsear variables
    ```scala
    import o3.ParserLepifyo
    
    case class Programa(expresiones: List[Expresion])
    
    class Expresion
  
    case class Numero(valor: Int) extends Expresion
    case class Resta(minuendo: Expresion, sustraendo: Expresion) extends Expresion
  
    case class DeclaracionVariable(nombre: String, valorInicial: Expresion) extends Expresion
    case class Variable(nombre: String) extends Expresion
    case class Asignacion(nombre: String, valorNuevo: Expresion) extends Expresion
    
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = Programa,
      numero = Numero,
      resta = Resta,
      declaracionVariable = DeclaracionVariable,
      variable = Variable,
      asignacion = Asignacion
    )
    
    val ast = parser.parsear(
        """
          | let variable = 12
          | let numero = 10
          | numero = 2
          | variable - numero
          |""".stripMargin
    )
    // `ast` es equivalente a `Programa(List(
    //    DeclaracionVariable("variable", Numero(12)), 
    //    DeclaracionVariable("numero", Numero(10)), 
    //    Asignacion("numero", Numero(2)),
    //    Resta(Variable("variable"), Variable("numero"))
    //  ))`
    ```

- Parsear Strings y Funciones de I/O
    ```scala
    import o3.ParserLepifyo
    
    case class Programa(expresiones: List[Expresion])
    
    class Expresion
  
    case class Numero(valor: Int) extends Expresion
    case class Cadena(valor: String) extends Expresion
    case class Suma(sumando1: Expresion, sumando2: Expresion) extends Expresion
  
    case class DeclaracionVariable(nombre: String, valorInicial: Expresion) extends Expresion
    case class Variable(nombre: String) extends Expresion
    
    case class Concatenacion(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class PrintLn(valor: Expresion) extends Expresion
    case class PromptInt(valor: Expresion) extends Expresion
    case class PromptString(valor: Expresion) extends Expresion
    case class PromptBool(valor: Expresion) extends Expresion
  
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = Programa,
      numero = Numero,
      string = Cadena,
      suma = Suma,
      declaracionVariable = DeclaracionVariable,
      variable = Variable,
      concatenacion = Concatenacion,
      printLn = PrintLn,
      promptString = PromptString,
      promptInt = PromptInt,
      promptBool = PromptBool
    )
    
    val ast = parser.parsear(
        """
          | let anioActual = PromptInt("Ingrese el año actual: ")
          | PrintLn("El año que viene es: ", anioActual + 1)
          |""".stripMargin
    )
    // `ast` es equivalente a `Programa(List(
    //    DeclaracionVariable(anioActual,PromptInt(Cadena("Ingrese el año actual: "))), 
    //    PrintLn(Concatenacion(Cadena("El año que viene es: "),Suma(Variable(anioActual),Numero(1))))
    //    ))`
    ```

- Parsear if
    ```scala
    import o3.ParserLepifyo
    
    case class Programa(expresiones: List[Expresion])
    
    class Expresion
  
    case class Numero(valor: Int) extends Expresion
    case class Cadena(valor: String) extends Expresion
    case class Suma(sumando1: Expresion, sumando2: Expresion) extends Expresion
    case class MayorIgual(expresion1: Expresion, expresion2: Expresion) extends Expresion
     
    case class DeclaracionVariable(nombre: String, valorInicial: Expresion) extends Expresion
    case class Variable(nombre: String) extends Expresion
    case class Asignacion(nombre: String, valorNuevo: Expresion) extends Expresion
    
    case class Concatenacion(expresion1: Expresion, expresion2: Expresion) extends Expresion
    case class PrintLn(valor: Expresion) extends Expresion
    case class PromptInt(valor: Expresion) extends Expresion
  
    case class If(cond: Expresion, pos: List[Expresion], neg: List[Expresion]) extends Expresion
  
    val parser = new ParserLepifyo[Programa, Expresion](
      programa = Programa,
      numero = Numero,
      string = Cadena,
      suma = Suma,
      mayorIgual = MayorIgual,
      declaracionVariable = DeclaracionVariable,
      variable = Variable,
      asignacion = Asignacion,
      concatenacion = Concatenacion,
      printLn = PrintLn,
      promptInt = PromptInt,
      si = If
    )
    
    val ast = parser.parsear(
        """
          | let edad = PromptInt("Ingrese su edad: ")
          | let mayores = 0
          | let menores = 0
          | if (edad >= 18) then {
          |   mayores = mayores + 1
          | } else {
          |   menores = menores + 1
          | }
          | PrintLn("Hay mas cantidad de ", (if (mayores >= menores) then "mayores" else "menores"))
          |""".stripMargin
    )
    // `ast` es equivalente a `Programa(List(
    //    DeclaracionVariable(edad,PromptInt(Cadena("Ingrese su edad: "))), 
    //    DeclaracionVariable(mayores,Numero(0)), 
    //    DeclaracionVariable(menores,Numero(0)), 
    //    If(MayorIgual(Variable(edad),Numero(18)),
    //      List(Asignacion(mayores,Suma(Variable(mayores),Numero(1)))),
    //      List(Asignacion(menores,Suma(Variable(menores),Numero(1))))
    //    ), 
    //    PrintLn(Concatenacion(Cadena("Hay mas cantidad de "),If(MayorIgual(Variable(mayores),Variable(menores)),List(Cadena("mayores")),List(Cadena("menores")))))))`
    ```
  > El `else` es opcional

### Para más detalles pueden ver los [tests](./src/test/scala/o3/ParserSpec.scala)
