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
package com.invicara.tools.rh;

import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.Default;
import com.invicara.tools.rh.configuration.FileFormat;
import com.invicara.tools.rh.configuration.RESTfulURL;
import com.invicara.tools.rh.configuration.RuleInfo;
import com.invicara.tools.rh.configuration.Server;
import com.invicara.tools.rh.exception.ConfigurationException;
import static com.invicara.tools.rh.utils.JSONConstants.*;
import com.invicara.tools.rh.service.DatabaseManagement;
import com.invicara.tools.rh.utils.IOManagement;
import com.invicara.tools.rh.utils.UIUtils;
import freemarker.template.Configuration;
import freemarker.template.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        new MainApp().initialize();

        Parent parent = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Rules Hardening Tool");

        stage.setOnCloseRequest((WindowEvent event) -> {
            Connection connection = Database.EMPIRE_MANAGE.getConnection();
            if (connection != null) {
                try {
                    connection.close();
                    UIUtils.updateConsole("Empire Manage DB connection get closed");
                } catch (SQLException ex) {
                    UIUtils.updateConsole("Empire Manage DB connection failed to closed");
                }
            }
            connection = Database.PASSPORT.getConnection();
            if (connection != null) {
                try {
                    connection.close();
                    UIUtils.updateConsole("Passport DB connection get closed");
                } catch (SQLException ex) {
                    UIUtils.updateConsole("Passport DB connection failed to closed");
                }
            }
        });

        scene.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        scene.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.setDropCompleted(true);
            } else {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initialize() {
        /*
         get rules mapping
         */
        Map<String, String> maps;
        String mappingFile = "json/mapping.json";
        String userDir = Default.INSTANCE.getUserDir();
        String data = IOManagement.openExternalFile(userDir + File.separator + mappingFile);
        if (data.isEmpty()) {
            data = IOManagement.openInternalFile(mappingFile);
        }
        try {
                JSONArray array = new JSONArray(data);
                maps = new HashMap<>();
                for (int index = 0; index < array.length(); index++) {
                    JSONObject rule = array.getJSONObject(index);
                    maps.put(rule.getString("rule"), rule.toString());
                }
                RuleInfo.INSTANCE.setMaps(maps);
            } catch (JSONException ex) {
                UIUtils.updateConsole("mapping.json not found.\n" + ex.getMessage());
            }

        /*
         get Configuration information
         */
        String configFile = "json/config.json";
        data = IOManagement.openExternalFile(userDir + File.separator + configFile);
        if (data.isEmpty()) {
            data = IOManagement.openInternalFile(configFile);
        }
        try {
            JSONObject config = new JSONObject(data);

            Server.TOMCAT_PASSPORT.setServerUrl(config.getString(PASSPORT_SERVER_URL));
            Server.TOMCAT_EMPIRE_MANAGE.setServerUrl(config.getString(EMPIREMANAGE_SERVER_URL));

            Server.PASSPORT_DB.setServerUrl(config.getString(PASSPORT_DATABASE_URL));
            Server.EMPIRE_MANAGE_DB.setServerUrl(config.getString(PASSPORT_DATABASE_URL));

            Database.DB_AUTHENTICATION.setUser(config.getString(DATABASE_USER));
            Database.DB_AUTHENTICATION.setPassword(config.getString(DATABASE_PASSWORD));

            RESTfulURL.PASSPORT.setBasePath(config.getString(PASSPORT_BASE_URL));
            RESTfulURL.EMPIRE_MANAGE.setBasePath(config.getString(EMPIREMANAGE_BASE_URL));

            String homePath = System.getenv("HOME");
            String path = config.getJSONObject(FOLDER_PATH).getString(EXCEL);
            if (path.contains("Desktop")) {
                path = homePath + "\\Desktop";
                FileFormat.EXCEL.setFilePath(path);
                FileFormat.EXCEL.setFilePath(path);
                FileFormat.IMAGE.setFilePath(path);
                FileFormat.HTML.setFilePath(path);
                FileFormat.PDF.setFilePath(path);
            } else {
                FileFormat.EXCEL.setFilePath(path);
                FileFormat.IMAGE.setFilePath(path);
                FileFormat.HTML.setFilePath(path);
                FileFormat.PDF.setFilePath(path);
            }

            path = config.getJSONObject(FOLDER_PATH).getString(JSON);
            if (path.contains("Desktop")) {
                path = homePath + "\\Desktop";
                FileFormat.JSON.setFilePath(path);
                FileFormat.IMAGE.setFilePath(path);
                FileFormat.HTML.setFilePath(path);
                FileFormat.PDF.setFilePath(path);
            } else {
                FileFormat.JSON.setFilePath(path);
                FileFormat.IMAGE.setFilePath(path);
                FileFormat.HTML.setFilePath(path);
                FileFormat.PDF.setFilePath(path);
            }

            JSONObject user = config.getJSONArray(USERS).getJSONObject(0);
            DatabaseData.USER.setUsersData(config.getJSONArray(USERS).toString());
            DatabaseData.USER.setUserLoginData(user.getJSONObject(DATA).toString());
            DatabaseData.USER.setUserLoginId(user.getInt(ID));

            Default.INSTANCE.setConfiguration(new Configuration(new Version("2.3.0")));
        } catch (JSONException ex) {
            UIUtils.updateConsole("config.json not found.\n" + ex.getMessage());
        }

        /*
         get the Passport Database connection
         */
        Connection connection = Database.PASSPORT.getConnection();
        DatabaseManagement dbMgmt = new DatabaseManagement();
        if (connection == null) {
            try {
                Database.PASSPORT.setConnection(dbMgmt.configureToEmpireManageDatabase("passportdb"));
                UIUtils.updateConsole("Passport DB connection is opened");
            } catch (ConfigurationException e) {
                UIUtils.updateConsole("Passport DB connection exception " + e.getMessage());
                System.exit(0);
            }
        }
        /*
         get the Empire Manage Database connection
         */
        connection = Database.EMPIRE_MANAGE.getConnection();
        if (connection == null) {
            try {
                Database.EMPIRE_MANAGE.setConnection(dbMgmt.configureToEmpireManageDatabase("empiremanagedb"));
                UIUtils.updateConsole("Empire Manage DB connection is opened");
            } catch (ConfigurationException e) {
                UIUtils.updateConsole("Empire Manage DB connection exception " + e.getMessage());
                System.exit(0);
            }
        }
    }
}
