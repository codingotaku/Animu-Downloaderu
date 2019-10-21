package com.codingotaku.apps.custom;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

public class DownloadDialog extends Dialog<Boolean> {
	public DownloadDialog(int count) {
		var buttonType = new ButtonType("Yes", ButtonData.YES);
		var dialogPane = getDialogPane();
		setTitle("Confirm Download");
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		String question;
		if (count == 0) {
			question = "Please select at least one video";
			dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
		} else {
			question = String.format("Do you want to download %d episode(s)?", count);
			dialogPane.getButtonTypes().addAll(buttonType, ButtonType.NO);
		}

		var label = new Label(question);
		dialogPane.setContent(label);
		setResultConverter(dialogButton -> dialogButton == buttonType);
	}

}
