package com.zuas.bzb.dao;

import com.zuas.bzb.model.dto.*;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;


@Component
public class OrdersDAO {
    private static final Logger LOGGER =
            Logger.getLogger(OrdersDAO.class.getName());
    private final Connection connection;
    private final int TimeOut = 2;



    @Autowired
    public OrdersDAO(DataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
    }

    public OrderResponseDTO add(OrderDTO orderToAdd) {
        String userEmail = orderToAdd.getEmail();
        String userToken = orderToAdd.getToken();
        int userId = getUserId(userEmail,userToken);
        if (userId < 0){
            LOGGER.log(Logger.Level.INFO,"order not added because of user data not valid or user not found");
            return null;
        }
        if(orderToAdd.getText().isEmpty()) {
            LOGGER.log(Logger.Level.INFO,"order not added: description is empty");
            return null;
        }
        String sql = "insert into orders (user_id,latitude,longitude,description,order_date)"
                + "values (?,?,?,?,current_date)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setDouble(2, orderToAdd.getLatitude());
            preparedStatement.setDouble(3, orderToAdd.getLongitude());
            preparedStatement.setString(4, orderToAdd.getText());
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                int generatedId = rs.getInt(1);
                LOGGER.log(Logger.Level.INFO,"new order have successfully added with id = "+generatedId);
                return new OrderResponseDTO(generatedId);
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        LOGGER.log(Logger.Level.INFO,"order not added for some reason");
        return null;
    }

