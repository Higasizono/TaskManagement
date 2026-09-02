import { useEffect, useRef, useState } from 'react';
import { createCard } from '../api/cards';
import type { Card } from '../types/board';

export function CreateCardForm({
  boardId,
  columnId,
  onCreated,
}: {
  boardId: string;
  columnId: string;
  onCreated: (card: Card) => void;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    }
  }, [isOpen]);

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
      const card = await createCard(boardId, columnId, { title: trimmedTitle });
      onCreated(card);
      close();
    } catch {
      setError('カードの作成に失敗しました');
    } finally {
      setSubmitting(false);
    }
  }

  if (!isOpen) {
    return (
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        className="w-full rounded p-2 text-left text-sm text-gray-600 hover:bg-white transition-colors"
      >
        + カードを追加
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2 rounded bg-white p-2 shadow-sm">
      <input
        ref={inputRef}
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="カード名"
        required
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
          追加
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
