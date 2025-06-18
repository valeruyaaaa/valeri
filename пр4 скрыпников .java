
import .sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataAccessAPI {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private Connection connection;

    // Кэш для read-only данных
    private final Map<String, CachedData> readOnlyCache = new HashMap<>();

    // Замок для обеспечения потокобезопасности (т.к. 1 пользователь)
    private final Lock lock = new ReentrantLock();

    public DataAccessAPI(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    // Метод для установки соединения с БД (ленивая инициализация)
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Замените на драйвер вашей БД
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver not found: " + e.getMessage());
            }
        }
        return connection;
    }

    // Метод для загрузки данных (универсальный)
    public <T> T loadData(String query, boolean readOnly, Class<T> resultType) throws SQLException {
        lock.lock(); // Захватываем замок, чтобы обеспечить монопольный доступ
        try {
            if (readOnly) {
                if (readOnlyCache.containsKey(query)) {
                    CachedData cachedData = readOnlyCache.get(query);
                    if (cachedData.isValid()) {
                        System.out.println("Returning data from cache for query: " + query);
                        //noinspection unchecked
                        return (T) cachedData.getData();
                    } else {
                        System.out.println("Cache expired for query: " + query + ". Refreshing...");
                        Object data = fetchDataFromDB(query, resultType);
                        readOnlyCache.put(query, new CachedData(data));  // обновляем кэш
                        //noinspection unchecked
                        return (T) data;
                    }
                } else {
                    Object data = fetchDataFromDB(query, resultType);
                    readOnlyCache.put(query, new CachedData(data)); // Сохраняем в кэш
                    System.out.println("Fetching data from DB and caching for query: " + query);
                    //noinspection unchecked
                    return (T) data;
                }
            } else {
                System.out.println("Fetching non read-only data from DB for query: " + query);
                return fetchDataFromDB(query, resultType);
            }
        } finally {
            lock.unlock(); // Освобождаем замок
        }
    }

    // Метод для выгрузки данных в БД (универсальный)
    public void saveData(String query) throws SQLException {
        lock.lock(); // Захватываем замок
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
            System.out.println("Data saved/updated in DB using query: " + query);
            // Очищаем кэш, т.к. данные могли измениться.  Очищаем весь кэш, т.к. определить какие именно запросы invalidate - сложно.
            readOnlyCache.clear();
        } finally {
            lock.unlock(); // Освобождаем замок
        }
    }

    // Вспомогательный метод для получения данных из БД
    private <T> T fetchDataFromDB(String query, Class<T> resultType) throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            //  Пример обработки результатов (зависит от типа данных и resultType)
            if (resultSet.next()) {
                if (resultType == Integer.class) {
                    return resultType.cast(resultSet.getInt(1));
                } else if (resultType == String.class) {
                    return resultType.cast(resultSet.getString(1));
                }
                // Добавьте обработку других типов данных по мере необходимости
                else {
                    //  Обработка более сложных типов данных - зависит от конкретного случая и структуры БД
                    //  Например, можно возвращать объекты, если запрос SELECT * ...
                    System.out.println("Warning: Unsupported result type: " + resultType.getName() + " . Returning null.");
                    return null;
                }
            } else {
                return null; //  если нет данных
            }
        }
    }

    // Метод для закрытия соединения с БД
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // Класс для хранения кэшированных данных
    private static class CachedData {
        private final Object data;
        private final long creationTime;
        private static final long CACHE_EXPIRY_MS = 60000; // 1 минута

        public CachedData(Object data) {
            this.data = data;
            this.creationTime = System.currentTimeMillis();
        }

        public Object getData() {
            return data;
        }

        public boolean isValid() {
            return System.currentTimeMillis() - creationTime < CACHE_EXPIRY_MS;
        }
    }


    public static void main(String[] args) {
        // Пример использования

        // Замените на ваши параметры подключения к БД
        String dbUrl = "jdbc:mysql://localhost:3306/mydatabase";
        String dbUser = "myuser";
        String dbPassword = "mypassword";

        DataAccessAPI api = new DataAccessAPI(dbUrl, dbUser, dbPassword);

        try {
            // Пример запроса read-only данных (из кэша)
            String query1 = "SELECT COUNT(*) FROM users WHERE status = 'active'";
            Integer activeUsersCount = api.loadData(query1, true, Integer.class);
            System.out.println("Active users count: " + activeUsersCount);

            // Повторный запрос read-only данных (из кэша)
            Integer activeUsersCountCached = api.loadData(query1, true, Integer.class);
            System.out.println("Active users count (cached): " + activeUsersCountCached);

            // Ждем минуту чтобы кэш просрочился
            try {
                Thread.sleep(61000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Повторный запрос read-only данных (из кэша) - должен быть перезагружен.
            Integer activeUsersCountExpired = api.loadData(query1, true, Integer.class);
            System.out.println("Active users count (expired): " + activeUsersCountExpired);

            // Пример запроса данных, которые нельзя кэшировать
            String query2 = "SELECT name FROM users WHERE id = 1";
            String userName = api.loadData(query2, false, String.class);
            System.out.println("User name: " + userName);


            // Пример сохранения данных
            String updateQuery = "UPDATE products SET price = price * 1.1 WHERE category = 'electronics'";
            api.saveData(updateQuery);

        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        } finally {
            api.closeConnection();
        }
    }
}
