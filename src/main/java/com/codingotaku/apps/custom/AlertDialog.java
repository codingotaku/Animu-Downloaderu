package com.codingotaku.apps.custom;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class AlertDialog extends Dialog<Void> {
	public AlertDialog(String title, String message) {
		var dialogPane = getDialogPane();
		setTitle(title);
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		var label = new Label(message);

		dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

		GridPane.setConstraints(label, 1, 2);
		dialogPane.setContent(label);
	}

}
