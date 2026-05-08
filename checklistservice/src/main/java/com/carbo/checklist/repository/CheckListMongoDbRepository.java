package com.carbo.checklist.repository;

import com.carbo.checklist.model.CheckList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckListMongoDbRepository extends MongoRepository<CheckList, String> {
    List<CheckList> findByJobId(String jobId);
}
