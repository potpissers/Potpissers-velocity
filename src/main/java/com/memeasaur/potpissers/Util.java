package com.memeasaur.potpissers;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import static com.memeasaur.potpissers.Listeners.*;

public class Util {
    public static void executeQueryOptionalRunnable(String query, Object[] params, Runnable optionalClosingLambda) {
        proxy.getScheduler().buildTask(plugin, () -> {
            try (Connection connection = PQ_POOL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++)
                    preparedStatement.setObject(i + 1, params[i]);
                preparedStatement.execute();
                if (optionalClosingLambda != null)
                    optionalClosingLambda.run();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }).schedule();
    }
//    public static void executeQueryOptionalDictConsumer(String query, @Nullable Object[] objects, Consumer<Optional<HashMap<String, Object>>> consumer) {
//        scheduler.buildTask(plugin, () -> {
//            try (Connection connection = PQ_POOL.getConnection(); PreparedStatement preparedStatement = getOpenPreparedStatement(connection, query, objects); ResultSet resultSet = preparedStatement.executeQuery()) {
//
//                final HashMap<String, Object> resultDict;
//                if (resultSet.next()) {
//                    resultDict = new HashMap<>();
//                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
//                    for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
//                        resultDict.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));
//                }
//                else
//                    resultDict = null;
//                consumer.accept(Optional.ofNullable(resultDict));
//
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }).schedule();
//    }
//    private static PreparedStatement getOpenPreparedStatement(Connection connection, String query, @Nullable Object[] orderedList) throws SQLException {
//        PreparedStatement preparedStatement = connection.prepareStatement(query);
//        handlePreparedStatementObjects(orderedList, 1, preparedStatement, connection);
//        return preparedStatement;
//    }
//    private static void handlePreparedStatementObjects(Object[] objects, int index, PreparedStatement preparedStatement, Connection connection) throws SQLException {
//        if (objects != null) {
//            int i = index;
//            for (Object object : objects) {
//                if (object instanceof UUID uuid)
//                    preparedStatement.setObject(i, uuid, Types.OTHER); // TODO -> this sucks
//                else if (object instanceof UUID[] uuids)
//                    preparedStatement.setArray(i, connection.createArrayOf("uuid", uuids));
//                else
//                    preparedStatement.setObject(i, object);
//                i++;
//            }
//        }
//    }

    private static final SecretKey IP_REFERRAL_IP_HMAC_KEY = new SecretKeySpec(System.getenv("JAVA_AES_IP_REFERRAL_IP_KEY").getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    public static byte[] getIpBytes(String ip) { // TODO -> module or whatever to share functions with plugin
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(IP_REFERRAL_IP_HMAC_KEY);
            return mac.doFinal(ip.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
