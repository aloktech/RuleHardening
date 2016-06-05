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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.FileFormat;
import com.invicara.tools.rh.configuration.RESTfulURL;
import com.invicara.tools.rh.configuration.Server;
import com.invicara.tools.rh.utils.ControllerUtilities;
import static com.invicara.tools.rh.utils.JSONConstants.*;
import com.invicara.tools.rh.service.DatabaseManagement;
import static com.invicara.tools.rh.model.ConfigurationInfo.INSTANCE;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
@Log
public class ConfigurationWindowFXMLController implements Initializable {

    @FXML
    private TextField passportURL, empireManageURL, passportDBURL, empireManageDBURL,
            passportBaseURL, empireManageBaseURL, folderPath, user, password;

    @FXML
    private ComboBox<Integer> userId;

    @FXML
    private TextArea userData;

    @FXML
    private Button okButton, applyButton, cancelButton;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        INSTANCE.setPrePassportServerUrl(Server.TOMCAT_PASSPORT.getServerUrl());
        INSTANCE.setCurPassportServerUrl(Server.TOMCAT_PASSPORT.getServerUrl());
        passportURL.setText(INSTANCE.getCurPassportServerUrl());

        INSTANCE.setPreEmpireManageServerUrl(Server.TOMCAT_EMPIRE_MANAGE.getServerUrl());
        INSTANCE.setCurEmpireManageServerUrl(Server.TOMCAT_EMPIRE_MANAGE.getServerUrl());
        empireManageURL.setText(INSTANCE.getCurEmpireManageServerUrl());

        INSTANCE.setPreExcelFilePath(FileFormat.EXCEL.getFilePath());
        INSTANCE.setCurExcelFilePath(FileFormat.EXCEL.getFilePath());
        folderPath.setText(INSTANCE.getCurExcelFilePath());

        INSTANCE.setPreJsonFilePath(FileFormat.JSON.getFilePath());
        INSTANCE.setCurJsonFilePath(FileFormat.JSON.getFilePath());
        folderPath.setText(INSTANCE.getCurJsonFilePath());

        INSTANCE.setPrePassportDatabaseUrl(Server.PASSPORT_DB.getServerUrl());
        INSTANCE.setCurPassportDatabaseUrl(Server.PASSPORT_DB.getServerUrl());
        passportDBURL.setText(INSTANCE.getCurPassportDatabaseUrl());

        INSTANCE.setPreEmpireManageDatabaseUrl(Server.EMPIRE_MANAGE_DB.getServerUrl());
        INSTANCE.setCurEmpireManageDatabaseUrl(Server.EMPIRE_MANAGE_DB.getServerUrl());
        empireManageDBURL.setText(INSTANCE.getCurEmpireManageDatabaseUrl());

        INSTANCE.setPreDatabaseUser(Database.DB_AUTHENTICATION.getUser());
        INSTANCE.setCurDatabaseUser(Database.DB_AUTHENTICATION.getUser());
        user.setText(INSTANCE.getCurDatabaseUser());

        INSTANCE.setPreDatabasePassword(Database.DB_AUTHENTICATION.getPassword());
        INSTANCE.setCurDatabasePassword(Database.DB_AUTHENTICATION.getPassword());
        password.setText(INSTANCE.getCurDatabasePassword());

        INSTANCE.setPreEmpireManageRESTfulBaseUrl(RESTfulURL.EMPIRE_MANAGE.getBasePath());
        INSTANCE.setCurEmpireManageRESTfulBaseUrl(RESTfulURL.EMPIRE_MANAGE.getBasePath());
        empireManageBaseURL.setText(INSTANCE.getCurEmpireManageRESTfulBaseUrl());

        INSTANCE.setPrePassportRESTfulBaseUrl(RESTfulURL.PASSPORT.getBasePath());
        INSTANCE.setCurPassportRESTfulBaseUrl(RESTfulURL.PASSPORT.getBasePath());
        passportBaseURL.setText(INSTANCE.getCurPassportRESTfulBaseUrl());

