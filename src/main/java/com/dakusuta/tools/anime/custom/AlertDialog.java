package com.dakusuta.tools.anime.custom;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

public class AlertDialog extends Dialog<Void> {
	public AlertDialog(String title, String message) {
		DialogPane dialogPane = getDialogPane();
		initStyle(StageStyle.UNDECORATED);
		dialogPane.getStylesheets().add(getClass().getResource("css/combo.css").toExternalForm());

		HBox titleBar = new HBox();
		Label header = new Label(title);
		header.setId("head");
		Label close = new Label("X");
		close.setId("head");
		Pane space = new Pane();
		HBox.setHgrow(space, Priority.ALWAYS);
		close.setAlignment(Pos.TOP_RIGHT);
		close.setOnMouseEntered(e -> close.setId("none"));
		close.setOnMouseExited(e -> close.setId("head"));
		close.setOnMouseClicked(e -> close());
		titleBar.getChildren().addAll(header, space, close);
		GridPane gridPane = new GridPane();
		gridPane.setVgap(10);
		dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

		Label label = new Label(message);
		GridPane.setConstraints(titleBar, 0, 0, 2, 1);
		GridPane.setConstraints(label, 1, 2);
		gridPane.getChildren().addAll(titleBar, label);
		dialogPane.setContent(gridPane);
	}

}
