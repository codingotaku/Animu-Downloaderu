package com.dakusuta.tools.anime.downloader;

import java.lang.reflect.Field;

import org.w3c.dom.Document;

import com.sun.webkit.WebPage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;

public class WebDocumentListener implements ChangeListener<Document> {

	private final WebEngine webEngine;

	public WebDocumentListener(WebEngine webEngine) {
		this.webEngine = webEngine;
	}

	@Override
	public void changed(ObservableValue<? extends Document> arg0, Document arg1, Document arg2) {
		try {
			// Use reflection to retrieve the WebEngine's private 'page' field.
			Field f = webEngine.getClass().getDeclaredField("page");
			f.setAccessible(true);
			WebPage page = (WebPage) f.get(webEngine);
			// Set the background color of the page to be transparent.
			page.setBackgroundColor((new java.awt.Color(42, 42, 42)).getRGB());
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}

	}

}
