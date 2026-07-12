import type { Column } from '../types/board';
import { CardItem } from './CardItem';

export function BoardColumn({ column }: { column: Column }) {
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
    </div>
  );
}
