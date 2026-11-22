package com.pharmasync.headoffice.service;

import com.pharmasync.headoffice.entity.CentralBatchStock;
import com.pharmasync.headoffice.entity.Recall;
import com.pharmasync.headoffice.repository.CentralBatchStockRepository;
import com.pharmasync.headoffice.repository.RecallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecallService {

    private final RecallRepository recallRepository;
    private final CentralBatchStockRepository stockRepository;

    public RecallService(RecallRepository recallRepository,
                         CentralBatchStockRepository stockRepository) {
        this.recallRepository = recallRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Recall createRecall(String batchNumber, String medicineId, Instant recallTime, String reason) {
        Recall recall = new Recall(batchNumber, medicineId, recallTime, reason);
        return recallRepository.save(recall);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> stockPerBranchForRecall(String batchNumber) {
        List<CentralBatchStock> stocks = stockRepository.findAll().stream()
                .filter(s -> batchNumber.equals(s.getBatchNumber()))
                .collect(Collectors.toList());
        return stocks.stream()
                .collect(Collectors.toMap(CentralBatchStock::getBranchId,
                        CentralBatchStock::getQuantityOnHand,
                        Integer::sum));
    }
}
