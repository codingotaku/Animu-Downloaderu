package com.dakusuta.tools.anime.custom;

import org.jsoup.nodes.Element;

import javafx.scene.control.Label;

public class CustomLabel extends Label {
	private String value = "";

	private CustomLabel(String val, String text) {
		super(text);
		value = val;
	}

	public CustomLabel(Element element) {
		super(element.text());
		value = element.attr("href");
	}

	public boolean hasValue(String value) {
		return getText().toLowerCase().contains(value.toLowerCase());
	}

	public String getValue() {
		return value;
	}

	public CustomLabel copy() {
		return new CustomLabel(value, getText());
	}

	public String toString() {
		return getText();
	}
}
