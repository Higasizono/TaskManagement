import { apiGet } from './client';
import type { BoardDetail, BoardSummary } from '../types/board';

export function fetchBoards(): Promise<BoardSummary[]> {
  return apiGet<BoardSummary[]>('/api/boards');
}

export function fetchBoardDetail(boardId: string): Promise<BoardDetail> {
  return apiGet<BoardDetail>(`/api/boards/${boardId}`);
}
