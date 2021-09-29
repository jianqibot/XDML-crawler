package com.github.jianqibot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Random;


public class MockDataGenerator {

    public static void main(String[] args) {
        mockGenerate(1_000_000);
    }

    @SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", "DMI_RANDOM_USED_ONLY_ONCE"})
    public static void mockGenerate(int mockNumber) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Random random = new Random();
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            int currentNewsNumber = session.selectOne("com.github.jianqibot.MockMapper.countTotalLink");
            try {
                for (int i = 0; i < mockNumber; i++) {
                    int selectNewsID = random.nextInt(currentNewsNumber) + 1;
                    News currentNews = session.selectOne("com.github.jianqibot.MockMapper.getRandomLink", selectNewsID);
                    Instant newTimeStamp = currentNews.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 365));
                    currentNews.setCreatedAt(newTimeStamp);
                    currentNews.setModifiedAt(newTimeStamp);
                    session.insert("com.github.jianqibot.MockMapper.insertMockedNewsLink", currentNews);
                    if (i % 10_000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
