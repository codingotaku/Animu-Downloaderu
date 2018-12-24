/**
 * @author otaku
 *
 */
module Animu_Downloaderu {
	requires java.desktop;
	requires java.prefs;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.web;
	requires org.jsoup;
	opens com.codingotaku.apps to javafx.fxml;
	opens com.codingotaku.apps.custom to javafx.fxml;

	exports com.codingotaku.apps;
}