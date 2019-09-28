package com.Server.src;

import java.sql.*;
import java.util.logging.Logger;

public class DbHandler {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public static final String CONNECTION = "jdbc:sqlite:E:\\Java Intellij Projects\\TextFileCommunicator\\";
    public static final String DATABASE = "communicator.db";
    public static final String TABLE_USERS = "users";

    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_HASH = "hash";

    public static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ( " +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_USER_NAME + " VARCHAR(50) NOT NULL, " +
                    COLUMN_USER_HASH + " VARCHAR(50) NOT NULL )";

    public static final String INSERT_USER =
            "INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_NAME + ", " +
                    COLUMN_USER_HASH + " ) VALUES(?, ?)";

    public static final String QUERY_USER_FOR_USERNAME =
            "SELECT " + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_HASH +
            " FROM " + TABLE_USERS + " WHERE " +
            COLUMN_USER_NAME + " = ?";

    public static final String QUERY_USER_FOR_USERNAME_AND_HASH =
            "SELECT " + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_HASH +
            " FROM " + TABLE_USERS + " WHERE " +
            COLUMN_USER_NAME + " = ? AND " + COLUMN_USER_HASH + " = ?";

    private PreparedStatement insertUserStmnt;
    private PreparedStatement queryUserForUsernameStmnt;
    private PreparedStatement queryUserForUsernameAndHashStmnt;

    private Connection conn;

    public boolean open(){
        try{
            conn = DriverManager.getConnection(CONNECTION + DATABASE);
            createUserTable();
            insertUserStmnt = conn.prepareStatement(INSERT_USER);
            queryUserForUsernameStmnt = conn.prepareStatement(QUERY_USER_FOR_USERNAME);
            queryUserForUsernameAndHashStmnt = conn.prepareStatement(QUERY_USER_FOR_USERNAME_AND_HASH);
            return true;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while opening db: " + ex.toString());
            return false;
        }
    }

    public boolean closeConnection(){
        try{
            if(insertUserStmnt != null){
                insertUserStmnt.close();
            }
            if(queryUserForUsernameStmnt != null){
                queryUserForUsernameStmnt.close();
            }
            if(queryUserForUsernameAndHashStmnt != null){
                queryUserForUsernameAndHashStmnt.close();
            }

            if(conn != null){
                conn.close();
            }
            return true;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while closing database: " + ex.toString());
            return false;
        }
    }

    public ResultSet queryUserForUsernameAndHash(String username, String hash){
        try{
            queryUserForUsernameAndHashStmnt.setString(1, username);
            queryUserForUsernameAndHashStmnt.setString(2, hash);
            ResultSet result = queryUserForUsernameAndHashStmnt.executeQuery();
            if(!result.isBeforeFirst()){
                return null;
            }
            return result;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while queryUserForUsernameAndPwd " + ex.toString());
            return null;
        }
    }

    public ResultSet queryUserForUsername(String username){
        try{
            queryUserForUsernameStmnt.setString(1, username);
            ResultSet result = queryUserForUsernameStmnt.executeQuery();
            if(!result.isBeforeFirst()){
                return null;
            }
            return result;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while queryUserForUsername " + ex.toString());
            return null;
        }
    }

    public UserContext getUserContextForUsername(String username){
        ResultSet result = queryUserForUsername(username);
        if(result == null){
            return null;
        }
        try{
            final int resultUserId = result.getInt(COLUMN_USER_ID);
            final String resultUsername = result.getString(COLUMN_USER_NAME);
            final String resultHash = result.getString(COLUMN_USER_HASH);
            return new UserContext(resultUserId, resultUsername, resultHash);
        }
        catch(SQLException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public boolean insertUser(String username, String hash){
        try {
            conn.setAutoCommit(false);
            LOGGER.fine("DbHandler: setAutoCommitFalse");
            queryUserForUsernameStmnt.setString(1, username);
            ResultSet result = queryUserForUsernameStmnt.executeQuery();
            if (result.next()) {
                LOGGER.warning("DbHandler: User already exists");
                return false;
            }

            insertUserStmnt.setString(1, username);
            insertUserStmnt.setString(2, hash);

            int affectedRows = insertUserStmnt.executeUpdate();
            if(affectedRows == 1){
                conn.commit();
                return true;
            }
            else {
                throw new SQLException("User insertion failed");
            }
        }
        catch (Exception ex){
            LOGGER.warning("Exception thrown while insertUser " + ex.toString());
            performRollback();
            return false;
        }
        finally{
            setAutoCommitTrue();
        }
    }

    private void performRollback(){
        try{
            LOGGER.fine("Performing rollback");
            conn.rollback();
        }
        catch (SQLException ex){
            LOGGER.warning("Exception thrown while rollback" + ex.toString());
            ex.printStackTrace();
        }
    }

    private void setAutoCommitTrue(){
        try{
            LOGGER.fine("DbHandler: setAutoCommitTrue");
            conn.setAutoCommit(true);
        }
        catch (SQLException ex){
            LOGGER.warning("Exception thrown while reset autocommit: " + ex.toString());
        }
    }

    public boolean createUserTable(){
        try(Statement statement = conn.createStatement()) {
            statement.execute(CREATE_TABLE_USERS);
            return true;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while createUserTable: " + ex.toString());
            return false;
        }
    }

    public boolean deleteUser(String username){
        try(Statement statement = conn.createStatement();){
            int deleted = statement.executeUpdate(
                    "DELETE FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_NAME + "='" + username + "'");
            if(deleted != 1){
                return false;
            }
            return true;
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
