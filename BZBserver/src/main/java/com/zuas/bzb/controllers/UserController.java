package com.zuas.bzb.controllers;

import com.zuas.bzb.dao.UserDAO;
import com.zuas.bzb.model.dto.UserCheckDTO;
import com.zuas.bzb.model.dto.UserIdDTO;
import com.zuas.bzb.model.dto.UserUpdatePasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserDAO userDAO;

    @Autowired
    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping("/registration")
    public UserCheckDTO userRegistration (@RequestBody UserCheckDTO dto)
            throws SQLException {
        return userDAO.register(dto);
    }
    @PostMapping("/login")
    public UserCheckDTO userLogin (@RequestBody UserCheckDTO dto)
            throws SQLException {
        return userDAO.login(dto);
    }
    @PostMapping("/update")
    public UserCheckDTO userPasswordUpdate (@RequestBody UserUpdatePasswordDTO dto)
            throws SQLException {
        return userDAO.update(dto);
    }
    @PostMapping("/delete")
    public boolean userDelete(@RequestBody UserCheckDTO dto) throws SQLException {
        return userDAO.delete(dto);
    }
    @PostMapping("/token")
    public UserCheckDTO checkToken(@RequestBody UserCheckDTO dto) throws SQLException {
        return userDAO.userCheckToken(dto);
    }

}
