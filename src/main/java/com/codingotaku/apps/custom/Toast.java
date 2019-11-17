package com.codingotaku.apps.custom;

import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * @author alcoolis
 * @see {@link answer https://stackoverflow.com/a/38373408}
 * 
 * @modifiedBy codingotaku
 **/
public final class Toast {
	/**
	 * Delay in seconds for {@link Toast}
	 */
	public enum Delay {
		/** 1 Second delay */
		VERY_SHORT(1),
		/** 3 Seconds delay */
		SHORT(3),
		/** 5 Seconds delay */
		LONG(5),
		/** 10 Seconds delay */
		VERY_LONG(10);

		private final int duration;

		private Delay(int duration) {
			this.duration = duration * 1000;
		}

		int getDuration() {
			return this.duration;
		}
	}

	private static Logger logger = Logger.getLogger(Toast.class.getName());
	private final Stage toastStage;
	private final int toastDelay;
	private final int fadeInDelay;
	private final int fadeOutDelay;

	private Toast(Stage toastStage, int toastDelay, int fadeInDelay, int fadeOutDelay) {
		this.toastStage = toastStage;
		this.toastDelay = toastDelay;
		this.fadeInDelay = fadeInDelay;
		this.fadeOutDelay = fadeOutDelay;
	}

	public static Toast makeToast(Window window, String toastMsg, Delay toastDelay, Delay fadeInDelay,
			Delay fadeOutDelay) {
		return makeToast(window, toastMsg, toastDelay.getDuration(), fadeInDelay.getDuration(),
				fadeOutDelay.getDuration());
	}

	public static Toast makeToast(Window window, String toastMsg, int toastDelay, int fadeInDelay, int fadeOutDelay) {
		Stage toastStage = new Stage();
		toastStage.initOwner(window);
		toastStage.setResizable(false);
		toastStage.initStyle(StageStyle.TRANSPARENT);
		Text text = new Text(toastMsg);
		text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		text.setFill(Color.RED);

		StackPane root = new StackPane(text);
		root.setStyle("-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20px;");
		root.setOpacity(0);

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		toastStage.setScene(scene);
		return new Toast(toastStage, toastDelay, fadeInDelay, fadeOutDelay);
	}

	public void show() {
		toastStage.show();

		Timeline fadeInTimeline = new Timeline();
		KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay),
				new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
		fadeInTimeline.getKeyFrames().add(fadeInKey1);
		fadeInTimeline.setOnFinished(ae -> new Thread(() -> {
			try {
				Thread.sleep(toastDelay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.severe(e.getMessage());
			}
			Timeline fadeOutTimeline = new Timeline();
			KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay),
					new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
			fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
			fadeOutTimeline.setOnFinished(aeb -> toastStage.close());
			fadeOutTimeline.play();
		}).start());
		fadeInTimeline.play();
	}
}