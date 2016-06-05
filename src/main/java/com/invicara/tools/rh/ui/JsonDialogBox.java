/**
 * ***********************************************************************
 * INVICARA INC CONFIDENTIAL
 * ***********************************************************************
 *
 * Copyright (C) [2012] - [2014] INVICARA INC, INVICARA Pte Ltd, INVICARA INDIA
 * PVT LTD All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Invicara Inc and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to Invicara Inc and its suppliers
 * and may be covered by U.S. and Foreign Patents, patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Invicara Inc.
 */
package com.invicara.tools.rh.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Alok Ranjan Meher
 */
public class JsonDialogBox extends Stage {

    InputDialogBox instance;
    String data;
    boolean validate;

    public JsonDialogBox(boolean validate) {
        this.validate = validate;
    }

    public JsonDialogBox(boolean validate, InputDialogBox instance, String data) {
        this.instance = instance;
        this.validate = validate;
        this.data = data;
        
        
        createGUI();
    }

    private void createGUI() {
        final Label label = new Label();
        label.setTextFill(Color.RED);
        GridPane grid = new GridPane();
        final TextArea jsonTextArea = new TextArea(data == null ? "" : data);
        jsonTextArea.setPrefHeight(200);
        jsonTextArea.setPrefWidth(350);
        grid.addRow(0, jsonTextArea);
        grid.setHgap(10);
        grid.setVgap(5);

        // create action buttons for the dialog.
        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);

        // add action handlers for the dialog buttons.
        ok.setOnAction((ActionEvent event) -> {
            try {
                instance.updateUI(jsonTextArea.getText());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            closeDialog();
        });
        cancel.setOnAction((ActionEvent event) -> {
            closeDialog();
        });

        // layout the dialog.
        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(10);
        buttonPanel.setPadding(new Insets(20, 5, 0, 5));
        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);

        VBox verticalPanel = new VBox();
        verticalPanel.setSpacing(5.0);
        verticalPanel.setPadding(new Insets(5, 5, 5, 5));
        verticalPanel.getChildren().addAll(grid, label, buttonPanel);

        setWidth(350);
        setHeight(250);
        setTitle("Json Data");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setScene(new Scene(verticalPanel));
    }

    public void closeDialog() {
        close();
    }

    public void showDialog() {
        show();
    }

}
