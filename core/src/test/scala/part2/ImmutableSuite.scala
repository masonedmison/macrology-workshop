package part2

import org.scalatest.funsuite.AnyFunSuite

class ImmutableSuite extends AnyFunSuite {
  test("dummy class is immutable") {
    final class Dummy
    assert(Immutable.is[Dummy])
  }

  test("class with var is Int immutable") {
    final class C(var x: Int)
    assert(!Immutable.is[C])
  }

  test("Deep mutability check - should not produce immutable value") {
    final class M(var x: Int)
    final class C(m: M)
    assert(!Immutable.is[C])
  }

  test("self recursive class is immutable") {
    final class Recursive(rec: Recursive)
    assert(Immutable.is[Recursive])
  }

  test("mutually recursive classes are immutable") {
    final class Mutally(rec: Rec)
    final class Rec(mutually: Mutally)
    assert(Immutable.is[Mutally])
    assert(Immutable.is[Rec])
  }

  test("polymorphic class might or might not be immutable") {
    final class M(var x: Int)
    final class C[T](val x: T)
    assert(!Immutable.is[C[M]])
    assert(Immutable.is[C[Int]])
  }

  test("open class might not be immutable") {
    class C
    assert(!Immutable.is[C])
  }

  test("objects with immutable fields are immutable") {
    object C { val x = 0 }
    assert(Immutable.is[C.type])
  }

  test("objects with mutable fields are not immutable") {
    object C { var x = 0; val y = 1 }
    assert(!Immutable.is[C.type])
  }

  test("objects with nested objects can immutable") {
    final class C { object O { val x: Int = 0 } }
    assert(Immutable.is[C])
  }

  test("sealed hierarchies where each subClass is immutable") {
    sealed trait A
    final case class B(val x: Int) extends A
    final case class C(val y: Int) extends A
    assert(Immutable.is[A])
  }

  test("sealed hierarchies where subClass has var") {
    sealed trait A
    final case class B(var x: Int) extends A
    final case class C(val y: Int) extends A
    assert(!Immutable.is[A])
  }

  test("type variable bounded by immutable type is immutable") {
    sealed class C
    final class A extends C
    def f[T <: C] = Immutable.is[T]

    assert(f)
  }

  test("arrays are mutable") {
    assert(!Immutable.is[Array[Int]])
  }

  test("primitives are immutable") {
    implicitly[Immutable[Byte]]
    implicitly[Immutable[Short]]
    implicitly[Immutable[Int]]
    implicitly[Immutable[Long]]
    implicitly[Immutable[Char]]
    implicitly[Immutable[Float]]
    implicitly[Immutable[Double]]
    implicitly[Immutable[Boolean]]
    implicitly[Immutable[Unit]]
    implicitly[Immutable[Null]]
  }

  test("immutable collections are... well... immutable") {
    implicit def whitelistList[T: Immutable]: Immutable[collection.immutable.List[T]]                = null
    implicit def whitelistMap[K: Immutable, V: Immutable]: Immutable[collection.immutable.Map[K, V]] = null
    assert(Immutable.is[List[Int]])
    assert(Immutable.is[Map[String, Int]])

    class C(var x: Int)
    assert(!Immutable.is[Map[String, C]])
    assert(!Immutable.is[Map[C, Int]])
  }
}
