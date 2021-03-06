import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

// state: created --> checked --> confirmed --> paid --> finished
enum OrderState {
    CREATED,
    CHECKING,
    CONFIRMED,
    PAID,
    FINISHED,
}

public class ProcessSimulator {

    private Connection con;

    public ProcessSimulator(Connection con) {
        this.con = con;
    }

    public void createTables() throws SQLException {
        Statement stmt = this.con.createStatement();
        String createUsersSQL = "CREATE TABLE USERS (id INTEGER NOT NULL, name VARCHAR(20), PRIMARY KEY ( id ))";
        stmt.executeUpdate(createUsersSQL);
        System.out.println("Created users table");
        String createOrdersSQL = "CREATE TABLE ORDERS (id INTEGER NOT NULL, user_id INTEGER NULL, state VARCHAR(10), PRIMARY KEY ( id ), CONSTRAINT fk_users FOREIGN KEY(user_id) REFERENCES USERS(id))";
        stmt.executeUpdate(createOrdersSQL);
        System.out.println("Created orders table");
        String createInvoicesSQL = "CREATE TABLE INVOICES (id INTEGER NOT NULL, order_id INTEGER NULL, state VARCHAR(10), PRIMARY KEY ( id ), CONSTRAINT fk_orders FOREIGN KEY(order_id) REFERENCES ORDERS(id))";
        stmt.executeUpdate(createInvoicesSQL);
        System.out.println("Created invoices table");
    }

    public void dropTables() throws SQLException {
        Statement stmt = this.con.createStatement();
        String sql = "DROP TABLE INVOICES";
        stmt.executeUpdate(sql);
        System.out.println("Dropped invoices table");
        sql = "DROP TABLE ORDERS";
        stmt.executeUpdate(sql);
        System.out.println("Dropped orders table");
        sql = "DROP TABLE USERS";
        stmt.executeUpdate(sql);
        System.out.println("Dropped users table");
    }

