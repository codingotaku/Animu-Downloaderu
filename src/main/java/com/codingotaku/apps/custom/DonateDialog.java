package com.codingotaku.apps.custom;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class DonateDialog extends Dialog<Boolean> {
	private static Logger logger = Logger.getLogger(DonateDialog.class.getName());

	public DonateDialog() {
		var buttonType = new ButtonType("Yes", ButtonData.YES);
		var dialogPane = getDialogPane();

		setTitle("Donate :3");
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		var gridPane = new GridPane();

		gridPane.setVgap(10);
		String question = "Thank you for clicking the donate button!! <3\n\n"
				+ "You can donate via liberapay, paypal, or help me by subscribing to my patreon!\n\n";

		Label librapay = new Label("Liberapay");
		Label paypal = new Label("Paypal");
		Label patrion = new Label("Patreon");
		HBox donateButtons = new HBox(15);
		donateButtons.setAlignment(Pos.CENTER);
		librapay.setCursor(Cursor.HAND);
		paypal.setCursor(Cursor.HAND);
		patrion.setCursor(Cursor.HAND);
		librapay.setId("link");
		paypal.setId("link");
		patrion.setId("link");

		librapay.setOnMouseClicked(e -> handleClick("https://liberapay.com/codingotaku/donate"));
		paypal.setOnMouseClicked(e -> handleClick("https://paypal.me/codingotaku"));
		patrion.setOnMouseClicked(e -> handleClick("https://www.patreon.com/bePatron?u=13678963"));
		donateButtons.getChildren().addAll(librapay, paypal, patrion);

		dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

		var label = new Label(question);
		GridPane.setConstraints(label, 1, 2);
		GridPane.setConstraints(donateButtons, 1, 3, 2, 1);
		gridPane.getChildren().addAll(label, donateButtons);
		dialogPane.setContent(gridPane);
		setResultConverter(dialogButton -> dialogButton == buttonType);
	}

	private void handleClick(String url) {
		try {
			new ProcessBuilder("x-www-browser", url).start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

}
