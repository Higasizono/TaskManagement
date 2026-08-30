import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import type { Card, Column } from '../types/board';
import { CardItem } from './CardItem';
import { CreateCardForm } from './CreateCardForm';

export function BoardColumn({
  boardId,
  column,
  isOver,
  onCardCreated,
  onCardTitleSave,
  cardErrors,
}: {
  boardId: string;
  column: Column;
  isOver: boolean;
  onCardCreated: (columnId: string, card: Card) => void;
  onCardTitleSave: (columnId: string, cardId: string, newTitle: string) => void;
  cardErrors: Record<string, string>;
}) {
  const { setNodeRef } = useDroppable({ id: column.id, data: { type: 'column' } });

  return (
    <div
      ref={setNodeRef}
      className={`w-[260px] shrink-0 rounded p-3 ${isOver ? 'bg-white ring-2 ring-blue-400' : 'bg-[#EBECF0]'}`}
    >
      <h2 className="mb-3 text-sm font-semibold text-gray-700">{column.title}</h2>

      <SortableContext items={column.cards.map((c) => c.id)} strategy={verticalListSortingStrategy}>
        {column.cards.length === 0 ? (
          <p className="text-sm text-gray-500">カードはまだありません</p>
        ) : (
          <div className="flex flex-col gap-2">
            {column.cards.map((card) => (
              <CardItem
                key={card.id}
                card={card}
                columnId={column.id}
                onTitleSave={onCardTitleSave}
                error={cardErrors[card.id]}
              />
            ))}
          </div>
        )}
      </SortableContext>

      <div className="mt-2">
        <CreateCardForm
          boardId={boardId}
          columnId={column.id}
          onCreated={(card) => onCardCreated(column.id, card)}
        />
      </div>
    </div>
  );
}
