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
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new LinkedList<>();
        Set<String> processedLinkPool = new HashSet<>();
        linkPool.add("https://sina.cn/");

        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(0);
            if (processedLinkPool.contains(link) || !meetCriterion(link)) {
                continue;
            }
            Document doc = httpGetAndParseHtml(link);
            processedLinkPool.add(link);
            System.out.println(link);
            doc.select("a").stream().map(aTaglink->aTaglink.attr("href")).forEach(linkPool::add);
            storeIntoDBIfIsNewsPage(doc);
        }
    }


    private static boolean meetCriterion(String link) {
        return isNewsPage(link) || isHomePage(link) && !isLoginPage(link);
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private static boolean isHomePage(String link) {
        return link.equals("https://sina.cn/");
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
                // do something useful with the response body
                // and ensure it is fully consumed
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