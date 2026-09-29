class KotlinInteropSuite extends munit.FunSuite:

  test("kotlinUnit evaluates a Scala Unit expression and returns kotlin.Unit") {
    var sideEffect = 0
    val result = KotlinInterop.kotlinUnit {
      sideEffect += 1
    }
    assertEquals(sideEffect, 1)
    assertEquals(result, kotlin.Unit.INSTANCE)
  }

  test("given Conversion[Unit, kotlin.Unit] converts Unit to kotlin.Unit") {
    import KotlinInterop.given

    def takesKotlinUnit(x: => kotlin.Unit): kotlin.Unit = x

    var sideEffect = 0
    val result = takesKotlinUnit {
      sideEffect += 1
    }
    assertEquals(sideEffect, 1)
    assertEquals(result, kotlin.Unit.INSTANCE)
  }

  test(
    "given Conversion[() => Unit, kotlin.jvm.functions.Function0[kotlin.Unit]] converts the function"
  ) {
    import KotlinInterop.given

    var sideEffect = 0
    val scalaFn: () => Unit = () => sideEffect += 1
    val kotlinFn: kotlin.jvm.functions.Function0[kotlin.Unit] = scalaFn

    val result = kotlinFn.invoke()
    assertEquals(sideEffect, 1)
    assertEquals(result, kotlin.Unit.INSTANCE)
  }

  test("the converted Function0 invokes the Scala function on each call") {
    import KotlinInterop.given

    var counter = 0
    val scalaFn: () => Unit = () => counter += 1
    val kotlinFn: kotlin.jvm.functions.Function0[kotlin.Unit] = scalaFn

    kotlinFn.invoke()
    kotlinFn.invoke()
    assertEquals(counter, 2)
  }
