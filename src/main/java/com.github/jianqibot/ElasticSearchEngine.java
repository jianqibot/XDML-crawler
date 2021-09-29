package com.github.jianqibot;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class ElasticSearchEngine {
    public static void main(String[] args) {
        while (true) {
            System.out.println("Please input key word for search");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            try {
                String keyWord = reader.readLine();
                search(keyWord);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static void search(String keyWord) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .query(new MultiMatchQueryBuilder(keyWord, "title", "content"));
            SearchRequest searchRequest = new SearchRequest("news");
            searchRequest.source(builder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            response.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
