import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  closestCorners,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragOverEvent,
} from '@dnd-kit/core';
import { fetchBoardDetail } from '../api/boards';
import { ApiError } from '../api/client';
import { moveCard, updateCard } from '../api/cards';
import type { BoardDetail, Card, Column } from '../types/board';
import { BoardColumn } from '../components/BoardColumn';

const SAVE_ERROR_MESSAGE = '保存に失敗しました。もう一度お試しください';

function renameCardInState(board: BoardDetail, columnId: string, cardId: string, newTitle: string): BoardDetail {
  return {
    ...board,
    columns: board.columns.map((column) =>
      column.id !== columnId
        ? column
        : { ...column, cards: column.cards.map((c) => (c.id === cardId ? { ...c, title: newTitle } : c)) },
    ),
  };
}

function moveCardInState(
  board: BoardDetail,
  cardId: string,
  sourceColumnId: string,
  targetColumnId: string,
  targetIndex: number,
): BoardDetail {
  const sourceColumn = board.columns.find((c) => c.id === sourceColumnId);
  const card = sourceColumn?.cards.find((c) => c.id === cardId);
  if (!card) return board;

  return {
    ...board,
    columns: board.columns.map((column) => {
      if (column.id === sourceColumnId && column.id === targetColumnId) {
        const withoutCard = column.cards.filter((c) => c.id !== cardId);
        const idx = Math.min(targetIndex, withoutCard.length);
        return { ...column, cards: [...withoutCard.slice(0, idx), card, ...withoutCard.slice(idx)] };
      }
      if (column.id === sourceColumnId) {
        return { ...column, cards: column.cards.filter((c) => c.id !== cardId) };
      }
      if (column.id === targetColumnId) {
        const idx = Math.min(targetIndex, column.cards.length);
        return { ...column, cards: [...column.cards.slice(0, idx), card, ...column.cards.slice(idx)] };
      }
      return column;
    }),
  };
}

function findCardById(board: BoardDetail, cardId: string): Card | null {
  for (const column of board.columns) {
    const card = column.cards.find((c) => c.id === cardId);
    if (card) return card;
  }
  return null;
}

function findColumnIdByCardId(board: BoardDetail, cardId: string): string | null {
  const column = board.columns.find((c) => c.cards.some((card) => card.id === cardId));
  return column?.id ?? null;
}

function resolveOverColumnId(board: BoardDetail, over: DragOverEvent['over']): string | null {
  if (!over) return null;
  const overData = over.data.current as { type?: 'column' | 'card' } | undefined;
  if (overData?.type === 'column') return over.id as string;
  return findColumnIdByCardId(board, over.id as string);
}

export function BoardPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const [board, setBoard] = useState<BoardDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cardErrors, setCardErrors] = useState<Record<string, string>>({});
  const [overColumnId, setOverColumnId] = useState<string | null>(null);
  const [activeCard, setActiveCard] = useState<Card | null>(null);

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 8 } }));

  useEffect(() => {
    if (!boardId) return;

    setBoard(null);
    setError(null);

    fetchBoardDetail(boardId).then(setBoard).catch((err: unknown) => {
      if (err instanceof ApiError && err.status === 404) {
        setError('ボードが見つかりません');
      } else {
        setError('ボードの取得に失敗しました');
      }
    });
  }, [boardId]);

  function handleCardCreated(columnId: string, card: Card) {
    setBoard((prev) =>
      prev
        ? {
            ...prev,
            columns: prev.columns.map((column) =>
              column.id === columnId ? { ...column, cards: [...column.cards, card] } : column,
            ),
          }
        : prev,
    );
  }

  async function handleCardTitleSave(columnId: string, cardId: string, newTitle: string) {
    if (!board) return;
    const previousBoard = board;
    setBoard(renameCardInState(board, columnId, cardId, newTitle));
    try {
      await updateCard(board.id, columnId, cardId, { title: newTitle });
      setCardErrors((prev) => {
        const next = { ...prev };
        delete next[cardId];
        return next;
      });
    } catch {
      setBoard(previousBoard);
      setCardErrors((prev) => ({ ...prev, [cardId]: SAVE_ERROR_MESSAGE }));
    }
  }

  function handleDragStart(event: { active: { id: string | number } }) {
    if (!board) return;
    setActiveCard(findCardById(board, event.active.id as string));
  }

  function handleDragOver(event: DragOverEvent) {
    if (!board) return;
    setOverColumnId(resolveOverColumnId(board, event.over));
  }

  async function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    setActiveCard(null);
    setOverColumnId(null);
    if (!over || !board) return;

    const cardId = active.id as string;
    const sourceColumnId = (active.data.current as { columnId?: string } | undefined)?.columnId;
    if (!sourceColumnId) return;

    const overData = over.data.current as { type?: 'column' | 'card'; columnId?: string } | undefined;
    const targetColumnId = overData?.type === 'column' ? (over.id as string) : overData?.columnId;
    if (!targetColumnId) return;

    const targetColumn = board.columns.find((c) => c.id === targetColumnId);
    const sourceColumn = board.columns.find((c) => c.id === sourceColumnId);
    if (!targetColumn || !sourceColumn) return;

    const targetIndex =
      overData?.type === 'column' ? targetColumn.cards.length : targetColumn.cards.findIndex((c) => c.id === over.id);

    const currentIndex = sourceColumn.cards.findIndex((c) => c.id === cardId);
    if (sourceColumnId === targetColumnId && currentIndex === targetIndex) return;

    const previousBoard = board;
    setBoard(moveCardInState(board, cardId, sourceColumnId, targetColumnId, targetIndex));
    try {
      await moveCard(board.id, sourceColumnId, cardId, { targetColumnId, targetIndex });
      setCardErrors((prev) => {
        const next = { ...prev };
        delete next[cardId];
        return next;
      });
    } catch {
      setBoard(previousBoard);
      setCardErrors((prev) => ({ ...prev, [cardId]: SAVE_ERROR_MESSAGE }));
    }
  }

  return (
    <div className="min-h-screen bg-[#F1F2F4]">
      <header className="flex items-center gap-4 bg-[#0052CC] px-6 py-4 text-white">
        <button type="button" onClick={() => navigate('/')} className="hover:underline">
          ← 戻る
        </button>
        {board && <span className="text-lg font-semibold">{board.title}</span>}
      </header>

      <main className="p-6">
        {error && <p className="text-red-600">{error}</p>}

        {!error && board === null && <p className="text-gray-600">読み込み中...</p>}

        {!error && board !== null && (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDragEnd={handleDragEnd}
          >
            <div className="flex gap-4 overflow-x-auto">
              {board.columns.map((column: Column) => (
                <BoardColumn
                  key={column.id}
                  boardId={board.id}
                  column={column}
                  isOver={overColumnId === column.id}
                  onCardCreated={handleCardCreated}
                  onCardTitleSave={handleCardTitleSave}
                  cardErrors={cardErrors}
                />
              ))}
            </div>
            <DragOverlay>
              {activeCard && (
                <div className="rounded bg-white p-2 shadow-md text-sm text-gray-900">{activeCard.title}</div>
              )}
            </DragOverlay>
          </DndContext>
        )}
      </main>
    </div>
  );
}
