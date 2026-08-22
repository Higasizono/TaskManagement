import { useState } from 'react';
import { createBoard } from '../api/boards';
import type { BoardSummary } from '../types/board';

export function CreateBoardForm({ onCreated }: { onCreated: (board: BoardSummary) => void }) {
  const [isOpen, setIsOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function close() {
    setIsOpen(false);
    setTitle('');
    setError(null);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmedTitle = title.trim();
    if (trimmedTitle === '') {
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const board = await createBoard({ title: trimmedTitle });
      onCreated(board);
      close();
    } catch {
      setError('ボードの作成に失敗しました');
    } finally {
      setSubmitting(false);
    }
  }

  if (!isOpen) {
    return (
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        className="w-[220px] rounded-md border border-dashed border-gray-400 p-4 text-left text-gray-600 hover:bg-white transition-colors"
      >
        + 新しいボード
      </button>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="w-[220px] rounded-md bg-white p-4 shadow flex flex-col gap-2"
    >
      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="ボード名"
        required
        autoFocus
        disabled={submitting}
        className="rounded border border-gray-300 px-2 py-1 text-sm text-gray-900"
      />
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={submitting || title.trim() === ''}
          className="rounded bg-[#0052CC] px-3 py-1 text-sm text-white disabled:opacity-50"
        >
          作成
        </button>
        <button
          type="button"
          onClick={close}
          disabled={submitting}
          className="rounded px-3 py-1 text-sm text-gray-600"
        >
          キャンセル
        </button>
      </div>
    </form>
  );
}
