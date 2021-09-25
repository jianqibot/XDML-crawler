package com.github.jianqibot;

import java.io.IOException;
import java.sql.SQLException;
gstimport java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws SQLException, IOException {
        CrawlerDao dao = new MybatisCrawlerDao();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            executor.submit(new Crawler(dao));
        }
    }
}
