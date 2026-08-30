import { useState } from 'react';
import { updateBoard } from '../api/boards';
import type { BoardSummary } from '../types/board';

export function EditableBoardTitle({
  boardId,
  title,
  onUpdated,
}: {
  boardId: string;
  title: string;
  onUpdated: (board: BoardSummary) => void;
}) {
  const [isEditing, setIsEditing] = useState(false);
  const [draftTitle, setDraftTitle] = useState(title);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEditing() {
    setDraftTitle(title);
    setError(null);
    setIsEditing(true);
  }

  function cancel() {
    setIsEditing(false);
    setDraftTitle(title);
    setError(null);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmedTitle = draftTitle.trim();
    if (trimmedTitle === '' || trimmedTitle === title) {
      cancel();
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const updated = await updateBoard(boardId, { title: trimmedTitle });
      onUpdated(updated);
      setIsEditing(false);
    } catch {
      setError('ボード名の更新に失敗しました');
    } finally {
      setSubmitting(false);
    }
  }

  if (!isEditing) {
    return (
      <button
        type="button"
        onClick={startEditing}
        className="rounded border border-transparent px-2 py-1 text-left text-xl font-semibold text-gray-900 hover:underline"
        title="タイトルを編集"
      >
        {title}
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="flex items-center gap-2">
      <input
        type="text"
        value={draftTitle}
        onChange={(e) => setDraftTitle(e.target.value)}
        autoFocus
        disabled={submitting}
        className="rounded border border-gray-300 px-2 py-1 text-xl font-semibold text-gray-900"
      />
      <button
        type="submit"
        disabled={submitting || draftTitle.trim() === ''}
        className="rounded bg-[#0052CC] px-2 py-1 text-sm text-white disabled:opacity-50"
      >
        保存
      </button>
      <button
        type="button"
        onClick={cancel}
        disabled={submitting}
        className="rounded px-2 py-1 text-sm text-gray-600"
      >
        キャンセル
      </button>
      {error && <p className="text-red-600 text-sm">{error}</p>}
    </form>
  );
}
