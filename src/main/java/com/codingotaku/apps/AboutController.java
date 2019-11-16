package com.codingotaku.apps;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codingotaku.apps.custom.DonateDialog;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AboutController {
	@FXML private GridPane root;
	@FXML private Hyperlink repoLink;
	private static Logger logger = Logger.getLogger(AboutController.class.getName());

	@FXML private void openRepo() {
		try {
			new ProcessBuilder("x-www-browser", repoLink.getText()).start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	@FXML private void donate() {
		var dialog = new DonateDialog();
		Stage stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
		// Calculate the center position of the parent Stage
		double centerXPosition = stage.getX() + stage.getWidth() / 2d;
		double centerYPosition = stage.getY() + stage.getHeight() / 2d;

		dialog.setOnShowing(e -> {
			dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
			dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
		});

		dialog.showAndWait();
	}
}
