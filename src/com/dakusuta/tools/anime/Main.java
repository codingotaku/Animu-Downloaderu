package com.dakusuta.tools.anime;

import java.io.IOException;

import com.dakusuta.tools.anime.util.Backup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	private static final double WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.75;
	private static final double HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.75;

	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, WIDTH, HEIGHT);
			stage.setMinWidth(WIDTH);
			stage.setMinHeight(HEIGHT);
			stage.initStyle(StageStyle.UNDECORATED);
			Image icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));
			stage.getIcons().add(icon);
			stage.centerOnScreen();
			scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
			stage.setTitle("Animu Downloaderu");
			stage.setScene(scene);
			stage.show();
			MainFXMLController controller = (MainFXMLController) loader.getController();
			controller.loadAnime(stage);
			Backup.loadDownloadFolder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
