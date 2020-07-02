package com.codingotaku.apps;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codingotaku.apps.custom.ConfirmDialog;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.util.Backup;
import com.codingotaku.apps.util.Constants;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AnimuDownloaderu extends Application {
	private static Logger logger = Logger.getLogger(AnimuDownloaderu.class.getName());
	private static final double WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.80;
	private static final double HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.75;

	// It was a workaround, may be the issue is fixed but I'm too lazy to check
	// Called from Main.java file
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		try {
			var loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
			Parent root = loader.load();
			var scene = new Scene(root, WIDTH, HEIGHT);
			var icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));

			stage.setMinWidth(WIDTH);
			stage.setMinHeight(HEIGHT);

			stage.getIcons().add(icon);
			stage.centerOnScreen();
			stage.setTitle("Animu Downloaderu");
			stage.setScene(scene);
			stage.show();

			Backup.loadDownloadFolder();
			Backup.loadThreadCount();

			stage.setOnCloseRequest(event -> {
				event.consume();
				var dialog = new ConfirmDialog("Exit?", Constants.EXIT_QUESTION);

				// Calculate the center position of the parent Stage
				double centerXPosition = stage.getX() + stage.getWidth() / 2d;
				double centerYPosition = stage.getY() + stage.getHeight() / 2d;

				dialog.setOnShowing(e -> {
					dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
					dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
				});

				var res = dialog.showAndWait();
				if (res.isPresent() && Boolean.TRUE.equals(res.get())) {
					DownloadManager.getInstance().pauseAll();
					stage.close();
					System.exit(0);// I shouldn't do this but for now I'll force close the app.
				}
			});

		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}
}
