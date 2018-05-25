package com.dakusuta.tools.anime.custom;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ProgressIndicatorTableCell<T> extends TableCell<T, Double> {
	public static <T> Callback<TableColumn<T, Double>, TableCell<T, Double>> forTableColumn() {
		return new Callback<TableColumn<T, Double>, TableCell<T, Double>>() {
			@Override
			public TableCell<T, Double> call(TableColumn<T, Double> param) {
				return new ProgressIndicatorTableCell<T>();
			}
		};
	}

	private final ProgressIndicator progressIndicator;
	private ObservableValue<Double> observable;

	public ProgressIndicatorTableCell() {
		this.getStyleClass().add("progress-indicator-table-cell");

		this.progressIndicator = new ProgressIndicator();
		
		setGraphic(progressIndicator);
	}

	@Override
	public void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setGraphic(null);
		} else {
			progressIndicator.progressProperty().unbind();

			observable = getTableColumn().getCellObservableValue(getIndex());
			if (observable != null) {
				progressIndicator.progressProperty().bind(observable);
			} else {
				progressIndicator.setProgress(item);
			}

			setGraphic(progressIndicator);
		}
	}
}