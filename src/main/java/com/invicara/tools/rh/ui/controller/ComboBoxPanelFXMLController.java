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
import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.service.DatabaseManagement;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.UIUtils;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Alok Ranjan Meher
 */
public class ComboBoxPanelFXMLController implements Initializable {

    @FXML
    ComboBox<Integer> accountCB, projectCB, packageMasterCB;

    private DatabaseManagement dbMgmt;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbMgmt = new DatabaseManagement();

        dbMgmt.setConnection(Database.PASSPORT.getConnection());
        ObservableList<Integer> obsList = FXCollections.observableList(dbMgmt.getAccountIds());
        accountCB.getItems().clear();
        accountCB.getItems().addAll(obsList);

        ControllerUtilities.INSTANCE.addController(ComboBoxPanelFXMLController.class, this);
    }

    @FXML
    public void selectAccount(ActionEvent event) {
        if (accountCB.getSelectionModel().getSelectedItem() != null) {
            DatabaseData.ACCOUNT.setId(accountCB.getSelectionModel().getSelectedItem());
            Console.INSTANCE.setConsoleMessage("Selected Account Id " + DatabaseData.ACCOUNT.getId());
            dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
            List<Integer> projectsIds = dbMgmt.getProjectIds(DatabaseData.ACCOUNT.getId());
            ObservableList<Integer> obsList = FXCollections.observableList(projectsIds);
            projectCB.getItems().clear();
            projectCB.getItems().addAll(obsList);
        }
    }

    @FXML
    public void selectProject(ActionEvent event) {
        if (projectCB.getSelectionModel().getSelectedItem() != null) {
            DatabaseData.PROJECT.setId(projectCB.getSelectionModel().getSelectedItem());
            Console.INSTANCE.setConsoleMessage("Selected Project Id " + DatabaseData.PROJECT.getId());
            dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
            List<Integer> pmIds = dbMgmt.getPackageMasterIds(DatabaseData.PROJECT.getId());
            ObservableList<Integer> obsList = FXCollections.observableList(pmIds);
            packageMasterCB.getItems().clear();
            packageMasterCB.getItems().addAll(obsList);
        }
    }

    @FXML
    public void selectPackageMaster(ActionEvent event) {
        if (packageMasterCB.getSelectionModel().getSelectedItem() != null) {
            DatabaseData.PACKAGE_MASTER.setId(packageMasterCB.getSelectionModel().getSelectedItem());
            Console.INSTANCE.setConsoleMessage("Selected Package Master Id " + DatabaseData.PACKAGE_MASTER.getId());
            setModelName();

            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            BIMModel model = sceneController.getModelList().getSelectionModel().getSelectedItem();
            model.setName(Model.INSTANCE.getCurrentModel().getName());
            UIUtils.refreshUI(() -> {
                sceneController.getModelVersion().setText(String.valueOf(model.getVersion()));
                sceneController.getModelName().setText(model.getName());
            });
            Model.INSTANCE.setCurrentModel(model);
        }
    }

    void setModelName() {
        dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
        if (UIUtils.validateConfiguration()) {
            String name = dbMgmt.getModelName(DatabaseData.PACKAGE_MASTER.getId(), DatabaseData.PROJECT.getId());
            if (name.endsWith(".bimpk")) {
                name = name.substring(0, name.lastIndexOf(".bimpk"));
            }
            Model.INSTANCE.setName(name);
            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            BIMModel model = Model.INSTANCE.getModel(name);
            if (model != null) {
                model.setVersion(1);
                TestcaseNode node = model.getNode();
                if (node != null) {
                    sceneController.getModelList().getSelectionModel().select(model);
                }
            }
        }
    }
}
