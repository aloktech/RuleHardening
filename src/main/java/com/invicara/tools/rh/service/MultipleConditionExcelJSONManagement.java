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
package com.invicara.tools.rh.service;

import static com.invicara.tools.rh.utils.JSONConstants.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class MultipleConditionExcelJSONManagement implements ExcelJSONManagement {

    static int colCounter = 0, modelIndex = increment(), ruleIndex = increment(), descriptionIndex = increment(),
            elementIncludeIndex = increment(), elementExcludeIndex = increment(), conditionsIndex = increment(),
            expressionsValueIndex = increment(), expressionsIndex = increment(), operatorIndex = increment(), propertyIndex = increment(),
            valueIndex = increment(), fromIndex = increment(), toIndex = increment(),
            unitIndex = increment(), expectedStatusIndex = increment(), totalElementsIndex = increment(), issueElementsIndex = increment(),
            statusIndex = increment(), descriptorIndex = increment(), descriptorMsgIndex = increment(),
            runTimeFromDBIndex = increment(), workflowExecutionTimeIndex = increment(), ruleExecutionTimeIndex = increment();

    static Sheet testdataSheet, referenceSheet;
    static String ruleName, testCaseName;
    public int rowCounter;

    public MultipleConditionExcelJSONManagement() {
        rowCounter = 4;
    }

    public JSONArray excelToJSON(String excelFilePath) throws IOException, FileNotFoundException, InvalidFormatException {
        MultipleConditionExcelJSONManagement mgmt = new MultipleConditionExcelJSONManagement();
        Workbook wb = mgmt.openExcelFile(excelFilePath);
        JSONArray testCases = new JSONArray();
        Sheet sheet = wb.getSheet("testdata");
        int rowCount = sheet.getLastRowNum();
        rowCounter = 4;
        for (int p = 4; p <= rowCount; p++) {
            mgmt.importFromExcel(sheet, rowCounter, testCases);
        }
        rowCounter = 4;

        return testCases;
    }

    public void importFromExcel(Sheet sheet, int p, JSONArray testCases) throws JSONException {
        JSONObject testCase;
        JSONObject value;
        Row row = sheet.getRow(rowCounter);
        if (row != null) {
            testCase = new JSONObject();
            addModel(row, testCase);

            addRuleName(row, testCase);

            addRuleDescription(row, testCase);

            value = new JSONObject();
            testCase.put(ACTUAL_ARGUMENT, value);

            addElement(row, value);

            addExpressionValue(row, value);

//            addConditions(row, value);
            addExpression(row, value);

            addOperator(row, value);

            addExpectedStatus(row, testCase);

            value = new JSONObject();
            addTotalElements(row, value);

            addNumElements(row, value);

            addIssues(row, value);

            testCase.put(EXPECTED_OUTPUT, value);

            addRuntime(row, testCase);

            addWorkflowRuntime(row, testCase);

            addRuleExecutionTime(row, testCase);

            testCases.put(testCase);
        }
    }

    public void jsonToExcel(String testCasesJsonFilePath, String excelFilePath) throws JSONException, IOException, InvalidFormatException {

        String data = readFile(testCasesJsonFilePath);
        JSONArray testCases = new JSONArray(data);
        JSONObject testCase;
        Workbook workbook = openExcelFileWithCleanSheet(excelFilePath);
        rowCounter = 4;
        for (int p = 0; p < testCases.length(); p++) {
            try {
                testCase = testCases.getJSONObject(p);
                exportToExcel(workbook, 1, testCase);
            } catch (JSONException e) {

            }
        }
        rowCounter = 4;
        saveExcelFile(excelFilePath, workbook);
    }

    @Override
    public void exportToExcel(Workbook workbook, int i, JSONObject testCase) throws JSONException {
        testdataSheet = workbook.getSheet("testdata");
        referenceSheet = workbook.getSheet("reference");
        Row row = testdataSheet.createRow(rowCounter);

        extractModelName(testCase, row);

        extractRuleName(testCase, row);

        extractRuleDescription(testCase, row);

        extractExpectedStatus(testCase, row);

        JSONObject actualArgument = testCase.getJSONObject(ACTUAL_ARGUMENT);
        extractElement(actualArgument, row);

        extractExpressionValue(actualArgument, row);

        extractConditions(actualArgument, row);

        extractExpression(actualArgument, row);

        extractOperator(actualArgument, row);

        JSONObject expectedOutput = testCase.getJSONObject(EXPECTED_OUTPUT);
        extractOutput(expectedOutput, row);

        extractRuntime(testCase, row);

        extractWorkflowExecutionTime(testCase, row);

        extractRuleExecutionTime(testCase, row);
    }

    @Override
    public void saveAsNewExcelFile(String filePath, Workbook wb) throws IOException, FileNotFoundException {
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            wb.write(output);
        }
    }

    public void updateExcelFile(String filePath, Workbook wb) throws IOException, FileNotFoundException {
        try {
            Workbook workbook = openExcelFile(filePath);
            cleanSheet(workbook, "testdata");
        } catch (InvalidFormatException ex) {
            Logger.getLogger(MultipleConditionExcelJSONManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            wb.write(output);
        }
    }

    private Workbook cleanSheet(Workbook workbook, String name) {
        Sheet sheet = workbook.getSheet(name);
        int count = sheet.getLastRowNum();
        for (int p = 3; p < count; p++) {
            Row row = sheet.getRow(p);
            Iterator<Cell> itr = row.cellIterator();
            while (itr.hasNext()) {
                Cell cell = itr.next();
                cell.setCellValue("");
            }
        }
        return workbook;
    }

    private static void saveExcelFile(String filePath, Workbook wb) throws IOException, FileNotFoundException {
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            wb.write(output);
        }
    }

    public Workbook openExcelFileWithCleanSheet(String filePath) throws IOException, FileNotFoundException, InvalidFormatException {
        File file = new File(filePath);
        Workbook wb;
        boolean newFile = false;
        if (!file.exists()) {
            wb = new XSSFWorkbook();
            wb = basicConfiguration(wb);
            try (FileOutputStream output = new FileOutputStream(filePath)) {
                wb.write(output);
            }
            newFile = true;
        }

        try (FileInputStream input = new FileInputStream(file)) {
            wb = WorkbookFactory.create(input);
            if (!newFile) {
                wb = cleanSheet(wb, "testdata");
            }
        }
        return wb;
    }

    @Override
    public Workbook openExcelFile(String filePath) throws IOException, FileNotFoundException, InvalidFormatException {
        File file = new File(filePath);
        Workbook wb;
        if (!file.exists()) {
            wb = new XSSFWorkbook();
            try (FileOutputStream output = new FileOutputStream(filePath)) {
                wb.write(output);
            }
        }

        try (FileInputStream input = new FileInputStream(file)) {
            wb = WorkbookFactory.create(input);
        }
        return wb;
    }

    public void setCellValueAsInteger(Row row, int columnIndex, JSONObject json, String key, boolean hidden) {
        if (json.has(key)) {
            Cell cell = row.createCell(columnIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(json.getJSONObject(key).getDouble(VALUE));
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.setHidden(hidden);
            cell.setCellStyle(cellStyle);
        }
    }

    public void setCellValueAsInteger(Row row, int columnIndex, JSONObject data, String key) {
        if (data.has(key)) {
            Cell cell = row.createCell(columnIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(data.getJSONObject(key).getDouble(VALUE));
        }
    }

    private static void extractExpectedStatus(JSONObject actualArgument, Row row) throws JSONException {
        if (actualArgument.has(EXPECTED_STATUS)) {
            Cell cell = row.createCell(expectedStatusIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(actualArgument.getInt(EXPECTED_STATUS));
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.setHidden(true);
            cell.setCellStyle(cellStyle);
        }
    }

    private static void extractWorkflowExecutionTime(JSONObject actualArgument, Row row) throws JSONException {
        if (actualArgument.has(WORKFLOW_RUNTIME)) {
            Cell cell = row.createCell(workflowExecutionTimeIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(actualArgument.getLong(WORKFLOW_RUNTIME));
        }
    }

    private static void extractRuntime(JSONObject actualArgument, Row row) throws JSONException {
        if (actualArgument.has(RUNTIME)) {
            Cell cell = row.createCell(runTimeFromDBIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(actualArgument.getLong(RUNTIME));
        }
    }

    private static void extractRuleExecutionTime(JSONObject actualArgument, Row row) throws JSONException {
        if (actualArgument.has(RULE_RUNTIME)) {
            Cell cell = row.createCell(ruleExecutionTimeIndex);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(actualArgument.getLong(RULE_RUNTIME));
        }
    }

    private static void extractOutput(JSONObject expectedOutput, Row row) throws JSONException {
        JSONObject issue;
        if (expectedOutput.has(TOTAL_ELEMENTS)) {
            createCellValueAsInteger(row, totalElementsIndex, expectedOutput.getInt(TOTAL_ELEMENTS));
        }
        if (expectedOutput.has(NUM_ELEMENTS)) {
            createCellValueAsInteger(row, issueElementsIndex, expectedOutput.getInt(NUM_ELEMENTS));
        }
        JSONArray issues = expectedOutput.getJSONArray(ISSUES);
        for (int q = 0; q < issues.length(); q++) {
            issue = issues.getJSONObject(0);
            if (issue.has(STATUS)) {
                createStatusCellValueAsInteger(row, statusIndex, issue.getInt(STATUS));
            }
            if (issue.has(DESCRIPTION)) {
                createCellValueAsInteger(row, descriptorIndex, issue.getInt(DESCRIPTION));
            }
            if (issue.has(DESCRIPTION_MSG)) {
                extractDescriptorMessage(row, issue);
            }
            if (issue.has(RULE_RUNTIME)) {
                createCellValueAsLong(row, ruleExecutionTimeIndex, issue.getLong(RULE_RUNTIME));
            }
            break;
        }
    }

    private static void extractDescriptorMessage(Row row, JSONObject issue) throws JSONException {
        Cell cell = row.createCell(descriptorMsgIndex);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(issue.getString(DESCRIPTION_MSG));
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setWrapText(true);
        cell.setCellStyle(style);
    }

    private static void extractPropertyValue(JSONObject actual_data, Row row) throws JSONException {
        JSONObject value = null;
        if (actual_data.has(ELEMENT_COUNT)) {
            value = actual_data.getJSONObject(ELEMENT_COUNT);
        } else if (actual_data.has(PROPERTY_VALUE)) {
            value = actual_data.getJSONObject(PROPERTY_VALUE);
        }
        if (value == null) {
            return;
        }
        Object obj = value.get(VALUE);
        if (obj instanceof JSONObject) {
            JSONObject tValue = (JSONObject) obj;
            if (tValue.has(FROM) && tValue.has(TO)) {
                JSONObject fromValue = updateFromAndToValue(tValue, row, FROM, fromIndex);
                if (fromValue.has(UNIT)) {
                    createCellValueAsString(row, unitIndex, fromValue.getString(UNIT));
                }
                JSONObject toValue = updateFromAndToValue(tValue, row, TO, toIndex);
                if (toValue.has(UNIT)) {
                    createCellValueAsString(row, unitIndex, toValue.getString(UNIT));
                }
            } else if (tValue.has(FROM)) {
                JSONObject fromValue = updateFromAndToValue(tValue, row, FROM, fromIndex);
                if (fromValue.has(UNIT)) {
                    createCellValueAsString(row, unitIndex, fromValue.getString(UNIT));
                }
            } else if (tValue.has(TO)) {
                JSONObject toValue = updateFromAndToValue(tValue, row, TO, toIndex);
                if (toValue.has(UNIT)) {
                    createCellValueAsString(row, unitIndex, toValue.getString(UNIT));
                }
            } else {
                obj = tValue.get(VALUE);
                if (obj instanceof String) {
                    createCellValueAsString(row, valueIndex, tValue.getString(VALUE));
                } else if (obj instanceof Number) {
                    createCellValueAsDouble(row, valueIndex, tValue.getDouble(VALUE));
                }
                if (tValue.has(UNIT)) {
                    createCellValueAsString(row, unitIndex, tValue.getString(UNIT));
                }
            }

        } else if (obj instanceof JSONArray) {
            if ("CheckPropertyValueMatchesTermsOrValues".equals(ruleName)) {
                JSONArray array = (JSONArray) obj;
                Workbook wb = row.getSheet().getWorkbook();
                referenceSheet = wb.getSheet("reference");
                Row refRow = referenceSheet.createRow(referenceSheet.getPhysicalNumberOfRows());
                Cell cell;
                cell = refRow.createCell(0);
                cell.setCellValue(ruleName + ":" + testCaseName);
                for (int p = 0; p < array.length(); p++) {
                    Object arrayItem = array.get(p);
                    if (arrayItem instanceof String) {
                        cell = refRow.createCell(p + 1);
                        cell.setCellValue((String) arrayItem);
                    } else if (arrayItem instanceof Integer) {
                        cell = refRow.createCell(p + 1);
                        cell.setCellValue((Integer) arrayItem);
                    } else if (arrayItem instanceof Double) {
                        cell = refRow.createCell(p + 1);
                        cell.setCellValue((Double) arrayItem);
                    }
                }
            } else {
                JSONArray array = (JSONArray) obj;
                Cell cell = row.createCell(valueIndex);
                String data = "";
                for (int p = 0; p < array.length(); p++) {
                    Object arrayItem = array.get(p);
                    if (arrayItem instanceof String) {
                        data += (String) arrayItem+",";
                    } else if (arrayItem instanceof Integer) {
                        data += String.valueOf((Integer) arrayItem)+",";
                    } else if (arrayItem instanceof Double) {
                        data += String.valueOf((Double) arrayItem)+",";
                    }
                }
                cell.setCellValue(data.substring(0, data.lastIndexOf(",")));
            }
        } else if (obj instanceof String) {
            createCellValueAsString(row, valueIndex, value.getString(VALUE));
            if (value.has(UNIT)) {
                createCellValueAsString(row, unitIndex, value.getString(UNIT));
            }
        } else if (obj instanceof Number) {
            if (obj instanceof Integer) {
                createCellValueAsInteger(row, valueIndex, value.getInt(VALUE));
            } else if (obj instanceof Double) {
                createCellValueAsDouble(row, valueIndex, value.getDouble(VALUE));
            }

            if (value.has(UNIT)) {
                createCellValueAsString(row, unitIndex, value.getString(UNIT));
            }
        }
    }

    private static JSONObject updateFromAndToValue(JSONObject tValue, Row row, String type, int columnIndex) throws JSONException {
        Object obj;
        JSONObject value = tValue.getJSONObject(type);
        obj = value.get(VALUE);
        if (obj instanceof String) {
            createCellValueAsString(row, columnIndex, value.getString(VALUE));
        } else if (obj instanceof Number) {
            createCellValueAsDouble(row, columnIndex, value.getDouble(VALUE));
        }
        return value;
    }

    private static void extractPropertyName(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(PROPERTY_NAME)) {
            setStringValueToCell(row, propertyIndex, actual_data.getJSONObject(PROPERTY_NAME).getString(VALUE));
        }
    }

    private static void extractElement(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(ELEMENT_TYPES)) {
            setStringValueToCell(row, elementIncludeIndex, actual_data, ELEMENT_TYPES, INCLUDE_LIST);
            setStringValueToCell(row, elementExcludeIndex, actual_data, ELEMENT_TYPES, EXCLUDE_LIST);
        }
    }

    private static void extractExpressionValue(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(MATCH)) {
            JSONObject match = actual_data.getJSONObject(MATCH);
            Cell cell = row.createCell(expressionsValueIndex);
            cell.setCellValue(match.getBoolean(VALUE));
        }
    }

    private void extractConditions(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(EXPRESSION)) {
            JSONObject expression = actual_data.getJSONObject(EXPRESSION);
            JSONObject expressionValue = expression.getJSONObject(VALUE);
            JSONArray conditions = expressionValue.getJSONObject(CONDITIONS).getJSONArray(VALUE);
            Cell cell = row.createCell(conditionsIndex);
            cell.setCellValue(conditions.length());
        }
    }

    private void extractExpression(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(EXPRESSION)) {
            JSONObject expression = actual_data.getJSONObject(EXPRESSION);
            JSONObject expressionValue = expression.getJSONObject(VALUE);
            JSONObject formula = expressionValue.getJSONObject(FORMULA);
            Cell cell = row.createCell(expressionsIndex);
            cell.setCellValue(formula.getString(VALUE));
        }
    }

    private void extractOperator(JSONObject actual_data, Row row) throws JSONException {
        if (actual_data.has(EXPRESSION)) {
            JSONObject expression = actual_data.getJSONObject(EXPRESSION);
            JSONObject expressionValue = expression.getJSONObject(VALUE);
            JSONObject conditions = expressionValue.getJSONObject(CONDITIONS);
            JSONArray conditionsValue = conditions.getJSONArray(VALUE);
            JSONObject condition;
            Sheet sheet = row.getSheet();
            Cell cell;
            for (int index = 0; index < conditionsValue.length(); index++) {
                condition = conditionsValue.getJSONObject(index);
                if (index > 0) {
                    row = sheet.createRow(rowCounter);
                }
                rowCounter++;
                cell = row.createCell(operatorIndex);
                cell.setCellValue(condition.getJSONObject(OPERATOR).getString(VALUE));

                extractPropertyName(condition, row);

                extractPropertyValue(condition, row);
            }
        }
    }

    private static void setStringValueToCell(Row row, int columnIndex, JSONObject actualData, String elementType, String elementList) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        JSONObject jdata = actualData.getJSONObject(elementType).getJSONObject(VALUE);
        if (jdata.has(elementList)) {
            JSONArray array = jdata.getJSONObject(elementList).getJSONArray(VALUE);
            String data;
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                builder.append(array.getString(index));
                builder.append(",");
            }
            data = builder.toString();
            data = data.substring(0, data.lastIndexOf(","));
            cell.setCellValue(data);
        }
    }

    private static void setStringValueToCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(value);
    }

    private static void extractRuleName(JSONObject testCase, Row row) throws JSONException {
        Cell cell;
        if (testCase.has(RULE_NAME)) {
            cell = row.createCell(ruleIndex);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            String name = testCase.getString(RULE_NAME);
            ruleName = name = name.endsWith("Test") ? name.substring(0, name.indexOf("Test")) : name;
            cell.setCellValue(name);
        }
    }

    private static void extractRuleDescription(JSONObject testCase, Row row) throws JSONException {
        Cell cell;
        if (testCase.has(TESTCASE_NAME)) {
            cell = row.createCell(descriptionIndex);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            testCaseName = testCase.getString(TESTCASE_NAME);
            cell.setCellValue(testCaseName);
        }
    }

    private static void extractModelName(JSONObject testCase, Row row) throws JSONException {
        Cell cell;
        if (testCase.has(MODEL_NAME)) {
            cell = row.createCell(modelIndex);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue(testCase.getString(MODEL_NAME));
        }
    }

    public void excelToJSONFile(String excelFilePath, String testCasesJsonFilePath) throws InvalidFormatException, JSONException, IOException {
        Workbook wb = openExcelFile(excelFilePath);
        JSONArray testCases = new JSONArray();
        Sheet sheet = wb.getSheet("testdata");
        int rowCount = sheet.getLastRowNum();
        for (int p = 4; p <= rowCount; p++) {
            MultipleConditionExcelJSONManagement.this.importFromExcel(sheet, p, testCases);
        }

        writeFile(testCasesJsonFilePath, testCases.toString());
    }

    private static void addRuleDescription(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(descriptionIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
            testCaseName = cell.getStringCellValue();
            testCase.put(TESTCASE_NAME, testCaseName);
        } else {
            testCase.put(TESTCASE_NAME, "");
        }
    }

    private static void addDescriptorMsg(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(descriptorMsgIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
            testCase.put(DESCRIPTION_MSG, cell.getStringCellValue());
        } else {
            testCase.put(DESCRIPTION_MSG, "");
        }
    }

    private static void addDescription(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(descriptorIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(DESCRIPTION, cell.getNumericCellValue());
        } else {
            testCase.put(DESCRIPTION, 0);
        }
    }

    private static void addStatus(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(statusIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(STATUS, cell.getNumericCellValue());
        } else {
            testCase.put(STATUS, 0);
        }
    }

    private static void addNumElements(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(issueElementsIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(NUM_ELEMENTS, cell.getNumericCellValue());
        } else {
            testCase.put(NUM_ELEMENTS, 0);
        }
    }

    private static void addIssues(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        JSONArray array = new JSONArray();
        JSONObject value = new JSONObject();
        cell = row.getCell(issueElementsIndex);
        boolean hasData = false;
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            value.put(ISSUE_ELEMENTS, cell.getNumericCellValue());
            hasData = true;
        }
        cell = row.getCell(statusIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            value.put(STATUS, cell.getNumericCellValue());
            hasData = true;
        }
        cell = row.getCell(descriptorIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            value.put(DESCRIPTION, cell.getNumericCellValue());
            hasData = true;
        }
        cell = row.getCell(descriptorMsgIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
            value.put(DESCRIPTION_MSG, cell.getStringCellValue());
            hasData = true;
        }
        cell = row.getCell(ruleExecutionTimeIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            value.put(RULE_RUNTIME, cell.getNumericCellValue());
            hasData = true;
        }
        if (hasData) {
            array.put(value);
        }
        testCase.put(ISSUES, array);
    }

    private static void addTotalElements(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(totalElementsIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(TOTAL_ELEMENTS, cell.getNumericCellValue());
        } else {
            testCase.put(TOTAL_ELEMENTS, 0);
        }
    }

    private static void addModel(Row row, JSONObject testCase) throws JSONException {
        Cell cell = row.getCell(modelIndex);
        if (cell != null) {
            testCase.put(MODEL_NAME, cell.getStringCellValue());
        } else {
            testCase.put(MODEL_NAME, "");
        }
    }

    private static String addRuleName(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(ruleIndex);
        if (cell != null) {
            ruleName = cell.getStringCellValue();
            testCase.put(RULE_NAME, ruleName);
        } else {
            testCase.put(RULE_NAME, "");
        }
        return ruleName;
    }

    private static void addExpectedStatus(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(expectedStatusIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(EXPECTED_STATUS, cell.getNumericCellValue());
        }
    }

    private static void addRuleExecutionTime(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(ruleExecutionTimeIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(RULE_RUNTIME, cell.getNumericCellValue());
        }
    }

    private static void addWorkflowRuntime(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(workflowExecutionTimeIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(WORKFLOW_RUNTIME, cell.getNumericCellValue());
        }
    }

    private static void addRuntime(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        cell = row.getCell(runTimeFromDBIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            testCase.put(RUNTIME, cell.getNumericCellValue());
        }
    }

    private static void addPropertyValue(Row row, JSONObject testCase, String ruleName) throws JSONException {
        Cell cell;
        JSONObject pvalue;
        boolean value = true, from = true, to = true;
        cell = row.getCell(valueIndex);
        if (cell == null) {
            value = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim().isEmpty()) {
            value = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            value = false;
        }
        cell = row.getCell(fromIndex);
        if (cell == null) {
            from = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim().isEmpty()) {
            from = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            from = false;
        }
        cell = row.getCell(toIndex);
        if (cell == null) {
            to = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim().isEmpty()) {
            to = false;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            to = false;
        }

        if (from && to) {
            pvalue = new JSONObject();
            Cell fromCell, toCell, unitCell;
            fromCell = row.getCell(fromIndex);
            toCell = row.getCell(toIndex);
            unitCell = row.getCell(unitIndex);
            if ((fromCell.getCellType() == Cell.CELL_TYPE_NUMERIC) && (toCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                JSONObject fromValue = new JSONObject();
                fromValue.put(VALUE, fromCell.getNumericCellValue());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    fromValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(FROM, fromValue);

                JSONObject toValue = new JSONObject();
                toCell = row.getCell(toIndex);
                toValue.put(VALUE, toCell.getNumericCellValue());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    toValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(TO, toValue);
            } else if ((fromCell.getCellType() == Cell.CELL_TYPE_STRING) && (toCell.getCellType() == Cell.CELL_TYPE_STRING)) {
                JSONObject fromValue = new JSONObject();
                fromValue.put(VALUE, toCell.getStringCellValue().trim());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    fromValue.put(UNIT, unitCell.getStringCellValue());
                }
                pvalue.put(FROM, fromValue);

                JSONObject toValue = new JSONObject();
                toValue.put(VALUE, toCell.getStringCellValue().trim());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    toValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(TO, toValue);
            }
            JSONObject tvalue = new JSONObject();
            tvalue.put(VALUE, pvalue);
            if ("CheckObjectTypeQuantity".equals(ruleName) || "CheckContainedInSpatialStructure".equals(ruleName) || "CheckAggregatedInAggregator".equals(ruleName)) {
                testCase.put(ELEMENT_COUNT, tvalue);
            } else {
                testCase.put(PROPERTY_VALUE, tvalue);
            }
        } else if (from) {
            pvalue = new JSONObject();
            Cell fromCell, unitCell;
            fromCell = row.getCell(fromIndex);
            unitCell = row.getCell(unitIndex);
            if (fromCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                JSONObject fromValue = new JSONObject();
                fromValue.put(VALUE, fromCell.getNumericCellValue());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    fromValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(FROM, fromValue);
            } else if (fromCell.getCellType() == Cell.CELL_TYPE_STRING) {
                JSONObject fromValue = new JSONObject();
                String strValue = fromCell.getStringCellValue().trim();
                if (strValue.contains(",")) {
                    JSONArray array = new JSONArray();
                    int intData;
                    double doubleData;
                    for (String val : strValue.split(",")) {
                        try {
                            intData = Integer.parseInt(val);
                            array.put(intData);
                        } catch (NumberFormatException e1) {
                            try {
                                doubleData = Double.parseDouble(val);
                                array.put(doubleData);
                            } catch (NumberFormatException e2) {
                                array.put(val);
                            }
                        }
                    }
                } else {
                    fromValue.put(VALUE, strValue);
                }
                fromValue.put(VALUE, strValue);

                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    fromValue.put(UNIT, unitCell.getStringCellValue());
                }
                pvalue.put(FROM, fromValue);
            }
            JSONObject tvalue = new JSONObject();
            tvalue.put(VALUE, pvalue);
            if ("CheckObjectTypeQuantity".equals(ruleName)) {
                testCase.put(ELEMENT_COUNT, tvalue);
            } else {
                testCase.put(PROPERTY_VALUE, tvalue);
            }
        } else if (to) {
            pvalue = new JSONObject();
            Cell toCell, unitCell;
            toCell = row.getCell(toIndex);
            unitCell = row.getCell(unitIndex);
            if (toCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                JSONObject toValue = new JSONObject();
                toCell = row.getCell(toIndex);
                toValue.put(VALUE, toCell.getNumericCellValue());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    toValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(TO, toValue);
            } else if (toCell.getCellType() == Cell.CELL_TYPE_STRING) {
                JSONObject toValue = new JSONObject();
                toValue.put(VALUE, toCell.getStringCellValue().trim());
                if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING && !unitCell.getStringCellValue().trim().isEmpty()) {
                    toValue.put(UNIT, unitCell.getStringCellValue().trim());
                }
                pvalue.put(TO, toValue);
            }
            JSONObject tvalue = new JSONObject();
            tvalue.put(VALUE, pvalue);
            if ("CheckObjectTypeQuantity".equals(ruleName)) {
                testCase.put(ELEMENT_COUNT, tvalue);
            } else {
                testCase.put(PROPERTY_VALUE, tvalue);
            }
        } else if (value) {
            pvalue = new JSONObject();
            cell = row.getCell(valueIndex);
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                pvalue.put(VALUE, cell.getNumericCellValue());
                cell = row.getCell(unitIndex);
                if (cell != null && !cell.getStringCellValue().trim().isEmpty()) {
                    pvalue.put(UNIT, cell.getStringCellValue().trim());
                }
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                String strValue = cell.getStringCellValue().trim();
                if (strValue.contains(",")) {
                    JSONArray array = new JSONArray();
                    int intData;
                    double doubleData;
                    for (String val : strValue.split(",")) {
                        try {
                            doubleData = Double.parseDouble(val);
                            array.put(doubleData);
                        } catch (NumberFormatException e1) {
                            try {
                                intData = Integer.parseInt(val);
                                array.put(intData);
                            } catch (NumberFormatException e2) {
                                array.put(val);
                            }
                        }
                    }
                    pvalue.put(VALUE, array);
                } else {
                    pvalue.put(VALUE, strValue);
                }
                cell = row.getCell(unitIndex);
                if (cell != null && !cell.getStringCellValue().trim().isEmpty()) {
                    pvalue.put(UNIT, cell.getStringCellValue().trim());
                }
            }
            testCase.put(PROPERTY_VALUE, pvalue);
        } else {
            if ("CheckPropertyValueMatchesTermsOrValues".equals(ruleName)) {
                JSONArray array = new JSONArray();
                try {
                    Sheet s = row.getSheet().getWorkbook().getSheet("reference");
                    for (int index = 0; index <= s.getLastRowNum(); index++) {
                        Row r = s.getRow(index);
                        if (r != null) {
                            Iterator<Cell> itr = r.cellIterator();
                            boolean validLine = false;
                            while (itr.hasNext()) {
                                Cell c = itr.next();
                                if (Cell.CELL_TYPE_STRING == c.getCellType() && (ruleName + ":" + testCaseName).equals(c.getStringCellValue())) {
                                    validLine = true;
                                    continue;
                                }
                                if (validLine) {
                                    switch (c.getCellType()) {
                                        case Cell.CELL_TYPE_STRING:
                                            array.put(c.getStringCellValue());
                                            break;
                                        case Cell.CELL_TYPE_NUMERIC:
                                            array.put(c.getNumericCellValue());
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (validLine) {
                                break;
                            }
                        }
                    }
                    pvalue = new JSONObject();
                    pvalue.put(VALUE, array);
                    testCase.put(PROPERTY_VALUE, pvalue);
                } catch (Exception e) {
                    System.out.println("Name " + ruleName + " : " + testCaseName + " " + e.getMessage());
                }
            }
        }
    }

    private void addConditions(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        JSONObject value;
        cell = row.getCell(conditionsIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            value = new JSONObject();
            value.put(VALUE, cell.getNumericCellValue());
            testCase.put(PROPERTY_NAME, value);
        }
    }

    private void addExpressionValue(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        JSONObject value;
        cell = row.getCell(expressionsValueIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            value = new JSONObject();
            value.put(VALUE, cell.getBooleanCellValue());
            testCase.put(MATCH, value);
        }
    }

    private void addExpression(Row row, JSONObject testCase) throws JSONException {
        Cell cell;

        cell = row.getCell(expressionsIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().trim().isEmpty()) {
            JSONObject value = new JSONObject();
            value.put(VALUE, cell.getStringCellValue().trim());

            JSONObject formula = new JSONObject();
            formula.put(FORMULA, value);

            value = new JSONObject();
            value.put(VALUE, formula);

            testCase.put(EXPRESSION, value);
        }
    }

    private void addOperator(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        JSONObject condition;
        double count = 0;
        cell = row.getCell(conditionsIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            count = cell.getNumericCellValue();
        }
        Sheet sheet = row.getSheet();
        JSONArray conditions = new JSONArray();
        for (int index = 0; index < count; index++) {
            row = sheet.getRow(rowCounter);
            rowCounter++;
            condition = new JSONObject();
            cell = row.getCell(operatorIndex);
            if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().trim().isEmpty()) {
                JSONObject operatorValue = new JSONObject();
                operatorValue.put(VALUE, cell.getStringCellValue().trim());

                condition.put(OPERATOR, operatorValue);
            }

            addProperty(row, condition);

            addPropertyValue(row, condition, ruleName);

            conditions.put(condition);
        }
        JSONObject expressionValue, expression, conditionValue;
        if (testCase.has(EXPRESSION)) {
            expression = testCase.getJSONObject(EXPRESSION);
            expressionValue = expression.getJSONObject(VALUE);
        } else {
            expression = new JSONObject();
            expressionValue = new JSONObject();
        }

        if (expressionValue.has(CONDITIONS)) {
            conditionValue = expressionValue.getJSONObject(CONDITIONS);
        } else {
            conditionValue = new JSONObject();
        }

        conditionValue.put(VALUE, conditions);

        expressionValue.put(CONDITIONS, conditionValue);

        expression.put(VALUE, expressionValue);
        testCase.put(EXPRESSION, expression);

    }

    private static void addProperty(Row row, JSONObject testCase) throws JSONException {
        Cell cell;
        JSONObject value;
        cell = row.getCell(propertyIndex);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().trim().isEmpty()) {
            value = new JSONObject();
            value.put(VALUE, cell.getStringCellValue().trim());
            testCase.put(PROPERTY_NAME, value);
        }
    }

    private static void addElement(Row row, JSONObject testCase) throws JSONException {
        if (isStringCellNotEmpty(row, elementIncludeIndex)) {
            createElement(row, testCase, ELEMENT_TYPES, elementIncludeIndex, elementExcludeIndex);
        }

    }

    private static void createElement(Row row, JSONObject testCase, String elementType, int includeIndex, int excludeIndex) throws JSONException {
        JSONObject value = new JSONObject();
        JSONObject listValue = new JSONObject();

        listValue = createElementValue(row, listValue, includeIndex, INCLUDE_LIST);
        listValue = createElementValue(row, listValue, excludeIndex, EXCLUDE_LIST);

        value.put(VALUE, listValue);
        testCase.put(elementType, value);
    }

    private static JSONObject createElementValue(Row row, JSONObject value, int cellIndex, String listType) throws JSONException {
        JSONObject elementList = new JSONObject();
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return value;
        }
        String data = cell.getStringCellValue();
        if (data != null && !data.trim().isEmpty()) {
            JSONArray array = new JSONArray();
            StringTokenizer tokenizer = new StringTokenizer(data, ",|");
            while (tokenizer.hasMoreTokens()) {
                array.put(tokenizer.nextToken());
            }
            elementList.put(VALUE, array);
            value.put(listType, elementList);
        }

        return value;
    }

    private static boolean isStringCellNotEmpty(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return false;
        }
        String data = cell.getStringCellValue();
        if (data == null) {
            return false;
        }
        return !data.trim().isEmpty();
    }

    public void uploadFile(String excelFilePath, String jsonFilePath) throws JSONException, InvalidFormatException, IOException {
        Workbook wb = openExcelFile(excelFilePath);
        Sheet sheet = wb.getSheet("testdata");
        Row row;
        JSONArray rules = new JSONArray(readFile(jsonFilePath));
        for (int p = 0; p < rules.length(); p++) {
            JSONObject rule = rules.getJSONObject(p);
            row = sheet.createRow(p + 3);
            createCellValueAsString(row, modelIndex, "");
            createCellValueAsString(row, ruleIndex, rule.getString("rule"));
        }

        sheet = wb.getSheet("reference");
        for (int p = 0; p < rules.length(); p++) {
            JSONObject rule = rules.getJSONObject(p);
            row = sheet.createRow(p);
            createCellValueAsString(row, modelIndex, "");
            createCellValueAsString(row, ruleIndex, rule.getString("rule"));
        }
        saveExcelFile(excelFilePath, wb);
    }

    private static void writeFile(String filePath, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.append(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultipleConditionExcelJSONManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultipleConditionExcelJSONManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String readFile(String filePath) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultipleConditionExcelJSONManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultipleConditionExcelJSONManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return builder.toString();
    }

    @Override
    public Workbook basicConfiguration(Workbook wb) throws InvalidFormatException, IOException {
        Sheet sheet = wb.getSheet("testdata");
        if (sheet == null) {
            sheet = wb.createSheet("testdata");
        }
        referenceSheet = wb.getSheet("reference");
        if (referenceSheet == null) {
            referenceSheet = wb.createSheet("reference");
        }
        referenceSheet.createRow(0);
        referenceSheet.createRow(1);
        referenceSheet.createRow(2);

        Row row = sheet.createRow(0);
        createHeader(row, modelIndex, "Model\nName");
        createHeader(row, ruleIndex, "Rule");
        createHeader(row, descriptionIndex, "Description");
        createHeader(row, elementIncludeIndex, "Input");
        createHeader(row, expectedStatusIndex, "Expected\nStatus");
        createHeader(row, totalElementsIndex, "Output");

        row = sheet.createRow(1);
        createHeader(row, elementIncludeIndex, "Element");
        createHeader(row, conditionsIndex, "Conditions");
        createHeader(row, expressionsValueIndex, "Expression/nValue");
        createHeader(row, expressionsIndex, "Expression");
        createHeader(row, operatorIndex, "Operator");
        createHeader(row, propertyIndex, "Property");
        createHeader(row, valueIndex, "Property Value");
        createHeader(row, unitIndex, "Unit");
        createHeader(row, totalElementsIndex, "Total\nElements");
        createHeader(row, issueElementsIndex, "Issue\nElements");
        createHeader(row, statusIndex, "Status");
        createHeader(row, descriptorIndex, "Message\nIndex");
        createHeader(row, descriptorMsgIndex, "Message\nDescription");
        createHeader(row, runTimeFromDBIndex, "Runtime\nFrom DB");
        createHeader(row, workflowExecutionTimeIndex, "Workflow\nRuntime");
        createHeader(row, ruleExecutionTimeIndex, "Rule\nRuntime");

        row = sheet.createRow(2);
        createHeader(row, elementIncludeIndex, "Element");
        createHeader(row, valueIndex, "Value");
        createHeader(row, fromIndex, "From");
        createHeader(row, toIndex, "To");
        createHeader(row, elementIncludeIndex, "Include");
        createHeader(row, elementExcludeIndex, "Exclude");

        sheet.addMergedRegion(new CellRangeAddress(0, 2, modelIndex, modelIndex)); // Model Name
        sheet.addMergedRegion(new CellRangeAddress(0, 2, ruleIndex, ruleIndex)); // Rule Name
        sheet.addMergedRegion(new CellRangeAddress(0, 2, descriptionIndex, descriptionIndex)); // Testcase description
        sheet.addMergedRegion(new CellRangeAddress(0, 0, totalElementsIndex, ruleExecutionTimeIndex)); // Output
        sheet.addMergedRegion(new CellRangeAddress(1, 1, elementIncludeIndex, elementExcludeIndex)); // Element
        sheet.addMergedRegion(new CellRangeAddress(1, 2, conditionsIndex, conditionsIndex)); // Conditions
        sheet.addMergedRegion(new CellRangeAddress(1, 2, expressionsValueIndex, expressionsValueIndex)); // Expresson Value
        sheet.addMergedRegion(new CellRangeAddress(1, 2, expressionsIndex, expressionsIndex)); // Expresson
        sheet.addMergedRegion(new CellRangeAddress(1, 2, operatorIndex, operatorIndex)); // Operator
        sheet.addMergedRegion(new CellRangeAddress(1, 2, propertyIndex, propertyIndex)); // Property Name
        sheet.addMergedRegion(new CellRangeAddress(1, 1, valueIndex, toIndex)); // Property Value
        sheet.addMergedRegion(new CellRangeAddress(1, 2, unitIndex, unitIndex)); //
        sheet.addMergedRegion(new CellRangeAddress(0, 2, expectedStatusIndex, expectedStatusIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, totalElementsIndex, totalElementsIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, issueElementsIndex, issueElementsIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, statusIndex, statusIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, descriptorIndex, descriptorIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, descriptorMsgIndex, descriptorMsgIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, workflowExecutionTimeIndex, workflowExecutionTimeIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, ruleExecutionTimeIndex, ruleExecutionTimeIndex));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, runTimeFromDBIndex, runTimeFromDBIndex));

        return wb;
    }

    private static Cell createCellValueAsString(Row row, int index, String value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(value);
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setAlignment(CellStyle.ALIGN_CENTER);

        cell.setCellStyle(style);

        return cell;
    }

    private static Cell createHeader(Row row, int index, String value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_STRING);

        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        cell.setCellStyle(style);
        cell.setCellValue(value);

        return cell;
    }

    private static Cell createCellValueAsInteger(Row row, int index, int value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        return cell;
    }

    private static Cell createCellValueAsLong(Row row, int index, long value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        return cell;
    }

    private static Cell createStatusCellValueAsInteger(Row row, int index, int value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        return cell;
    }

    private static Cell createCellValueAsDouble(Row row, int index, double value) {
        Cell cell = row.createCell(index);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        return cell;
    }

    private static int increment() {
        return colCounter++;
    }
}
