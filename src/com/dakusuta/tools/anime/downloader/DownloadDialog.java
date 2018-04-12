package com.dakusuta.tools.anime.downloader;

import java.util.List;

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
	DownloadDialog(List<CustomLabel> episodes) {
		setTitle("Select episodes to download");

		ButtonType buttonType = new ButtonType("OK", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));
		List<CustomLabel> copy = Utils.copyList(episodes);
		ComboBox<CustomLabel> from = new ComboBox<>();
		from.getItems().addAll(episodes);
		ComboBox<CustomLabel> to = new ComboBox<>();
		to.getItems().addAll(copy);

		from.setPromptText("From");
		to.setPromptText("To");

		gridPane.add(new Label("From:"), 0, 0);
		gridPane.add(from, 1, 0);
		gridPane.add(new Label("To:"), 2, 0);
		gridPane.add(to, 3, 0);
		getDialogPane().getStylesheets().add(Main.class.getResource("combo.css").toExternalForm());

		getDialogPane().setContent(gridPane);

		Platform.runLater(() -> from.requestFocus());

		setResultConverter(dialogButton -> {
			if (dialogButton == buttonType) { return new Pair<>(from.getSelectionModel().getSelectedIndex(), to.getSelectionModel().getSelectedIndex()); }
			return null;
		});
	}
}