        INSTANCE.setPreLoginUserId(DatabaseData.USER.getUserLoginId());
        INSTANCE.setCurLoginUserId(DatabaseData.USER.getUserLoginId());
        ObservableList<Integer> list = FXCollections.observableArrayList();
        DatabaseManagement dbInstance = new DatabaseManagement();
        dbInstance.setConnection(Database.PASSPORT.getConnection());
        list.addAll(dbInstance.getUserIds());
        userId.setItems(list);
        userId.getSelectionModel().selectFirst();
        userId.setOnAction((ActionEvent event) -> {
            JSONArray usersData = new JSONArray(DatabaseData.USER.getUsersData());
            for (int index = 0; index < usersData.length(); index++) {
                JSONObject tempUserData = usersData.getJSONObject(index);
                if (tempUserData.getInt(ID) == userId.getValue()) {
                    INSTANCE.setCurLoginUserId(tempUserData.getInt(ID));
                    DatabaseData.USER.setUserLoginData(tempUserData.getJSONObject(DATA).toString());
                    INSTANCE.setPreLoginUserData(DatabaseData.USER.getUserLoginData());
                    INSTANCE.setCurLoginUserData(DatabaseData.USER.getUserLoginData());
                }
            }
        });

        INSTANCE.setPreLoginUserData(DatabaseData.USER.getUserLoginData());
        INSTANCE.setCurLoginUserData(DatabaseData.USER.getUserLoginData());
        String data = DatabaseData.USER.getUserLoginData();
        if (data != null) {
            data = data.replaceAll("\n", "");
            data = data.replaceAll("\\{", "\\{\n");
            data = data.replaceAll("\\{\\{", "\\{\n\\{");
            data = data.replaceAll("\\},", "\\},\n");
            data = data.replaceAll("\\}\\},", "\\}\n\\}");
            data = data.replaceAll("\\}\n", "\n\\}\n");
            data = data.replaceAll(",", ",\n");
        } else {
            data = "";
        }
        userData.setText(data);

        ControllerUtilities.INSTANCE.addController(ConfigurationWindowFXMLController.class, this);

        passportURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurPassportServerUrl(newValue);
        });

        empireManageURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurEmpireManageServerUrl(newValue);
        });

        passportDBURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurPassportDatabaseUrl(newValue);
        });

        empireManageDBURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurEmpireManageDatabaseUrl(newValue);
        });

        passportBaseURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurPassportRESTfulBaseUrl(newValue);
        });

        empireManageBaseURL.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurEmpireManageRESTfulBaseUrl(newValue);
        });

        folderPath.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurJsonFilePath(newValue);
            INSTANCE.setCurExcelFilePath(newValue);
        });

        userId.setOnAction((ActionEvent event) -> {
            INSTANCE.setCurLoginUserId(userId.getValue());
        });

        userData.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            INSTANCE.setCurUserData(userData.getText());
        });
    }

    @FXML
    public void onOKAction(ActionEvent event) {
        if (!INSTANCE.getPrePassportServerUrl().equals(INSTANCE.getCurPassportServerUrl())) {
            Server.TOMCAT_PASSPORT.setServerUrl(INSTANCE.getCurPassportServerUrl());
        }
        if (!INSTANCE.getPreEmpireManageServerUrl().equals(INSTANCE.getCurEmpireManageServerUrl())) {
            Server.TOMCAT_EMPIRE_MANAGE.setServerUrl(INSTANCE.getCurEmpireManageServerUrl());
        }

        if (!INSTANCE.getPrePassportDatabaseUrl().equals(INSTANCE.getCurPassportDatabaseUrl())) {
            Server.PASSPORT_DB.setServerUrl(INSTANCE.getCurPassportDatabaseUrl());
        }
        if (!INSTANCE.getPreEmpireManageDatabaseUrl().equals(INSTANCE.getCurEmpireManageDatabaseUrl())) {
            Server.EMPIRE_MANAGE_DB.setServerUrl(INSTANCE.getCurEmpireManageDatabaseUrl());
        }

        if (!INSTANCE.getPreDatabaseUser().equals(INSTANCE.getCurDatabaseUser())) {
            Database.DB_AUTHENTICATION.setUser(INSTANCE.getCurDatabaseUser());
        }
        if (!INSTANCE.getPreDatabasePassword().equals(INSTANCE.getCurDatabasePassword())) {
            Database.DB_AUTHENTICATION.setPassword(INSTANCE.getCurDatabasePassword());
        }

        if (!INSTANCE.getPrePassportRESTfulBaseUrl().equals(INSTANCE.getCurPassportRESTfulBaseUrl())) {
            RESTfulURL.PASSPORT.setBasePath(INSTANCE.getCurPassportRESTfulBaseUrl());
        }
        if (!INSTANCE.getPreEmpireManageRESTfulBaseUrl().equals(INSTANCE.getCurEmpireManageRESTfulBaseUrl())) {
            RESTfulURL.EMPIRE_MANAGE.setBasePath(INSTANCE.getCurPassportRESTfulBaseUrl());
        }

        if (!INSTANCE.getPreExcelFilePath().equals(INSTANCE.getCurExcelFilePath())) {
            FileFormat.EXCEL.setFilePath(INSTANCE.getCurExcelFilePath());
        }
        if (!INSTANCE.getPreJsonFilePath().equals(INSTANCE.getCurJsonFilePath())) {
            FileFormat.JSON.setFilePath(INSTANCE.getCurJsonFilePath());
        }

        if (INSTANCE.getPreLoginUserId() != INSTANCE.getCurLoginUserId()) {
            DatabaseData.USER.setUserLoginId(INSTANCE.getCurLoginUserId());
        }
        if (!INSTANCE.getPreLoginUserData().equals(INSTANCE.getCurLoginUserData())) {
            DatabaseData.USER.setUserLoginData(INSTANCE.getCurLoginUserData());
        }

        JSONObject configData = new JSONObject();
        configData.put(PASSPORT_SERVER_URL, Server.TOMCAT_PASSPORT.getServerUrl());
        configData.put(EMPIREMANAGE_SERVER_URL, Server.TOMCAT_EMPIRE_MANAGE.getServerUrl());
        configData.put(PASSPORT_DATABASE_URL, Server.PASSPORT_DB.getServerUrl());
        configData.put(EMPIREMANAGE_DATABASE_URL, Server.EMPIRE_MANAGE_DB.getServerUrl());
        configData.put(DATABASE_USER, Database.DB_AUTHENTICATION.getUser());
        configData.put(DATABASE_PASSWORD, Database.DB_AUTHENTICATION.getPassword());
        configData.put(PASSPORT_BASE_URL, RESTfulURL.PASSPORT.getBasePath());
        configData.put(EMPIREMANAGE_BASE_URL, RESTfulURL.EMPIRE_MANAGE.getBasePath());

        JSONObject fileData = new JSONObject();
        fileData.put(EXCEL, FileFormat.EXCEL.getFilePath());
        fileData.put(JSON, FileFormat.JSON.getFilePath());
        configData.put(FOLDER_PATH, fileData);

        JSONArray usersDataJSON = new JSONArray();
        JSONObject userDataJSON = new JSONObject();
        userDataJSON.put(ID, DatabaseData.USER.getUserLoginId());
        userDataJSON.put(DATA, new JSONObject(DatabaseData.USER.getUserLoginData()));
        usersDataJSON.put(userDataJSON);
        configData.put(USERS, usersDataJSON);

        String currentDirectory = System.getProperty("user.dir") + "/src/main/resources/json/config.json";
        try (PrintWriter pw = new PrintWriter(currentDirectory)) {
            pw.flush();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigurationWindowFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(currentDirectory), mapper.readTree(configData.toString()));
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationWindowFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onApplyAction(ActionEvent event) {
        INSTANCE.setCurPassportServerUrl(passportURL.getText());
        INSTANCE.setCurEmpireManageServerUrl(empireManageURL.getText());

        INSTANCE.setCurPassportDatabaseUrl(passportDBURL.getText());
        INSTANCE.setCurEmpireManageDatabaseUrl(empireManageDBURL.getText());

        INSTANCE.setCurDatabaseUser(user.getText());
        INSTANCE.setCurDatabasePassword(password.getText());

        INSTANCE.setCurPassportRESTfulBaseUrl(passportBaseURL.getText());
        INSTANCE.setCurEmpireManageRESTfulBaseUrl(empireManageBaseURL.getText());

        INSTANCE.setCurExcelFilePath(folderPath.getText());
        INSTANCE.setCurJsonFilePath(folderPath.getText());

        if (userId.selectionModelProperty().getValue() != null) {
            INSTANCE.setCurLoginUserId(userId.selectionModelProperty().getValue().getSelectedItem());
        }
        INSTANCE.setCurLoginUserData(userData.getText());
    }

    @FXML
    public void onCancelAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
