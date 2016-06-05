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

import com.invicara.tools.rh.service.Command;
import com.invicara.tools.rh.ui.service.Execution;
import com.invicara.tools.rh.utils.Status;
import com.invicara.tools.rh.exception.TerminationException;
import com.invicara.tools.rh.model.TestcaseNode;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Alok Ranjan Meher
 */
public class MasterController {

    protected Image success, failure;

    public MasterController() {
        success = new Image(getClass().getClassLoader().getResourceAsStream("images/correct.png"));
        failure = new Image(getClass().getClassLoader().getResourceAsStream("images/error.png"));
    }

    public Command selectedAndStatusUpdated(CheckBoxTreeItem<TestcaseNode> treeNode, boolean status) {
        return () -> {
            if (treeNode != null) {
                treeNode.setSelected(Boolean.TRUE);
                treeNode.setGraphic(new ImageView(status ? success : failure));
            }
        };
    }

    public void execute(ButtonPanelFXMLController btnController, Execution execution, final CheckBoxTreeItem<TestcaseNode> node) {
        try {
            execution.execute(node);
        } catch (TerminationException ex) {
        }
        Status.RUNNING.setActive(false);
        btnController.buttonEnable();
        btnController.buttonReset();
    }
}
