package com.github.jianqibot;

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
import java.util.stream.Collectors;

public class Crawler extends Thread {
    private final CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        String link;
        try {
            while ((link = dao.getUrlFromProcessingDBThenDelete()) != null) {
                if (!dao.isLinkAlreadyProcessed(link) && meetCriterion(link)) {
                    System.out.println(link);
                    dao.addLinkIntoProcessedDB(link);
                    Document doc = httpGetAndParseHtml(link);
                    CollectLinksAndStoreIntoProcessingDB(doc);
                    storeIntoDBIfIsNewsPage(doc, link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean meetCriterion(String link) {
        return (isNewsPage(link) || isHomePage(link)) && !isLoginPage(link);
    }

    private boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private boolean isHomePage(String link) {
        return link.equals("https://sina.cn");
    }

    private boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private Document httpGetAndParseHtml(String link) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            HttpGet httpGet = new HttpGet(link);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:92.0) Gecko/20100101 Firefox/92.0");
            try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                HttpEntity entity1 = response1.getEntity();
                String html = EntityUtils.toString(entity1);
                return Jsoup.parse(html);
            }
        }
    }

    private void CollectLinksAndStoreIntoProcessingDB(Document doc) throws SQLException {
        for (Element aTagLink : doc.select("a")) {
            String href = aTagLink.attr("href");
            dao.insertLinkIntoProcessingDB(href);
        }
    }

    private void storeIntoDBIfIsNewsPage (Document doc, String link) throws SQLException {
        ArrayList<Element> articleTagLinks = doc.select("article");
        if (!articleTagLinks.isEmpty()) {
            for (Element articleTagLink : articleTagLinks) {
                String title = articleTagLink.child(0).text();
                String content = articleTagLink.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
                dao.addLinkIntoNewsDB(title, content, link);
            }
        }
    }
}
