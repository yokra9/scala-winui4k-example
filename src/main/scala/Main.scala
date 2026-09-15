import com.appkitbox.winui4k.{
  WFrame,
  WGrid,
  WMenuBar,
  WMenuBarItem,
  WMenuFlyoutItem,
  WTextField,
  WLabel,
  WContentDialog,
  ContentDialogResult,
  ContentDialogButton,
  WinUiUtilities,
  WDimension,
  GridLength
}
import java.nio.file.{Files, Paths}
import KotlinInterop.given

/** winui4k を使ったシンプルなメモ帳アプリケーションのエントリーポイント。 */
@main def main(): Unit =
  // WinUI のスレッド上で UI を構築する
  WinUiUtilities.INSTANCE.invokeLater { () =>
    // アプリケーション名
    val appName = "Scala WinUI4k Example"

    // メインウィンドウ
    val frame = new WFrame(s"無題 - $appName")

    // 複数行入力可能なテキストエリア
    val textArea = new WTextField()

    // 現在編集中のファイルパス。None の場合は未保存の新規ドキュメント
    var currentFile: Option[java.nio.file.Path] = None

    // 改行を受け付けるようにして、プレースホルダーを設定
    textArea.setAcceptsReturn(true)
    textArea.setPlaceholderText("ここに入力してください...")

    /** エラーメッセージを表示するダイアログ。
      *
      * @param message
      *   表示するエラーメッセージ
      */
    def showErrorDialog(message: String): Unit =
      val dialog = new WContentDialog("エラー", new WLabel(message))
      dialog.setPrimaryButtonText("OK")
      dialog.setDefaultButton(ContentDialogButton.PRIMARY)
      dialog.show(textArea, _ => ())

    /** 指定されたパスに現在のテキストを書き込む。 保存後、currentFile を更新し、ウィンドウタイトルを変更する。
      *
      * @param path
      *   保存先のファイルパス
      */
    def saveTo(path: java.nio.file.Path): Unit =
      try
        Files.writeString(path, textArea.getText())
        currentFile = Some(path)
        frame.setTitle(s"${path.getFileName()} - $appName")
      catch
        case e: Exception =>
          showErrorDialog(s"ファイルの保存に失敗しました。\n${e.getMessage}")

    /** 指定されたパスのファイルを開いてテキストエリアに読み込む。 ファイルが存在しない場合は何もしない。
      *
      * @param path
      *   開くファイルパス
      */
    def openFrom(path: java.nio.file.Path): Unit =
      if !Files.exists(path) then showErrorDialog(s"ファイルが見つかりません。\n${path}")
      else
        try
          textArea.setText(Files.readString(path))
          currentFile = Some(path)
          frame.setTitle(s"${path.getFileName()} - $appName")
        catch
          case e: Exception =>
            showErrorDialog(s"ファイルの読み込みに失敗しました。\n${e.getMessage}")

    /** 「名前を付けて保存」ダイアログを表示する。 winui4k の WContentDialog を使用し、相対パスを手入力して保存する。
      */
    def showSaveAsDialog(): Unit =
      val fileNameInput = new WTextField()
      fileNameInput.setPlaceholderText("メモ.txt")

      val label = new WLabel("ファイル名：")

      val dialogContent = new WGrid()
      dialogContent.addColumn(GridLength.Companion.star(1.0))
      dialogContent.addRow(GridLength.Companion.getAUTO())
      dialogContent.addRow(GridLength.Companion.getAUTO())
      dialogContent.add(label, 0, 0, 1, 1)
      dialogContent.add(fileNameInput, 1, 0, 1, 1)

      val dialog = new WContentDialog("名前を付けて保存", dialogContent)
      dialog.setPrimaryButtonText("保存")
      dialog.setCloseButtonText("キャンセル")
      dialog.setDefaultButton(ContentDialogButton.PRIMARY)
      dialog.show(
        textArea,
        result =>
          if result == ContentDialogResult.PRIMARY then
            val input = fileNameInput.getText()
            if input != null && input.trim.nonEmpty then
              try saveTo(Paths.get(input.trim))
              catch
                case e: Exception =>
                  showErrorDialog(s"ファイル名が無効です。\n${e.getMessage}")
      )

    // [ファイル] メニューの各項目を構築

    val newItem = new WMenuFlyoutItem("新規", null)
    newItem.addActionListener { () =>
      textArea.setText("")
      currentFile = None
      frame.setTitle(s"無題 - $appName")
    }

    val openItem = new WMenuFlyoutItem("開く", null)
    openItem.addActionListener { () =>
      showOpenDialog()
    }

    /** 「開く」ダイアログを表示する。 winui4k の WContentDialog を使用し、相対パスを手入力してファイルを開く。
      */
    def showOpenDialog(): Unit =
      val fileNameInput = new WTextField()
      fileNameInput.setPlaceholderText("メモ.txt")

      val label = new WLabel("ファイル名：")

      val dialogContent = new WGrid()
      dialogContent.addColumn(GridLength.Companion.star(1.0))
      dialogContent.addRow(GridLength.Companion.getAUTO())
      dialogContent.addRow(GridLength.Companion.getAUTO())
      dialogContent.add(label, 0, 0, 1, 1)
      dialogContent.add(fileNameInput, 1, 0, 1, 1)

      val dialog = new WContentDialog("開く", dialogContent)
      dialog.setPrimaryButtonText("開く")
      dialog.setCloseButtonText("キャンセル")
      dialog.setDefaultButton(ContentDialogButton.PRIMARY)
      dialog.show(
        textArea,
        result =>
          if result == ContentDialogResult.PRIMARY then
            val input = fileNameInput.getText()
            if input != null && input.trim.nonEmpty then
              try openFrom(Paths.get(input.trim))
              catch
                case e: Exception =>
                  showErrorDialog(s"ファイル名が無効です。\n${e.getMessage}")
      )

    val saveItem = new WMenuFlyoutItem("上書き保存", null)
    saveItem.addActionListener { () =>
      // 既にファイルが関連付けられていれば上書き、なければ「名前を付けて保存」と同じ動作
      currentFile match
        case Some(path) => saveTo(path)
        case None       => showSaveAsDialog()
    }

    val saveAsItem = new WMenuFlyoutItem("名前を付けて保存", null)
    saveAsItem.addActionListener { () =>
      showSaveAsDialog()
    }

    // [ファイル] メニューを組み立てる
    val fileMenu = new WMenuBarItem("ファイル")
    fileMenu.add(newItem)
    fileMenu.add(openItem)
    fileMenu.add(saveItem)
    fileMenu.add(saveAsItem)

    val menuBar = new WMenuBar()
    menuBar.add(fileMenu)

    // メニュー行とテキストエリア行を縦に配置
    val grid = new WGrid()
    grid.addColumn(GridLength.Companion.star(1.0))
    grid.addRow(GridLength.Companion.getAUTO()) // メニューバー
    grid.addRow(GridLength.Companion.star(1.0)) // テキストエリア（残り全体）
    grid.add(menuBar, 0, 0, 1, 1)
    grid.add(textArea, 1, 0, 1, 1)

    // ウィンドウの内容を設定して表示
    frame.setContentPane(grid)
    frame.getAppWindow().setSize(new WDimension(800, 600))
    frame.setVisible(true)
  }
