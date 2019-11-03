package com.codingotaku.apps.callback;

import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class TableSelectListener implements Callback<TableView<DownloadInfo>, TableRow<DownloadInfo>> {
	private static DownloadManager manager = DownloadManager.getInstance();
	// For selected row
	private final MenuItem pause;
	private final MenuItem resume;
	private final MenuItem cancel;
	private final MenuItem retry;
	private final MenuItem restart;
	private final MenuItem delete;

	final ContextMenu rowMenu = new ContextMenu();

	public TableSelectListener() {
		pause = new MenuItem("pause");
		resume = new MenuItem("Resume");
		cancel = new MenuItem("Cancel");
		retry = new MenuItem("Retry");
		restart = new MenuItem("Restart");
		delete = new MenuItem("Delete");

		rowMenu.getItems().addAll(pause, resume, cancel, retry, restart, delete);
	}

	private void initListeners(TableView<DownloadInfo> view, DownloadInfo info) {
		// Add for selected row (add nothing if the row is empty)
		if (info != null) {
			pause.setOnAction(e -> manager.pause(info));
			resume.setOnAction(e -> manager.resume(info));
			cancel.setOnAction(e -> manager.cancel(info));
			retry.setOnAction(e -> manager.retry(info));
			restart.setOnAction(e -> manager.restart(info));
			delete.setOnAction(e -> {
				manager.cancel(info);
				view.getItems().remove(info);
			});
		}
		// For all rows

	}

	@Override
	public TableRow<DownloadInfo> call(TableView<DownloadInfo> view) {
		final TableRow<DownloadInfo> row = new TableRow<>();
		DownloadInfo info = view.getSelectionModel().getSelectedItem();
		initListeners(view, info);
		row.contextMenuProperty().bind(
				Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

		row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			final int index = row.getIndex();
			if (!event.isPrimaryButtonDown())
				return; // no action if it is not primary button
			if (index >= view.getItems().size() || view.getSelectionModel().isSelected(index)) {
				view.getSelectionModel().clearSelection();
				event.consume();
			}
		});
		return row;
	}
}
