package com.github.jianqibot;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao{
    private final SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUrlFromProcessingDBThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.github.jianqibot.MyMapper.getNextLink");
            if (url != null) {
                session.delete("com.github.jianqibot.MyMapper.deleteLink", url);
            }
            return url;
        }
    }

    @Override
    public void insertLinkIntoProcessingDB(String href) throws SQLException {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_TO_BE_PROCESSED");
        param.put("link", href);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.jianqibot.MyMapper.insertLink", param);
        }
    }

    @Override
    public void addLinkIntoProcessedDB(String link) throws SQLException {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.jianqibot.MyMapper.insertLink", param);
        }
    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.jianqibot.MyMapper.countLink");
            return count != 0;
        }
    }

    @Override
    public void addLinkIntoNewsDB(String title, String content, String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.jianqibot.MyMapper.insertNewsLink", new News(title, content, link));
        }
    }
}
