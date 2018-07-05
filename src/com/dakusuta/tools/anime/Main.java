package com.dakusuta.tools.anime;

import java.io.IOException;

import com.dakusuta.tools.anime.download.DownloadManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private static final double WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.75;
	private static final double HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.75;

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, WIDTH, HEIGHT);
			primaryStage.setMinWidth(WIDTH);
			primaryStage.setMinHeight(HEIGHT);
			scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
			primaryStage.setTitle("Animu Downloaderu");
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e -> {
				DownloadManager.getInstance().pauseAll();
				System.exit(0);// I shouldn't do this but for now I'll force close the app
			});
			primaryStage.show();
			MainFXMLController controller = (MainFXMLController) loader.getController();
			controller.loadAnime(primaryStage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
