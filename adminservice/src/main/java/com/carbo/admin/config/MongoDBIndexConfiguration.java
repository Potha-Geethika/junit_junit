package com.carbo.admin.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoDBIndexConfiguration {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("users");

        Collation collation = Collation.builder()
                .locale("en")
                .collationStrength(CollationStrength.SECONDARY) // strength 2 for case-insensitive
                .build();

        IndexOptions indexOptions = new IndexOptions().unique(true).collation(collation);

        Document indexKeys = new Document("userName", 1);

        collection.createIndex(indexKeys, indexOptions);
    }
}
