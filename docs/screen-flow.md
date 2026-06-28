# 画面遷移図

---

## 1. ページ間遷移

```mermaid
flowchart TD
    Start([アプリ起動]) --> Top

    Top["トップページ (/)"]
    Board["ボードページ (/board/:id)"]

    Top -->|ボードカードをクリック| Board
    Board -->|「← 戻る」クリック| Top
    Board -->|ブラウザの戻るボタン| Top
```

---

## 2. トップページ内のUIフロー

```mermaid
flowchart TD
    Top[トップページ表示]

    Top --> HasBoards{ボードが存在するか}
    HasBoards -->|0件| Empty[空状態を表示\nメッセージ＋作成ボタン]
    HasBoards -->|1件以上| List[ボード一覧を表示]

    %% ボード作成フロー
    List --> ClickCreate[「＋ 新規ボードを作成」クリック]
    Empty --> ClickCreate
    ClickCreate --> ShowForm[インライン入力フォームを表示\n自動フォーカス]

    ShowForm --> |Escキー| Top
    ShowForm --> |文字を入力してEnter / 作成ボタン| Validate{バリデーション}
    Validate -->|NG| ShowError[エラーメッセージ表示]
    ShowError --> ShowForm
    Validate -->|OK| SaveBoard[ボードを保存・一覧末尾に追加]
    SaveBoard --> Top

    %% ボード削除フロー
    List --> ClickDelete[「×」クリック]
    ClickDelete --> Confirm{削除確認ダイアログ}
    Confirm -->|キャンセル| Top
    Confirm -->|削除する| DeleteBoard[ボード・配下データを削除\n一覧を更新]
    DeleteBoard --> Top
```

---

## 3. ボードページ内のUIフロー

```mermaid
flowchart TD
    Board[ボードページ表示]

    Board --> HasColumns{カラムが存在するか}
    HasColumns -->|0件| EmptyBoard[空状態を表示\nカラム追加ボタンのみ]
    HasColumns -->|1件以上| Columns[カラム一覧を表示]

    %% カラム追加フロー
    Columns --> ClickAddCol[「＋ カラムを追加」クリック]
    EmptyBoard --> ClickAddCol
    ClickAddCol --> ShowColForm[カラム入力フォームを表示\n自動フォーカス]
    ShowColForm -->|Escキー| Board
    ShowColForm -->|Enter / 追加ボタン| ValidateCol{バリデーション}
    ValidateCol -->|NG| ColError[エラーメッセージ表示]
    ColError --> ShowColForm
    ValidateCol -->|OK| SaveCol[カラムを保存・末尾に追加]
    SaveCol --> Board

    %% カラム削除フロー
    Columns --> ClickDelCol[カラムの「×」クリック]
    ClickDelCol --> ConfirmCol{削除確認ダイアログ}
    ConfirmCol -->|キャンセル| Board
    ConfirmCol -->|削除する| DeleteCol[カラム・配下カードを削除]
    DeleteCol --> Board

    %% カード追加フロー
    Columns --> ClickAddCard[「＋ カードを追加」クリック]
    ClickAddCard --> ShowCardForm[カードフォームをカラム内に展開\n自動フォーカス]
    ShowCardForm -->|Esc / キャンセル| Board
    ShowCardForm -->|Enter / 追加ボタン| ValidateCard{バリデーション}
    ValidateCard -->|NG| CardError[エラーメッセージ表示]
    CardError --> ShowCardForm
    ValidateCard -->|OK| SaveCard[カードを保存・カラム末尾に追加]
    SaveCard --> Board

    %% カード編集フロー
    Columns --> ClickTitle[カードのタイトルをクリック]
    ClickTitle --> EditMode[タイトルがインライン入力に切り替わる\n現在テキストを選択状態]
    EditMode -->|Escキー| CancelEdit[編集キャンセル・元のタイトルに戻す]
    CancelEdit --> Board
    EditMode -->|Enterキー| ValidateEdit{バリデーション}
    ValidateEdit -->|NG| EditError[エラーメッセージ表示]
    EditError --> EditMode
    ValidateEdit -->|OK| SaveEdit[タイトルを更新・表示モードに戻す]
    SaveEdit --> Board

    %% カード削除フロー
    Columns --> ClickDelCard[カードの「×」クリック]
    ClickDelCard --> DeleteCard[カードを即時削除]
    DeleteCard --> Board

    %% カード移動フロー（ドラッグ&ドロップ）
    Columns --> DragStart[カードをドラッグ開始]
    DragStart --> Dragging[ゴースト表示・移動先カラムハイライト]
    Dragging -->|元の位置に戻す / Escキー| Board
    Dragging -->|別カラムにドロップ| MoveCard[カードの columnId と order を更新]
    MoveCard --> Board
    Dragging -->|同カラム内の別位置にドロップ| ReorderCard[カードの order を更新]
    ReorderCard --> Board
```

---

## 4. データ保存タイミング

| 操作 | 保存タイミング |
|------|--------------|
| ボード作成 | 作成確定時（即時） |
| ボード削除 | 削除確定時（即時） |
| カラム追加 | 追加確定時（即時） |
| カラム削除 | 削除確定時（即時） |
| カード追加 | 追加確定時（即時） |
| カード編集 | 編集確定時（Enterキー時） |
| カード削除 | 削除時（即時） |
| カード移動 | ドロップ完了時（即時） |
