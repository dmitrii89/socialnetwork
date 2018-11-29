package com.social.network.dao;

import com.social.network.connection.Connective;
import com.social.network.models.User;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dmitrii on 14.11.2018.
 */
public class UserDao{
    private static final Logger logger = Logger.getLogger(UserDao.class);
    private static final String CREATION_OF_USER_FAILED_NO_ID_OBTAINED = "Creation of user failed, no id obtained.";
    private static final String CAN_T_INSERT_USER_IN_THE_DATABASE = "Can't insert user in the database: ";
    private static final String CAN_T_UPDATE_USER_WITH_ID_S_IN_THE_DATABASE = "Can't update user with id=%s in the database";
    private static final String CAN_T_GET_ALL_USERS_FROM_THE_DATABASE = "Can't get all users from the database";
    private static final String CAN_T_DELETE_USER_WITH_ID_S_FROM_THE_DATABASE = "Can't delete user with id=%s from the database";
    private static final String CAN_T_UPDATE_IMAGE = "Can't update image of user with id=%s in the database";
    private static final String CAN_T_UPDATE_USER_PASSWORD = "Can't update user with id=%s with new password in the database";
    private static final String CAN_T_PARSE_USER_RESULT_SET = "Can't parse user result set";
    
    private static final String SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SELECT_USER = "SELECT * FROM users WHERE id=?";
    private static final String UPDATE_USER = "UPDATE users SET firstname=?, lastname=?, dob=?, sex=?, phone=? WHERE id=?";
    private static final String DELETE_USER = "DELETE FROM users WHERE id=?";
    private static final String INSERT_USER = "INSERT INTO users VALUES(NULL, ?, ?, ?, ?, ?, ?, ?, 2, 'false', NULL);";
    private static final String SELECT_FROM_USERS_WHERE_EMAIL_AND_PASSWORD = "SELECT * FROM users WHERE email=? AND password=?";
    private static final String UPDATE_USERS_SET_IMAGE_WHERE_ID = "UPDATE USERS SET image=? WHERE id=?";
    private static final String UPDATE_USERS_SET_PASSWORD_WHERE_ID = "UPDATE USERS SET password=? WHERE id=?";


    private final Connective connective;
    
    public UserDao(Connective connective) {
        this.connective = connective;
    }

    public User get(int id) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(SELECT_USER);) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            List<User> list  = parseResultSet(rs);
            return list.get(0);
        } catch (SQLException e) {
            logger.error(String.format("Can't get object with id=%s in the database", id));
            throw new RuntimeException();
        }
    }

    public User insert(User entity) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);) {
            prepareStatementForInsert(stm, entity);
            stm.executeUpdate();
            try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return get(id);
                } else {
                    throw new SQLException(CREATION_OF_USER_FAILED_NO_ID_OBTAINED);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            String message = CAN_T_INSERT_USER_IN_THE_DATABASE + entity;
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    public void update(User entity) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(UPDATE_USER);) {
            prepareStatementForUpdate(stm, entity);
            stm.executeUpdate();
        } catch (SQLException e) {
            String message = String.format(CAN_T_UPDATE_USER_WITH_ID_S_IN_THE_DATABASE, entity.getId());
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    public void delete(User entity) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(DELETE_USER);) {
            stm.setInt(1, entity.getId());
            stm.executeUpdate();
        } catch (SQLException e) {
            String message = String.format(CAN_T_DELETE_USER_WITH_ID_S_FROM_THE_DATABASE, entity.getId());
            logger.error(message);
            throw new RuntimeException(message);
        }
    }


    public List<User> getAll() {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(SELECT_ALL_USERS);) {
            ResultSet rs = stm.executeQuery();
            return parseResultSet(rs);
        } catch (SQLException e) {
            logger.error(CAN_T_GET_ALL_USERS_FROM_THE_DATABASE);
            throw new RuntimeException(CAN_T_GET_ALL_USERS_FROM_THE_DATABASE);
        }
    }

    public User getUserByCredentials(String email, String password) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(SELECT_FROM_USERS_WHERE_EMAIL_AND_PASSWORD);) {
            stm.setString(1, email);
            stm.setString(2, password);
            ResultSet rs = stm.executeQuery();
            List<User> list  = parseResultSet(rs);
            if(list.size() == 1) {
                return list.get(0);
            }
        } catch (SQLException e) {
            logger.error(String.format("No user found with such credentials email = %s and password = ****", email));
        }
        return null;
    }

    public User updateImage(User user) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(UPDATE_USERS_SET_IMAGE_WHERE_ID)) {
            stm.setString(1, user.getImage());
            stm.setInt(2, user.getId());
            stm.executeUpdate();
            return get(user.getId());
        } catch (SQLException e) {
            String message = String.format(CAN_T_UPDATE_IMAGE, user.getId());
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    public User updatePassword(User user) {
        try(Connection con = connective.getConnection();
            PreparedStatement stm = con.prepareStatement(UPDATE_USERS_SET_PASSWORD_WHERE_ID)) {
            stm.setString(1, user.getPassword());
            stm.setInt(2, user.getId());
            stm.executeUpdate();
            return get(user.getId());
        } catch (SQLException e) {
            String message = String.format(CAN_T_UPDATE_USER_PASSWORD, user.getId());
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    private List<User> parseResultSet(ResultSet rs) {
        List<User> users = new LinkedList<>();
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                Date dob = rs.getDate("dob");
                int sex = rs.getInt("sex");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String password = rs.getString("password");
                Integer role = rs.getInt("role");
                Boolean blocked = rs.getBoolean("blocked");
                String image = rs.getString("image");

                User user = new User();
                user.setId(id);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if(dob != null){
                    user.setDob(new Date(dob.getTime()));
                }
                user.setSex(sex);
                user.setPhone(phone);
                user.setEmail(email);
                user.setPassword(password);
                user.setRole(role);
                user.setBlocked(blocked);
                user.setImage(image);

                users.add(user);
            }
        }catch (Exception e) {
            logger.error(CAN_T_PARSE_USER_RESULT_SET);
        }
        return users;
    }

    private PreparedStatement prepareStatementForUpdate(PreparedStatement st, User entity) throws SQLException {
        st.setString(1, entity.getFirstName());
        st.setString(2, entity.getLastName());
        Date date = new Date(entity.getDob().getTime());
        st.setDate(3, date);
        st.setInt(4, entity.getSex());
        st.setString(5, entity.getPhone());
        st.setInt(6, entity.getId());
        return st;
    }


    private PreparedStatement prepareStatementForInsert(PreparedStatement st, User entity) throws SQLException {
        st.setString(1, entity.getFirstName());
        st.setString(2, entity.getLastName());
        Date date = null;
        if(entity.getDob() != null) {
            date = new Date(entity.getDob().getTime());
        }
        st.setDate(3, date);
        if(entity.getSex() != null) {
            st.setInt(4, entity.getSex());
        } else {
            st.setInt(4, 1);
        }
        st.setString(5, entity.getPhone());
        st.setString(6, entity.getEmail());
        st.setString(7, entity.getPassword());
        return st;
    }
}