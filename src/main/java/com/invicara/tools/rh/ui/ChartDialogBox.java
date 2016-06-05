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

import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.model.TestcaseNode;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/**
 *
 * @author Alok Ranjan Meher
 */
public class ChartDialogBox extends Stage {

    public ChartDialogBox() {

    }

    public void createGUI() {

        VBox verticalPanel = new VBox();
        verticalPanel.setPadding(new Insets(5, 10, 5, 5));

        ObservableList<TestcaseNode> checkBoxs = FXCollections.observableArrayList();
        Model.INSTANCE.getListOfModel().stream().forEach((node) -> {
            checkBoxs.add(node.getNode());
        });

        ComboBox<TestcaseNode> cmb = new ComboBox<>(checkBoxs);
        cmb.setPrefWidth(350);
        cmb.setCellFactory(new Callback<ListView<TestcaseNode>, ListCell<TestcaseNode>>() {
            @Override
            public ListCell<TestcaseNode> call(ListView<TestcaseNode> p) {
                return new ListCell<TestcaseNode>() {
                    private CheckBox checkBox;
                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        checkBox = new CheckBox();
                    }

                    @Override
                    protected void updateItem(TestcaseNode item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            checkBox.setText(item.getModelName());
                            checkBox.setUserData(item);
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });
        verticalPanel.getChildren().add(cmb);

        HBox buttonPanel = new HBox();
        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(60);
        okBtn.setOnAction((ActionEvent event) -> {
            Iterator<BIMModel> itr = Model.INSTANCE.getListOfModel().iterator();
            while (itr.hasNext()) {
//                    cmb.getSelectionModel().getSelectedItem()
            }
            close();
        });
        buttonPanel.getChildren().add(okBtn);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(60);
        cancelBtn.setOnAction((ActionEvent event) -> {
            close();
        });
        buttonPanel.getChildren().add(cancelBtn);
        
        verticalPanel.getChildren().add(buttonPanel);

        setHeight(200);
        setWidth(400);

        setTitle("Chart Configuration DialogBox");
        initModality(Modality.WINDOW_MODAL);
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
