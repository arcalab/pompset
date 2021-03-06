package choreo.api

/**
 * Created by   on 15/02/2022
 */


trait Code:

  override def toString: String = toCode(0)

  def toCode(implicit i:Int):String

  def ind(i:Int):String = " "*i*2

  def brackets(args:List[String],ln:Boolean=true)(implicit i:Int):String =
    if args.isEmpty then ""
    else if !ln || args.size < 4  then args.mkString("[",", ","]")
    else argsLn(args,"[",", ","]")

  def params(args:List[String],sep:String=", ",ln:Boolean=false)(implicit i:Int):String =
    if !ln /*&& (args.size < 4) && args.map(_.length).sum <60)*/ then args.mkString("(",sep,")")
    else argsLn(args,"(",sep,")")

  def argsLn(args:List[String],fst:String,sep:String,lst:String)(implicit i:Int):String =
    args.map(a=>ind(i)+a).mkString(s"$fst\n",sep+"\n",s"\n${ind(i-1)}$lst")

  def length(args:List[String]):Int =
    args.map(_.length).sum

  def sep(strings:List[String]):String = strings match
    case Nil => ""
    case _   => strings.mkString("\n\n") ++ "\n\n"


  def singleComment(str:String)(implicit i:Int):String =
    ind(i) ++ "// " ++ str

  def comment(str:String)(implicit i:Int):String =
    val lines = str.split("\n")
    ind(i) ++
      "/** \n" ++
      lines.map(l => ind(i)+" * "+l).mkString("\n") ++ "\n" ++
      ind(i)++ " */"
