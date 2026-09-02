import { useEffect, useId, useRef } from 'react';

export function ConfirmDialog({
  title,
  message,
  confirmLabel,
  cancelLabel,
  onConfirm,
  onCancel,
  submitting,
  error,
}: {
  title: string;
  message: string;
  confirmLabel: string;
  cancelLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
  submitting?: boolean;
  error?: string | null;
}) {
  const titleId = useId();
  const dialogRef = useRef<HTMLDialogElement>(null);

  // showModal() により、フォーカスのダイアログ内への移動・フォーカストラップ・
  // Escキーでのキャンセル・背面要素の不活性化がブラウザ標準で得られる。
  useEffect(() => {
    dialogRef.current?.showModal();
  }, []);

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={titleId}
      onCancel={(e) => {
        // Escでのネイティブな閉じ動作は抑止し、Reactの状態経由で閉じる。
        e.preventDefault();
        if (!submitting) onCancel();
      }}
      className="w-[320px] rounded-md bg-white p-6 shadow-lg backdrop:bg-black/40"
    >
      <h2 id={titleId} className="mb-2 text-base font-semibold text-gray-900">
        {title}
      </h2>
      <p className="mb-4 text-sm text-gray-700 whitespace-pre-line">{message}</p>
      {error && <p className="mb-2 text-sm text-red-600">{error}</p>}
      <div className="flex justify-end gap-2">
        <button
          type="button"
          onClick={onCancel}
          disabled={submitting}
          className="rounded px-3 py-1 text-sm text-gray-600"
        >
          {cancelLabel}
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={submitting}
          className="rounded bg-red-600 px-3 py-1 text-sm text-white disabled:opacity-50"
        >
          {confirmLabel}
        </button>
      </div>
    </dialog>
  );
}
