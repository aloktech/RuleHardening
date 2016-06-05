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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alok Ranjan Meher
 */
public class IOManagement {

    public static List<String> openList(String filePath) {
        try (FileInputStream fin = new FileInputStream(filePath);
                ObjectInputStream ois = new ObjectInputStream(fin);) {

            return (List<String>) ois.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    public static void saveList(String filePath, List<String> list) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {
            oos.writeObject(list);
        } catch (IOException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String openInternalFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(IOManagement.class.getClassLoader().getResourceAsStream(filePath)))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static String openExternalFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static void saveToFile(String filePath, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.append(data);
        } catch (IOException ex) {
            Logger.getLogger(IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> readFileAsLine(String filePath) {
        String line;
        List<String> fileNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                fileNames.add(line);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileNames;
    }

    public static String readFileAsString(String filePath) {
        String line;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return builder.toString();
    }

    public static void writeToFileAsString(String filePath, String data) {
        File file = new File(filePath);
        file.delete();
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.append(data);
        } catch (IOException ex) {
            Logger.getLogger(com.invicara.tools.rh.utils.IOManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
