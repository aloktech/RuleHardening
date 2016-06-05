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

import com.invicara.tools.rh.model.TestcaseNode;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 *
 * @author Alok Ranjan Meher
 */
public class TreeTestcaseNode extends CheckBoxTreeCell<TestcaseNode> {

    TreeView<TestcaseNode> treeView;

    public TreeTestcaseNode(TreeView<TestcaseNode> treeView) {
        this.treeView = treeView;

        if (treeView.getSelectionModel().getSelectedItem() != null) {
            treeView.getSelectionModel().getSelectedItem().valueProperty().addListener((ObservableValue<? extends TestcaseNode> observable, TestcaseNode oldValue, TestcaseNode newValue) -> {
                Platform.runLater(() -> {
                    Event.fireEvent(treeView.getSelectionModel().getSelectedItem(), 
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), 
                                    treeView.getSelectionModel().getSelectedItem(), newValue));
                });
            });
        }
    }

    @Override
    public void updateItem(TestcaseNode node, boolean empty) {
        super.updateItem(node, empty);

        if (empty) {
            setGraphic(null);
        } else {
            if (treeView.getSelectionModel().getSelectedItem() != null) {
                treeView.getSelectionModel().getSelectedItem().valueProperty().addListener((ObservableValue<? extends TestcaseNode> observable, TestcaseNode oldValue, TestcaseNode newValue) -> {
                    System.out.println(oldValue);
                    System.out.println(newValue);
                    Platform.runLater(() -> {
                        Event.fireEvent(treeView.getSelectionModel().getSelectedItem(), new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeView.getSelectionModel().getSelectedItem(), newValue));
                    });
                });
            }
        }
    }
}
