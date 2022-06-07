import part1.internal.Optional

object Test {
  class C { override def toString = "C" }

  def main(args: Array[String]): Unit = {
    def foo(f: => C): C = f
    val x1              = new Optional(new C)
    val x2              = x1.map(x => foo({ val y = x; y }))
    println(x2)
  }

}
