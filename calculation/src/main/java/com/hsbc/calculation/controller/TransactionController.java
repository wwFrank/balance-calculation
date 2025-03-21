package com.hsbc.calculation.controller;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.result.TransactionResult;
import com.hsbc.calculation.service.TransactionService;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/process")
    public TransactionResult<TransactionDO> processTransaction(@RequestBody TransactionDO transaction) {
        try {
            if(StringUtils.isBlank(transaction.getSourceAccountNumber())) {
                return TransactionResult.error(TransactionConstants.TRANSACTION_SOURCE_BLANK);
            }
            if(StringUtils.isBlank(transaction.getTargetAccountNumber())) {
                return TransactionResult.error(TransactionConstants.TRANSACTION_TARGET_BLANK);
            }
            TransactionDO transactionDO = transactionService.processTransaction(transaction);
            return TransactionResult.success(transactionDO);
        } catch (Exception e) {
            logger.warn("TransactionController calls transactionService.processTransaction transaction="+transaction, e);
            return TransactionResult.error(TransactionConstants.TRANSACTION_SERVER_FAILED);
        }
    }
}
