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
package com.invicara.tools.rh.utils;

import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.service.Command;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;

/**
 *
 * @author Alok Ranjan Meher
 */
public class UIUtils {
    
    public static void showConfirmationWindow(String message) {
        refreshUILater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmation Message");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.getDialogPane().setContentText(message);
            alert.getDialogPane().setHeaderText("Confirmation Message");
            alert.show();
        });
    }

    public static void showWarningWindow(String message) {
        refreshUILater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Warning Message");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.getDialogPane().setContentText(message);
            alert.getDialogPane().setHeaderText("Warning Message");
            alert.show();
        });
    }

    public static void showInfoWindow(String message) {
        refreshUILater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Info Message");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.getDialogPane().setContentText(message);
            alert.getDialogPane().setHeaderText("Info Message");
            alert.show();
        });
    }

    public static void appendEmptyLines(int count) {
        Console.INSTANCE.appendEmptyLines(count);
    }

    public static void updateConsole(String msg) {
        Console.INSTANCE.setConsoleMessage(msg);
    }

    public static boolean validateConfiguration() {
        if ((DatabaseData.ACCOUNT.getId() == 0)
                || (DatabaseData.PROJECT.getId() == 0)
                || (DatabaseData.PACKAGE_MASTER.getId() == 0)) {
            showWarningWindow("Account Id or Project Id or Package Master id is not selected.");
            updateConsole("Account Id or Project Id or Package Master id are not selected.");
            return false;
        }
        return true;
    }

    public static void refreshUILater(Command cmd) {
        Platform.runLater(() -> {
            cmd.execute();
        });
    }
    
    public static void refreshUI(Command cmd) {
        cmd.execute();
    }
}
