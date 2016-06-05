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
import static com.invicara.tools.rh.utils.JSONConstants.*;
import java.util.StringTokenizer;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
public class InputDialogBox extends Stage {

    private String uidata, schema, testcaseName;
    private final String SEPARATOR = ", ";
    private JSONObject jsonData;
    private TextField testcaseNameTextField, elementTypeAInclude, elementTypeAExclude, elementTypeBInclude, elementTypeBExclude,
            elementCount, propertyName, propertyValue, propertyUnit,
            propertyFromValue, propertyToValue, propertyFromUnit, propertyToUnit,
            level, ratio, threshold;
    int counter = 0;
    private TestcaseNode node;
    boolean fieldEdited;
    private boolean createOutput;
    private TreeView<TestcaseNode> treeView;
    private CheckBoxTreeItem<TestcaseNode> selectedNode;
    private ImageView success, failure, warning;

    public InputDialogBox(String actualsArgument) {
        this.uidata = actualsArgument;

        createGUI();
    }

    public InputDialogBox(TreeView<TestcaseNode> treeView, CheckBoxTreeItem<TestcaseNode> selectedNode) {
        this.treeView = treeView;
        this.selectedNode = selectedNode;
        this.node = (TestcaseNode) selectedNode.getValue();
        this.uidata = node.getActualArgument();
        this.testcaseName = node.getTestcaseName();
        this.schema = node.getParentRule().getSchema();
        this.jsonData = new JSONObject(uidata);

        initialize();
        createGUI();
    }

    private void initialize() {
        testcaseNameTextField = new TextField();
        elementTypeAInclude = new TextField();
        elementTypeAExclude = new TextField();
        elementTypeBInclude = new TextField();
        elementTypeBExclude = new TextField();
        elementCount = new TextField();
        propertyName = new TextField();
        propertyValue = new TextField();
        propertyUnit = new TextField();
        propertyFromValue = new TextField();
        propertyToValue = new TextField();
        propertyFromUnit = new TextField();
        propertyToUnit = new TextField();
        level = new TextField();
        ratio = new TextField();
        threshold = new TextField();

        success = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/success.png")));
        failure = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/failure.png")));
        warning = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/warning.png")));
    }

