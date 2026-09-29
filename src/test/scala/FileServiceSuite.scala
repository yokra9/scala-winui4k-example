import java.nio.file.{Files, Path}
import java.util.concurrent.{CountDownLatch, TimeUnit, TimeoutException}

class FileServiceSuite extends munit.FunSuite:

  private var tempDir: Path = null

  override def beforeEach(context: BeforeEach): Unit =
    tempDir = Files.createTempDirectory("fileservice-test-")

  override def afterEach(context: AfterEach): Unit =
    if tempDir != null then
      Files
        .walk(tempDir)
        .sorted(java.util.Comparator.reverseOrder[Path]())
        .forEach(Files.deleteIfExists(_))
      ()

  private def awaitCallback[T](
      run: (T => Unit, Throwable => Unit) => Unit,
      timeoutSeconds: Long = 5
  ): Either[Throwable, T] =
    val latch = CountDownLatch(1)
    var result: Either[Throwable, T] = null
    run(
      value =>
        result = Right(value)
        latch.countDown()
      ,
      error =>
        result = Left(error)
        latch.countDown()
    )
    val completed = latch.await(timeoutSeconds, TimeUnit.SECONDS)
    if !completed then Left(TimeoutException("コールバックがタイムアウトしました"))
    else result

  test("save writes text to a file") {
    val path = tempDir.resolve("saved.txt")
    val text = "こんにちは、FileService！"

    val result = awaitCallback[Path](
      FileService.save(path, text, _, _)
    )

    assertEquals(result, Right(path))
    assertEquals(Files.readString(path), text)
  }

  test("open reads the content of a file") {
    val path = tempDir.resolve("existing.txt")
    val text = "読み込みテスト"
    Files.writeString(path, text)

    val result = awaitCallback[String](
      FileService.open(path, _, _)
    )

    assertEquals(result, Right(text))
  }

  test("open returns an error when the file does not exist") {
    val path = tempDir.resolve("missing.txt")

    val result = awaitCallback[String](
      FileService.open(path, _, _)
    )

    assert(result.isLeft)
    assertEquals(result.swap.toOption.get.getMessage, s"ファイルが見つかりません。\n$path")
  }

  test("save prevents concurrent execution") {
    val path1 = tempDir.resolve("first.txt")
    val path2 = tempDir.resolve("second.txt")
    val latch = CountDownLatch(1)

    var firstResult: Option[Path] = None
    var secondCalled = false

    FileService.save(
      path1,
      "first",
      saved =>
        firstResult = Some(saved)
        latch.countDown()
      ,
      _ => latch.countDown()
    )

    FileService.save(
      path2,
      "second",
      _ => secondCalled = true,
      _ => ()
    )

    latch.await(5, TimeUnit.SECONDS)
    assertEquals(firstResult, Some(path1))
    assert(!secondCalled, "second save should not be executed while busy")
  }
