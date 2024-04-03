package com.techwhizer.medicalshop;

import com.techwhizer.medicalshop.method.Method;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

class DateTimePickerPopup extends VBox implements Initializable {

	private final DateTimePicker parentControl;

	@FXML
	private Accordion accordion;

	@FXML
    private ComboBox<String> comHour,comMinute,comAmPm;

	@FXML
	private DatePicker datePicker;


	public DateTimePickerPopup(final DateTimePicker parentControl2) {

		this.parentControl = parentControl2;

		// Load FXML
		final FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(
						"DateTimePickerPopup.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void setMinute(){

		ObservableList<String> list = FXCollections.observableArrayList();

		list.add("00");

		for (int i = 1; i <= 59; i++) {

			String val = "00";

			if (i < 10){
				val = "0"+i;
			}else {
				val = String.valueOf(i);
			}
			list.add(val);
		}
		comMinute.setItems(list);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setMinute();
		new Method().convertDateFormat_2(datePicker);

		if (parentControl != null && parentControl.dateTimeProperty().get() != null) {
			datePicker.valueProperty().set(
					parentControl.dateTimeProperty().get().toLocalDate());

			LocalTime lt = parentControl.dateTimeProperty().get().toLocalTime();

			setDefaultTime(lt);
		} else {
			datePicker.setValue(LocalDate.now());
			setDefaultTime(LocalTime.now());
		}

	}

	private void setDefaultTime(LocalTime localTime) {

		if(localTime != null){

			try {
				String[] time = localTime.format(DateTimeFormatter.ofPattern("hh:mm a")).split(" ");
				String[] hourMin = time[0].split(":");

				comHour.getSelectionModel().select(Objects.equals(hourMin[0], "12") ?"00":hourMin[0].toUpperCase());
				comMinute.getSelectionModel().select(hourMin[1].toUpperCase());

				String amPm = time[1];

				if(null == amPm || amPm.isEmpty()){
					comAmPm.getSelectionModel().selectFirst();
				}else {
					comAmPm.getSelectionModel().select(amPm.toUpperCase());
				}
			}catch (Exception e){}

		}


	}

	void setDate(final LocalDate date) {
		datePicker.setValue(date);
	}

	LocalDate getDate() {
		return datePicker.getValue();
	}

	void setTime(final LocalTime time) {

		setDefaultTime(time);
	}


	LocalTime getTime(){
		String hour =comHour.getSelectionModel().isEmpty()?"0":comHour.getSelectionModel().getSelectedItem();
		String minute =comMinute.getSelectionModel().isEmpty()?"0":comMinute.getSelectionModel().getSelectedItem();
		String amPm = comAmPm.getSelectionModel().isEmpty()?"0":comAmPm.getSelectionModel().getSelectedItem();
	    return LocalTime.of((Integer.parseInt(hour)+(Objects.equals(amPm, "PM") ?12:0)),Integer.parseInt(minute));
	}



	@FXML
	void handleOkButtonAction() {

		if (comHour.getSelectionModel().isEmpty()){
			new Method().show_popup("Please Select Hour",comHour, Side.BOTTOM);
			return;
		}else if (comHour.getSelectionModel().isEmpty()){
			new Method().show_popup("Please Select Minute",comHour, Side.BOTTOM);
			return;
		}else if (comHour.getSelectionModel().isEmpty()){
			new Method().show_popup("Please Select AM/PM",comHour, Side.BOTTOM);;
			return;
		}else{
			parentControl.hidePopup();
		}


	}
}
