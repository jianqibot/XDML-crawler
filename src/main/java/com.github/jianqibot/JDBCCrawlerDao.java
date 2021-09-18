package com.github.jianqibot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class JDBCCrawlerDao implements CrawlerDao{
    private static final String DB_USER_NAME = "jianqi";
    private static final String DB_USER_PASSWORD = "jianqi";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JDBCCrawlerDao() {
        try {
            this.connection =  DriverManager.getConnection("jdbc:h2:file:/home/jianqi/IdeaProjects/XDML-crawler/news", DB_USER_NAME, DB_USER_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUrlFromProcessingDBThenDelete() throws SQLException {
        String link;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED limit 1")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                link = resultSet.getString(1);
                removeLinkFromProcessingDB(link);
                return link;
            }
        }
        return  null;
    }

    public void removeLinkFromProcessingDB(String link) throws SQLException {
        updateDB(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
    }

    @Override
    public void insertLinkIntoProcessingDB(String href) throws SQLException {
        updateDB(href, "insert into LINKS_TO_BE_PROCESSED (link) values(?)");
    }

    @Override
    public void addLinkIntoProcessedDB(String link) throws SQLException {
        updateDB(link, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");
    }

    public void updateDB(String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public void addLinkIntoNewsDB(String title, String content, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (title, content, url, created_at, modified_at) values(?, ?, ?, now(), now())")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, link);
            preparedStatement.executeUpdate();
        }
    }

}
