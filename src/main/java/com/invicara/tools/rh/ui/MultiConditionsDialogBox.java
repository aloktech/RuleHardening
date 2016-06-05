/**
 * ***********************************************************************
 * INVICARA INC CONFIDENTIAL ***********************************************************************
 *
 * Copyright (C) [2012] - [2014] INVICARA INC, INVICARA Pte Ltd, INVICARA INDIA PVT LTD All Rights
 * Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Invicara Inc and its
 * suppliers, if any. The intellectual and technical concepts contained herein are proprietary to
 * Invicara Inc and its suppliers and may be covered by U.S. and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Invicara Inc.
 */
package com.invicara.tools.rh.ui;

import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.JSONConstants;
import static com.invicara.tools.rh.utils.JSONConstants.*;
import com.invicara.tools.rh.utils.UIConstants;
import static com.invicara.tools.rh.utils.UIConstants.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class MultiConditionsDialogBox extends Stage {

    private final int LABEL_WIDTH = 100;
    private final int BUTTON_WIDTH = 60;
    private final int PANEL_HEIGHT = 35;
    private final Accordion accordion = new Accordion();
    private TitledPane conditionPane;
    private String uidata, schema;
    private String operator;
    private TextField propertyName, propertyValue, propertyUnit, propertyFromValue, propertyToValue, 
            propertyFromUnit, propertyToUnit;
    private TextArea expression;
    int counter = 0, paneCounter = 0;
    private boolean createOutput;
    private ImageView add;
    private final ObservableList<String> operators;
    private final ObservableList<Boolean> matchStatus;
    private JSONArray conditions;
    private final CheckBoxTreeItem<TestcaseNode> selectedNode;
    private final Button addBtn;

    public MultiConditionsDialogBox(CheckBoxTreeItem<TestcaseNode> selectedNode) {
        this.selectedNode = selectedNode;

        this.uidata = selectedNode.getValue().getActualArgument();

        operators = FXCollections.observableArrayList(
                EQUALS, 
                NOT_EQUALS, 
                EXISTS, 
                NOT_EXISTS, 
                VALUE_EXISTS,
                VALUE_NOT_EXISTS, 
                LESS_THAN, 
                GREATER_THAN, 
                LESS_THAN_OR_EQUAL, 
                GREATER_THAN_OR_EQUAL, 
                RANGE,
                PATTERN, 
                MULTI_VALUE);

        matchStatus = FXCollections.observableArrayList(Boolean.TRUE, Boolean.FALSE);

        addBtn = new Button();

        initialize();
        createGUI();
    }

    private void initialize() {
        propertyName = new TextField();
        propertyValue = new TextField();
        propertyUnit = new TextField();
        propertyFromValue = new TextField();
        propertyToValue = new TextField();
        propertyFromUnit = new TextField();
        propertyToUnit = new TextField();
        expression = new TextArea();

        add = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/plus.png")));
    }

    private void createGUI() {

        JSONObject actual = new JSONObject(uidata);
        conditions = actual.getJSONObject(EXPRESSION).getJSONObject(JSONConstants.VALUE).getJSONObject(CONDITIONS)
                .getJSONArray(JSONConstants.VALUE);

        VBox verticalPanel = new VBox();
        verticalPanel.setSpacing(5);
        verticalPanel.setPadding(new Insets(5, 5, 5, 5));

        HBox matchPanel = new HBox();
        Label label = new Label("Match");
        label.setPrefWidth(LABEL_WIDTH);
        matchPanel.getChildren().add(label);
        ComboBox<String> matchCB = new ComboBox(matchStatus);
        matchCB.getSelectionModel().select(String.valueOf(actual.getJSONObject(MATCH).getBoolean(JSONConstants.VALUE)));
        matchPanel.getChildren().add(matchCB);
        verticalPanel.getChildren().add(matchPanel);

        HBox expressionPanel = new HBox();
        label = new Label("Expression");
        label.setPrefWidth(LABEL_WIDTH);
        expressionPanel.getChildren().add(label);
        expression.setText(actual.getJSONObject(EXPRESSION).getJSONObject(JSONConstants.VALUE).getJSONObject(FORMULA)
                .getString(JSONConstants.VALUE));
        expression.setPrefWidth(285);
        expression.setPrefHeight(35);
        expressionPanel.getChildren().add(expression);
        verticalPanel.getChildren().add(expressionPanel);

        VBox dataPanel = new VBox();
        dataPanel.setSpacing(5);
        counter = 0;
        paneCounter = 0;
        createConditionUI(-1, dataPanel);
        verticalPanel.getChildren().add(dataPanel);

        addBtn.setGraphic(add);
        addBtn.setDisable(true);
        addBtn.setOnAction((ActionEvent event) -> {
            int count = accordion.getChildrenUnmodifiable().size();
            if (!operator.isEmpty()) {
                conditionPane = new TitledPane(operator + " : " + (count + 1), createPane(0));
                accordion.getPanes().add(conditionPane);
            }

        });
        verticalPanel.getChildren().add(addBtn);

        for (int index = 0; index < conditions.length(); index++) {
            JSONObject condition = conditions.getJSONObject(index);
            String value = condition.getJSONObject(OPERATOR).getString(JSONConstants.VALUE);
            paneCounter = 0;
            conditionPane = new TitledPane(value + " : " + (index + 1), createPane(index));
            conditionPane.setPrefHeight(paneCounter * 30);
            accordion.getPanes().add(conditionPane);
        }

        accordion.setPrefHeight(400);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(accordion);
        scrollPane.setPrefHeight(350);

        verticalPanel.getChildren().add(scrollPane);

        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(30);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(0, 5, 5, 5));
        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(BUTTON_WIDTH);
        okBtn.setOnAction((ActionEvent event) -> {
            actual.getJSONObject(MATCH).put(JSONConstants.VALUE, matchCB.getSelectionModel().getSelectedItem());
            actual.getJSONObject(EXPRESSION).getJSONObject(JSONConstants.VALUE).getJSONObject(CONDITIONS).put(JSONConstants.VALUE, conditions);
            selectedNode.getValue().setActualArgument(actual.toString());
            System.out.println(actual.toString());
            close();
        });
        buttonPanel.getChildren().add(okBtn);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(BUTTON_WIDTH);
        cancelBtn.setOnAction((ActionEvent event) -> {
            close();
        });
        buttonPanel.getChildren().add(cancelBtn);
        verticalPanel.getChildren().add(buttonPanel);

        setWidth(400);
        setHeight(500);

        setTitle("Input DialogBox");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setScene(new Scene(verticalPanel));
    }

    private VBox createPane(int index) {
        counter = 0;
        VBox dataPanel = new VBox();
        dataPanel.setSpacing(5);

        createConditionUI(index, dataPanel);

        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(30);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(BUTTON_WIDTH);
        deleteBtn.setOnAction((ActionEvent event) -> {
            accordion.getPanes().remove(index);
            conditions.remove(index);
        });
        buttonPanel.getChildren().add(deleteBtn);

        Button resetBtn = new Button("Reset");
        resetBtn.setPrefWidth(BUTTON_WIDTH);
        resetBtn.setOnAction((ActionEvent event) -> {
            dataPanel.getChildren().stream()
                    .filter((node) -> (node instanceof HBox))
                    .map((node) -> (HBox) node)
                    .map((horizontalPane) -> (TextField) horizontalPane.getChildren().get(1))
                    .forEach((textField) -> {
                        textField.setText("");
                    });
        });
        buttonPanel.getChildren().add(resetBtn);

        dataPanel.getChildren().add(buttonPanel);

        return dataPanel;
    }

    private void createConditionUI(int index, VBox dataPanel) {
        JSONObject condition = conditions.getJSONObject(index < 0 ? 0 : index);
        operator = index < 0 ? "" : condition.getJSONObject(OPERATOR).getString(JSONConstants.VALUE);
        VBox valuePanel = new VBox();
        valuePanel.setSpacing(5);
        HBox horizontalBox = new HBox();
        Label label = new Label("Operator");
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);
        ComboBox<String> operatorsCB = new ComboBox<>(operators);
        operatorsCB.getSelectionModel().select(operator);
        operatorsCB.setOnAction((ActionEvent event) -> {
            addBtn.setDisable(false);
            valuePanel.getChildren().clear();
            String value = operatorsCB.getSelectionModel().getSelectedItem();
            if (index < 0) {
                createEmptyPropertyValueUI(value, valuePanel);
            } else {
                createPropertyValueUI(index, value, valuePanel, condition);
            }
        });
        horizontalBox.getChildren().add(operatorsCB);
        dataPanel.getChildren().add(horizontalBox);
        paneCounter++;

        horizontalBox = new HBox();
        label = new Label("Property Name");
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);
        propertyName = new TextField();
        propertyName.setText(index < 0 ? "" : condition.getJSONObject(PROPERTY_NAME).getString(JSONConstants.VALUE));
        setValueKeyPressedListener(propertyName, condition.getJSONObject(PROPERTY_NAME));
        horizontalBox.getChildren().add(propertyName);
        dataPanel.getChildren().add(horizontalBox);
        paneCounter++;

        if (!operator.isEmpty()) {
            createPropertyValueUI(index, operator, dataPanel, condition);
        }

        dataPanel.getChildren().add(valuePanel);

    }

    private void createEmptyPropertyValueUI(String operator, VBox dataPanel) {

        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
                dataPanel.getChildren().add(createValuePanel(null));

                propertyUnit = new TextField();
                dataPanel.getChildren().add(createUnitPanel(propertyUnit, null));
                break;
            case PATTERN:
            case MULTI_VALUE:
                dataPanel.getChildren().add(createValuePanel(null));
                break;
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
                dataPanel.getChildren().add(createFromPanel(null));

                propertyFromUnit = new TextField();
                dataPanel.getChildren().add(createUnitPanel(propertyFromUnit, null));
                break;
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
                dataPanel.getChildren().add(createToPanel(null));

                propertyToUnit = new TextField();
                dataPanel.getChildren().add(createUnitPanel(propertyToUnit, null));
                break;
            case RANGE:
                dataPanel.getChildren().add(createFromPanel(null));

                propertyFromUnit = new TextField();
                dataPanel.getChildren().add(createUnitPanel(propertyFromUnit, null));

                dataPanel.getChildren().add(createToPanel(null));

                propertyToUnit = new TextField();
                dataPanel.getChildren().add(createUnitPanel(propertyToUnit, null));
        }

        dataPanel.prefHeight(counter * 40);
    }

    private void createPropertyValueUI(int index, String operator, VBox dataPanel, JSONObject condition) {
        try {
            JSONObject jsonData = condition.getJSONObject(PROPERTY_VALUE);
            JSONObject jsonValueData;
            switch (operator) {
                case EQUALS:
                case NOT_EQUALS:
                    dataPanel.getChildren().add(createValuePanel(jsonData));

                    propertyUnit = new TextField();
                    dataPanel.getChildren().add(createUnitPanel(propertyUnit, jsonData));
                    break;
                case PATTERN:
                case MULTI_VALUE:
                    dataPanel.getChildren().add(createValuePanel(jsonData));
                    break;
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    dataPanel.getChildren().add(createFromPanel(jsonData));

                    propertyFromUnit = new TextField();
                    jsonValueData = jsonData.getJSONObject(JSONConstants.VALUE).getJSONObject(JSONConstants.FROM);
                    dataPanel.getChildren().add(createUnitPanel(propertyFromUnit, jsonValueData));
                    break;
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                    dataPanel.getChildren().add(createToPanel(jsonData));

                    propertyToUnit = new TextField();
                    jsonValueData = jsonData.getJSONObject(JSONConstants.VALUE).getJSONObject(JSONConstants.TO);
                    dataPanel.getChildren().add(createUnitPanel(propertyToUnit, jsonValueData));
                    break;
                case RANGE:
                    dataPanel.getChildren().add(createFromPanel(jsonData));

                    propertyFromUnit = new TextField();
                    jsonValueData = jsonData.getJSONObject(JSONConstants.VALUE).getJSONObject(JSONConstants.FROM);
                    dataPanel.getChildren().add(createUnitPanel(propertyFromUnit, jsonValueData));

                    dataPanel.getChildren().add(createToPanel(jsonData));

                    propertyToUnit = new TextField();
                    jsonValueData = jsonData.getJSONObject(JSONConstants.VALUE).getJSONObject(JSONConstants.TO);
                    dataPanel.getChildren().add(createUnitPanel(propertyToUnit, jsonValueData));
            }
        } catch (JSONException e) {

        }

        dataPanel.prefHeight(counter * 40);
    }

    private void setValueKeyPressedListener(TextField textField, JSONObject condition) {
        textField.setOnKeyReleased((KeyEvent event) -> {
            String value = textField.getText();
            if (value.contains(",")) {
                JSONArray array = new JSONArray();
                for (String val : value.split(",")) {
                    try {
                        array.put(Double.parseDouble(val.trim()));
                    } catch (JSONException e1) {
                        try {
                            array.put(Integer.parseInt(val.trim()));
                        } catch (JSONException e2) {
                            array.put(val.trim());
                        }
                    }
                }
                condition.put(JSONConstants.VALUE, array);
            } else {
                try {
                    condition.put(JSONConstants.VALUE, Double.parseDouble(value));
                } catch (NumberFormatException e1) {
                    try {
                        condition.put(JSONConstants.VALUE, Integer.parseInt(value));
                    } catch (NumberFormatException e2) {
                        condition.put(JSONConstants.VALUE, value);
                    }
                }
            }
        });
    }

    private void setUnitKeyPressedListener(TextField textField, JSONObject condition) {
        textField.setOnKeyReleased((KeyEvent event) -> {
            condition.put(JSONConstants.UNIT, textField.getText());
        });
    }

    private HBox createValuePanel(JSONObject condition) {
        HBox horizontalBox = new HBox();
        horizontalBox.setPrefHeight(PANEL_HEIGHT);

        Label label = new Label(UIConstants.VALUE);
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);

        propertyValue = new TextField();
        if (condition == null) {
            propertyValue.setText("");
        } else {
            setValueKeyPressedListener(propertyValue, condition);
            Object obj = condition.get(JSONConstants.VALUE);
            setValueAsText(obj, propertyValue);
        }

        horizontalBox.getChildren().add(propertyValue);

        counter++;
        paneCounter++;

        return horizontalBox;
    }

    private HBox createFromPanel(JSONObject condition) {
        HBox horizontalBox = new HBox();
        horizontalBox.setPrefHeight(PANEL_HEIGHT);

        Label label = new Label(UIConstants.FROM);
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);

        propertyFromValue = new TextField();
        if (condition == null) {
            propertyFromValue.setText("");
        } else {
            JSONObject value = condition.getJSONObject(JSONConstants.VALUE)
                    .getJSONObject(JSONConstants.FROM);
            setValueKeyPressedListener(propertyFromValue, value);
            Object obj = value.get(JSONConstants.VALUE);
            setValueAsText(obj, propertyFromValue);
        }

        horizontalBox.getChildren().add(propertyFromValue);

        counter++;
        paneCounter++;

        return horizontalBox;
    }

    private HBox createToPanel(JSONObject condition) {
        HBox horizontalBox = new HBox();
        horizontalBox.setPrefHeight(PANEL_HEIGHT);

        Label label = new Label(UIConstants.TO);
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);

        propertyToValue = new TextField();
        if (condition == null) {
            propertyToValue.setText("");
        } else {
            JSONObject value = condition.getJSONObject(JSONConstants.VALUE)
                    .getJSONObject(JSONConstants.TO);
            setValueKeyPressedListener(propertyToValue, value);
            Object obj = value.get(JSONConstants.VALUE);
            setValueAsText(obj, propertyToValue);
        }

        horizontalBox.getChildren().add(propertyToValue);

        counter++;
        paneCounter++;

        return horizontalBox;
    }

    private HBox createUnitPanel(TextField textField, JSONObject condition) {
        HBox horizontalBox = new HBox();
        horizontalBox.setPrefHeight(PANEL_HEIGHT);

        Label label = new Label(UIConstants.UNIT);
        label.setPrefWidth(LABEL_WIDTH);
        horizontalBox.getChildren().add(label);

        if (condition == null) {
            textField.setText("");
        } else {
            textField.setText(condition.has(JSONConstants.UNIT) ? condition.getString(JSONConstants.UNIT) : "");
        }
        setUnitKeyPressedListener(textField, condition);

        horizontalBox.getChildren().add(textField);

        counter++;
        paneCounter++;

        return horizontalBox;
    }

    private void setValueAsText(Object obj, TextField textField) {
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                Object ob = array.get(index);
                if (ob instanceof Double) {
                    builder.append(String.valueOf((double) ob)).append(", ");
                } else if (ob instanceof Integer) {
                    builder.append(String.valueOf((int) ob)).append(", ");
                } else {
                    builder.append((String) ob).append(", ");
                }
            }
            textField.setText(builder.substring(0, builder.lastIndexOf(",")));
        } else if (obj instanceof Double) {
            textField.setText(String.valueOf((double) obj));
        } else if (obj instanceof Integer) {
            textField.setText(String.valueOf((int) obj));
        } else {
            textField.setText((String) obj);
        }
    }

    public void closeDialog() {
        close();
    }

    public void showDialog() {
        show();
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUidata() {
        return uidata;
    }

    public void setUidata(String uidata) {
        this.uidata = uidata;
    }

    public boolean isCreateOutput() {
        return createOutput;
    }
}