    public boolean update(List<OrderToUpdateDTO> ordersToUpdate) {
        // create & initialize variable to count success operations
        int numberOfUpdatedRows = 0;

        //Check user valid getting first member of list as a marker;
        if (ordersToUpdate != null && ordersToUpdate.size() > 0)
        {
            OrderToUpdateDTO firstOrderToUpdate = ordersToUpdate.get(0);
            String token = firstOrderToUpdate.getToken();
            if(token == null) return false;
            int orderId = firstOrderToUpdate.getOrderId();
            // check user token valid
            if (!checkUserValid(orderId,token)) return false;
        }
        else
        {
            return false;
        }

        for(OrderToUpdateDTO x: ordersToUpdate){
            int orderId = x.getOrderId();
            String sql = "update orders " +
                    "set description= ?, " +
                    "status=? " +
                    "where order_id = ?";
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, x.getText());
                preparedStatement.setBoolean(2, x.isStatus());
                preparedStatement.setInt(3, orderId);
                numberOfUpdatedRows += preparedStatement.executeUpdate();
            }catch (SQLException e){
                LOGGER.log(Logger.Level.ERROR,e);
            }
        }
        boolean condition = numberOfUpdatedRows == ordersToUpdate.size();
        LOGGER.log(Logger.Level.INFO,"was update success: "+ condition);
        return condition;
    }

    public MarkerDescriptionDTO getDescription(MarkerDTO markerToDescribe) {
        double lat = markerToDescribe.getLatitude();
        double lon = markerToDescribe.getLongitude();
        String sql = "select * from orders where latitude = ? and longitude = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql,Statement.KEEP_CURRENT_RESULT)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                String description = rs.getString("description");
                LOGGER.log(Logger.Level.INFO,"description of point "+lat+", "+lon+" was sent");
                return new MarkerDescriptionDTO(description);
            }
        }catch(SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        LOGGER.log(Logger.Level.INFO,"description of point "+lat+", "+lon+" not found");
        return null;
    }
    public List<MarkerDTO> showAll() {
        List<MarkerDTO> markers = new ArrayList<>();
        String sql = "select * from orders where status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql,Statement.KEEP_CURRENT_RESULT)) {
            ps.setBoolean(1, true);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lon = rs.getDouble("longitude");
                markers.add(new MarkerDTO(lat, lon));
            }
            LOGGER.log(Logger.Level.INFO,"markerList was sent. size of list = " + markers.size());
            return markers;
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return null;
    }

    public List<UserOrderDTO> getOrdersUsingUserId(UserIdDTO someUserId) {

        if(someUserId == null) return null;
        int userId = someUserId.getUserId();
        String userToken = someUserId.getToken();
        List<UserOrderDTO> orderList = new ArrayList<>();
        if (!checkUserValidUsingUserId(userId,userToken)) {
            int order_id = 0;
            double lat = 0.0;
            double lon = 0.0;
            String text = "";
            String datestring = "";
            boolean status = true;
            orderList.add(new UserOrderDTO(order_id,lat,lon,text,datestring,status));
            return orderList;
        }
        String sqlOrders = "select * from orders where user_id= ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlOrders, Statement.KEEP_CURRENT_RESULT)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            while (rs.next()) {
                int order_id = rs.getInt("order_id");
                double lat = rs.getDouble("latitude");
                double lon = rs.getDouble("longitude");
                String text = rs.getString("description");
                String datestring = rs.getDate("order_date").toString();
                boolean status = rs.getBoolean("status");
                orderList.add(new UserOrderDTO(order_id, lat, lon, text, datestring, status));
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        LOGGER.log(Logger.Level.INFO,
                "list of orders was sent to user with id = "+ userId);
        return orderList;
    }

    private boolean checkUserValidUsingUserId (int userId,String token) {
        if(token == null) {
            LOGGER.log(Logger.Level.INFO,"token of user with id= "+userId+" is NULL");
            return false;
        }
        String sqlPerson = "select * from person where account_id = "+ userId;
        try ( PreparedStatement preparedStatement =
                      connection.prepareStatement(sqlPerson, Statement.KEEP_CURRENT_RESULT) ){
            preparedStatement.execute();
            try(ResultSet rs = preparedStatement.getResultSet()) {
                if (rs.next()) {
                    String personToken = rs.getString("token");
                    // compare token values
                    if (!token.equals(personToken)) {
                        LOGGER.log(Logger.Level.INFO,"user token is wrong!");
                        return false;
                    }
                    // check token valid time (out of date or not)
                    Date currentDate = new GregorianCalendar().getTime();
                    Date tokenDate = rs.getDate("token_date");
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(tokenDate);
                    // add "extra time" = TimeOut to token_date to be valid
                    calendar.add(Calendar.DAY_OF_MONTH, TimeOut);
                    Date bestBefore = calendar.getTime();
                    boolean condition = !currentDate.after(bestBefore);
                    if (!condition) {
                        LOGGER.log(Logger.Level.INFO,"token of user with id= "+userId+" is out of date");
                    }
                    else {
                        LOGGER.log(Logger.Level.INFO,
                                "user with id= "+userId+" has successfully passed the token verification");
                    }
                    return condition;
                }
                LOGGER.log(Logger.Level.INFO,"user with id= "+userId+"was not found");
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return false;
    }

    private boolean checkUserValid (int orderId, String token) {
        if(token == null) return false;
        String sqlOrders = "select * from orders where order_id= ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlOrders, Statement.KEEP_CURRENT_RESULT)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                return checkUserValidUsingUserId(userId, token);
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return false;
    }

    private int getUserId(String userEmail, String userToken) {

        String sql = "select * from person where email = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.KEEP_CURRENT_RESULT);) {
            preparedStatement.setString(1, userEmail);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            if (rs.next()) {
                String token = rs.getString("token");
                // compare token values
                if (!userToken.equals(token)) {
                    return -2;
                }

                // check token valid time (out of date or not)
                Date currentDate = new GregorianCalendar().getTime();
                Date TokenDate = rs.getDate("token_date");
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(TokenDate);
                // add "extra time" = TimeOut to token_date to be valid
                calendar.add(Calendar.DAY_OF_MONTH, TimeOut);
                Date bestBefore = calendar.getTime();
                if (currentDate.after(bestBefore)) {
                    return -3;
                }
                int uid = rs.getInt(1);
                LOGGER.log(Logger.Level.INFO,"user find with id = "+uid);
                return uid;
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        // if no matches return -1;
        return -1;
    }

    public OrderDTO check (OrderDTO dto) {
        Double[] coord = checkLongLatMatch(dto.getLatitude(),dto.getLongitude());
        if (coord!= null && coord.length == 2) return new OrderDTO(coord[0], coord[1]);
        return null;
    }

    private Double[] checkLongLatMatch(double lat,double lon) {

        String sql = "select * from orders where latitude < ? and latitude > ? and longitude < ? and longitude > ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql,Statement.KEEP_CURRENT_RESULT)) {
            preparedStatement.setDouble(1, lat + 0.000001);
            preparedStatement.setDouble(2, lat - 0.000001);
            preparedStatement.setDouble(3, lon + 0.000001);
            preparedStatement.setDouble(4, lon - 0.000001);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            if (rs.next()) {
                double latt = rs.getDouble("latitude");
                double lonn = rs.getDouble("longitude");
                return new Double[]{latt, lonn};
            }
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return null;
    }

    public boolean deleteOrder(OrderResponseDTO orderToDelete) {
        if(orderToDelete == null) return false;
        int orderId = orderToDelete.getOrder_id();
        if (orderId == 0) return false;
        String sql = "delete from orders where order_id= ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);
            int numberOfDeletedRows = preparedStatement.executeUpdate();
            LOGGER.log(Logger.Level.INFO,"was order with id = "+orderId+" deleted? "+ (numberOfDeletedRows>0));
            return (numberOfDeletedRows > 0);
        }catch (SQLException e){
            LOGGER.log(Logger.Level.ERROR,e);
        }
        return false;
    }
}
