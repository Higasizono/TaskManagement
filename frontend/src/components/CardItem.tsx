import { useEffect, useRef, useState } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { Card } from '../types/board';

export function CardItem({
  card,
  columnId,
  onTitleSave,
  onDelete,
  error,
}: {
  card: Card;
  columnId: string;
  onTitleSave: (columnId: string, cardId: string, newTitle: string) => void;
  onDelete: (columnId: string, cardId: string) => void;
  error?: string;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: card.id,
    data: { type: 'card', columnId },
  });
  const style = { transform: CSS.Transform.toString(transform), transition };

  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(card.title);
  const [validationError, setValidationError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isEditing) {
      inputRef.current?.focus();
      inputRef.current?.select();
    }
  }, [isEditing]);

  function startEdit() {
    setEditTitle(card.title);
    setValidationError(null);
    setIsEditing(true);
  }

  function cancelEdit() {
    setEditTitle(card.title);
    setValidationError(null);
    setIsEditing(false);
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const value = e.target.value;
    setEditTitle(value);
    if (validationError) {
      const trimmed = value.trim();
      if (trimmed !== '' && trimmed.length <= 100) {
        setValidationError(null);
      }
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Escape') {
      e.preventDefault();
      cancelEdit();
    }
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = editTitle.trim();
    if (trimmed === '') {
      setValidationError('カード名を入力してください');
      return;
    }
    if (trimmed.length > 100) {
      setValidationError('カード名は100文字以内で入力してください');
      return;
    }
    setIsEditing(false);
    if (trimmed !== card.title) {
      onTitleSave(columnId, card.id, trimmed);
    }
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`flex items-center gap-2 rounded bg-white p-2 shadow-sm text-sm text-gray-900 ${
        isDragging ? 'opacity-50' : ''
      }`}
    >
      <span {...attributes} {...listeners} className="cursor-grab text-gray-400 select-none">
        ⠿
      </span>

      <div className="flex-1">
        {isEditing ? (
          <form onSubmit={handleSubmit}>
            <input
              ref={inputRef}
              value={editTitle}
              onChange={handleChange}
              onKeyDown={handleKeyDown}
              className="w-full rounded border border-gray-300 px-1 py-0.5 text-sm text-gray-900"
            />
            {validationError && <p className="text-red-600 text-sm">{validationError}</p>}
          </form>
        ) : (
          <button type="button" onClick={startEdit} className="w-full cursor-pointer text-left">
            {card.title}
          </button>
        )}
        {!isEditing && error && <p className="text-red-600 text-sm">{error}</p>}
      </div>

      <button
        type="button"
        onClick={() => onDelete(columnId, card.id)}
        aria-label="削除"
        className="text-gray-400 hover:text-red-600"
      >
        ×
      </button>
    </div>
  );
}
