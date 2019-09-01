package com.codingotaku.apps;

import java.io.IOException;

import com.codingotaku.apps.util.Backup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AnimuDownloaderu extends Application {
	private static final double WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.75;
	private static final double HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.75;

	//It is a workaround, Down't ask me whats happening here
	//Called from Main.java file
    public static void main(String[] args) {
    	launch(args);
    }

    @Override
	public void start(Stage stage) {
		try {
			var loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
			Parent root = loader.load();
			var scene = new Scene(root, WIDTH, HEIGHT);
			var icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));

			stage.setMinWidth(WIDTH);
			stage.setMinHeight(HEIGHT);
			stage.initStyle(StageStyle.UNDECORATED);
			
			stage.getIcons().add(icon);
			stage.centerOnScreen();
			scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
			stage.setTitle("Animu Downloaderu");
			stage.setScene(scene);
			stage.show();

			var controller = (MainFXMLController) loader.getController();
			controller.loadAnime(stage);
			Backup.loadDownloadFolder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
