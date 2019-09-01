package com.codingotaku.apps;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

public class ConfirmDialog extends Dialog<Boolean> {
	public ConfirmDialog(String title, String message) {
		var dialogPane = getDialogPane();

		initStyle(StageStyle.UNDECORATED);
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		var titleBar = new HBox();
		var header = new Label(title);
		var space = new Pane();
		var close = new Label("X");

		header.setId("head");
		close.setId("head");

		HBox.setHgrow(space, Priority.ALWAYS);
		close.setAlignment(Pos.TOP_RIGHT);
		close.setOnMouseEntered(e -> close.setId("none"));
		close.setOnMouseExited(e -> close.setId("head"));
		close.setOnMouseClicked(e -> close());
		titleBar.getChildren().addAll(header, space, close);

		var gridPane = new GridPane();
		gridPane.setVgap(10);
		dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		var label = new Label(message);
		GridPane.setConstraints(titleBar, 0, 0, 2, 1);
		GridPane.setConstraints(label, 1, 2);
		gridPane.getChildren().addAll(titleBar, label);
		dialogPane.setContent(gridPane);
		setResultConverter(buttonType -> buttonType == ButtonType.YES);
	}
}
