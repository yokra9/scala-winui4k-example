import scala.language.implicitConversions

/** Scala の `Unit` と Kotlin の `Unit` を相互運用するためのユーティリティ。
  *
  * winui4k のコールバック API は Kotlin の `kotlin.Unit` を返すことが求められるが、 Scala では通常 `Unit`
  * を返すため、明示的な変換が必要になる。 このオブジェクトは、以下の 2 通りの方法でその変換を提供する。
  *
  *   - `kotlinUnit { ... }`：明示的にブロックを `kotlin.Unit` に変換する
  *   - `import KotlinInterop.given`：Scala の `Unit` から `kotlin.Unit` への implicit
  *     conversion を有効にし、通常の `Unit` を返すブロックをそのまま渡せるようにする
  */
object KotlinInterop:

  /** 指定された Scala の式を評価し、その結果を `kotlin.Unit` として返す。
    *
    * このメソッドは、Kotlin の `Unit` を返す必要がある場所で、Scala の `Unit` 式を 明示的にラップするために使用する。
    *
    * @param body
    *   評価する Scala の `Unit` 式
    * @return
    *   `kotlin.Unit.INSTANCE`
    */
  inline def kotlinUnit(body: => Unit): kotlin.Unit =
    body
    kotlin.Unit.INSTANCE

  /** Scala の `Unit` から Kotlin の `Unit` への暗黙的な変換。
    *
    * `import KotlinInterop.given` でスコープに入れることで、 Kotlin の `Unit` を返す API に対して通常の
    * Scala の `Unit` ブロックをそのまま渡せる。
    *
    * @return
    *   `kotlin.Unit.INSTANCE`
    */
  given Conversion[Unit, kotlin.Unit] with
    def apply(x: Unit): kotlin.Unit =
      kotlin.Unit.INSTANCE
