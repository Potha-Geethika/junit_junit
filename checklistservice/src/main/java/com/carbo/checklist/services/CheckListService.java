package com.carbo.checklist.services;

import com.carbo.checklist.repository.CheckListMongoDbRepository;
import com.carbo.checklist.model.CheckList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CheckListService {
    private final CheckListMongoDbRepository checkListRepository;

    @Autowired
    public CheckListService(CheckListMongoDbRepository checkListRepository) {
        this.checkListRepository = checkListRepository;
    }

    public List<CheckList> getAll() {
        return checkListRepository.findAll();
    }

    public List<CheckList> getByJobId(String jobId) {
        return checkListRepository.findByJobId(jobId);
    }

    public Optional<CheckList> getCheckList(String checkListId) {
        return checkListRepository.findById(checkListId);
    }

    public CheckList saveCheckList(CheckList checkList) {
        return checkListRepository.save(checkList);
    }

    public void updateCheckList(CheckList checkList) {
        checkListRepository.save(checkList);
    }

    public void deleteCheckList(String checkListId) {
        checkListRepository.deleteById(checkListId);
    }
}
