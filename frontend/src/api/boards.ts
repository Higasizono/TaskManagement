import { apiGet, apiPost, apiPut } from './client';
import type { BoardDetail, BoardSummary, CreateBoardRequest, UpdateBoardRequest } from '../types/board';

export function fetchBoards(): Promise<BoardSummary[]> {
  return apiGet<BoardSummary[]>('/api/boards');
}

export function fetchBoardDetail(boardId: string): Promise<BoardDetail> {
  return apiGet<BoardDetail>(`/api/boards/${boardId}`);
}

export function createBoard(request: CreateBoardRequest): Promise<BoardSummary> {
  return apiPost<BoardSummary, CreateBoardRequest>('/api/boards', request);
}

export function updateBoard(boardId: string, request: UpdateBoardRequest): Promise<BoardSummary> {
  return apiPut<BoardSummary, UpdateBoardRequest>(`/api/boards/${boardId}`, request);
}