    private void createGUI() {
        if (uidata != null && !uidata.isEmpty()) {
            fieldEdited = false;
            counter = 0;
            VBox verticalPanel = new VBox();
            HBox panel;
            panel = createPanel(jsonData, "Testcase Name", testcaseName, testcaseNameTextField);
            verticalPanel.getChildren().add(panel);
            counter++;
            if (jsonData.has(ELEMENT_TYPES)) {
                createElement(verticalPanel, "Element Types", ELEMENT_TYPES, elementTypeAInclude, elementTypeAExclude);
            } else if (jsonData.has(ELEMENT_TYPES_A) && jsonData.has(ELEMENT_TYPES_B)) {
                createElement(verticalPanel, "Element A", ELEMENT_TYPES_A, elementTypeAInclude, elementTypeAExclude);

                createElement(verticalPanel, "Element B", ELEMENT_TYPES_B, elementTypeBInclude, elementTypeBExclude);
            } else if (jsonData.has(ELEMENT_TYPES_AGGREGATED) && jsonData.has(ELEMENT_TYPES_AGGREGATOR)) {
                createElement(verticalPanel, "Element Aggregated", ELEMENT_TYPES_AGGREGATED, elementTypeAInclude, elementTypeAExclude);

                createElement(verticalPanel, "Element Aggregator", ELEMENT_TYPES_AGGREGATOR, elementTypeBInclude, elementTypeBExclude);
            } else if (jsonData.has(ELEMENT_TYPES_SPATIAL_STRUCTURE) && jsonData.has(ELEMENT_TYPES_EQUIPMENT)) {
                createElement(verticalPanel, "Element Spatial Structure", ELEMENT_TYPES_SPATIAL_STRUCTURE, elementTypeAInclude, elementTypeAExclude);

                createElement(verticalPanel, "Element Equipment", ELEMENT_TYPES_EQUIPMENT, elementTypeBInclude, elementTypeBExclude);
            }

            if (jsonData.has(PROPERTY_NAME)) {
                panel = createPanel(jsonData, "Property", PROPERTY_NAME, propertyName);
                verticalPanel.getChildren().add(panel);
                counter++;
            }

            if (jsonData.has(PROPERTY_VALUE)) {
                Object object = jsonData.getJSONObject(PROPERTY_VALUE).get(VALUE);
                if (object instanceof JSONObject) {
                    JSONObject subObject = (JSONObject) object;
                    VBox vPanel = new VBox();
                    vPanel.getChildren().add(new Label("From"));
                    vPanel.getChildren().add(createPropertyValuePanel(subObject, FROM, propertyFromValue, propertyFromUnit));
                    vPanel.getChildren().add(new Label("To"));
                    vPanel.getChildren().add(createPropertyValuePanel(subObject, TO, propertyToValue, propertyToUnit));
                    verticalPanel.getChildren().add(vPanel);
                } else {
                    verticalPanel.getChildren().add(createPropertyValuePanel(jsonData, "", propertyValue, propertyUnit));
                }
            }
            if (jsonData.has(ELEMENT_COUNT)) {
                Object object = jsonData.getJSONObject(ELEMENT_COUNT).get(VALUE);
                if (object instanceof JSONObject) {
                    JSONObject subObject = (JSONObject) object;
                    VBox vPanel = new VBox();

                    if (subObject.has(FROM)) {
                        vPanel.getChildren().add(new Label("From"));
                        vPanel.getChildren().add(createPropertyValuePanel(subObject, FROM, propertyFromValue, propertyFromUnit));
                    }
                    if (subObject.has(TO)) {
                        vPanel.getChildren().add(new Label("To"));
                        vPanel.getChildren().add(createPropertyValuePanel(subObject, TO, propertyToValue, propertyToUnit));
                    }
                    verticalPanel.getChildren().add(vPanel);
                }
            }
            if (jsonData.has(LEVEL)) {
                level = new TextField();
                panel = createPanel(jsonData, "Level", LEVEL, level);
                verticalPanel.getChildren().add(panel);
                counter++;
            }
            if (jsonData.has(RATIO_A2B)) {
                ratio = new TextField();
                panel = createPanel(jsonData, "Ratio A to B", RATIO_A2B, ratio);
                verticalPanel.getChildren().add(panel);
                counter++;
            }
            if (jsonData.has(THRESHOLD)) {
                threshold = new TextField();
                panel = createPanel(jsonData, "Threshold", THRESHOLD, threshold);
                verticalPanel.getChildren().add(panel);
                counter++;
            }

            if ("CheckMultipleProperties".equals(node.getRuleName())) {
                HBox conditionPanel = new HBox();
                 conditionPanel.setPrefHeight(30);
                conditionPanel.setPadding(new Insets(2, 5, 2, 5));
                Button conditions = new Button("Conditions +");
                conditions.setPrefWidth(100);
                conditions.setOnMouseClicked((MouseEvent event) -> {
                    MultiConditionsDialogBox ruleConditionsDialogBox = new MultiConditionsDialogBox(selectedNode);
                    ruleConditionsDialogBox.showDialog();
                });
                conditionPanel.getChildren().add(conditions);
                verticalPanel.getChildren().add(conditionPanel);
                counter++;
            }

            JSONObject output = new JSONObject(node.getExpectedOutput() == null ? "{}" : node.getExpectedOutput());
            VBox outputPanel = new VBox();
            Label label;

            if (output.has(TOTAL_ELEMENTS)) {
                HBox totalElementPanel = new HBox();
                totalElementPanel.setPrefHeight(30);
                totalElementPanel.setPadding(new Insets(2, 5, 2, 5));
                label = new Label("Total Elements");
                label.setPrefWidth(100);
                Label totalElement = new Label("Total Elements");
                totalElement.setText(String.valueOf(output.getInt(TOTAL_ELEMENTS)));
                totalElement.setPrefWidth(100);
                totalElementPanel.getChildren().addAll(label, totalElement);
                outputPanel.getChildren().add(totalElementPanel);
                counter++;
            }

            if (output.has(NUM_ELEMENTS)) {
                HBox issuesPanel = new HBox();
                issuesPanel.setPrefHeight(30);
                issuesPanel.setPadding(new Insets(2, 5, 2, 5));
                label = new Label("Total Issues");
                label.setPrefWidth(100);
                Label issues = new Label();
                issues.setText(String.valueOf(output.getInt(NUM_ELEMENTS)));
                issues.setPrefWidth(100);
                issuesPanel.getChildren().addAll(label, issues);
                outputPanel.getChildren().add(issuesPanel);
                counter++;
            }

            if (output.has(ISSUES)) {

                HBox statusPanel = new HBox();
                statusPanel.setPrefHeight(30);
                statusPanel.setPadding(new Insets(2, 5, 2, 5));
                label = new Label("Status");
                label.setPrefWidth(100);
                Label status = new Label();
                try {
                    int statusValue = output.getJSONArray(ISSUES).getJSONObject(0).getInt(STATUS);
                    status.setGraphic(statusValue == 1 ? success : (statusValue == 2 ? failure : warning));
                    status.setText(String.valueOf(statusValue));
                } catch (JSONException e) {
                    status.setText(String.valueOf(0));
                }

                status.setPrefWidth(100);
                statusPanel.getChildren().addAll(label, status);
                outputPanel.getChildren().add(statusPanel);
                counter++;
            }

            if (output.has(ISSUES)) {
                HBox messagePanel = new HBox();
                messagePanel.setPrefHeight(50);
                messagePanel.setPadding(new Insets(2, 5, 2, 5));
                label = new Label("Message");
                label.setPrefWidth(100);
                Label message = new Label();
                try {
                    int statusValue = output.getJSONArray(ISSUES).getJSONObject(0).getInt(DESCRIPTION);
                    message.setText(String.valueOf(statusValue) + " : " + output.getJSONArray(ISSUES).getJSONObject(0).getString(DESCRIPTION_MSG));
                } catch (JSONException e) {
                    message.setText(String.valueOf(0));
                }

                message.setWrapText(true);
                message.setPrefWidth(220);
                messagePanel.getChildren().addAll(label, message);
                outputPanel.getChildren().add(messagePanel);
                counter++;
            }

            if (output.has(WORKFLOW_RUNTIME)) {
                HBox messagePanel = new HBox();
                messagePanel.setPrefHeight(50);
                messagePanel.setPadding(new Insets(2, 5, 2, 5));
                label = new Label("Workflow Runtime : ");
                label.setPrefWidth(100);
                Label message = new Label();
                long workflowTime = output.getLong(WORKFLOW_RUNTIME);
                message.setText(String.valueOf(workflowTime) + " ms");
                message.setPrefWidth(100);
                messagePanel.getChildren().addAll(label, message);
                outputPanel.getChildren().add(messagePanel);
                counter++;
            }

            verticalPanel.getChildren().add(outputPanel);

//            final CheckBox validate = new CheckBox("Validate");
//            verticalPanel.getChildren().add(validate);
            HBox buttonPanel = new HBox();
            Button jsonBtn = new Button("JSON");
            jsonBtn.setPrefWidth(60);
            jsonBtn.setOnAction((ActionEvent event) -> {
                uidata = selectedNode.getValue().getActualArgument();
                new JsonDialogBox(true, this,
                        uidata.replaceAll("\\n", "")
                        .replaceAll("\\{", "\\{\n")
                        .replaceAll(",", ",\n")
                        .replaceAll("\\}", "\n\\}"))
                        .showDialog();
            });
            buttonPanel.getChildren().add(jsonBtn);

            Button okBtn = new Button("OK");
            okBtn.setPrefWidth(60);
            okBtn.setOnAction((ActionEvent event) -> {
//                node.setActualArgument(jsonData.toString());
                node.setTestcaseName(testcaseNameTextField.getText());
                selectedNode.setValue(node);
                treeView.getSelectionModel().select(selectedNode);

                Event.fireEvent(selectedNode,
                        new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(),
                                selectedNode, node));

                close();
            });
            buttonPanel.getChildren().add(okBtn);

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setPrefWidth(60);
            cancelBtn.setOnAction((ActionEvent event) -> {
                close();
            });
            buttonPanel.getChildren().add(cancelBtn);

            buttonPanel.setAlignment(Pos.CENTER_RIGHT);
            buttonPanel.setSpacing(10);
            buttonPanel.setPadding(new Insets(5, 10, 5, 5));

            verticalPanel.getChildren().add(buttonPanel);
            verticalPanel.setPadding(new Insets(5, 10, 5, 5));

            setWidth(390);
            if (counter == 0) {
                setHeight(100);
            } else {
                setHeight(counter * 40 + 80);
            }
            setTitle("Input DialogBox");
            initModality(Modality.APPLICATION_MODAL);
            initStyle(StageStyle.UTILITY);
            setScene(new Scene(verticalPanel));
        }
    }

    private void createElement(VBox verticalPanel, String label, String elementType, TextField textFieldInclude, TextField textFieldExclude) throws JSONException {
        HBox panel = new HBox();
        panel.getChildren().add(new Label(label));
        verticalPanel.getChildren().add(panel);
        panel = createPanel(jsonData, "Include", elementType, textFieldInclude);
        verticalPanel.getChildren().add(panel);
        panel = createPanel(jsonData, "Exclude", elementType, textFieldExclude);
        verticalPanel.getChildren().add(panel);
        counter = counter + 2;
    }

    void updateUI(String json) {
        uidata = json;
        JSONObject root = new JSONObject(json);
        if (root.has(ELEMENT_TYPES) || root.has(ELEMENT_TYPES_AGGREGATED) || root.has(ELEMENT_TYPES_AGGREGATOR)
                || root.has(ELEMENT_TYPES_SPATIAL_STRUCTURE) || root.has(ELEMENT_TYPES_EQUIPMENT)) {
            if (root.has(ELEMENT_TYPES)) {
                setTextValueForElement(root, ELEMENT_TYPES, elementTypeAInclude, elementTypeAExclude);
            }

            if (root.has(ELEMENT_TYPES_AGGREGATED) || root.has(ELEMENT_TYPES_AGGREGATOR)) {
                setTextValueForElement(root, ELEMENT_TYPES_AGGREGATED, elementTypeAInclude, elementTypeAExclude);

                setTextValueForElement(root, ELEMENT_TYPES_AGGREGATOR, elementTypeBInclude, elementTypeBExclude);
            }

            if (root.has(ELEMENT_TYPES_SPATIAL_STRUCTURE) || root.has(ELEMENT_TYPES_EQUIPMENT)) {
                setTextValueForElement(root, ELEMENT_TYPES_SPATIAL_STRUCTURE, elementTypeAInclude, elementTypeAExclude);

                setTextValueForElement(root, ELEMENT_TYPES_EQUIPMENT, elementTypeBInclude, elementTypeBExclude);
            }

            if (root.has(PROPERTY_NAME)) {
                setTextValue(root, PROPERTY_NAME, propertyName);
            }

            if (root.has(PROPERTY_VALUE)) {
                Object obj = root.getJSONObject(PROPERTY_VALUE).get(VALUE);
                if (obj instanceof JSONArray) {
                    setValue(obj, propertyValue);
                } else if (obj instanceof JSONObject) {
                    JSONObject subObj = (JSONObject) obj;
                    obj = subObj.getJSONObject(FROM).get(VALUE);
                    setValue(obj, propertyFromValue);
                    if (subObj.getJSONObject(FROM).has(UNIT)) {
                        obj = subObj.getJSONObject(FROM).get(UNIT);
                        setValue(obj, propertyFromUnit);
                    }
                    obj = subObj.getJSONObject(TO).get(VALUE);
                    setValue(obj, propertyToValue);
                    if (subObj.getJSONObject(TO).has(UNIT)) {
                        obj = subObj.getJSONObject(TO).get(UNIT);
                        setValue(obj, propertyToUnit);
                    }
                } else {
                    setValue(obj, propertyValue);
                }
            }

            if (root.has(ELEMENT_COUNT)) {
                setTextValue(root, ELEMENT_COUNT, elementCount);
            }
            if (root.has(LEVEL)) {
                setTextValue(root, LEVEL, level);
            }
            if (root.has(RATIO_A2B)) {
                setTextValue(root, RATIO_A2B, ratio);
            }
            if (root.has(THRESHOLD)) {
                setTextValue(root, THRESHOLD, threshold);
            }
        } else if (root.has(ELEMENT_TYPES_A) && root.has(ELEMENT_TYPES_B)) {
            setTextValueForElement(root, ELEMENT_TYPES_A, elementTypeAInclude, elementTypeAExclude);
            setTextValueForElement(root, ELEMENT_TYPES_B, elementTypeBInclude, elementTypeBExclude);
        }
    }

    private void updateJSONValue(JSONObject value, TextField textField, String key) throws NumberFormatException, JSONException {
        String jsonValue = textField.getText();
        try {
            value.getJSONObject(key).put(VALUE, Integer.parseInt(jsonValue));
        } catch (NumberFormatException e) {
            try {
                value.getJSONObject(key).put(VALUE, Double.parseDouble(jsonValue));
            } catch (NumberFormatException ex) {
                value.getJSONObject(key).put(VALUE, jsonValue);
            }
        }
    }

    private void setTextValueForElement(JSONObject root, String key, TextField textFieldInclude, TextField textFieldExclude) throws JSONException {
        String value = "";
        JSONObject data = root.getJSONObject(key).getJSONObject(VALUE);
        JSONArray array = data.getJSONObject(INCLUDE_LIST).getJSONArray(VALUE);
        for (int index = 0; index < array.length(); index++) {
            value += array.getString(index) + SEPARATOR;
        }
        value = value.substring(0, value.lastIndexOf(SEPARATOR));
        textFieldInclude.setText(value);

        if (data.has(EXCLUDE_LIST)) {
            array = data.getJSONObject(EXCLUDE_LIST).getJSONArray(VALUE);
            value = "";
            for (int index = 0; index < array.length(); index++) {
                value += array.getString(index) + SEPARATOR;
            }
            value = value.substring(0, value.lastIndexOf(SEPARATOR));
            textFieldExclude.setText(value);
        }
    }

    private void setTextValue(JSONObject root, String key, TextField textFieldInclude) throws JSONException {
        String value = String.valueOf(root.getJSONObject(key).getString(VALUE));
        textFieldInclude.setText(value);

    }

    private VBox createPropertyValuePanel(JSONObject jsonData, String key, TextField valueTextfield, TextField unitTextfield) throws JSONException {
        VBox verticalPanel = new VBox();
        JSONObject tempJson;
        ObjectBinding<JSONObject> jsonValueBinding = bindingForValueTextField(valueTextfield, jsonData, key);
        valueTextfield.setOnKeyReleased((KeyEvent event) -> {
            if (jsonData.has(key)) {
                jsonData.getJSONObject(PROPERTY_VALUE).getJSONObject(VALUE).getJSONObject(key)
                        .put(VALUE, jsonValueBinding.get());
            } else {
                jsonData.put(VALUE, jsonValueBinding.get());
            }
        });
        if (jsonData.has(key)) {
            tempJson = addValue(verticalPanel, jsonData, valueTextfield, key);
        } else {
            tempJson = addValue(verticalPanel, jsonData, valueTextfield, PROPERTY_VALUE);
        }

        ObjectBinding<JSONObject> jsonUnitBinding = bindingForValueUnit(unitTextfield, jsonData);
        unitTextfield.setOnKeyReleased((KeyEvent event) -> {
            if (jsonData.has(key)) {
                jsonData.getJSONObject(PROPERTY_VALUE).getJSONObject(VALUE).getJSONObject(key)
                        .put(UNIT, jsonUnitBinding.get().getString(UNIT));
            } else {
                jsonData.getJSONObject(PROPERTY_VALUE).put(UNIT, jsonUnitBinding.get().getString(UNIT));
            }
        });
        addUnit(verticalPanel, tempJson, unitTextfield);
        return verticalPanel;
    }

    private ObjectBinding<JSONObject> bindingForValueTextField(TextField valueTextfield, JSONObject jsonData, String key) {
        ObjectBinding<JSONObject> jsonBinding = new ObjectBinding<JSONObject>() {
            {
                bind(valueTextfield.textProperty());
            }

            @Override
            protected JSONObject computeValue() {
                if (key.isEmpty()) {
                    String jsonValue = valueTextfield.getText();
                    try {
                        jsonData.put(VALUE, Integer.parseInt(jsonValue));
                    } catch (NumberFormatException e) {
                        try {
                            jsonData.put(VALUE, Double.parseDouble(jsonValue));
                        } catch (NumberFormatException ex) {
                            jsonData.put(VALUE, jsonValue);
                        }
                    }
                } else {
                    updateJSONValue(jsonData, valueTextfield, key);
                }
                return jsonData;
            }
        };
        return jsonBinding;
    }

    private ObjectBinding<JSONObject> bindingForValueUnit(TextField unitTextfield, JSONObject jsonData) {
        ObjectBinding<JSONObject> jsonBinding = new ObjectBinding<JSONObject>() {
            {
                bind(unitTextfield.textProperty());
            }

            @Override
            protected JSONObject computeValue() {
                jsonData.put(UNIT, unitTextfield.getText());
                return jsonData;
            }
        };
        return jsonBinding;
    }

    private JSONObject addValue(VBox verticalPanel, JSONObject jsondata, TextField textField, String key) throws JSONException {
        HBox hPanel;
        Label label;
        JSONObject tempJson;
        hPanel = new HBox();
        hPanel.setPrefHeight(30);
        hPanel.setPadding(new Insets(2, 5, 2, 5));
        label = new Label("Value");
        label.setPrefWidth(100);
        hPanel.getChildren().add(label);

        tempJson = jsondata.getJSONObject(key);
        textField.setPrefWidth(250);
        setValue(tempJson.get(VALUE), textField);
        hPanel.getChildren().add(textField);
        verticalPanel.getChildren().add(hPanel);
        counter++;
        return tempJson;
    }

    private void addUnit(VBox verticalPanel, JSONObject tempJson, TextField textField) throws JSONException {
        HBox hPanel;
        Label label;
        String value;
        hPanel = new HBox();
        hPanel.setPrefHeight(30);
        hPanel.setPadding(new Insets(2, 5, 2, 5));
        label = new Label("Unit");
        label.setPrefWidth(100);
        hPanel.getChildren().add(label);
        if (tempJson.has(UNIT) && !tempJson.isNull(UNIT)) {
            value = tempJson.getString(UNIT);
            textField.setText(value.isEmpty() ? "" : value);
        }
        textField.setPrefWidth(250);
        hPanel.getChildren().add(textField);
        verticalPanel.getChildren().add(hPanel);
        counter++;
    }

    private void setValue(Object object, TextField textField) throws JSONException {
        if (object instanceof String) {
            textField.setText((String) object);
        } else if (object instanceof Integer) {
            textField.setText(String.valueOf((Integer) object));
        } else if (object instanceof Double) {
            textField.setText(String.valueOf((Double) object));
        } else if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                Object obj = array.get(index);
                if (obj instanceof String) {
                    builder.append(obj);
                } else if (obj instanceof Integer) {
                    builder.append(String.valueOf((Integer) obj));
                } else if (obj instanceof Double) {
                    builder.append(String.valueOf((Double) obj));
                }
                builder.append("|");
            }
            textField.setText(builder.substring(0, builder.lastIndexOf("|")));
        }
    }

    private HBox createPanel(JSONObject jsondata, String label, String key, TextField textField) throws JSONException {
        HBox hPanel = new HBox();
        hPanel.setPrefHeight(30);
        hPanel.setPadding(new Insets(2, 5, 2, 5));
        Label pLabel = new Label(label);
        pLabel.setPrefWidth(100);

        textField.setPrefWidth(250);
        switch (key) {
            case THRESHOLD:
            case RATIO_A2B:
            case LEVEL:
                configureOtherTextField(textField, jsondata, key);
                break;
            case ELEMENT_TYPES:
            case ELEMENT_TYPES_A:
            case ELEMENT_TYPES_B:
            case ELEMENT_TYPES_AGGREGATED:
            case ELEMENT_TYPES_AGGREGATOR:
            case ELEMENT_TYPES_SPATIAL_STRUCTURE:
            case ELEMENT_TYPES_EQUIPMENT:
                configureElementTextField(label, textField, jsondata, key);
                break;
            case PROPERTY_NAME:
                configurePropertyTextField(textField, jsondata, key);
                break;
            default:
                textField.setText(key);
        }

        hPanel.getChildren().addAll(pLabel, textField);
        return hPanel;
    }

    private void configureElementTextField(String label, TextField textField, JSONObject jsondata, String key) throws JSONException {
        JSONObject data = jsondata.getJSONObject(key).getJSONObject(VALUE);
        JSONArray array = null;
        if (label.equals("Include")) {
            array = data.getJSONObject(INCLUDE_LIST).getJSONArray(VALUE);
        } else if (label.equals("Exclude") && data.has(EXCLUDE_LIST)) {
            array = data.getJSONObject(EXCLUDE_LIST).getJSONArray(VALUE);
        }

        if (array == null) {
            return;
        }
        String value = "";
        for (int index = 0; index < array.length(); index++) {
            value += array.getString(index) + ",";
        }
        value = value.substring(0, value.lastIndexOf(","));
        textField.setText(value);
        ObjectBinding<JSONObject> jsonBinding = bindingForElement(textField, jsondata.getJSONObject(key));
        textField.setOnKeyReleased((KeyEvent event) -> {
            JSONObject tempData = jsonData.getJSONObject(key).getJSONObject(VALUE);
            if (label.equals("Include")) {
                tempData.put(INCLUDE_LIST, jsonBinding.get());
            } else if (label.equals("Exclude") && data.has(EXCLUDE_LIST)) {
                tempData.put(EXCLUDE_LIST, jsonBinding.get());
            }
        });
    }

    private void configurePropertyTextField(TextField textField, JSONObject jsondata, String key) throws JSONException {
        textField.setText(jsondata.getJSONObject(key).getString(VALUE));
        ObjectBinding<JSONObject> jsonBinding = bindingForProperty(textField, jsondata.getJSONObject(key));
        textField.setOnKeyReleased((KeyEvent event) -> {
            jsonData.put(key, jsonBinding.get());
        });
    }

    private ObjectBinding<JSONObject> bindingForElement(TextField textField, JSONObject jsondata) {
        ObjectBinding<JSONObject> jsonBinding = new ObjectBinding<JSONObject>() {
            {
                bind(textField.textProperty());
            }

            @Override
            protected JSONObject computeValue() {
                String data = textField.getText();
                JSONArray array = new JSONArray();
                StringTokenizer tokenizer = new StringTokenizer(data, SEPARATOR);
                while (tokenizer.hasMoreTokens()) {
                    array.put(tokenizer.nextToken());
                }
                jsondata.put(VALUE, array);

                return jsondata;
            }
        };
        return jsonBinding;
    }

    private ObjectBinding<JSONObject> bindingForProperty(TextField textField, JSONObject jsondata) {
        ObjectBinding<JSONObject> jsonBinding = new ObjectBinding<JSONObject>() {
            {
                bind(textField.textProperty());
            }

            @Override
            protected JSONObject computeValue() {
                jsondata.put(VALUE, textField.getText());

                return jsondata;
            }
        };
        return jsonBinding;
    }

    private void configureOtherTextField(TextField textField, JSONObject jsondata, String key) throws JSONException {
        textField.setText(String.valueOf(jsondata.getJSONObject(key).getInt(VALUE)));
        ObjectBinding<JSONObject> jsonBinding = bindingOtherTextField(textField, jsondata, key);
        textField.setOnKeyReleased((KeyEvent event) -> {
            jsonData.put(key, jsonBinding.get());
        });
    }

    private ObjectBinding<JSONObject> bindingOtherTextField(TextField textField, JSONObject jsondata, String key) {
        ObjectBinding<JSONObject> jsonBinding = new ObjectBinding<JSONObject>() {
            {
                bind(textField.textProperty());
            }

            @Override
            protected JSONObject computeValue() {
                jsondata.getJSONObject(key).put(VALUE, Integer.parseInt(textField.getText()));
                return jsondata;
            }
        };
        return jsonBinding;
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

    public TestcaseNode getNode() {
        return node;
    }

    public boolean isCreateOutput() {
        return createOutput;
    }
}
