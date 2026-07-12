import { useNavigate } from 'react-router-dom';
import type { BoardSummary } from '../types/board';

export function BoardCard({ board }: { board: BoardSummary }) {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      onClick={() => navigate(`/board/${board.id}`)}
      className="w-[220px] rounded-md bg-white p-4 text-left shadow hover:shadow-md transition-shadow"
    >
      <span className="font-medium text-gray-900">{board.title}</span>
    </button>
  );
}
