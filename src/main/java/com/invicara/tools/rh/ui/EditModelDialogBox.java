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
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 *
 * @author Alok Ranjan Meher
 */
public class EditModelDialogBox extends Stage {

    boolean fieldEdited;
    TreeView<TestcaseNode> treeView;
    CheckBoxTreeItem<TestcaseNode> selectedNode;

    public EditModelDialogBox() {
//        ConfigurationBackup.INSTANCE.setPreServerUrl(Server.TOMCAT_PASSPORT.getServerUrl());
//        ConfigurationBackup.INSTANCE.setCurServerUrl(Server.TOMCAT_PASSPORT.getServerUrl());
//
//        ConfigurationBackup.INSTANCE.setPreLoginUserId(DatabaseData.USER.getId());
//        ConfigurationBackup.INSTANCE.setCurLoginUserId(DatabaseData.USER.getId());
//        ConfigurationBackup.INSTANCE.setPreLoginUserData(DatabaseData.USER.getUserLoginData());
//        ConfigurationBackup.INSTANCE.setCurLoginUserData(DatabaseData.USER.getUserLoginData());
//        
//        ConfigurationBackup.INSTANCE.setPreExcelFilePath(FileFormat.EXCEL.getFilePath());
//        ConfigurationBackup.INSTANCE.setCurExcelFilePath(FileFormat.EXCEL.getFilePath());
//        ConfigurationBackup.INSTANCE.setPreJsonFilePath(FileFormat.JSON.getFilePath());
//        ConfigurationBackup.INSTANCE.setCurJsonFilePath(FileFormat.JSON.getFilePath());
//        
//        ConfigurationBackup.INSTANCE.setPreDatabaseUrl(Server.EMPIRE_MANAGE_DB.getServerUrl());
//        ConfigurationBackup.INSTANCE.setCurDatabaseUrl(Server.EMPIRE_MANAGE_DB.getServerUrl());
//        ConfigurationBackup.INSTANCE.setPreDatabaseUser(Database.PASSPORT.getUser());
//        ConfigurationBackup.INSTANCE.setCurDatabaseUser(Database.PASSPORT.getUser());
//        ConfigurationBackup.INSTANCE.setPreDatabasePassword(Database.PASSPORT.getPassword());
//        ConfigurationBackup.INSTANCE.setCurDatabasePassword(Database.PASSPORT.getPassword());
        

    }

    public void createGUI() {
    }

    public void closeDialog() {
        close();
    }

    public void showDialog() {
        show();
    }
}
