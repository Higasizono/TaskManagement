import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchBoardDetail } from '../api/boards';
import { ApiError } from '../api/client';
import type { BoardDetail } from '../types/board';
import { BoardColumn } from '../components/BoardColumn';

export function BoardPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const [board, setBoard] = useState<BoardDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!boardId) return;

    setBoard(null);
    setError(null);

    fetchBoardDetail(boardId).then(setBoard).catch((err: unknown) => {
      if (err instanceof ApiError && err.status === 404) {
        setError('ボードが見つかりません');
      } else {
        setError('ボードの取得に失敗しました');
      }
    });
  }, [boardId]);

  return (
    <div className="min-h-screen bg-[#F1F2F4]">
      <header className="flex items-center gap-4 bg-[#0052CC] px-6 py-4 text-white">
        <button type="button" onClick={() => navigate('/')} className="hover:underline">
          ← 戻る
        </button>
        {board && <span className="text-lg font-semibold">{board.title}</span>}
      </header>

      <main className="p-6">
        {error && <p className="text-red-600">{error}</p>}

        {!error && board === null && <p className="text-gray-600">読み込み中...</p>}

        {!error && board !== null && (
          <div className="flex gap-4 overflow-x-auto">
            {board.columns.map((column) => (
              <BoardColumn key={column.id} column={column} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
