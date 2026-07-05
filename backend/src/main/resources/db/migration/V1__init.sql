-- ボードテーブル
CREATE TABLE boards (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(50) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW()
);

-- カラムテーブル（title・order_index は「未着手/進行中/完了」の3値に固定）
CREATE TABLE columns (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id    UUID        NOT NULL    REFERENCES boards(id) ON DELETE CASCADE,
    title       VARCHAR(30) NOT NULL    CHECK (title IN ('未着手', '進行中', '完了')),
    order_index INTEGER     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW()
);

-- カードテーブル
CREATE TABLE cards (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    column_id   UUID        NOT NULL    REFERENCES columns(id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    order_index INTEGER     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL    DEFAULT NOW()
);

-- インデックス（外部キー検索・表示順ソートを高速化）
CREATE INDEX idx_columns_board_id       ON columns(board_id);
CREATE INDEX idx_columns_board_order    ON columns(board_id, order_index);
CREATE INDEX idx_cards_column_id        ON cards(column_id);
CREATE INDEX idx_cards_column_order     ON cards(column_id, order_index);

-- updated_at を自動更新するトリガー関数
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_boards_updated_at
    BEFORE UPDATE ON boards
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_columns_updated_at
    BEFORE UPDATE ON columns
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_cards_updated_at
    BEFORE UPDATE ON cards
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
