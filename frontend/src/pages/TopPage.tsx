import { useEffect, useState } from 'react';
import { deleteBoard, fetchBoards } from '../api/boards';
import type { BoardSummary } from '../types/board';
import { BoardCard } from '../components/BoardCard';
import { CreateBoardForm } from '../components/CreateBoardForm';
import { ConfirmDialog } from '../components/ConfirmDialog';

export function TopPage() {
  const [boards, setBoards] = useState<BoardSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<BoardSummary | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  useEffect(() => {
    fetchBoards()
      .then(setBoards)
      .catch(() => setError('ボード一覧の取得に失敗しました'));
  }, []);

  function handleDeleteClick(board: BoardSummary) {
    setDeleteTarget(board);
    setDeleteError(null);
  }

  function handleCancelDelete() {
    setDeleteTarget(null);
    setDeleteError(null);
  }

  async function handleConfirmDelete() {
    if (!deleteTarget) return;

    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteBoard(deleteTarget.id);
      setBoards((prev) => prev && prev.filter((b) => b.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch {
      setDeleteError('削除に失敗しました。もう一度お試しください');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#F1F2F4]">
      <header className="bg-[#0052CC] px-6 py-4 text-white text-lg font-semibold">
        タスク管理アプリ
      </header>

      <main className="p-6">
        {error && <p className="text-red-600">{error}</p>}

        {!error && boards === null && <p className="text-gray-600">読み込み中...</p>}

        {!error && boards !== null && (
          <div className="flex flex-wrap gap-4">
            {boards.map((board) => (
              <BoardCard key={board.id} board={board} onDeleteClick={handleDeleteClick} />
            ))}
            <CreateBoardForm
              onCreated={(board) => setBoards((prev) => (prev ? [...prev, board] : [board]))}
            />
          </div>
        )}
      </main>

      {deleteTarget && (
        <ConfirmDialog
          title="ボードを削除しますか？"
          message={`「${deleteTarget.title}」を削除します。\n配下のカラム・カードもすべて削除されます。`}
          confirmLabel="削除する"
          cancelLabel="キャンセル"
          onConfirm={handleConfirmDelete}
          onCancel={handleCancelDelete}
          submitting={deleting}
          error={deleteError}
        />
      )}
    </div>
  );
}
