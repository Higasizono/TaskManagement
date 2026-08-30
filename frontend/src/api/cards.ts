import { apiDelete, apiPatch, apiPost } from './client';
import type { Card, CreateCardRequest, MoveCardRequest, UpdateCardRequest } from '../types/board';

export function createCard(boardId: string, columnId: string, request: CreateCardRequest): Promise<Card> {
  return apiPost<Card, CreateCardRequest>(`/api/boards/${boardId}/columns/${columnId}/cards`, request);
}

export function deleteCard(boardId: string, columnId: string, cardId: string): Promise<void> {
  return apiDelete(`/api/boards/${boardId}/columns/${columnId}/cards/${cardId}`);
}

export function updateCard(
  boardId: string,
  columnId: string,
  cardId: string,
  request: UpdateCardRequest,
): Promise<Card> {
  return apiPatch<Card, UpdateCardRequest>(`/api/boards/${boardId}/columns/${columnId}/cards/${cardId}`, request);
}

export function moveCard(
  boardId: string,
  columnId: string,
  cardId: string,
  request: MoveCardRequest,
): Promise<Card> {
  return apiPatch<Card, MoveCardRequest>(
    `/api/boards/${boardId}/columns/${columnId}/cards/${cardId}/move`,
    request,
  );
}
