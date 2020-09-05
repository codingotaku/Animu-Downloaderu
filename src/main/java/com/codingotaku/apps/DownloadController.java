package com.codingotaku.apps;

import java.io.File;

import com.codingotaku.apps.callback.NotificationListener;
import com.codingotaku.apps.callback.TableObserver;
import com.codingotaku.apps.callback.TableSelectListener;
import com.codingotaku.apps.custom.NotificationController;
import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.download.Status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	private TableColumn<DownloadInfo, String> statusString;

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
	private ObservableList<DownloadInfo> downloadList = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		DownloadManager manager = DownloadManager.getInstance();
		tableView.setRowFactory(new TableSelectListener());
		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		statusString.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("statusString"));
		tableView.setItems(downloadList);

		clearAll.setOnAction(e -> {
			downloadList.removeIf(d -> {
				boolean remove = d.getStatus() == Status.FINISHED || d.getStatus() == Status.CANCELLED;
				if (remove) {
					manager.remove(d);
				}
				return remove;
			});
		});
		pauseAll.setOnAction(e -> manager.pauseAll());
		resumeAll.setOnAction(e -> manager.resumeAll());
		cancelAll.setOnAction(e -> manager.cancelAll());
		retryAll.setOnAction(e -> manager.retryAll());
		restartAll.setOnAction(e -> manager.restartAll());
	}

	@Override
	public void added(DownloadInfo download) {
		downloadList.add(download);
	}

	@Override
	public void updated(DownloadInfo download) {
		String file = new File(download.getFileName()).getName();
		switch (download.getStatus()) {
		case ERROR:
			notificationListener.created(NotificationController.Type.ERROR,
					String.format("Failed Downloading %s", file));
			break;
		case CANCELLED:
			notificationListener.created(NotificationController.Type.WARNING,
					String.format("Cancelled Downloading %s", file));
			break;
		case FINISHED:
			notificationListener.created(NotificationController.Type.INFO,
					String.format("Completed Downloading %s", file));
			break;
		default:
			break;
		}
		tableView.refresh();
	}

	public void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}
}
