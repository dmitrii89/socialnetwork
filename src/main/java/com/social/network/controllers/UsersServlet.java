package com.social.network.controllers;

import com.social.network.dao.UserDao;
import com.social.network.models.User;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.social.network.utils.ServerUtils.getUserFromSession;

/**
 * Created by Dmitrii on 28.11.2018.
 */
public class UsersServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UsersServlet.class);
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = (UserDao) getServletContext().getAttribute("userDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User userFromSession = getUserFromSession(req);
        List<User> users = userDao.getAll();
        users = users.stream()
                .filter(user -> user.getId() != userFromSession.getId())
                .collect(Collectors.toList());

        req.setAttribute("users", users);
        req.getRequestDispatcher("users.jsp").forward(req, resp);
    }
}
