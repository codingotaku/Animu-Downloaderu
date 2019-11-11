package com.codingotaku.apps.custom;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class Message {
	private ObservableMap<String, String> messages;
	public static final String processed = "Processed ->";

	public Message() {
		Map<String, String> map = new HashMap<>();
		this.messages = FXCollections.observableMap(map);
	}

	public ObservableMap<String, String> getMessages() {
		return messages;
	}

	public void setMessage(String key, String value, boolean done) {
		messages.put(key, done ? processed + value : value);
	}

	public void clearMessages() {
		messages.clear();
	}
}
