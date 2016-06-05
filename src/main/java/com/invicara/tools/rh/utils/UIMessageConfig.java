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

import org.aeonbits.owner.Config;

/**
 *
 * @author Alok Ranjan Meher
 */
@Config.Sources("file:src/main/resources/properties/messages.properties")
public interface UIMessageConfig extends Config {

    /*
     Button
     */
    @Key("ui.button.executeAll")
    String executeAll();

    @Key("ui.button.executeSelected")
    String executeSelected();
    
    @Key("ui.button.exportAll")
    String exportAllRules();

    @Key("ui.button.exportSelected")
    String exportSelectedRules();

    @Key("ui.button.stop")
    String stop();

    @Key("ui.button.reset")
    String reset();

    @Key("ui.button.clear")
    String clear();

    @Key("ui.button.report")
    String report();

    /*
     Tooltip
     */
    @Key("ui.tooltip.button.executeAll")
    String tooltipExecuteAll();
    
    @Key("ui.tooltip.button.executeSelected")
    String tooltipExecuteSelected();
    
    @Key("ui.tooltip.button.exportAll")
    String tooltipExportAll();
    
    @Key("ui.tooltip.button.exportSelected")
    String tooltipExportSelected();
    
    @Key("ui.tooltip.button.stopExecute")
    String tooltipStopExecute();
    
    @Key("ui.tooltip.button.resetAllRules")
    String tooltipResetAllRules();
    
    @Key("ui.tooltip.button.clearAllRules")
    String tooltipClearAllRules();

    @Key("ui.tooltip.button.generateReport")
    String tooltipGenerateReport();
    
    /*
     Messages
     */
    @Key("messages.idsNotSelected")
    String idsNotSelected();

    @Key("messages.modelLoaded")
    String modelLoaded(String filePath);

    @Key("messages.exportFailed")
    String exportFailed();

    @Key("messages.fileSaved")
    String fileSaved(String filePath);
    
    @Key("messages.failedToSave")
    String failedToSave(String filePath);

    @Key("messages.fileUploaded")
    String fileUploaded(String fileName);

    @Key("messages.failedToLoad")
    String failedToLoad(String fileName);

    @Key("messages.htmlCreated")
    String htmlCreated(String filePath);

    @Key("messages.failedtoCreateHtml")
    String failedtoCreateHtml(String filePath);

    @Key("messages.invalidModelCount")
    String invalidModelCount(String filePath);

    @Key("messages.modelAdded")
    String modelAdded(String filePath);

    @Key("messages.modelRemoved")
    String modelRemoved(String filePath);

    @Key("messages.idsNotSelected")
    String idsNotSelected(String filePath);
}
