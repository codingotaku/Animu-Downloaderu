package com.codingotaku.apps.custom;

import com.codingotaku.apps.callback.NotificationListener;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class NotificationController {
	@FXML
	private Label messageType;
	@FXML
	private Label message;
	
	public enum Type {INFO, ERROR, WARNING}
	NotificationListener listener;
	
	@FXML
	private void close() {
		listener.closed(this);
	}

	public void init(String message, Type type, NotificationListener listener) {
		this.listener = listener;
		this.messageType.setText(type.name());
		this.message.setText(message);
	}
}