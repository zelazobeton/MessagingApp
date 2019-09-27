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

    public static final String QUERY_USER_USERNAME_PWD =
            "SELECT " + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_HASH +
            " FROM " + TABLE_USERS + " WHERE " +
            COLUMN_USER_NAME + " = ? AND " + COLUMN_USER_HASH + " = ?";



    private PreparedStatement insertUserStmnt;
    private PreparedStatement queryUserForUsernameStmnt;
    private PreparedStatement queryUserWithPwdStmnt;

    private Connection conn;

    public boolean open(){
        try{
            conn = DriverManager.getConnection(CONNECTION + DATABASE);
            createUserTable();
            insertUserStmnt = conn.prepareStatement(INSERT_USER);
            queryUserForUsernameStmnt = conn.prepareStatement(QUERY_USER_FOR_USERNAME);
            queryUserWithPwdStmnt = conn.prepareStatement(QUERY_USER_USERNAME_PWD);
            return true;
        }
        catch(SQLException ex){
            LOGGER.warning("Exception thrown while opening db: " + ex.getMessage());
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
            if(queryUserWithPwdStmnt != null){
                queryUserWithPwdStmnt.close();
            }

            if(conn != null){
                conn.close();
            }
            return true;
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public UserContext queryUserForUsernameAndHash(String username, String hash){
        try{
            queryUserWithPwdStmnt.setString(1, username);
            queryUserWithPwdStmnt.setString(2, hash);
            ResultSet result = queryUserWithPwdStmnt.executeQuery();
            if(!result.isBeforeFirst()){
                return null;
            }
            UserContext userContext = createUserDataFromQueryResult(result);
            return userContext;
        }
        catch(SQLException ex){
            LOGGER.warning("queryUserForUsernameAndPwd " + ex.getMessage());
            return null;
        }
    }

    public UserContext queryUserForUsername(String username){
        try{
            queryUserForUsernameStmnt.setString(1, username);
            ResultSet result = queryUserForUsernameStmnt.executeQuery();
            if(!result.isBeforeFirst()){
                return null;
            }
            UserContext userContext = createUserDataFromQueryResult(result);
            return userContext;
        }
        catch(SQLException ex){
            LOGGER.warning("queryUserForUsername " + ex.getMessage());
            return null;
        }
    }

    private UserContext createUserDataFromQueryResult(ResultSet result){
        try{
            final int userId = result.getInt(COLUMN_USER_ID);
            final String username = result.getString(COLUMN_USER_NAME);
            final String hash = result.getString(COLUMN_USER_HASH);
            return new UserContext(userId, username, hash);
        }
        catch(SQLException ex){
            LOGGER.warning(ex.toString());
            return null;
        }
    }

    public boolean insertUser(String username, String hash){
        try {
            conn.setAutoCommit(false);
            queryUserForUsernameStmnt.setString(1, username);
            ResultSet result = queryUserForUsernameStmnt.executeQuery();
            if (result.next()) {
                LOGGER.warning("DB ERROR: User already exists");
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
            LOGGER.warning(ex.getMessage());
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
            LOGGER.warning("Exception thrown during rollback" + ex.toString());
        }
    }

    private void setAutoCommitTrue(){
        try{
            LOGGER.fine("Resetting normal commit behavior");
            conn.setAutoCommit(true);
        }
        catch (SQLException ex){
            LOGGER.warning("Couldn't reset autocommit: " + ex.getMessage());
        }
    }

    public boolean createUserTable(){
        try(Statement statement = conn.createStatement()) {
            statement.execute(CREATE_TABLE_USERS);
            return true;
        }
        catch(SQLException ex){
            LOGGER.warning(ex.getMessage());
            return false;
        }
    }
}
