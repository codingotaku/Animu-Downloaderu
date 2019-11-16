package com.codingotaku.apps;

import java.io.File;

import com.codingotaku.apps.util.Backup;
import com.codingotaku.apps.util.Constants;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class SettingsController {
	@FXML private GridPane root;
	@FXML private Spinner<Integer> threadCountSpinner;
	@FXML private CheckBox switchTabs;
	@FXML private TextField folderPath;
	private String path;

	@FXML private void chooseFolder() {
		Window window = root.getScene().getWindow();
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Download folder");
		File defaultDirectory = new File(Constants.getDownloadFolder());
		if (!defaultDirectory.exists()) {// If the path doesn't exist.
			Constants.setDownloadFolder(System.getProperty("user.home") + File.separator + "Downloads");
			defaultDirectory = new File(Constants.getDownloadFolder());
		}

		chooser.setInitialDirectory(defaultDirectory);
		File selectedDir = chooser.showDialog(window);
		if (selectedDir != null && selectedDir.exists()) {
			this.path = selectedDir.getAbsolutePath();
			folderPath.setText(this.path);
		}
	}

	@FXML private void initialize() {
		Backup.loadDownloadFolder();
		this.path = Constants.getDownloadFolder();
		folderPath.setText(path);
	}

	@FXML private void save() {
		Constants.setDownloadFolder(this.path);
		Backup.saveDownloadFolder();
	}

	@FXML private void reset() {
		this.path = Constants.getDownloadFolder();
		folderPath.setText(this.path);
	}
}
