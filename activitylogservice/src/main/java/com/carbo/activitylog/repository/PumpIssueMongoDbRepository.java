package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.PumpIssue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PumpIssueMongoDbRepository extends MongoRepository<PumpIssue, String> {

    List<PumpIssue> findByIdIn(List<String> equipmentIssueId);

    @Query("{ '_id': { $in: ?0 } }")
    void deleteByIds(List<String> ids);
}
