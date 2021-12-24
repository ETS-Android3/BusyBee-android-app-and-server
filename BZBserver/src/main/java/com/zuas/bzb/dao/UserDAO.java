package com.zuas.bzb.dao;

import com.zuas.bzb.model.dto.UserCheckDTO;
import com.zuas.bzb.model.dto.UserIdDTO;
import com.zuas.bzb.model.dto.UserUpdatePasswordDTO;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.*;


@Component
public class UserDAO {

    private static final Logger LOGGER =
            Logger.getLogger(OrdersDAO.class.getName());
    private final Connection connection;

    @Autowired
    public UserDAO(DataSource dataSource) throws SQLException {
            connection = dataSource.getConnection();
    }

    public UserCheckDTO register(UserCheckDTO userToRegister) throws SQLException {

        // check password existing
        if (userToRegister.getPassword() == null) return null;
        String userEmail = userToRegister.getEmail();
        if (userToRegister.getEmail() == null) return null;
        // if email doesn't exist create password using sha-1 func
        if (GetPersonIdUsingEmail(userEmail) == -1){
            String userSalt = UserDAO.getSalt();
            String userPass = UserDAO.get_SHA_1_SecurePassword(userToRegister.getPassword(),userSalt);
            String userToken = UserDAO.getSalt();

            // create a record in the table person
            String sql = "insert into person (email,salt,password,token,token_date)"
                    + "values (?,?,?,?,current_date)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,userEmail);
            preparedStatement.setString(2,userSalt);
            preparedStatement.setString(3,userPass);
            preparedStatement.setString(4,userToken);

            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                int generatedId = rs.getInt(1);
                return new UserCheckDTO(generatedId, userToken);
            }
        }
        return null;
    }

    public UserCheckDTO login(UserCheckDTO userToCheck)
            throws SQLException {
        if (userToCheck == null) return null;
        // check email & password existing
        String userEmail = userToCheck.getEmail();
        String userPassword = userToCheck.getPassword();
        if (userPassword == null || userEmail == null) return null;
        // searching user in table person
        int personId = GetPersonIdUsingEmail(userEmail);
        if(personId < 0) return new UserCheckDTO(-1,"");
        boolean condition = isPasswordCorrect(personId,userPassword);

        // if success return new DTO with new token
        if (condition){
            return putNewToken(personId);
        }
        // if password is incorrect return -2 userId code
        return new UserCheckDTO(-2,"");
    }

    public UserCheckDTO update(UserUpdatePasswordDTO userToUpdate)
            throws SQLException {
        // check email & password existing
        String userEmail = userToUpdate.getEmail();
        String userOldPassword = userToUpdate.getOldPassword();
        String userNewPassword = userToUpdate.getNewPassword();
        if (userOldPassword == null || userEmail == null || userNewPassword == null) return null;
        // searching user in table person
        int personId = GetPersonIdUsingEmail(userEmail);
        if(personId < 0) return null;
        boolean condition = isPasswordCorrect(personId,userOldPassword);
        if (condition){
            // create new values of salt, password and token
            String salt = getSalt();
            String password = get_SHA_1_SecurePassword(userNewPassword,salt);
            String token = getSalt();
            String sqlToken = "update person "
                        +"set salt = ?, "
                        +"password = ?, "
                        +"token = ?, "
                        +"token_date = current_date "
                        +"where account_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlToken);
            preparedStatement.setString(1,salt);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,token);
            preparedStatement.setInt(4,personId);
            preparedStatement.executeUpdate();
            return new UserCheckDTO(personId,token);
        }
        else{
            personId = -1;
            String token = "";
            return new UserCheckDTO(personId,token);
        }
    }

    public boolean delete(UserCheckDTO userToDelete) throws SQLException {
        // check email & password existing
        String userEmail = userToDelete.getEmail();
        String userPassword = userToDelete.getPassword();
        if (userPassword == null || userEmail == null) return false;
        // searching user in table person
        int personId = GetPersonIdUsingEmail(userEmail);
        if(personId < 0) return false;
        boolean condition = isPasswordCorrect(personId,userPassword);
        if(condition){
            String sqlOrders = "delete from orders where user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlOrders);
            preparedStatement.setInt(1,personId);
            preparedStatement.executeUpdate();

            String sql = "delete from person where account_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1,personId);
            int countOfDeletedRows = ps.executeUpdate();
            return (countOfDeletedRows > 0);
        }
        return false;
    }

    public UserCheckDTO userCheckToken(UserCheckDTO userToCheck) throws SQLException {
        if (userToCheck == null) return null;
        int userId = userToCheck.getUserID();
        if (userId == 0) return null;
        String tokenToCompare = userToCheck.getUserToken();

        String sql = "select * from person where account_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.KEEP_CURRENT_RESULT);
        preparedStatement.setInt(1,userId);
        preparedStatement.execute();
        ResultSet rs = preparedStatement.getResultSet();
        if (rs.next()){
            String token = rs.getString("token");
            if (!token.equals(tokenToCompare)) return new UserCheckDTO(userId,"");
            return putNewToken(userId);
        }
        return null;
    }

    private UserCheckDTO putNewToken(int userId) throws SQLException {
        String newToken = UserDAO.getSalt();
        String sqlToken = "update person "
                +"set token = ?, "
                +"token_date = current_date "
                +"where account_id = ?";
        PreparedStatement ps = connection.prepareStatement(sqlToken);
        ps.setString(1,newToken);
        ps.setInt(2,userId);
        ps.executeUpdate();
        return new UserCheckDTO(userId,newToken);
    }

    // func to get salt or token
    private static String getSalt()
    {
        // Always use a SecureRandom generator
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        // Create array for salt
        byte[] salt = new byte[16];
        // Get a random salt
        sr.nextBytes(salt);
        // return salt
        //String str = salt.toString();
        return salt.toString();
    }

    // getting password using sha-1 and "salt"
    private static String get_SHA_1_SecurePassword(String passwordToHash, String salt){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    // check user_id using email in table person
    private int GetPersonIdUsingEmail(String emailToSearch){
        try {
            String sql = "select * from person where email = '" + emailToSearch + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.KEEP_CURRENT_RESULT);
            preparedStatement.execute();

            ResultSet rs = preparedStatement.getResultSet();

            if (rs.next()) return rs.getInt(1);
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return -1;
    }

    // check user password valid
    private boolean isPasswordCorrect(int userId, String userPassword) {
        // get user salt & password using id
        String sql = "select * from person where account_id = " + userId;
        try(Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);

            boolean condition = false;
            if (rs.next()) {
                String userSalt = rs.getString("salt");
                String personPassword = rs.getString("password");
                // check password
                condition = get_SHA_1_SecurePassword(userPassword, userSalt).equals(personPassword);
            }
            return condition;
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return false;
    }

}
