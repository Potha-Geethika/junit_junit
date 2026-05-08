package com.carbo.activitylog.services;

import com.carbo.activitylog.model.DeletedActivityLogEntry;
import com.carbo.activitylog.repository.DeletedActivityLogMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeletedActivityLogService {
    private final DeletedActivityLogMongoDbRepository deletedActivityLogMongoDbRepository;

    @Autowired
    public DeletedActivityLogService(DeletedActivityLogMongoDbRepository deletedActivityLogMongoDbRepository) {
        this.deletedActivityLogMongoDbRepository = deletedActivityLogMongoDbRepository;
    }

    public List<DeletedActivityLogEntry> getByOrganizationId(String organizationId) {
        return deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId);
    }

    public Optional<DeletedActivityLogEntry> getActivityLog(String activityLogId) {
        return deletedActivityLogMongoDbRepository.findById(activityLogId);
    }

    public List<DeletedActivityLogEntry> findByOrganizationIdAndJobId(String organizationId, String jobId) {
        return deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId);
    }

    public List<DeletedActivityLogEntry> findByOrganizationIdAndJobIdAndWellAndStage(String organizationId, String jobId, String well, Float stage) {
        return deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
    }

    public List<DeletedActivityLogEntry> findByOrganizationIdAndJobIdAndDay(String organizationId, String jobId, Integer day) {
        return deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
    }

    public DeletedActivityLogEntry saveActivityLog(DeletedActivityLogEntry activityLog) {
        return deletedActivityLogMongoDbRepository.save(activityLog);
    }

    public void updateActivityLog(DeletedActivityLogEntry activityLog) {
        deletedActivityLogMongoDbRepository.save(activityLog);
    }

    public void deleteActivityLog(String activityLogId) {
        deletedActivityLogMongoDbRepository.deleteById(activityLogId);
    }
}
