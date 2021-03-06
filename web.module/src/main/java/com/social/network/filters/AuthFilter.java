package com.social.network.filters;

import com.social.network.dao.UserDao;
import com.social.network.models.User;
import com.social.network.utils.Encryption;
import com.social.network.utils.ServerUtils;
import org.apache.log4j.Logger;

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
import static com.social.network.utils.ServerUtils.setRoleToRequest;
import static com.social.network.utils.ServerUtils.isNotBlank;

public class AuthFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthFilter.class);
    public static final String REDIRECT_USER_TO_PROFILE_PAGE = "Redirect user to profile page from ";
    public static final String USER_UNSUCCESSFULLY_TRIED_TO_LOG_IN = "User unsuccessfully tried to log in with email: ";

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

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        User user;
        if(session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");

            setRoleToRequest(request, user);

            if(requestedPath.equals("")) {
                logger.info(REDIRECT_USER_TO_PROFILE_PAGE + requestedPath);
                response.sendRedirect("/profile");
                return;
            }

            filterChain.doFilter(request, response);
            return;
        } else if(isNotBlank(email) && isNotBlank(password)) {
            password = Encryption.encryptPassword(password);

            if((user = userDao.getUserByCredentials(email, password)) != null) {
                if(user.getBlocked()) {
                    response.sendRedirect("service/blocked.jsp");
                    return;
                }

                session.setAttribute("user", user);

                setRoleToRequest(request, user);

                filterChain.doFilter(request, response);
                return;
            } else {
                logger.info(USER_UNSUCCESSFULLY_TRIED_TO_LOG_IN + email);
            }
        } else if(requestedPath.equals("")) {
            filterChain.doFilter(request, response);
            return;
        }
        response.sendRedirect("/");
    }

    @Override
    public void destroy() {}
}
