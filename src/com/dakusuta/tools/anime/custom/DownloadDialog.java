package com.dakusuta.tools.anime.custom;

import java.util.List;

import com.dakusuta.tools.anime.Main;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class DownloadDialog extends Dialog<Pair<Integer, Integer>> {
	public DownloadDialog(List<CustomLabel> episodes) {
		setTitle("Select episodes to download");

		ButtonType buttonType = new ButtonType("OK", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 20, 10, 20));

		ComboBox<String> from = new ComboBox<>();
		ComboBox<String> to = new ComboBox<>();
		episodes.forEach(label->{
			from.getItems().add(label.getText());	
			to.getItems().add(label.getText());
		});

		from.setPromptText("Select episode");
		to.setPromptText("Select episode");

		gridPane.add(new Label("From:"), 0, 0);
		gridPane.add(from, 1, 0);
		gridPane.add(new Label("To:"), 2, 0);
		gridPane.add(to, 3, 0);
		getDialogPane().getStylesheets().add(Main.class.getResource("/css/combo.css").toExternalForm());

		getDialogPane().setContent(gridPane);

		Platform.runLater(() -> from.requestFocus());

		setResultConverter(dialogButton -> {
			if (dialogButton == buttonType) { return new Pair<>(from.getSelectionModel().getSelectedIndex(), to.getSelectionModel().getSelectedIndex()); }
			return null;
		});
	}
}
