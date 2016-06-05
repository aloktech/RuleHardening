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
package com.invicara.tools.rh.ui.controller;

import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.UIUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

/**
 *
 * @author Alok Ranjan Meher
 */
public class SceneFXMLController extends AnchorPane implements Initializable {

    @FXML
    @Getter
    private Label modelVersion;
    
    @FXML
    @Getter
    private Label modelName;

    @FXML
    @Getter
    private TextArea console;

    @FXML
    @Getter
    private ComboBox<BIMModel> modelList;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Console.INSTANCE.setConsoleView(console);
        ControllerUtilities.INSTANCE.addController(SceneFXMLController.class, this);

        modelList.setOnAction((event) -> {

            if (modelList.getSelectionModel().getSelectedIndex() >-1) {
                BIMModel model = modelList.getSelectionModel().getSelectedItem();
                TestcaseNode node = model.getNode();
                Model.INSTANCE.setCurrentModel(model);
                TreeViewFXMLController treeViewController = (TreeViewFXMLController) ControllerUtilities.INSTANCE.getController(TreeViewFXMLController.class);
                treeViewController.getTreeRootNode().setValue(node);
                treeViewController.populateTreeFromRootNode(node);
                Console.INSTANCE.setConsoleMessage("Model " + model.getName() + ":" + model.getVersion() + " is selected");
                UIUtils.refreshUI(() -> {
                    modelVersion.setText(String.valueOf(model.getVersion()));
                    modelName.setText(model.getName());
                });
            }
        });
    }
}
