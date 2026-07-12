import type { Card } from '../types/board';

export function CardItem({ card }: { card: Card }) {
  return (
    <div className="rounded bg-white p-2 shadow-sm text-sm text-gray-900">
      {card.title}
    </div>
  );
}
