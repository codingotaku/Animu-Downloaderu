package com.codingotaku.apps.custom;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableColumn;

/**
 * @author Rahul S<br>
 *         Table column with a percentage with property<br>
 *         This is useful when you have to resize columns with percentage
 */
public class CustomTableColumn<S, T> extends TableColumn<S, T> {
	private final DoubleProperty percWidth = new SimpleDoubleProperty(1);

	public CustomTableColumn() {
		tableViewProperty().addListener((observableValue, tableView1, tableView2) -> {
			if (prefWidthProperty().isBound()) prefWidthProperty().unbind();
			tableView1 = tableView1 == null ? tableView2 : tableView1;
			prefWidthProperty().bind(tableView1.widthProperty().multiply(percWidth));
		});
	}

	public final DoubleProperty percentageWidthProperty() {
		return percWidth;
	}

	public final double getWidthPercentage() {
		return percentageWidthProperty().get();
	}

	public final void setWidthPercentage(double value) throws IllegalArgumentException {
		if (value >= 0 && value <= 1) {
			percentageWidthProperty().set(value);
		} else {
			throw new IllegalArgumentException(String.format("The percentage width should be between 0.0 and 1.0. provided : %1$s", value));
		}
	}
}
