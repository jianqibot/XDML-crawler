package com.github.jianqibot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws SQLException, IOException {
        CrawlerDao dao = new MybatisCrawlerDao();
/*       for (int i = 0; i < 8; i++) {
           new Crawler(dao).start();
       }*/

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            executor.submit(new Crawler(dao));
        }
    }
}
