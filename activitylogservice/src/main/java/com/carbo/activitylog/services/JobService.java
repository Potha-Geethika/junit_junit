package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.repository.JobMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobMongoDbRepository jobMongoDbRepository;

    @Autowired
    public JobService(JobMongoDbRepository jobRepository) {
        this.jobMongoDbRepository = jobRepository;
    }

    public List<Job> getByOrganizationId(String organizationId) {
        return jobMongoDbRepository.findByOrganizationId(organizationId);
    }

    public Optional<Job> getByOrganizationIdAndJobId(String organizationId, String jobId) {
        List<Job> fromDB = jobMongoDbRepository.findByOrganizationIdAndId(organizationId, jobId);
        return fromDB.size() > 0 ? Optional.of(fromDB.get(0)) : Optional.empty();
    }

    public List<Job> getJobByJobNumber(String jobNumber) {
        return jobMongoDbRepository.findByJobNumber(jobNumber);
    }

    public Optional<Job> findByJobId(String jobId) {
        return jobMongoDbRepository.findById(jobId);
    }
}
