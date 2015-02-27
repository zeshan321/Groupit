package com.groupit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL {
    public static Connection con;
    private static String sMySQLAddr = "vps.snow-network.com";
    private static String sMySQLPort = "3306";
    private static String sMySQLDataBase = "groupit";
    private static String sMySQLUser = "groupit";
    private static String sMySQLPass = "u7QrcAFPhjhC7Sep";

    public static void startUp() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection("jdbc:mysql://" + sMySQLAddr + ":" + sMySQLPort + "/" + sMySQLDataBase+ "?user=" + sMySQLUser + "&password=" + sMySQLPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveValues(String s)
    {
        try
        {
            String sql = "UPDATE Messages SET Message = ? WHERE UUID = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, s);
            ps.setString(2, "1000");

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
