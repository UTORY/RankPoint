package net.teamuni.rankpoint.data.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Database {
    Connection getConnection() throws SQLException;
    void initTable(Connection conn) throws SQLException;
    PreparedStatement getSelectStatement(Connection conn) throws SQLException;
    PreparedStatement getInsertStatement(Connection conn) throws SQLException;
}
