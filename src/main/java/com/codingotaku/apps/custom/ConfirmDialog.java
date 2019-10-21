package com.codingotaku.apps.custom;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

public class ConfirmDialog extends Dialog<Boolean> {
	public ConfirmDialog(String title, String message) {
		var dialogPane = getDialogPane();

		setTitle(title);
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		var label = new Label(message);
		dialogPane.setContent(label);
		setResultConverter(buttonType -> buttonType == ButtonType.YES);
	}
}
