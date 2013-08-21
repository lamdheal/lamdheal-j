package lamdheal

import java.io.StringReader

import org.codehaus.janino.SimpleCompiler

//import org.codehaus.commons.compiler.jdk.SimpleCompiler

/*  Copyright 2013 Davi Pereira dos Santos
    This file is part of Lamdheal.

    Lamdheal is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Lamdheal is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Lamdheal.  If not, see <http://www.gnu.org/licenses/>.*/

object Compiling {

   import TypeSystem._

   def return_type(t: Type): String = t match {
      case NumberT => "double"
      case EmptyT => "Object"
      case ListT(et) => "ArrayList"
      case v@VariableT(i) => return_type(v.instance.get) //case None ??
   }

   //   var var_counter=0
   def run(ex: Expr): String = {
      ex match {
         case ApplyE(f, a) => (f, a) match {
            //                     case (ShowE, x) => x + ".toString()"
            case (TypeE(t), ListE(l)) =>
               val java_lines = l.map(_.asInstanceOf[CharE].c).mkString.split("\n")
               val code = if (t == EmptyT) {
                  java_lines.mkString(";\n") + "return new Empty();"
               } else {
                  java_lines.dropRight(1).mkString(";\n") + "return " + java_lines.last + ";\n"
               }
               "new Anon() { public Node f(Node obj) {\n" +
                  code + "\n" +
                  "} }.f(null)\n"
//               "new Anon() { public " + return_type(t) + " f() {\n" +
//                  code + "\n" +
//                  "} }.f()\n"
            case (x, y) => "Runtime.apply(" + run(x) + ", " + run(y) + ")"
         }
         case AssignE(id, expr) =>
            //                     var_counter += 1
            //                     val tranlated_id = id + var_counter.toString
            //                     translation += (id -> tranlated_id)
            //               println(expr + " before match")
            expr.t match {
               case NumberT => "final Double " + id + " = " + run(expr) + ";\n"
               case ListT(CharT) => "final String " + id + " = " + run(expr) + ";\n"
               case ListT(_) => "final ArrayList " + id + " = " + run(expr) + ";\n"
            }
         case c: CharE => "'" + c.toString + "'"
         case b@BlockE(l) =>
            val statements = l.filterNot(EmptyE ==).map(run).map("   " +)
            if (statements.length > 0) {
               "new Anon() { public Object f(Object obj) {\n" +
                  statements.dropRight(1).mkString(";\n") + ";\n" +
                  "return " + (if (return_type(b.t) != "void") statements.last + ";" else " new Empty();") + "\n" +
                  "} } "
//               "new Anon() { public " + return_type(b.t) + " f() {\n" +
//                  statements.dropRight(1).mkString(";\n") + ";\n" +
//                  "return " + (if (return_type(b.t) != "void") statements.last + ";" else " new Empty();") + "\n" +
//                  "} } "
            } else "new Empty()"
         //
         case EmptyE => ""
         //         case la@LambdaE(param, BlockE(l)) =>
         //            val from_type = la.t.asInstanceOf[FunctionT].from match {
         //               case NumberT => "double"
         //               case EmptyT => "void"
         //            }
         //            val to_type = la.t.asInstanceOf[FunctionT].to match {
         //               case NumberT => "double"
         //               case EmptyT => "void"
         //            }
         //            "new Anon() { public " + to_type + " f(" + from_type + " " + param + ") {\n" +
         //               l.filterNot(EmptyExpr ==).map(run).map("   " +).mkString(";\n") +
         //               "};\n"
         case NumberE(n) => n
         //         case PrintLnE => "System.out.println"
         case IdentE(name) => name
         //         case NumberExpr(n) => n.toString
//         case li@ListE(l) =>
//            "new Anon() { public " + return_type(li.t) + " f() {\n" +
//               "      ArrayList al = new ArrayList();\n" +
//               l.map(x => "      al.add(" + run(x) + ");\n").mkString +
//               "      return al;\n" +
//               "} } "
         case li@ListE(l) =>
            "new Anon() { public Object f(Object obj) {\n" +
               "      ArrayList al = new ArrayList();\n" +
               l.map(x => "      al.add(" + run(x) + ");\n").mkString +
               "      return al;\n" +
               "} } "


         //         case ListInterval(i, f) => "built_in_function_range(" + run(i) + "," + run(f) + ")"
      }
   }

   def compile(expr: Expr) {
      val source = "import java.util.ArrayList;\n" +
         "import java.util.Iterator;\n" +
         "public class Empty {\n" +
         "   public String toString() {" +
         "      return \"Ø\";\n" +
         "   }" +
         "}\n" +
         "public interface Anon {\n" +
         "   Object f(Object o);" +
         "}\n" +
         "public class RuntimeMain implements Runnable {\n" +
         "   public static void main(String[] args) {\n" +
         "      RuntimeMain m = new RuntimeMain();\n" +
         "      m.run();\n" +
         "   }\n" +
         "    \n" +
         "   public ArrayList built_in_function_range(Double i, Double f) {\n" +
         "      ArrayList al = new ArrayList();\n" +
         "      for (Double x=i; x<=f; x++)\n" +
         "         al.add(x);\n" +
         "      return al;\n" +
         "   }\n" +
         "    \n" +
         "   public ArrayList built_in_function_map(ArrayList al, Anon A) {\n" +
         "      ArrayList al2 = new ArrayList();\n" +
         "      for (int x=0; x<al.size(); x++)\n" +
         "         al2.add(A.f(al.get(x)));\n" +
         "      return al2;\n" +
         "   }\n" +
         "    \n" +
         "   public void run() {\n" +
         "      System.out.println(\n" +
         run(expr).split('\n').map("         " +).mkString("\n") + "\n" +
         "      );\n" +
         "   }\n" +
         "}\n"

      println(source)

      val compiler = new SimpleCompiler()
      compiler.cook(new StringReader(source))
      val clss = compiler.getClassLoader.loadClass("RuntimeMain")
      val eval = clss.newInstance().asInstanceOf[Runnable]
      eval.run()
   }
}
