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

import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.Server;
import com.invicara.tools.rh.exception.ConfigurationException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alok Ranjan Meher
 */
public class DatabaseManagement {

    private Connection emConnection = null;

    public Connection configureToEmpireManageDatabase(String database) throws ConfigurationException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            StringBuilder builder = new StringBuilder();
            builder.append(Server.EMPIRE_MANAGE_DB.getServerUrl());
            if (Server.EMPIRE_MANAGE_DB.getServerUrl().endsWith("/")) {
                builder.append(database);
            } else {
                builder.append("/");
                builder.append(database);
            }
            emConnection = DriverManager.getConnection(builder.toString(),
                    Database.DB_AUTHENTICATION.getUser(),
                    Database.DB_AUTHENTICATION.getPassword());
        } catch (ClassNotFoundException | SQLException ex) {
            throw new ConfigurationException(ex);
        }

        return emConnection;
    }

    public Connection configureToPassportDatabase(String database) throws ConfigurationException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            StringBuilder builder = new StringBuilder();
            builder.append(Server.PASSPORT_DB.getServerUrl());
            if (Server.PASSPORT_DB.getServerUrl().endsWith("/")) {
                builder.append(database);
            } else {
                builder.append("/");
                builder.append(database);
            }
            emConnection = DriverManager.getConnection(builder.toString(),
                    Database.DB_AUTHENTICATION.getUser(),
                    Database.DB_AUTHENTICATION.getPassword());
        } catch (ClassNotFoundException | SQLException ex) {
            throw new ConfigurationException(ex);
        }

        return emConnection;
    }

    public List<Integer> getUserIds() {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Integer> userIds = new ArrayList<>();
         try {
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT id ");
                builder.append("FROM xosuser ");
                String sqlQuery = builder.toString();
                pstmt = emConnection.prepareStatement(sqlQuery);
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    userIds.add(resultSet.getInt("id"));
                }
            } catch (SQLException e) {
                Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                closeResultSet(resultSet);
                closePreparedStatement(pstmt);
            }
        return userIds;
    }

    public List<Integer> getAccountIds() {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Integer> accountIds = new ArrayList<>();
     try {
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT id ");
                builder.append("FROM account_concrete ");
                String sqlQuery = builder.toString();
                pstmt = emConnection.prepareStatement(sqlQuery);
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    accountIds.add(resultSet.getInt("id"));
                }
            } catch (SQLException e) {
                Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                closeResultSet(resultSet);
                closePreparedStatement(pstmt);
            }
        return accountIds;
    }

    public List<Integer> getProjectIds(int accountId) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Integer> projectIds = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT id ");
            builder.append("FROM project_concrete ");
            builder.append("WHERE account_id = ? ");
            String SQL = builder.toString();
            pstmt = emConnection.prepareStatement(SQL);
            pstmt.setInt(1, accountId);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                projectIds.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }
        return projectIds;

    }

    public List<Integer> getPackageMasterIds(int projectId) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Integer> packageMasterIds = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT id ");
            builder.append("FROM package_master_concrete ");
            builder.append("WHERE project_id = ? ");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, projectId);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                packageMasterIds.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }

        return packageMasterIds;
    }

    public String getModelName(int packageMasterId, int projectId) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        String name = "";
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT package_name ");
            builder.append("FROM package_master_concrete ");
            builder.append("WHERE id = ? ");
            builder.append("AND project_id = ? ");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, packageMasterId);
            pstmt.setInt(2, projectId);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString(1);
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }

        return name;
    }

    public int getSystemRuleId(String ruleName) {
        int index = 0;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {

            StringBuilder builder = new StringBuilder();
            builder.append("SELECT id ");
            builder.append("FROM system_rules ");
            builder.append("WHERE rule_name= ? ");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setString(1, ruleName);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                index = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }

        return index;
    }

    public int getAccountRuleId(int systemRuleIndex, int accountIndex) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        int index = 0;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT id ");
            builder.append("FROM account_rules ");
            builder.append("WHERE system_rules_id = ? ");
            builder.append("AND account_id= ?");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, systemRuleIndex);
            pstmt.setInt(2, accountIndex);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                index = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }
        return index;
    }

    public boolean checkRuleExecutionStatus(int analysisRunId) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        int index = 0;
        Date date = null;
        long increment = 0;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT completed_at ");
            builder.append("FROM analysis_runs ");
            builder.append("WHERE id = ? ");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, analysisRunId);
            while (true) {
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    date = resultSet.getDate(1);
                }
                if (index > 59) {
                    break;
                }
                if (date == null) {
                    index++;
                } else {
                    return true;
                }
                increment = index * 100;
                sleepForAWhile(increment);
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class
                    .getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }

        return false;
    }

    private void sleepForAWhile(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Logger.getLogger(DatabaseManagement.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getAnalysisRunId(int packageVersionId, int analysisId) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT id ");
            builder.append("FROM analysis_runs ");
            builder.append("WHERE package_version_id = ? ");
            builder.append("AND analysis_id = ? ");
            String sqlQuery = builder.toString();
            pstmt = emConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, packageVersionId);
            pstmt.setInt(2, analysisId);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            Logger.getLogger(DatabaseManagement.class
                    .getName()).log(Level.SEVERE, null, e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(pstmt);
        }

        return 0;
    }

    private void closePreparedStatement(PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return emConnection;
    }

    public void setConnection(Connection connection) {
        this.emConnection = connection;
    }

}
