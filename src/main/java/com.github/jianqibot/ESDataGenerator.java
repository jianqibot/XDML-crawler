package com.github.jianqibot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESDataGenerator {
    public static void main(String[] args) {
        generateESData();
    }

    public static void generateESData() {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<News> dataFromMySQL = getDataFromDB(sqlSessionFactory);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> writeMockDataIntoESDataBase(dataFromMySQL)).start();
        }
    }

    private static void writeMockDataIntoESDataBase(List<News> dataFromDB) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 100; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News data : dataFromDB) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("title", data.getTitle());
                    jsonMap.put("content", data.getContent().substring(0, 5));
                    jsonMap.put("url", data.getUrl());
                    jsonMap.put("createdAt", data.getCreatedAt());
                    jsonMap.put("modifiedAt", data.getModifiedAt());
                    IndexRequest indexRequest = new IndexRequest("news").source(jsonMap);
                    bulkRequest.add(indexRequest);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Current Thread: " + Thread.currentThread().getName() +
                        " finishes " + i + "th bulk task, status: " + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static List<News> getDataFromDB(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.jianqibot.MockMapper.getLargeNumberOfLink");
        }
    }
}
