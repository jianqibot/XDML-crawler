package com.github.jianqibot;

import java.sql.SQLException;

public interface CrawlerDao {
    String getUrlFromProcessingDBThenDelete() throws SQLException;

    void insertLinkIntoProcessingDB(String href) throws SQLException;

    void addLinkIntoProcessedDB(String link) throws SQLException;

    boolean isLinkAlreadyProcessed(String link) throws SQLException;

    void addLinkIntoNewsDB(String title, String content, String link) throws SQLException;
}
