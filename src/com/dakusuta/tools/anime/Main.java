package com.dakusuta.tools.anime;

import java.io.IOException;

import com.dakusuta.tools.anime.custom.LoadDialog;
import com.dakusuta.tools.anime.download.DownloadManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 700;

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root, WIDTH, HEIGHT);
			primaryStage.setMinWidth(WIDTH);
			primaryStage.setMinHeight(HEIGHT);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Animu Downloaderu");
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e->DownloadManager.getInstance().pauseAll());
			primaryStage.show();
			LoadDialog.showDialog(primaryStage, "Please wait", "Loading anime..");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
