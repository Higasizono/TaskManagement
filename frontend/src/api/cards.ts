import { apiPost } from './client';
import type { Card, CreateCardRequest } from '../types/board';

export function createCard(boardId: string, columnId: string, request: CreateCardRequest): Promise<Card> {
  return apiPost<Card, CreateCardRequest>(`/api/boards/${boardId}/columns/${columnId}/cards`, request);
}
