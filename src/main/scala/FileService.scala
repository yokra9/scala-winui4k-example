import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.CompletableFuture

/** winui4k サンプル用の簡易ファイル入出力サービス。
  *
  * UI スレッドをブロックしないよう、バックグラウンドスレッドでファイル I/O を実行し、 結果をコールバックで返す。
  */
object FileService:

  /** ファイル入出力処理中は true になり、二重実行を防ぐ。 */
  private var isBusy: Boolean = false

  /** バックグラウンドスレッドで処理を実行し、結果を指定したコールバックに渡す。 */
  private def runInBackground[T](
      task: => T,
      onSuccess: T => Unit,
      onError: Throwable => Unit
  ): Unit =
    CompletableFuture.supplyAsync(() => task).whenComplete { (result, error) =>
      if error != null then onError(error)
      else onSuccess(result)
    }
    ()

  /** テキストを指定パスに保存する。
    *
    * @param path
    *   保存先パス
    * @param text
    *   保存するテキスト
    * @param onSuccess
    *   保存成功時に呼ばれるコールバック
    * @param onError
    *   エラー発生時に呼ばれるコールバック
    */
  def save(
      path: Path,
      text: String,
      onSuccess: Path => Unit,
      onError: Throwable => Unit
  ): Unit =
    if isBusy then return
    isBusy = true
    runInBackground(
      {
        Files.writeString(path, text)
        path
      },
      savedPath => {
        isBusy = false
        onSuccess(savedPath)
      },
      e => {
        isBusy = false
        onError(e)
      }
    )

  /** 指定パスのファイルを読み込む。
    *
    * @param path
    *   読み込み元パス
    * @param onSuccess
    *   読み込み成功時に呼ばれるコールバック
    * @param onError
    *   エラー発生時に呼ばれるコールバック
    */
  def open(
      path: Path,
      onSuccess: String => Unit,
      onError: Throwable => Unit
  ): Unit =
    if isBusy then return
    if !Files.exists(path) then
      onError(new IllegalArgumentException(s"ファイルが見つかりません。\n$path"))
    else
      isBusy = true
      runInBackground(
        Files.readString(path),
        content => {
          isBusy = false
          onSuccess(content)
        },
        e => {
          isBusy = false
          onError(e)
        }
      )
