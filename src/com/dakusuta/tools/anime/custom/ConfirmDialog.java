package com.dakusuta.tools.anime.custom;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ConfirmDialog extends Dialog<Boolean> {
	public ConfirmDialog(boolean all, int count) {
		setTitle("Confirm Download");

		ButtonType buttonType = new ButtonType("Yes", ButtonData.YES);
		getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.NO);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 20, 10, 20));
		String question = "Do you want to download ";

		if (all) question += "all ";
		question += count + " episodes?";

		Label label = new Label(question);
		gridPane.getChildren().add(label);
		getDialogPane().setContent(gridPane);
		setResultConverter(dialogButton -> dialogButton == buttonType);
	}
}