    public void updateInvoice(int id) throws SQLException {
        String SQL = "UPDATE INVOICES SET state = 'sent' WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated invoice " + id + " state to 'sent'");
        } else {
            System.out.println("An error occurred updating invoice " + id);
        }
    }

    public void updateOrder(int id, OrderState state) throws SQLException {
        String stateValue = "";
        switch(state) {
            case CREATED: stateValue = "created"; break;
            case CHECKING: stateValue = "checking"; break;
            case CONFIRMED: stateValue = "confirmed"; break;
            case PAID: stateValue = "paid"; break;
            case FINISHED: stateValue = "finished"; break;
        }
        if (stateValue.equals("")) {
            System.out.println("Order state change to invalid state");
            return;
        }

        String SQL = "UPDATE ORDERS SET state = ? WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, stateValue);
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated order " + id + " state to '" + stateValue + "'");
        } else {
            System.out.println("An error occurred updating order " + id);
        }
    }

    public void addInvoice(int id, int orderId) throws SQLException {
        String SQL = "INSERT INTO INVOICES(id, order_id, state) VALUES(?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setInt(2, orderId);
        pstmt.setString(3, "created");
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created invoice " + id);
        } else {
            System.out.println("An error occurred creating invoice " + id);
        }
    }

    public void addOrder(int id, int userId) throws SQLException {
        String SQL = "INSERT INTO ORDERS(id, user_id, state) VALUES(?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setInt(2, userId);
        pstmt.setString(3, "created");
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created order " + id);
        } else {
            System.out.println("An error occurred creating order " + id);
        }
    }

    public void addUser(int id, String name) throws SQLException {
        String SQL = "INSERT INTO USERS(id, name) VALUES(?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setString(2, name);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created user " + id);
        } else {
            System.out.println("An error occurred creating user " + id);
        }
    }

    public void deleteOrder(int id) throws SQLException {
        String SQL = "DELETE FROM ORDERS WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted order " + id);
        } else {
            System.out.println("An error occurred deleting order " + id);
        }
    }

    public void deleteInvoice(int id) throws SQLException {
        String SQL = "DELETE FROM INVOICES WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted invoice " + id);
        } else {
            System.out.println("An error occurred deleting invoice " + id);
        }
    }

    public static void waitSecondsUpTo(int seconds) {
        long waitingTime = ThreadLocalRandom.current().nextLong(0, seconds * 1000);
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
        }
    }

    public static boolean decide(double threshold) {
        double decision = ThreadLocalRandom.current().nextDouble(0, 1);
        return decision <= threshold;
    }

    public static void simulateProcess(ProcessSimulator processSimulator, int id) throws SQLException {
        long startTime = System.currentTimeMillis();
        // Create 0 to 9 orders
        int numberOfOrders = ThreadLocalRandom.current().nextInt(0, 10); // So that users exist that never create an order
        for(int i = 0; i < numberOfOrders; i++) {
            int orderId = 10 * id + i;
            ProcessSimulator.waitSecondsUpTo(300);
            long orderStartTime = System.currentTimeMillis();
            processSimulator.addOrder(orderId, id);
            ProcessSimulator.waitSecondsUpTo(30);
            processSimulator.updateOrder(orderId, OrderState.CHECKING);
            ProcessSimulator.waitSecondsUpTo(120);
            if (ProcessSimulator.decide(0.2)) {
                // Order declined
                processSimulator.deleteOrder(orderId);
                long orderTimeDiff = (System.currentTimeMillis() - orderStartTime) / 1000;
                System.out.println("Declined order " + orderId + " (Instance time: " + orderTimeDiff + " s)");
                return;
            }
            processSimulator.updateOrder(orderId, OrderState.CONFIRMED);
            processSimulator.addInvoice(orderId, orderId);
            ProcessSimulator.waitSecondsUpTo(60);
            processSimulator.updateInvoice(orderId);
            if (ProcessSimulator.decide(0.3)) {
                // Order canceled
                processSimulator.deleteInvoice(orderId);
                processSimulator.deleteOrder(orderId);
                long orderTimeDiff = (System.currentTimeMillis() - orderStartTime) / 1000;
                System.out.println("Canceled order " + orderId + " (Instance time: " + orderTimeDiff + " s)");
                return;
            }
            processSimulator.updateOrder(orderId, OrderState.PAID);
            ProcessSimulator.waitSecondsUpTo(200);
            processSimulator.updateOrder(orderId, OrderState.FINISHED);
            long orderTimeDiff = (System.currentTimeMillis() - orderStartTime) / 1000;
            System.out.println("Finished order " + orderId + " (Instance time: " + orderTimeDiff + " s)");
        }
        long timeDiff = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Finished thread " + id + " in " + timeDiff + " s");
    }

    public void reset() throws SQLException {
        this.dropTables();
        this.createTables();
    }

    public static void main(String[] args) {

        String dbIp = "xxx.xxx.xxx.xxx";
        String dbPort = "1234";
        String dbUser = "system";
        String dbPassword = "oracle";

        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Connect to database");
            Connection con= DriverManager.getConnection(
                    "jdbc:oracle:thin:@" + dbIp + ":" + dbPort + "/orcl",dbUser,dbPassword);
            System.out.println("Connected");
            final ProcessSimulator processSimulator = new ProcessSimulator(con);
            processSimulator.reset();

            String[] names = new String[] {"Liam", "Emma", "Noah", "Olivia", "William", "Ava", "James", "Isabella", "Oliver"};

            int simulatedUsers = 49; // Due to db restrictions about number of open connections
            Thread[] threads = new Thread[simulatedUsers];
            long startTime = System.currentTimeMillis();

            // Start independent thread for each user
            for (int userId = 0; userId < simulatedUsers; userId++) {
                processSimulator.addUser(userId, names[userId % names.length]);
                final int id = userId;
                threads[userId] = new Thread(() -> {
                    try {
                        ProcessSimulator.simulateProcess(processSimulator, id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                threads[userId].start();
            }

            for (int userId = 0; userId < simulatedUsers; userId++) {
                threads[userId].join();
            }

            long timeDiff = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Finished Simulation in " + timeDiff + "s");

            con.close();
        }catch(Exception e){ System.out.println(e);}
    }
}
