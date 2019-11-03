package com.codingotaku.apps;

import java.io.File;

import com.codingotaku.apps.callback.TableObserver;
import com.codingotaku.apps.callback.TableSelectListener;
import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.download.Status;
import com.codingotaku.apps.util.Backup;
import com.codingotaku.apps.util.Constants;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class DownloadController implements TableObserver {
	private Window window;

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

	private static DownloadManager manager;

	@FXML
	private void initialize() {
		manager = DownloadManager.getInstance();
		tableView.setRowFactory(new TableSelectListener());
		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		clearAll.setOnAction(e -> tableView.getItems()
				.removeIf(d -> d.getStatus() == Status.FINISHED || d.getStatus() == Status.CANCELLED));
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
		tableView.refresh();
	}

	@FXML
	private void chooseFolder() {
		if (window == null)
			window = root.getScene().getWindow();

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Download folder");
		File defaultDirectory;

		defaultDirectory = new File(Constants.downloadFolder);
		if (!defaultDirectory.exists()) {// If the path was external HD or it doesn't exist.
			Constants.downloadFolder = System.getProperty("user.home") + File.separator + "Downloads";
			defaultDirectory = new File(Constants.downloadFolder);
		}

		chooser.setInitialDirectory(defaultDirectory);
		File selectedDir = chooser.showDialog(window);
		if (selectedDir != null && selectedDir.exists()) {
			Constants.downloadFolder = selectedDir.getAbsolutePath();
			Backup.saveDownloadFolder();
		}
	}
}
