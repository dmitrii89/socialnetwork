package com.social.network.filters;

import com.social.network.constants.Role;
import com.social.network.dao.impl.UserDao;
import com.social.network.models.User;
import com.social.network.utils.ServerUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static com.social.network.utils.ServerUtils.getConfigUrls;
import static com.social.network.utils.ServerUtils.stringIsNotEmpty;

public class AuthFilter implements Filter {
    private UserDao userDao;
    private List<String> allowedUrls;


    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext servletContext = filterConfig.getServletContext();
        userDao = (UserDao) servletContext.getAttribute("userDao");

        String allowedUrls = servletContext.getInitParameter("allowedUrls");
        this.allowedUrls = getConfigUrls(allowedUrls);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                                                                                throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        String url = request.getRequestURL().toString();
        if(allowedUrls.stream().anyMatch(url::contains)) {
            filterChain.doFilter(request, response);
            return;
        }
        String requestedPath = ServerUtils.getRequestedUrl(url);
        if(requestedPath != null && (requestedPath.equals("") || requestedPath.equals("register"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession(false);
        User user;
        if(session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");

            setRoleToRequest(request, user);

            filterChain.doFilter(request, response);
        } else if(stringIsNotEmpty(email) && stringIsNotEmpty(password) &&
                (user = userDao.getUserByCredentials(email, password)) != null) {
            HttpSession newSession = request.getSession();
            newSession.setAttribute("user", user);

            setRoleToRequest(request, user);

            filterChain.doFilter(request, response);
        } else {
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    private void setRoleToRequest(HttpServletRequest request, User user) {
        int role = user.getRole();
        Role roleModel = Role.getRoleByKey(role);
        request.setAttribute("role", roleModel.getRoleString());
    }

    @Override
    public void destroy() {

    }
}