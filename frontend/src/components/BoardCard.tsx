import { useNavigate } from 'react-router-dom';
import type { BoardSummary } from '../types/board';

export function BoardCard({
  board,
  onDeleteClick,
}: {
  board: BoardSummary;
  onDeleteClick: (board: BoardSummary) => void;
}) {
  const navigate = useNavigate();

  return (
    <div className="relative w-[220px] rounded-md bg-white p-4 shadow hover:shadow-md transition-shadow">
      <button
        type="button"
        onClick={() => navigate(`/board/${board.id}`)}
        className="block w-full pr-4 text-left"
      >
        <span className="font-medium text-gray-900">{board.title}</span>
      </button>
      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onDeleteClick(board);
        }}
        aria-label="削除"
        className="absolute right-2 top-2 text-gray-400 hover:text-red-600"
      >
        ×
      </button>
    </div>
  );
}
