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
  type DragStartEvent,
} from '@dnd-kit/core';
import { fetchBoardDetail } from '../api/boards';
import { ApiError } from '../api/client';
import { deleteCard, moveCard, updateCard } from '../api/cards';
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

function removeCardFromState(board: BoardDetail, columnId: string, cardId: string): BoardDetail {
  return {
    ...board,
    columns: board.columns.map((column) =>
      column.id !== columnId ? column : { ...column, cards: column.cards.filter((c) => c.id !== cardId) },
    ),
  };
}

function insertCardIntoState(
  board: BoardDetail,
  columnId: string,
  card: Card,
  index: number,
): BoardDetail {
  return {
    ...board,
    columns: board.columns.map((column) => {
      if (column.id !== columnId) return column;
      const idx = Math.min(index, column.cards.length);
      return { ...column, cards: [...column.cards.slice(0, idx), card, ...column.cards.slice(idx)] };
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

    // boardIdの高速な切り替えやStrictModeの二重実行で、古いレスポンスが
    // 新しい表示を上書きしないよう、破棄されたリクエストの結果は捨てる。
    let ignore = false;

    async function load(id: string) {
      try {
        const detail = await fetchBoardDetail(id);
        if (!ignore) setBoard(detail);
      } catch (err: unknown) {
        if (ignore) return;
        if (err instanceof ApiError && err.status === 404) {
          setError('ボードが見つかりません');
        } else {
          setError('ボードの取得に失敗しました');
        }
      }
    }

    void load(boardId);

    return () => {
      ignore = true;
    };
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

  function clearCardError(cardId: string) {
    setCardErrors((prev) => {
      if (!(cardId in prev)) return prev;
      const next = { ...prev };
      delete next[cardId];
      return next;
    });
  }

  async function handleCardTitleSave(columnId: string, cardId: string, newTitle: string) {
    if (!board) return;
    const previousTitle = findCardById(board, cardId)?.title;
    if (previousTitle === undefined) return;

    setBoard((prev) => (prev ? renameCardInState(prev, columnId, cardId, newTitle) : prev));
    try {
      await updateCard(board.id, columnId, cardId, { title: newTitle });
      clearCardError(cardId);
    } catch {
      // ボード全体のスナップショットではなく、対象カードの変更だけを戻す。
      // 並行して行われた他カードの更新を巻き戻さないため。
      setBoard((prev) => (prev ? renameCardInState(prev, columnId, cardId, previousTitle) : prev));
      setCardErrors((prev) => ({ ...prev, [cardId]: SAVE_ERROR_MESSAGE }));
    }
  }

  async function handleCardDelete(columnId: string, cardId: string) {
    if (!board) return;
    const column = board.columns.find((c) => c.id === columnId);
    const previousIndex = column?.cards.findIndex((c) => c.id === cardId) ?? -1;
    const deletedCard = previousIndex >= 0 ? column?.cards[previousIndex] : undefined;
    if (!deletedCard) return;

    setBoard((prev) => (prev ? removeCardFromState(prev, columnId, cardId) : prev));
    try {
      await deleteCard(board.id, columnId, cardId);
      clearCardError(cardId);
    } catch {
      // 削除したカードを元の位置に戻す。
      setBoard((prev) =>
        prev ? insertCardIntoState(prev, columnId, deletedCard, previousIndex) : prev,
      );
      setCardErrors((prev) => ({ ...prev, [cardId]: SAVE_ERROR_MESSAGE }));
    }
  }

  function handleDragStart(event: DragStartEvent) {
    if (!board) return;
    setActiveCard(findCardById(board, String(event.active.id)));
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

    const cardId = String(active.id);
    const sourceColumnId = (active.data.current as { columnId?: string } | undefined)?.columnId;
    if (!sourceColumnId) return;

    const overData = over.data.current as { type?: 'column' | 'card'; columnId?: string } | undefined;
    const targetColumnId = overData?.type === 'column' ? String(over.id) : overData?.columnId;
    if (!targetColumnId) return;

    const targetColumn = board.columns.find((c) => c.id === targetColumnId);
    const sourceColumn = board.columns.find((c) => c.id === sourceColumnId);
    if (!targetColumn || !sourceColumn) return;

    const targetIndex =
      overData?.type === 'column' ? targetColumn.cards.length : targetColumn.cards.findIndex((c) => c.id === over.id);

    const currentIndex = sourceColumn.cards.findIndex((c) => c.id === cardId);
    if (sourceColumnId === targetColumnId && currentIndex === targetIndex) return;

    setBoard((prev) =>
      prev ? moveCardInState(prev, cardId, sourceColumnId, targetColumnId, targetIndex) : prev,
    );
    try {
      await moveCard(board.id, sourceColumnId, cardId, { targetColumnId, targetIndex });
      clearCardError(cardId);
    } catch {
      // 移動したカードだけを元のカラム・位置へ戻す。
      setBoard((prev) =>
        prev ? moveCardInState(prev, cardId, targetColumnId, sourceColumnId, currentIndex) : prev,
      );
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
                  onCardDelete={handleCardDelete}
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
