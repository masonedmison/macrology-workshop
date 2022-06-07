package part2

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

trait Immutable[-T]

object Immutable {
  implicit def materialize[T: InvariantDummy]: Immutable[T] = macro Macro.materializeImpl[T]

  def is[T]: Boolean = macro Macro.isImpl[T]

  def collapseToNull[T](evidence: Immutable[T]): Immutable[T] = macro Macro.collapseToNullImpl[T]

  trait InvariantDummy[T]
  object InvariantDummy {
    implicit def materialize[T]: InvariantDummy[T] = macro materializeImpl[T]
    def materializeImpl[T: c.WeakTypeTag](c: Context) = {
      import c.universe._
      q"null"
    }
  }

  class Macro(val c: Context) {
    import c.universe._, definitions.ArrayClass

    def isImpl[T: c.WeakTypeTag] = {
      val immutableOfT = appliedType(typeOf[Immutable[_]], weakTypeOf[T])
      val inferred     = c.inferImplicitValue(immutableOfT, silent = true)
      q"${inferred.nonEmpty}"
    }

    def collapseToNullImpl[T](evidence: c.Tree): c.Tree = {
      q"null"
    }

    def materializeImpl[T: c.WeakTypeTag](dummy: c.Tree) = {
      val T = weakTypeOf[T]
      val deps =
        T.typeSymbol match {
          case sym if sym == ArrayClass =>
            c.abort(c.enclosingPosition, "Arrays are mutable.")
          case sym: ClassSymbol =>
            if (!sym.isFinal && !sym.isModuleClass && !sym.isSealed)
              c.abort(c.enclosingPosition, "open classes are not permitted.")
            val childTpes = sym.knownDirectSubclasses.toList.map { case sub: ClassSymbol => sub.toType }
            val fieldTpes = T.members.collect {
              case s: TermSymbol if !s.isMethod =>
                if (s.isVar) c.abort(c.enclosingPosition, s"$T is not immutable becuase it has mutable field ${s.name}")
                else s.typeSignatureIn(T)
            }
            childTpes ++ fieldTpes
          case sym: TypeSymbol =>
            val TypeBounds(_, high) = sym.info
            high :: Nil
        }
      val implicitlies = deps.map { tpe =>
        q"_root_.scala.Predef.implicitly[Immutable[$tpe]]"
      }
      val name   = TermName(c.freshName())
      val result = q"""
      _root_.part2.Immutable.collapseToNull {
        implicit object $name extends Immutable[$T]
        ..$implicitlies
        $name 
      }
      """
      println(showCode(result))
      result
    }
  }
}
