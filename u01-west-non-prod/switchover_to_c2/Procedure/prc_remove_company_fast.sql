CREATE
OR REPLACE PROCEDURE prc_remove_company_fast(uniqueid IN VARCHAR)  LANGUAGE plpgsql AS
    $$
BEGIN

  --Company -> AnnualBillingItem
DELETE
FROM PSP_ANNUAL_BILLING_ITEM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> ATFPaymentsToProcess
DELETE
FROM PSP_ATFPAYMENTS_TO_PROCESS
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CheckPrintPaycheck
DELETE
FROM PSP_CHECK_PRINT_PAYCHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE ORIGINAL_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE ORIGINAL_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE ORIGINAL_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE ORIGINAL_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE VOID_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE ORIGINAL_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE ORIGINAL_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMP_ADJUST_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE ORIGINAL_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMP_ADJUST_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE VOID_SUBMISSION_FK IN (
                    SELECT COMP_ADJUST_SUBMISSION_SEQ
                    FROM PSP_COMP_ADJUST_SUBMISSION
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE COMP_ADJUST_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE VOID_SUBMISSION_FK IN (
                SELECT COMP_ADJUST_SUBMISSION_SEQ
                FROM PSP_COMP_ADJUST_SUBMISSION
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMP_ADJUST_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE VOID_SUBMISSION_FK IN (
            SELECT COMP_ADJUST_SUBMISSION_SEQ
            FROM PSP_COMP_ADJUST_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE VOID_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE VOID_SUBMISSION_FK IN (
        SELECT COMP_ADJUST_SUBMISSION_SEQ
        FROM PSP_COMP_ADJUST_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE VOID_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE COMP_ADJUST_SUBMISSION_FK IN (
    SELECT COMP_ADJUST_SUBMISSION_SEQ
    FROM PSP_COMP_ADJUST_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAdjustmentSubmission
DELETE
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyAgency -> ACHEnrollment -> ACHEnrollmentDetail
DELETE
FROM PSP_ACHENROLLMENT_DETAIL
WHERE A_C_H_ENROLLMENT_FK IN (
    SELECT ACHENROLLMENT_SEQ
    FROM PSP_ACHENROLLMENT
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> ACHEnrollment
DELETE
FROM PSP_ACHENROLLMENT
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> CompanyLaw -> PayrollItemTaxableTo
DELETE
FROM PSP_PAYROLL_ITEM_TAXABLE_TO
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> Tax
DELETE
FROM PSP_TAX
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> EmployeeLawQtrTotals
DELETE
FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLawRate
DELETE
FROM PSP_COMPANY_LAW_RATE
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> PayrollItemTaxableTo
DELETE
FROM PSP_PAYROLL_ITEM_TAXABLE_TO
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> Tax
DELETE
FROM PSP_TAX
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeLawQtrTotals
DELETE
FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> CompanyLawRate
DELETE
FROM PSP_COMPANY_LAW_RATE
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> CompanyLaw
DELETE
FROM PSP_COMPANY_LAW
WHERE ADDITIONAL_COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeTax -> TaxTableMiscData
DELETE
FROM PSP_TAX_TABLE_MISC_DATA
WHERE EMPLOYEE_TAX_FK IN (
    SELECT EMPLOYEE_TAX_SEQ
    FROM PSP_EMPLOYEE_TAX
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE ADDITIONAL_COMPANY_LAW_FK IN (
            SELECT COMPANY_LAW_SEQ
            FROM PSP_COMPANY_LAW
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeTax
DELETE
FROM PSP_EMPLOYEE_TAX
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> QbdtPayrollItemInfo
DELETE
FROM PSP_QBDT_PAYROLL_ITEM_INFO
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE ADDITIONAL_COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> CompanyLaw
DELETE
FROM PSP_COMPANY_LAW
WHERE ADDITIONAL_COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> EmployeeTax -> TaxTableMiscData
DELETE
FROM PSP_TAX_TABLE_MISC_DATA
WHERE EMPLOYEE_TAX_FK IN (
    SELECT EMPLOYEE_TAX_SEQ
    FROM PSP_EMPLOYEE_TAX
    WHERE COMPANY_LAW_FK IN (
        SELECT COMPANY_LAW_SEQ
        FROM PSP_COMPANY_LAW
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> EmployeeTax
DELETE
FROM PSP_EMPLOYEE_TAX
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw -> QbdtPayrollItemInfo
DELETE
FROM PSP_QBDT_PAYROLL_ITEM_INFO
WHERE COMPANY_LAW_FK IN (
    SELECT COMPANY_LAW_SEQ
    FROM PSP_COMPANY_LAW
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyLaw
DELETE
FROM PSP_COMPANY_LAW
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> CompanyAgencyFormTemplate
DELETE
FROM PSP_COMPANYAGENCY_FRMTEMPLATE
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE TAX_PENALTY_INTEREST_FK IN (
            SELECT TAX_PENALTY_INTEREST_SEQ
            FROM PSP_TAX_PENALTY_INTEREST
            WHERE COMPANY_AGENCY_FK IN (
                SELECT COMPANY_AGENCY_SEQ
                FROM PSP_COMPANY_AGENCY
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE TAX_PENALTY_INTEREST_FK IN (
        SELECT TAX_PENALTY_INTEREST_SEQ
        FROM PSP_TAX_PENALTY_INTEREST
        WHERE COMPANY_AGENCY_FK IN (
            SELECT COMPANY_AGENCY_SEQ
            FROM PSP_COMPANY_AGENCY
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE TAX_PENALTY_INTEREST_FK IN (
    SELECT TAX_PENALTY_INTEREST_SEQ
    FROM PSP_TAX_PENALTY_INTEREST
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> TaxPenaltyInterest
DELETE
FROM PSP_TAX_PENALTY_INTEREST
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> RAFEnrollment -> RAFEnrollmentDetail
DELETE
FROM PSP_RAFENROLLMENT_DETAIL
WHERE R_A_F_ENROLLMENT_FK IN (
    SELECT RAFENROLLMENT_SEQ
    FROM PSP_RAFENROLLMENT
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> RAFEnrollment
DELETE
FROM PSP_RAFENROLLMENT
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> EftpsEnrollment -> EftpsEnrollmentDetail
DELETE
FROM PSP_EFTPS_ENROLLMENT_DETAIL
WHERE EFTPS_ENROLLMENT_FK IN (
    SELECT EFTPS_ENROLLMENT_SEQ
    FROM PSP_EFTPS_ENROLLMENT
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> EftpsEnrollment
DELETE
FROM PSP_EFTPS_ENROLLMENT
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> CompanyRateRequest
DELETE
FROM PSP_COMPANY_RATE_REQUEST
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyFilingAmount
DELETE
FROM PSP_COMPANY_FILING_AMOUNT
WHERE COMPANY_AGENCY_PMT_TEMPLATE_FK IN (
    SELECT COMPANYAGENCY_PMTTEMPLATE_SEQ
    FROM PSP_COMPANYAGENCY_PMTTEMPLATE
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyPaymentTemplateAgencyId
DELETE
FROM PSP_COMP_PMT_TEMPLATE_AGENCYID
WHERE COMPANY_AGENCY_PMT_TEMPLATE_FK IN (
    SELECT COMPANYAGENCY_PMTTEMPLATE_SEQ
    FROM PSP_COMPANYAGENCY_PMTTEMPLATE
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> EffectiveDepositFrequency
DELETE
FROM PSP_EFFECTIVE_DEPOSIT_FREQ
WHERE COMPANY_AGENCY_PMT_TEMPLATE_FK IN (
    SELECT COMPANYAGENCY_PMTTEMPLATE_SEQ
    FROM PSP_COMPANYAGENCY_PMTTEMPLATE
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyPaymentTemplatePaymentMethod
DELETE
FROM PSP_COMP_PMTTEMPLATE_PMTMETHOD
WHERE COMPANY_AGENCY_PMT_TEMPLATE_FK IN (
    SELECT COMPANYAGENCY_PMTTEMPLATE_SEQ
    FROM PSP_COMPANYAGENCY_PMTTEMPLATE
    WHERE COMPANY_AGENCY_FK IN (
        SELECT COMPANY_AGENCY_SEQ
        FROM PSP_COMPANY_AGENCY
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyAgency -> CompanyAgencyPaymentTemplate
DELETE
FROM PSP_COMPANYAGENCY_PMTTEMPLATE
WHERE COMPANY_AGENCY_FK IN (
    SELECT COMPANY_AGENCY_SEQ
    FROM PSP_COMPANY_AGENCY
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyAgency
DELETE
FROM PSP_COMPANY_AGENCY
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyDailyLiability
DELETE
FROM PSP_COMPANY_DAILY_LIABILITY
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyEvent -> CompanyEventEmail -> CompanyEventEmailParam
DELETE
FROM PSP_COMPANY_EVENT_EMAIL_PARAM
WHERE COMPANY_EVENT_EMAIL_FK IN (
    SELECT COMPANY_EVENT_EMAIL_SEQ
    FROM PSP_COMPANY_EVENT_EMAIL
    WHERE COMPANY_EVENT_FK IN (
        SELECT COMPANY_EVENT_SEQ
        FROM PSP_COMPANY_EVENT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyEvent -> CompanyEventEmail
DELETE
FROM PSP_COMPANY_EVENT_EMAIL
WHERE COMPANY_EVENT_FK IN (
    SELECT COMPANY_EVENT_SEQ
    FROM PSP_COMPANY_EVENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEvent -> CompanyEventDetail
DELETE
FROM PSP_COMPANY_EVENT_DETAIL
WHERE COMPANY_EVENT_FK IN (
    SELECT COMPANY_EVENT_SEQ
    FROM PSP_COMPANY_EVENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEvent -> CompanyNote
DELETE
FROM PSP_COMPANY_NOTE
WHERE COMPANY_EVENT_FK IN (
    SELECT COMPANY_EVENT_SEQ
    FROM PSP_COMPANY_EVENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEvent -> FraudEvent
DELETE
FROM PSP_FRAUD_EVENT
WHERE COMPANY_EVENT_FK IN (
    SELECT COMPANY_EVENT_SEQ
    FROM PSP_COMPANY_EVENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEvent -> EventAs400Sync
DELETE
FROM PSP_EVENT_AS400_SYNC
WHERE COMPANY_EVENT_FK IN (
    SELECT COMPANY_EVENT_SEQ
    FROM PSP_COMPANY_EVENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEvent
DELETE
FROM PSP_COMPANY_EVENT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyEventDetail
DELETE
FROM PSP_COMPANY_EVENT_DETAIL
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyEventEmail -> CompanyEventEmailParam
DELETE
FROM PSP_COMPANY_EVENT_EMAIL_PARAM
WHERE COMPANY_EVENT_EMAIL_FK IN (
    SELECT COMPANY_EVENT_EMAIL_SEQ
    FROM PSP_COMPANY_EVENT_EMAIL
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyEventEmail
DELETE
FROM PSP_COMPANY_EVENT_EMAIL
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyEventEmailParam
DELETE
FROM PSP_COMPANY_EVENT_EMAIL_PARAM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyOffer
DELETE
FROM PSP_COMPANY_OFFER
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyOffering
DELETE
FROM PSP_COMPANY_OFFERING
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyPaycheckBatch -> CheckPrintPaycheck
DELETE
FROM PSP_CHECK_PRINT_PAYCHECK
WHERE COMPANY_PAYCHECK_BATCH_FK IN (
    SELECT COMPANY_PAYCHECK_BATCH_SEQ
    FROM PSP_COMPANY_PAYCHECK_BATCH
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPaycheckBatch
DELETE
FROM PSP_COMPANY_PAYCHECK_BATCH
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyPayrollItem -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> PayrollItemTaxableTo
DELETE
FROM PSP_PAYROLL_ITEM_TAXABLE_TO
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> EmployeePayrollItem
DELETE
FROM PSP_EMPLOYEE_PAYROLL_ITEM
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> EmployeePayrollItemQtrTotals
DELETE
FROM PSP_EE_PAYROLLITEM_QTRTOTALS
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
            SELECT COMPANY_PAYROLL_ITEM_SEQ
            FROM PSP_COMPANY_PAYROLL_ITEM
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
            SELECT COMPANY_PAYROLL_ITEM_SEQ
            FROM PSP_COMPANY_PAYROLL_ITEM
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
            SELECT COMPANY_PAYROLL_ITEM_SEQ
            FROM PSP_COMPANY_PAYROLL_ITEM
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> PayrollItemTaxableTo
DELETE
FROM PSP_PAYROLL_ITEM_TAXABLE_TO
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeePayrollItem
DELETE
FROM PSP_EMPLOYEE_PAYROLL_ITEM
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeePayrollItemQtrTotals
DELETE
FROM PSP_EE_PAYROLLITEM_QTRTOTALS
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> CompanyPayrollItem
DELETE
FROM PSP_COMPANY_PAYROLL_ITEM
WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
            SELECT COMPANY_PAYROLL_ITEM_SEQ
            FROM PSP_COMPANY_PAYROLL_ITEM
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollItemInfo
DELETE
FROM PSP_QBDT_PAYROLL_ITEM_INFO
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
            SELECT COMPANY_PAYROLL_ITEM_SEQ
            FROM PSP_COMPANY_PAYROLL_ITEM
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> CompanyPayrollItem
DELETE
FROM PSP_COMPANY_PAYROLL_ITEM
WHERE ADDITIONAL_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> QbdtPayrollItemInfo
DELETE
FROM PSP_QBDT_PAYROLL_ITEM_INFO
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE COMPANY_PAYROLL_ITEM_FK IN (
        SELECT COMPANY_PAYROLL_ITEM_SEQ
        FROM PSP_COMPANY_PAYROLL_ITEM
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> CompanyPayrollItem -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE COMPANY_PAYROLL_ITEM_FK IN (
    SELECT COMPANY_PAYROLL_ITEM_SEQ
    FROM PSP_COMPANY_PAYROLL_ITEM
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyPayrollItem
DELETE
FROM PSP_COMPANY_PAYROLL_ITEM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyPIN
DELETE
FROM PSP_COMPANY_PIN
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyTFSSubmission
DELETE
FROM PSP_COMPANY_TFSSUBMISSION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> DepositFrequencyFileRec
DELETE
FROM PSP_DEPOSIT_FREQUENCY_FILE_REC
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> DisburseAdvice -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE TIPS_LIABILITY_FK IN (
    SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
    FROM PSP_DISBURSE_ADVICE_TAX_LIAB
    WHERE TIPS_LIABILITY_FK IN (
        SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
        FROM PSP_DISBURSE_ADVICE_TAX_LIAB
        WHERE DISBURSE_ADVICE_FK IN (
            SELECT DISBURSE_ADVICE_SEQ
            FROM PSP_DISBURSE_ADVICE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> DisburseAdvice -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE TIPS_LIABILITY_FK IN (
    SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
    FROM PSP_DISBURSE_ADVICE_TAX_LIAB
    WHERE DISBURSE_ADVICE_FK IN (
        SELECT DISBURSE_ADVICE_SEQ
        FROM PSP_DISBURSE_ADVICE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> DisburseAdvice -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE DISBURSE_ADVICE_FK IN (
    SELECT DISBURSE_ADVICE_SEQ
    FROM PSP_DISBURSE_ADVICE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> DisburseAdvice
DELETE
FROM PSP_DISBURSE_ADVICE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE TIPS_LIABILITY_FK IN (
    SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
    FROM PSP_DISBURSE_ADVICE_TAX_LIAB
    WHERE TIPS_LIABILITY_FK IN (
        SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
        FROM PSP_DISBURSE_ADVICE_TAX_LIAB
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE TIPS_LIABILITY_FK IN (
    SELECT DISBURSE_ADVICE_TAX_LIAB_SEQ
    FROM PSP_DISBURSE_ADVICE_TAX_LIAB
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> DisburseAdviceTaxLiab
DELETE
FROM PSP_DISBURSE_ADVICE_TAX_LIAB
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EdiPaymentDetail
DELETE
FROM PSP_EDI_PAYMENT_DETAIL
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EftpsPaymentDetail
DELETE
FROM PSP_EFTPS_PAYMENT_DETAIL
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EmployeeLawQtrTotals
DELETE
FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EmpTotalsPayrollRun
DELETE
FROM PSP_EMP_TOTALS_PAYROLL_RUN
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EntitlementUnit
DELETE
FROM PSP_ENTITLEMENT_UNIT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EntityChange
DELETE
FROM PSP_ENTITY_CHANGE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EntityUpdate
DELETE
FROM PSP_ENTITY_UPDATE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EntryDetailRecord
DELETE
FROM PSP_ENTRY_DETAIL_RECORD
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EventAs400Sync
DELETE
FROM PSP_EVENT_AS400_SYNC
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FraudAddress
DELETE
FROM PSP_FRAUD_ADDRESS
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FraudBankAccount
DELETE
FROM PSP_FRAUD_BANK_ACCOUNT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FraudCompany
DELETE
FROM PSP_FRAUD_COMPANY
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FraudContact
DELETE
FROM PSP_FRAUD_CONTACT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FraudEvent
DELETE
FROM PSP_FRAUD_EVENT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FsetFilingDetail
DELETE
FROM PSP_FSET_FILING_DETAIL
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> LedgerBalance
DELETE
FROM PSP_LEDGER_BALANCE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> LiabilityCheck -> LiabilityCheckBillingDetailAssoc
DELETE
FROM PSP_LIAB_CHECK_BILLING_ASSOC
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> LiabilityCheck -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE LIABILITY_CHECK_FK IN (
        SELECT LIABILITY_CHECK_SEQ
        FROM PSP_LIABILITY_CHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> LiabilityCheck -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> LiabilityCheck -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> LiabilityCheck
DELETE
FROM PSP_LIABILITY_CHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> OnHoldReason -> FinancialTransaction
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE ON_HOLD_REASON_FK IN (
    SELECT ON_HOLD_REASON_SEQ
    FROM PSP_ON_HOLD_REASON
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> OnHoldReason
DELETE
FROM PSP_ON_HOLD_REASON
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PaycheckUsage -> PaycheckUsageHist
DELETE
FROM PSP_PAYCHECK_USAGE_HIST
WHERE PAYCHECK_USAGE_FK IN (
    SELECT PAYCHECK_USAGE_SEQ
    FROM PSP_PAYCHECK_USAGE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PaycheckUsage
DELETE
FROM PSP_PAYCHECK_USAGE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PaycheckUsageHist
DELETE
FROM PSP_PAYCHECK_USAGE_HIST
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE BILL_PAYMENT_SPLIT_FK IN (
    SELECT BILL_PAYMENT_SPLIT_SEQ
    FROM PSP_BILL_PAYMENT_SPLIT
    WHERE BILL_PAYMENT_FK IN (
        SELECT BILL_PAYMENT_SEQ
        FROM PSP_BILL_PAYMENT
        WHERE PAYEE_FK IN (
            SELECT PAYEE_SEQ
            FROM PSP_PAYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Payee -> BillPayment -> BillPaymentSplit
DELETE
FROM PSP_BILL_PAYMENT_SPLIT
WHERE BILL_PAYMENT_FK IN (
    SELECT BILL_PAYMENT_SEQ
    FROM PSP_BILL_PAYMENT
    WHERE PAYEE_FK IN (
        SELECT PAYEE_SEQ
        FROM PSP_PAYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Payee -> BillPayment
DELETE
FROM PSP_BILL_PAYMENT
WHERE PAYEE_FK IN (
    SELECT PAYEE_SEQ
    FROM PSP_PAYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE PAYEE_BANK_ACCOUNT_FK IN (
                SELECT PAYEE_BANK_ACCOUNT_SEQ
                FROM PSP_PAYEE_BANK_ACCOUNT
                WHERE PAYEE_FK IN (
                    SELECT PAYEE_SEQ
                    FROM PSP_PAYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE PAYEE_BANK_ACCOUNT_FK IN (
            SELECT PAYEE_BANK_ACCOUNT_SEQ
            FROM PSP_PAYEE_BANK_ACCOUNT
            WHERE PAYEE_FK IN (
                SELECT PAYEE_SEQ
                FROM PSP_PAYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE BILL_PAYMENT_SPLIT_FK IN (
    SELECT BILL_PAYMENT_SPLIT_SEQ
    FROM PSP_BILL_PAYMENT_SPLIT
    WHERE PAYEE_BANK_ACCOUNT_FK IN (
        SELECT PAYEE_BANK_ACCOUNT_SEQ
        FROM PSP_PAYEE_BANK_ACCOUNT
        WHERE PAYEE_FK IN (
            SELECT PAYEE_SEQ
            FROM PSP_PAYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Payee -> PayeeBankAccount -> BillPaymentSplit
DELETE
FROM PSP_BILL_PAYMENT_SPLIT
WHERE PAYEE_BANK_ACCOUNT_FK IN (
    SELECT PAYEE_BANK_ACCOUNT_SEQ
    FROM PSP_PAYEE_BANK_ACCOUNT
    WHERE PAYEE_FK IN (
        SELECT PAYEE_SEQ
        FROM PSP_PAYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Payee -> PayeeBankAccount
DELETE
FROM PSP_PAYEE_BANK_ACCOUNT
WHERE PAYEE_FK IN (
    SELECT PAYEE_SEQ
    FROM PSP_PAYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Payee
DELETE
FROM PSP_PAYEE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PaymentBatchAssoc
DELETE
FROM PSP_PAYMENT_BATCH_ASSOC
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PriorPaymentSubmission -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE PRIOR_PAYMENT_SUBMISSION_FK IN (
    SELECT PRIOR_PAYMENT_SUBMISSION_SEQ
    FROM PSP_PRIOR_PAYMENT_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE PRIOR_PAYMENT_SUBMISSION_FK IN (
            SELECT PRIOR_PAYMENT_SUBMISSION_SEQ
            FROM PSP_PRIOR_PAYMENT_SUBMISSION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE PRIOR_PAYMENT_SUBMISSION_FK IN (
        SELECT PRIOR_PAYMENT_SUBMISSION_SEQ
        FROM PSP_PRIOR_PAYMENT_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE PRIOR_PAYMENT_SUBMISSION_FK IN (
        SELECT PRIOR_PAYMENT_SUBMISSION_SEQ
        FROM PSP_PRIOR_PAYMENT_SUBMISSION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PriorPaymentSubmission -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE PRIOR_PAYMENT_SUBMISSION_FK IN (
    SELECT PRIOR_PAYMENT_SUBMISSION_SEQ
    FROM PSP_PRIOR_PAYMENT_SUBMISSION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PriorPaymentSubmission
DELETE
FROM PSP_PRIOR_PAYMENT_SUBMISSION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PropertyAudit
DELETE
FROM PSP_PROPERTY_AUDIT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PstubEmployeeInfo -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PstubEmployeeInfo -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PstubEmployeeInfo -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PstubEmployeeInfo -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PstubEmployeeInfo -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PSTUB_EMPLOYEE_INFO_FK IN (
    SELECT PSTUB_EMPLOYEE_INFO_SEQ
    FROM PSP_PSTUB_EMPLOYEE_INFO
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PstubEmployeeInfo
DELETE
FROM PSP_PSTUB_EMPLOYEE_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtEmployeeInfo
DELETE
FROM PSP_QBDT_EMPLOYEE_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtPayrollItemInfo
DELETE
FROM PSP_QBDT_PAYROLL_ITEM_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QbdtUnprocessedRequest
DELETE
FROM PSP_QBDT_UNPROCESSED_REQUEST
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> TaxPaymentOnHoldReason
DELETE
FROM PSP_TAX_PAYMENT_ON_HOLD_REASON
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> TaxTableMiscData
DELETE
FROM PSP_TAX_TABLE_MISC_DATA
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> VmpEmployeeInfo
DELETE
FROM PSP_VMP_EMPLOYEE_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> VoidedCheck
DELETE
FROM PSP_VOIDED_CHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> WcCompany
DELETE
FROM PSP_WC_COMPANY
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Tax
DELETE
FROM PSP_TAX
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> SMSMigration
DELETE
FROM PSP_SMSMIGRATION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> PayrollRun -> ATFPayrollsToProcess
DELETE
FROM PSP_ATFPAYROLLS_TO_PROCESS
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILLING_DETAIL_FK IN (
            SELECT BILLING_DETAIL_SEQ
            FROM PSP_BILLING_DETAIL
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILLING_DETAIL_FK IN (
        SELECT BILLING_DETAIL_SEQ
        FROM PSP_BILLING_DETAIL
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillingDetail -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE BILLING_DETAIL_FK IN (
    SELECT BILLING_DETAIL_SEQ
    FROM PSP_BILLING_DETAIL
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> BillingDetail -> LiabilityCheckBillingDetailAssoc
DELETE
FROM PSP_LIAB_CHECK_BILLING_ASSOC
WHERE BILLING_DETAIL_FK IN (
    SELECT BILLING_DETAIL_SEQ
    FROM PSP_BILLING_DETAIL
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> BillingDetail
DELETE
FROM PSP_BILLING_DETAIL
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE BILL_PAYMENT_SPLIT_FK IN (
            SELECT BILL_PAYMENT_SPLIT_SEQ
            FROM PSP_BILL_PAYMENT_SPLIT
            WHERE BILL_PAYMENT_FK IN (
                SELECT BILL_PAYMENT_SEQ
                FROM PSP_BILL_PAYMENT
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE BILL_PAYMENT_SPLIT_FK IN (
        SELECT BILL_PAYMENT_SPLIT_SEQ
        FROM PSP_BILL_PAYMENT_SPLIT
        WHERE BILL_PAYMENT_FK IN (
            SELECT BILL_PAYMENT_SEQ
            FROM PSP_BILL_PAYMENT
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE BILL_PAYMENT_SPLIT_FK IN (
    SELECT BILL_PAYMENT_SPLIT_SEQ
    FROM PSP_BILL_PAYMENT_SPLIT
    WHERE BILL_PAYMENT_FK IN (
        SELECT BILL_PAYMENT_SEQ
        FROM PSP_BILL_PAYMENT
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> BillPayment -> BillPaymentSplit
DELETE
FROM PSP_BILL_PAYMENT_SPLIT
WHERE BILL_PAYMENT_FK IN (
    SELECT BILL_PAYMENT_SEQ
    FROM PSP_BILL_PAYMENT
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> BillPayment
DELETE
FROM PSP_BILL_PAYMENT
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> CompanyServiceBankAccount
DELETE
FROM PSP_COMPANY_SERVICE_BANK_ACCT
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> EmpTotalsPayrollRun
DELETE
FROM PSP_EMP_TOTALS_PAYROLL_RUN
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> FraudEvent
DELETE
FROM PSP_FRAUD_EVENT
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckBillingDetailAssoc
DELETE
FROM PSP_LIAB_CHECK_BILLING_ASSOC
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_LINE_FK IN (
    SELECT LIABILITY_CHECK_LINE_SEQ
    FROM PSP_LIABILITY_CHECK_LINE
    WHERE LIABILITY_CHECK_FK IN (
        SELECT LIABILITY_CHECK_SEQ
        FROM PSP_LIABILITY_CHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckLine
DELETE
FROM PSP_LIABILITY_CHECK_LINE
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> LiabilityCheck -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_CHECK_FK IN (
    SELECT LIABILITY_CHECK_SEQ
    FROM PSP_LIABILITY_CHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> LiabilityCheck
DELETE
FROM PSP_LIABILITY_CHECK
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> TransmissionPayrollRun
DELETE
FROM PSP_TRANSMISSION_PAYROLL_RUN
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE PAYROLL_RUN_FK IN (
                    SELECT PAYROLL_RUN_SEQ
                    FROM PSP_PAYROLL_RUN
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE PAYROLL_RUN_FK IN (
                SELECT PAYROLL_RUN_SEQ
                FROM PSP_PAYROLL_RUN
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE PAYROLL_RUN_FK IN (
            SELECT PAYROLL_RUN_SEQ
            FROM PSP_PAYROLL_RUN
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE PAYROLL_RUN_FK IN (
        SELECT PAYROLL_RUN_SEQ
        FROM PSP_PAYROLL_RUN
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> PayrollRun -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun -> FailedPayrollRun
DELETE
FROM PSP_FAILED_PAYROLL_RUN
WHERE PAYROLL_RUN_FK IN (
    SELECT PAYROLL_RUN_SEQ
    FROM PSP_PAYROLL_RUN
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> PayrollRun
DELETE
FROM PSP_PAYROLL_RUN
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyNote
DELETE
FROM PSP_COMPANY_NOTE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Employee -> EmployeeAccrual
DELETE
FROM PSP_EMPLOYEE_ACCRUAL
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeCustomField
DELETE
FROM PSP_EMPLOYEE_CUSTOM_FIELD
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeLawQtrTotals
DELETE
FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeePayrollItem
DELETE
FROM PSP_EMPLOYEE_PAYROLL_ITEM
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeePayrollItemQtrTotals
DELETE
FROM PSP_EE_PAYROLLITEM_QTRTOTALS
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeTax -> TaxTableMiscData
DELETE
FROM PSP_TAX_TABLE_MISC_DATA
WHERE EMPLOYEE_TAX_FK IN (
    SELECT EMPLOYEE_TAX_SEQ
    FROM PSP_EMPLOYEE_TAX
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> EmployeeTax
DELETE
FROM PSP_EMPLOYEE_TAX
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeW2Totals
DELETE
FROM PSP_EMPLOYEE_W2_TOTALS
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeWagePlan
DELETE
FROM PSP_EMPLOYEE_WAGE_PLAN
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> FraudEvent
DELETE
FROM PSP_FRAUD_EVENT
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> LiabilityAdjustment -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> LiabilityAdjustment -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE LIABILITY_ADJUSTMENT_FK IN (
    SELECT LIABILITY_ADJUSTMENT_SEQ
    FROM PSP_LIABILITY_ADJUSTMENT
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> LiabilityAdjustment
DELETE
FROM PSP_LIABILITY_ADJUSTMENT
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> PayItem
DELETE
FROM PSP_PAY_ITEM
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PSTUB_EMPLOYEE_INFO_FK IN (
        SELECT PSTUB_EMPLOYEE_INFO_SEQ
        FROM PSP_PSTUB_EMPLOYEE_INFO
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> PstubEmployeeInfo -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PSTUB_EMPLOYEE_INFO_FK IN (
    SELECT PSTUB_EMPLOYEE_INFO_SEQ
    FROM PSP_PSTUB_EMPLOYEE_INFO
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> PstubEmployeeInfo
DELETE
FROM PSP_PSTUB_EMPLOYEE_INFO
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> ThirdParty401kBatchEmployee
DELETE
FROM PSP_TP401K_BATCH_EMPLOYEE
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE D_D_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE D_D_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE D_D_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE D_D_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE D_D_EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
                SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
                FROM PSP_EMPLOYEE_BANK_ACCOUNT
                WHERE EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
            SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
            FROM PSP_EMPLOYEE_BANK_ACCOUNT
            WHERE EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
        SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
        FROM PSP_EMPLOYEE_BANK_ACCOUNT
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> EmployeeBankAccount -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE EMPLOYEE_BANK_ACCOUNT_FK IN (
    SELECT EMPLOYEE_BANK_ACCOUNT_SEQ
    FROM PSP_EMPLOYEE_BANK_ACCOUNT
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> EmployeeBankAccount
DELETE
FROM PSP_EMPLOYEE_BANK_ACCOUNT
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> Paycheck -> EmployerContribution -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE EMPLOYER_CONTRIBUTION_FK IN (
    SELECT EMPLOYER_CONTRIBUTION_SEQ
    FROM PSP_EMPLOYER_CONTRIBUTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> EmployerContribution
DELETE
FROM PSP_EMPLOYER_CONTRIBUTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubPayItem
DELETE
FROM PSP_PSTUB_PAY_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubDDItem
DELETE
FROM PSP_PSTUB_DDITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubMsg
DELETE
FROM PSP_PSTUB_MSG
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub -> PstubPaidTimeoffItem
DELETE
FROM PSP_PSTUB_PAID_TIMEOFF_ITEM
WHERE PAYSTUB_FK IN (
    SELECT PAYSTUB_SEQ
    FROM PSP_PAYSTUB
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Paystub
DELETE
FROM PSP_PAYSTUB
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kBatchPaycheck
DELETE
FROM PSP_TP401K_BATCH_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Compensation -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE COMPENSATION_FK IN (
    SELECT COMPENSATION_SEQ
    FROM PSP_COMPENSATION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Compensation
DELETE
FROM PSP_COMPENSATION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> Tax
DELETE
FROM PSP_TAX
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE PAYCHECK_SPLIT_FK IN (
            SELECT PAYCHECK_SPLIT_SEQ
            FROM PSP_PAYCHECK_SPLIT
            WHERE PAYCHECK_FK IN (
                SELECT PAYCHECK_SEQ
                FROM PSP_PAYCHECK
                WHERE SOURCE_EMPLOYEE_FK IN (
                    SELECT EMPLOYEE_SEQ
                    FROM PSP_EMPLOYEE
                    WHERE COMPANY_FK IN (
                        uniqueid
                        )
                )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE PAYCHECK_SPLIT_FK IN (
        SELECT PAYCHECK_SPLIT_SEQ
        FROM PSP_PAYCHECK_SPLIT
        WHERE PAYCHECK_FK IN (
            SELECT PAYCHECK_SEQ
            FROM PSP_PAYCHECK
            WHERE SOURCE_EMPLOYEE_FK IN (
                SELECT EMPLOYEE_SEQ
                FROM PSP_EMPLOYEE
                WHERE COMPANY_FK IN (
                    uniqueid
                    )
            )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE PAYCHECK_SPLIT_FK IN (
    SELECT PAYCHECK_SPLIT_SEQ
    FROM PSP_PAYCHECK_SPLIT
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> PaycheckSplit
DELETE
FROM PSP_PAYCHECK_SPLIT
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> QbdtPaycheckInfo
DELETE
FROM PSP_QBDT_PAYCHECK_INFO
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState
DELETE
FROM PSP_TP401K_PAYCHECK_STATE
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState
DELETE
FROM PSP_TP401K_PAYCHECK_PENDING
WHERE THIRD_PARTY401K_PAYCHECK_FK IN (
    SELECT TP401K_PAYCHECK_SEQ
    FROM PSP_TP401K_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> ThirdParty401kPaycheck
DELETE
FROM PSP_TP401K_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState
DELETE
FROM PSP_WC_PAYCHECK_STATE
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState
DELETE
FROM PSP_WC_PAYCHECK_PENDING
WHERE WORKERS_COMP_PAYCHECK_FK IN (
    SELECT WC_PAYCHECK_SEQ
    FROM PSP_WC_PAYCHECK
    WHERE PAYCHECK_FK IN (
        SELECT PAYCHECK_SEQ
        FROM PSP_PAYCHECK
        WHERE SOURCE_EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> Paycheck -> WorkersCompPaycheck
DELETE
FROM PSP_WC_PAYCHECK
WHERE PAYCHECK_FK IN (
    SELECT PAYCHECK_SEQ
    FROM PSP_PAYCHECK
    WHERE SOURCE_EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> Paycheck
DELETE
FROM PSP_PAYCHECK
WHERE SOURCE_EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> PstubEmployeePreference
DELETE
FROM PSP_PSTUB_EMPLOYEE_PREFERENCE
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> QbdtEmployeeInfo
DELETE
FROM PSP_QBDT_EMPLOYEE_INFO
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANS_LINE_FK IN (
    SELECT QBDT_PAYROLL_TRANS_LINE_SEQ
    FROM PSP_QBDT_PAYROLL_TRANS_LINE
    WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
        SELECT QBDT_PAYROLL_TRANSACTION_SEQ
        FROM PSP_QBDT_PAYROLL_TRANSACTION
        WHERE EMPLOYEE_FK IN (
            SELECT EMPLOYEE_SEQ
            FROM PSP_EMPLOYEE
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> Employee -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine
DELETE
FROM PSP_QBDT_PAYROLL_TRANS_LINE
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> QbdtPayrollTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE QBDT_PAYROLL_TRANSACTION_FK IN (
    SELECT QBDT_PAYROLL_TRANSACTION_SEQ
    FROM PSP_QBDT_PAYROLL_TRANSACTION
    WHERE EMPLOYEE_FK IN (
        SELECT EMPLOYEE_SEQ
        FROM PSP_EMPLOYEE
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> Employee -> QbdtPayrollTransaction
DELETE
FROM PSP_QBDT_PAYROLL_TRANSACTION
WHERE EMPLOYEE_FK IN (
    SELECT EMPLOYEE_SEQ
    FROM PSP_EMPLOYEE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Employee
DELETE
FROM PSP_EMPLOYEE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyBankAccount -> CompanyServiceBankAccount
DELETE
FROM PSP_COMPANY_SERVICE_BANK_ACCT
WHERE COMPANY_BANK_ACCOUNT_FK IN (
    SELECT COMPANY_BANK_ACCOUNT_SEQ
    FROM PSP_COMPANY_BANK_ACCOUNT
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyBankAccount
DELETE
FROM PSP_COMPANY_BANK_ACCOUNT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyService -> CompanyServiceBankAccount
DELETE
FROM PSP_COMPANY_SERVICE_BANK_ACCT
WHERE COMPANY_SERVICE_FK IN (
    SELECT COMPANY_SERVICE_SEQ
    FROM PSP_COMPANY_SERVICE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> CompanyService
DELETE
FROM PSP_BPCOMPANY_SERVICE_INFO
WHERE BPCOMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                     FROM PSP_COMPANY_SERVICE
                                     WHERE COMPANY_FK IN (
                                         uniqueid
                                         ));
DELETE
FROM PSP_CDCOMPANY_SERVICE_INFO
WHERE CDCOMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                     FROM PSP_COMPANY_SERVICE
                                     WHERE COMPANY_FK IN (
                                         uniqueid
                                         ));
DELETE
FROM PSP_RACOMPANY_SERVICE_INFO
WHERE RACOMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                     FROM PSP_COMPANY_SERVICE
                                     WHERE COMPANY_FK IN (
                                         uniqueid
                                         ));
DELETE
FROM PSP_TAX_COMPANY_SERVICE_INFO
WHERE TAX_COMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                       FROM PSP_COMPANY_SERVICE
                                       WHERE COMPANY_FK IN (
                                           uniqueid
                                           ));
DELETE
FROM PSP_TP401KCOMPANY_SERVICE_INFO
WHERE TP401KCOMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                         FROM PSP_COMPANY_SERVICE
                                         WHERE COMPANY_FK IN (
                                             uniqueid
                                             ));
DELETE
FROM PSP_DDCOMPANY_SERVICE_INFO
WHERE DDCOMPANY_SERVICE_INFO_SEQ IN (SELECT COMPANY_SERVICE_SEQ
                                     FROM PSP_COMPANY_SERVICE
                                     WHERE COMPANY_FK IN (
                                         uniqueid
                                         ));
DELETE
FROM PSP_COMPANY_SERVICE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Contact
DELETE
FROM PSP_CONTACT
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> TransactionResponse -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE TRANSACTION_RESPONSE_FK IN (
    SELECT TRANSACTION_RESPONSE_SEQ
    FROM PSP_TRANSACTION_RESPONSE
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> TransactionResponse
DELETE
FROM PSP_TRANSACTION_RESPONSE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> MoneyMovementTransaction -> ATFPaymentsToProcess
DELETE
FROM PSP_ATFPAYMENTS_TO_PROCESS
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> EdiPaymentDetail
DELETE
FROM PSP_EDI_PAYMENT_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> EftpsPaymentDetail
DELETE
FROM PSP_EFTPS_PAYMENT_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> EntryDetailRecord
DELETE
FROM PSP_ENTRY_DETAIL_RECORD
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> FsetFilingDetail
DELETE
FROM PSP_FSET_FILING_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> TaxPaymentOnHoldReason
DELETE
FROM PSP_TAX_PAYMENT_ON_HOLD_REASON
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE RELATABLE_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT FINANCIAL_TRANSACTION_SEQ
        FROM PSP_FINANCIAL_TRANSACTION
        WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> ATFPaymentsToProcess
DELETE
FROM PSP_ATFPAYMENTS_TO_PROCESS
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EdiPaymentDetail
DELETE
FROM PSP_EDI_PAYMENT_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EftpsPaymentDetail
DELETE
FROM PSP_EFTPS_PAYMENT_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EntryDetailRecord
DELETE
FROM PSP_ENTRY_DETAIL_RECORD
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FsetFilingDetail
DELETE
FROM PSP_FSET_FILING_DETAIL
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> TaxPaymentOnHoldReason
DELETE
FROM PSP_TAX_PAYMENT_ON_HOLD_REASON
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE RELATABLE_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> TransactionOffloadBatch
DELETE
FROM PSP_TRANSACTION_OFFLOAD_BATCH
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransactionState
DELETE
FROM PSP_FINANCIAL_TRANS_STATE
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> OnHoldReason
DELETE
FROM PSP_FINTXN_ONHOLDREASON_ASSOC
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE FINANCIAL_TRANSACTION_FK IN (
    SELECT FINANCIAL_TRANSACTION_SEQ
    FROM PSP_FINANCIAL_TRANSACTION
    WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE ORIGINAL_TRANSACTION_FK IN (
            SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
            FROM PSP_MONEY_MOVEMENT_TRANSACTION
            WHERE COMPANY_FK IN (
                uniqueid
                )
        )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction
DELETE
FROM PSP_FINANCIAL_TRANSACTION
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> MoneyMovementTransaction
DELETE
FROM PSP_MONEY_MOVEMENT_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> TransactionReturn
DELETE
FROM PSP_TRANSACTION_RETURN
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> PaymentBatchAssoc
DELETE
FROM PSP_PAYMENT_BATCH_ASSOC
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> VoidedCheck
DELETE
FROM PSP_VOIDED_CHECK
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE ORIGINAL_TRANSACTION_FK IN (
        SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
        FROM PSP_MONEY_MOVEMENT_TRANSACTION
        WHERE COMPANY_FK IN (
            uniqueid
            )
    )
);

--Company -> MoneyMovementTransaction -> MoneyMovementTransaction
DELETE
FROM PSP_MONEY_MOVEMENT_TRANSACTION
WHERE ORIGINAL_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> TransactionReturn
DELETE
FROM PSP_TRANSACTION_RETURN
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> PaymentBatchAssoc
DELETE
FROM PSP_PAYMENT_BATCH_ASSOC
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> QbdtTransactionInfo
DELETE
FROM PSP_QBDT_TRANSACTION_INFO
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction -> VoidedCheck
DELETE
FROM PSP_VOIDED_CHECK
WHERE MONEY_MOVEMENT_TRANSACTION_FK IN (
    SELECT MONEY_MOVEMENT_TRANSACTION_SEQ
    FROM PSP_MONEY_MOVEMENT_TRANSACTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> MoneyMovementTransaction
DELETE
FROM PSP_MONEY_MOVEMENT_TRANSACTION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> TransactionReturn
DELETE
FROM PSP_TRANSACTION_RETURN
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> Deduction -> QbdtPaylineInfo
DELETE
FROM PSP_QBDT_PAYLINE_INFO
WHERE DEDUCTION_FK IN (
    SELECT DEDUCTION_SEQ
    FROM PSP_DEDUCTION
    WHERE COMPANY_FK IN (
        uniqueid
        )
);

--Company -> Deduction
DELETE
FROM PSP_DEDUCTION
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CheckPrintSignature
DELETE
FROM PSP_CHECK_PRINT_SIGNATURE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> CompanyAdditionalInfo
DELETE
FROM PSP_COMPANY_ADDITIONAL_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> EmployerPreference
DELETE
FROM PSP_EMPLOYER_PREFERENCE
WHERE COMPANY_FK IN (
    uniqueid
    );

--Company -> QuickbooksInfo
DELETE
FROM PSP_QUICKBOOKS_INFO
WHERE COMPANY_FK IN (
    uniqueid
    );

DELETE
FROM PSP_COMPANY
WHERE COMPANY_SEQ = uniqueid;
END;
$$;