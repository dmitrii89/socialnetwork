package dao;

import com.social.network.connective.Connective;
import com.social.network.dao.FriendsDao;
import com.social.network.dao.InitializationDao;
import com.social.network.dao.MessagesDao;
import com.social.network.dao.UserDao;

import java.sql.SQLException;

import static com.social.network.connection.ConnectionPool.getConnectionPool;

public class BaseDaoTest {
    protected static UserDao userDao;
    protected static MessagesDao messagesDao;
    protected static FriendsDao friendsDao;

    static {
        try {
            Connective connective = getConnectionPool(null)::getConnection;
            InitializationDao initializationDao = new InitializationDao(connective);
            initializationDao.initializeStubData();

            userDao = new UserDao(connective);
            messagesDao = new MessagesDao(connective);
            friendsDao = new FriendsDao(connective);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
