import type { Card, Column } from '../types/board';
import { CardItem } from './CardItem';
import { CreateCardForm } from './CreateCardForm';

export function BoardColumn({
  boardId,
  column,
  onCardCreated,
}: {
  boardId: string;
  column: Column;
  onCardCreated: (columnId: string, card: Card) => void;
}) {
  return (
    <div className="w-[260px] shrink-0 rounded bg-[#EBECF0] p-3">
      <h2 className="mb-3 text-sm font-semibold text-gray-700">{column.title}</h2>

      {column.cards.length === 0 ? (
        <p className="text-sm text-gray-500">カードはまだありません</p>
      ) : (
        <div className="flex flex-col gap-2">
          {column.cards.map((card) => (
            <CardItem key={card.id} card={card} />
          ))}
        </div>
      )}

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
