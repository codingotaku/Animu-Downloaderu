package com.codingotaku.apps;

import java.io.File;

import com.codingotaku.apps.callback.NotificationListener;
import com.codingotaku.apps.callback.TableObserver;
import com.codingotaku.apps.callback.TableSelectListener;
import com.codingotaku.apps.custom.NotificationController;
import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.download.Status;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class DownloadController implements TableObserver {

	@FXML
	private VBox root;
	@FXML
	private TableView<DownloadInfo> tableView;
	@FXML
	private TableColumn<DownloadInfo, String> fileName;
	@FXML
	private TableColumn<DownloadInfo, Double> size;
	@FXML
	private TableColumn<DownloadInfo, Double> downloaded;
	@FXML
	private TableColumn<DownloadInfo, String> progress;
	@FXML
	private TableColumn<DownloadInfo, Status> status;

	@FXML
	private Button clearAll;
	@FXML
	private Button pauseAll;
	@FXML
	private Button resumeAll;
	@FXML
	private Button cancelAll;
	@FXML
	private Button retryAll;
	@FXML
	private Button restartAll;
	private NotificationListener notificationListener;

	@FXML
	private void initialize() {
		DownloadManager manager = DownloadManager.getInstance();
		tableView.setRowFactory(new TableSelectListener());
		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		clearAll.setOnAction(e -> {
			tableView.getItems().removeIf(d -> {
				boolean remove = d.getStatus() == Status.FINISHED || d.getStatus() == Status.CANCELLED;
				if (remove) {
					manager.remove(d);
				}
				return remove;
			});
			tableView.refresh();
		});
		pauseAll.setOnAction(e -> manager.pauseAll());
		resumeAll.setOnAction(e -> manager.resumeAll());
		cancelAll.setOnAction(e -> manager.cancelAll());
		retryAll.setOnAction(e -> manager.retryAll());
		restartAll.setOnAction(e -> manager.restartAll());
	}

	@Override
	public void added(DownloadInfo download) {
		tableView.getItems().add(download);
	}

	@Override
	public void updated(DownloadInfo download) {
		switch (download.getStatus()) {
		case ERROR:
			String file = new File(download.getFileName()).getName();
			notificationListener.created(NotificationController.Type.ERROR,
					String.format("Downloading %s failed!", file));
			break;
		case CANCELLED:
			notificationListener.created(NotificationController.Type.ERROR,
					String.format("Downloading %s Cancelled!", download.getFileName()));
			break;
		case FINISHED:
			notificationListener.created(NotificationController.Type.ERROR,
					String.format("Downloading %s Completed!", download.getFileName()));
			break;
		default:
			break;
		}
//		tableView.refresh();
	}

	public void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}
}
