export interface BoardSummary {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface Card {
  id: string;
  title: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface Column {
  id: string;
  title: string;
  orderIndex: number;
  cards: Card[];
}

export interface BoardDetail {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  columns: Column[];
}
