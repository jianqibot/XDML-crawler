package com.github.jianqibot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
    private static final String DB_USER_NAME = "jianqi";
    private static final String DB_USER_PASSWORD = "jianqi";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/home/jianqi/IdeaProjects/XDML-crawler/news", DB_USER_NAME, DB_USER_PASSWORD);
        List<String> linkPool = loadUrlsFromDB(connection);

        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(0);
            removeLinkFromProcessingDB(connection, link);
            if (!isLinkAlreadyProcessed(connection, link) && meetCriterion(link)) {
                System.out.println(link);
                insertLinkIntoProcessedDB(connection, link);
                Document doc = httpGetAndParseHtml(link);
                CollectLinksAndStoreIntoProcessingDB(connection, doc);
                storeIntoDBIfIsNewsPage(doc);
            }
        }
    }

    private static void CollectLinksAndStoreIntoProcessingDB(Connection connection, Document doc) throws SQLException {
        for (Element aTagLink : doc.select("a")) {
            String href = aTagLink.attr("href");
            insertLinkIntoProcessingDB(connection, href);
        }
    }

    private static List<String> loadUrlsFromDB(Connection connection) throws SQLException {
        List<String> list = new LinkedList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        }
        return  list;
    }

    private static void removeLinkFromProcessingDB(Connection connection, String link) throws SQLException {
        updateDB(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
    }


    private static void insertLinkIntoProcessingDB(Connection connection, String href) throws SQLException {
        updateDB(connection, href, "insert into LINKS_TO_BE_PROCESSED (link) values(?)");
    }


    private static void insertLinkIntoProcessedDB(Connection connection, String link) throws SQLException {
        updateDB(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");
    }

    private static void updateDB(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    private static boolean isLinkAlreadyProcessed(Connection connection, String link) throws SQLException {
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

    private static boolean meetCriterion(String link) {
        return (isNewsPage(link) || isHomePage(link)) && !isLoginPage(link);
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private static boolean isHomePage(String link) {
        return link.equals("https://sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            HttpGet httpGet = new HttpGet(link);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:92.0) Gecko/20100101 Firefox/92.0");
            try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                System.out.println(response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                String html = EntityUtils.toString(entity1);
                return Jsoup.parse(html);
            }
        }
    }

    private static void storeIntoDBIfIsNewsPage (Document doc) {
        ArrayList<Element> articleTagLinks = doc.select("article");
        if (!articleTagLinks.isEmpty()) {
            for (Element articleTagLink : articleTagLinks) {
                String title = articleTagLink.select("h1").text();
                System.out.println(title);
            }
        }
    }
}
