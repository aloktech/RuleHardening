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
package com.invicara.tools.rh.configuration;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextArea;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum Console {

    INSTANCE;
    
    private TextArea consoleView;
    private final SimpleObjectProperty<StringBuilder> builderProperty = new SimpleObjectProperty(new StringBuilder());

    public String getConsoleData() {
        return builderProperty.get().toString();
    }

    public void appendEmptyLines(final int count) {
        executeLater(() -> {
            for (int index = 1; index <= count; index++) {
                consoleView.appendText("\n");
            }
            consoleView.positionCaret(consoleView.getLength());
            consoleView.setScrollTop(Double.MIN_VALUE);
        });
    }

    public void clearConsole() {
        executeLater(() -> {
            consoleView.setText("");
            consoleView.positionCaret(consoleView.getLength());
            consoleView.setScrollTop(Double.MIN_VALUE);
        });
    }

    public void setConsoleMessage(final String consoleData) {
        System.out.println(consoleData == null ? "\n" : (consoleData.isEmpty() ? "\n" : consoleData));

        executeLater(() -> {
            if (consoleView == null) {
                return;
            }
            consoleView.appendText("\n");
            consoleView.appendText(consoleData == null ? "" : consoleData);
            consoleView.positionCaret(consoleView.getLength());
            consoleView.setScrollTop(Double.MIN_VALUE);
        });
    }

    private void executeLater(Runnable runnable) {
        Platform.runLater(runnable);
    }

    public TextArea getConsoleView() {
        return consoleView;
    }

    public void setConsoleView(TextArea consolePanel) {
        this.consoleView = consolePanel;
    }

    public SimpleObjectProperty<StringBuilder> getBuilderProperty() {
        return builderProperty;
    }

}
