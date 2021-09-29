package com.github.jianqibot;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, IOException {
        CrawlerDao dao = new MybatisCrawlerDao();
        for (int i = 0; i < 8; i++) {
            new Crawler(dao).start();
        }
    }
}
