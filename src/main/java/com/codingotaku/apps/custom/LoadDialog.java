package com.codingotaku.apps.custom;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class LoadDialog {
	private LoadDialog() {

	}

	private static Alert alert;

	public static void showDialog(Window owner, String title, String message) {
		alert = new Alert(AlertType.NONE);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.initStyle(StageStyle.UNDECORATED);
		alert.getDialogPane().getStylesheets()
				.add(LoadDialog.class.getResource("/css/application.css").toExternalForm());
		// Calculate the center position of the parent Stage
		double centerXPosition = owner.getX() + owner.getWidth() / 2d;
		double centerYPosition = owner.getY() + owner.getHeight() / 2d;

		alert.setOnShowing(e -> {
			alert.setX(centerXPosition - alert.getDialogPane().getWidth() / 2d);
			alert.setY(centerYPosition - alert.getDialogPane().getHeight() / 2d);
		});
		alert.show();
	}

	public static void stopDialog() {
		if (alert != null && alert.isShowing()) {
			Platform.runLater(() -> {
				DialogPane dialogPane = alert.getDialogPane();
				dialogPane.getButtonTypes().add(ButtonType.CANCEL);
				alert.close();
			});
		}
	}

	public static void setMessage(String message) {
		if (alert != null && alert.isShowing()) {
			Platform.runLater(() -> alert.setContentText(message));
		}
	}
}
