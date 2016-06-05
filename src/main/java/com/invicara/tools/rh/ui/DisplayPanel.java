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

import static com.invicara.tools.rh.utils.JSONConstants.DESCRIPTION_MSG;
import static com.invicara.tools.rh.utils.JSONConstants.ISSUES;
import com.invicara.tools.rh.model.TestcaseNode;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class DisplayPanel extends StackPane {

    double preLayoutX, preLayoutY;
    final VBox vBox = new VBox();

    public DisplayPanel(final TestcaseNode node) {

        setPrefSize(12, 12);
        
        try {
            final Label modelLabel = new Label("Model Name : " + node.getModelName());
            final Label ruleLabel = new Label("Rule Name : " + node.getRuleName());
            final Label testcaseLabel = new Label("Testcase Name : " + node.getTestcaseName());
            final Label timeLabel = new Label("Workflow time : " + node.getWorkflowRunTime() + " milliseconds");
            int value = 0;
            value = checkIssues(node, value);
            HBox statusPanel = new HBox();
            final Label statusLabel = new Label("Status : ");
            statusPanel.getChildren().add(statusLabel);

            Image image = checkImageStatus(value);
            final Label status = new Label("", new ImageView(image));
            statusPanel.getChildren().add(status);

            vBox.getChildren().addAll(modelLabel, ruleLabel, testcaseLabel, timeLabel, statusPanel);

            if (value > 0) {
                HBox descriptionPanel = new HBox();
                final Label descriptionLabel = new Label("Description : ");
                JSONObject output = new JSONObject(node.getExpectedOutput());
                Label description = new Label();
                description.setText(output.getJSONArray(ISSUES).getJSONObject(0).getString(DESCRIPTION_MSG));
                descriptionPanel.getChildren().addAll(descriptionLabel, description);
                vBox.getChildren().add(descriptionPanel);
            }
        } catch (Exception e) {
            final Label status = new Label("Rule \"" + node.getRuleName() + "\"\n do not have the testcase \"" + node.getTestcaseName() + "\"\n for the model " + node.getModelName());
            status.setWrapText(true);
            vBox.getChildren().add(status);
        }
        vBox.setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        vBox.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        vBox.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line", ".default-color5.chart-symbol { /* hollow circle */\n"
                + "    -fx-background-color: #860061, white;\n"
                + "    -fx-background-insets: 0, 2;\n"
                + "    -fx-background-radius: 5px;\n"
                + "    -fx-padding: 5px;\n"
                + "}");

//        setOnMouseClicked((MouseEvent event) -> {
//            System.out.println("LayoutX : " + getLayoutX() + " LayoutY : " + getLayoutY());
//            System.out.println("Height : " + getMinHeight() + " Width : " + getMinWidth());
//            System.out.println("Display MinX : " + getBoundsInParent().getMinX() + " Display MaxX : " + getBoundsInParent().getMaxX());
//            System.out.println("Display MinY : " + getBoundsInParent().getMinY() + " Display MaxY : " + getBoundsInParent().getMaxY());
//            System.out.println("Chart MinX : " + getParent().getBoundsInLocal().getMinX() + " Chart MaxX : " + getParent().getBoundsInLocal().getMaxX());
//            System.out.println("Chart MinY : " + getParent().getBoundsInLocal().getMinY() + " Chart MaxY : " + getParent().getBoundsInLocal().getMaxY());
//        });
        setOnMouseEntered((MouseEvent event) -> {
            getChildren().add(vBox);

            preLayoutX = getLayoutX();
            preLayoutY = getLayoutY();
            
            checkWidth();

            checkHeight();

            toFront();
        });

        setOnMouseExited((MouseEvent event) -> {
            getChildren().clear();
            setLayoutX(preLayoutX);
            setLayoutY(preLayoutY);
        });
    }

    private void checkHeight() {
        double displayHeight = vBox.getHeight();
        if (getLayoutY() < 50 && displayHeight == 0) {
            setLayoutY(50);
        } else if (getLayoutY() < displayHeight) {
            setLayoutY(displayHeight / 2);
        }
        
        double chartDisplayHeight = getParent().getBoundsInLocal().getMaxY();
        if (getLayoutY() > 535 && chartDisplayHeight == 0) {
            setLayoutY(535);
        } else if ((getLayoutY() + displayHeight) > chartDisplayHeight) {
            setLayoutY(chartDisplayHeight - displayHeight - 8);
        }
    }

    private void checkWidth() {
        double displayWidth = vBox.getWidth();
//            if (getLayoutX() < 130 && displayWidth == 0) {
//                setLayoutX(130);
//            } else if (getLayoutX() < 0) {
//                setLayoutX(130);
//            }
if (getLayoutX() < 130) {
    setLayoutX(130);
}

double chartDisplayWidth = getParent().getBoundsInLocal().getMaxX();
if (getLayoutX() > 1650 && displayWidth == 0) {
    setLayoutX(1650);
} else if ((getLayoutX() + displayWidth) > chartDisplayWidth) {
    setLayoutX(chartDisplayWidth - displayWidth - 5);
}
    }

    private Image checkImageStatus(int value) {
        Image image;
        switch (value) {
            case 1:
                image = new Image("images/success.png");
                break;
            case 2:
                image = new Image("images/failure.png");
                break;
            case 3:
                image = new Image("images/warning.png");
                break;
            default:
                image = new Image("images/error.png");
        }
        return image;
    }

    private int checkIssues(final TestcaseNode node, int value) {
        try {
            JSONArray array = new JSONObject(node.getExpectedOutput()).getJSONArray("issues");
            if (array != null) {
                value = array.getJSONObject(0).getInt("status");
            }
        } catch (JSONException e) {
            value = 0;
        }
        return value;
    }

    public VBox getDisplayPanel() {
        return vBox;
    }

}
