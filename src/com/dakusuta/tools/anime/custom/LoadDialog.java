package com.dakusuta.tools.anime.custom;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class LoadDialog {
	private static Alert alert;

	public static void showDialog(Window owner, String title, String message) {
		alert = new Alert(AlertType.NONE);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.initStyle(StageStyle.UNDECORATED);
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
			Platform.runLater(() -> {
				alert.setContentText(message);
			});
		}
	}
}
