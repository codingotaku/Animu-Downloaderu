package com.codingotaku.apps.custom;

import javafx.application.Platform;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Window;

public class LoadDialog {
	private LoadDialog() {

	}

	private static Alert alert;

	public static void showDialog(Window owner, String title, Message message) {
		alert = new Alert(AlertType.NONE);
		alert.initOwner(owner);
		alert.setTitle(title);
		ObservableMap<String, String> messages = message.getMessages();
		StringBuilder msg = new StringBuilder();
		messages.forEach((key, value) -> msg.append(String.format("%s\t: %s%n", key, value)));
		alert.getDialogPane().setMinHeight(messages.size() * 30d);
		alert.setContentText(msg.toString());

		// Run with small delay on each change
		messages.addListener((Change<? extends String, ? extends String> change) -> Platform.runLater(() -> {
			StringBuilder msgChange = new StringBuilder();
			messages.forEach((key, value) -> msgChange.append(String.format("%s\t: %s%n", key, value)));
			alert.setContentText(msgChange.toString());
			if (messages.values().stream().allMatch(val -> val.startsWith(Message.processed))) {
				stopDialog();
				message.clearMessages();
			}
		}));

		alert.initModality(Modality.APPLICATION_MODAL);
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
				dialogPane.getButtonTypes().clear();
				dialogPane.getButtonTypes().add(ButtonType.CANCEL);
				alert.close();
			});
		}
	}
}
