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
  GridLength,
  VirtualKey,
  VirtualKeyModifier,
  WFileOpenPicker,
  WFileSavePicker,
  PickerLocationId
}
import java.nio.file.{Path, Paths}
import java.util.function.Consumer
import file.FileService
import KotlinInterop.given

/** winui4k を使ったシンプルなメモ帳アプリケーションのエントリーポイント。 */
@main def main(): Unit =
  // WinUI のスレッド上で UI を構築する
  WinUiUtilities.invokeLater { () =>
    // アプリケーション名
    val appName = "Scala WinUI4k Example"

    // メインウィンドウ
    val frame = new WFrame(s"無題 - $appName")

    // 複数行入力可能なテキストエリア
    val textArea = new WTextField()

    // 現在編集中のファイルパス。None の場合は未保存の新規ドキュメント
    var currentFile: Option[Path] = None

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

    /** UI スレッドで実行するためのヘルパー。 */
    def runOnUi(action: => Unit): Unit =
      WinUiUtilities.invokeLater { () => action }

    /** 指定されたパスに現在のテキストを書き込む。 保存後、currentFile を更新し、ウィンドウタイトルを変更する。
      *
      * @param path
      *   保存先のファイルパス
      */
    def saveTo(path: Path): Unit =
      textArea.setEnabled(false)
      FileService.save(
        path,
        textArea.getText(),
        savedPath =>
          runOnUi {
            currentFile = Some(savedPath)
            frame.setTitle(s"${savedPath.getFileName} - $appName")
            textArea.setEnabled(true)
          },
        e =>
          runOnUi {
            textArea.setEnabled(true)
            showErrorDialog(s"ファイルの保存に失敗しました。\n${e.getMessage}")
          }
      )

    /** 指定されたパスのファイルを開いてテキストエリアに読み込む。 ファイルが存在しない場合は何もしない。
      *
      * @param path
      *   開くファイルパス
      */
    def openFrom(path: Path): Unit =
      textArea.setEnabled(false)
      FileService.open(
        path,
        content =>
          runOnUi {
            textArea.setText(content)
            currentFile = Some(path)
            frame.setTitle(s"${path.getFileName} - $appName")
            textArea.setEnabled(true)
          },
        e =>
          runOnUi {
            textArea.setEnabled(true)
            showErrorDialog(s"ファイルの読み込みに失敗しました。\n${e.getMessage}")
          }
      )

    /** 「名前を付けて保存」ダイアログを表示する。 winui4k の WFileSavePicker を使用する。 */
    def showSaveAsDialog(): Unit =
      val picker = new WFileSavePicker(frame)
      picker.setDefaultFileExtension(".txt")
      picker.setSuggestedStartLocation(PickerLocationId.DOCUMENTS_LIBRARY)
      picker.pickSaveFile(new Consumer[String] {
        override def accept(path: String): Unit =
          if path != null then
            try saveTo(Paths.get(path))
            catch
              case e: Exception =>
                showErrorDialog(s"ファイル名が無効です。\n${e.getMessage}")
      })

    // [ファイル] メニューの各項目を構築

    val newItem = new WMenuFlyoutItem("新規", null)
    newItem.setKeyboardAcceleratorText("Ctrl+N")
    newItem.addActionListener { () =>
      textArea.setText("")
      currentFile = None
      frame.setTitle(s"無題 - $appName")
    }

    val openItem = new WMenuFlyoutItem("開く", null)
    openItem.setKeyboardAcceleratorText("Ctrl+O")
    openItem.addActionListener { () =>
      showOpenDialog()
    }

    /** 「開く」ダイアログを表示する。 winui4k の WFileOpenPicker を使用する。 */
    def showOpenDialog(): Unit =
      val picker = new WFileOpenPicker(frame)
      picker.addFileTypeFilter(".txt")
      picker.setSuggestedStartLocation(PickerLocationId.DOCUMENTS_LIBRARY)
      picker.pickSingleFile(new Consumer[String] {
        override def accept(path: String): Unit =
          if path != null then
            try openFrom(Paths.get(path))
            catch
              case e: Exception =>
                showErrorDialog(s"ファイル名が無効です。\n${e.getMessage}")
      })

    val saveItem = new WMenuFlyoutItem("上書き保存", null)
    saveItem.setKeyboardAcceleratorText("Ctrl+S")
    saveItem.addActionListener { () =>
      // 既にファイルが関連付けられていれば上書き、なければ「名前を付けて保存」と同じ動作
      currentFile match
        case Some(path) => saveTo(path)
        case None       => showSaveAsDialog()
    }

    val saveAsItem = new WMenuFlyoutItem("名前を付けて保存", null)
    saveAsItem.setKeyboardAcceleratorText("Ctrl+Shift+S")
    saveAsItem.addActionListener { () =>
      showSaveAsDialog()
    }

    // テキストエリアにキーボードショートカットを登録する
    textArea.addKeyboardAccelerator(
      VirtualKey.N,
      Array(VirtualKeyModifier.CONTROL),
      () => {
        textArea.setText("")
        currentFile = None
        frame.setTitle(s"無題 - $appName")
      }
    )
    textArea.addKeyboardAccelerator(
      VirtualKey.O,
      Array(VirtualKeyModifier.CONTROL),
      () => showOpenDialog()
    )
    textArea.addKeyboardAccelerator(
      VirtualKey.S,
      Array(VirtualKeyModifier.CONTROL),
      () =>
        currentFile match
          case Some(path) => saveTo(path)
          case None       => showSaveAsDialog()
    )
    textArea.addKeyboardAccelerator(
      VirtualKey.S,
      Array(VirtualKeyModifier.CONTROL, VirtualKeyModifier.SHIFT),
      () => showSaveAsDialog()
    )

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
    grid.addRow() // メニューバー
    grid.addRow(GridLength.Companion.star(1.0)) // テキストエリア（残り全体）
    grid.add(menuBar, 0, 0, 1, 1)
    grid.add(textArea, 1, 0, 1, 1)

    // ウィンドウの内容を設定して表示
    frame.setContentPane(grid)
    frame.getAppWindow().setSize(new WDimension(800, 600))
    frame.setVisible(true)

    System.out.println("App Started")
  }
