import { useEffect, useState } from 'react';
import { fetchBoards } from '../api/boards';
import type { BoardSummary } from '../types/board';
import { BoardCard } from '../components/BoardCard';

export function TopPage() {
  const [boards, setBoards] = useState<BoardSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchBoards()
      .then(setBoards)
      .catch(() => setError('ボード一覧の取得に失敗しました'));
  }, []);

  return (
    <div className="min-h-screen bg-[#F1F2F4]">
      <header className="bg-[#0052CC] px-6 py-4 text-white text-lg font-semibold">
        タスク管理アプリ
      </header>

      <main className="p-6">
        {error && <p className="text-red-600">{error}</p>}

        {!error && boards === null && <p className="text-gray-600">読み込み中...</p>}

        {!error && boards !== null && boards.length === 0 && (
          <p className="text-gray-600">まだボードがありません</p>
        )}

        {!error && boards !== null && boards.length > 0 && (
          <div className="flex flex-wrap gap-4">
            {boards.map((board) => (
              <BoardCard key={board.id} board={board} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
