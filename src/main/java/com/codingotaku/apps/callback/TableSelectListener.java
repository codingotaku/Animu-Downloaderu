package com.codingotaku.apps.callback;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class TableSelectListener implements Callback<TableView<DownloadInfo>, TableRow<DownloadInfo>> {
	private static Logger logger = Logger.getLogger(TableSelectListener.class.getName());
	private static DownloadManager manager = DownloadManager.getInstance();
	// For selected row
	private final MenuItem pause;
	private final MenuItem resume;
	private final MenuItem cancel;
	private final MenuItem retry;
	private final MenuItem restart;
	private final MenuItem delete;
	private final MenuItem open;

	final ContextMenu rowMenu = new ContextMenu();

	public TableSelectListener() {
		pause = new MenuItem("pause");
		resume = new MenuItem("Resume");
		cancel = new MenuItem("Cancel");
		retry = new MenuItem("Retry");
		restart = new MenuItem("Restart");
		delete = new MenuItem("Delete");
		open = new MenuItem("Open Folder");

		rowMenu.getItems().addAll(pause, resume, cancel, retry, restart, delete, open);
	}

	private void pause(DownloadInfo info) {
		Platform.runLater(() -> manager.pause(info));
	}

	private void resume(DownloadInfo info) {
		Platform.runLater(() -> manager.resume(info));
	}

	private void cancel(DownloadInfo info) {
		Platform.runLater(() -> manager.cancel(info));
	}

	private void retry(DownloadInfo info) {
		Platform.runLater(() -> manager.retry(info));
	}

	private void restart(DownloadInfo info) {
		Platform.runLater(() -> manager.restart(info));
	}

	private void delete(TableView<DownloadInfo> view, DownloadInfo info) {
		Platform.runLater(() -> {
			view.getItems().remove(info);
			manager.cancel(info);
			manager.remove(info);
			view.refresh();
		});
	}

	private void open(DownloadInfo info) {
		if (Desktop.isDesktopSupported()) {
			new Thread(() -> {
				try {
					Desktop desktop = Desktop.getDesktop();
					desktop.open(new File(info.getFileName()).getParentFile());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage());
				}
			}).start();
		}
	}

	private void initListeners(TableView<DownloadInfo> view, DownloadInfo info) {
		// Add for selected row (add nothing if the row is empty)
		if (info != null) {
			pause.setOnAction(e -> pause(info));
			resume.setOnAction(e -> resume(info));
			cancel.setOnAction(e -> cancel(info));
			retry.setOnAction(e -> retry(info));
			restart.setOnAction(e -> restart(info));
			delete.setOnAction(e -> delete(view, info));
			open.setOnAction(e -> open(info));
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
