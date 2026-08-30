import { apiDelete, apiGet, apiPost } from './client';
import type { BoardDetail, BoardSummary, CreateBoardRequest } from '../types/board';

export function fetchBoards(): Promise<BoardSummary[]> {
  return apiGet<BoardSummary[]>('/api/boards');
}

export function fetchBoardDetail(boardId: string): Promise<BoardDetail> {
  return apiGet<BoardDetail>(`/api/boards/${boardId}`);
}

export function createBoard(request: CreateBoardRequest): Promise<BoardSummary> {
  return apiPost<BoardSummary, CreateBoardRequest>('/api/boards', request);
}

export function deleteBoard(boardId: string): Promise<void> {
  return apiDelete(`/api/boards/${boardId}`);
}
