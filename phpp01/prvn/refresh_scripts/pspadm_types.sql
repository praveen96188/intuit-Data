CREATE TYPE ft_record AS
(
    ft_seq                 varchar(255),
    ft_company_fk          varchar(255),
    ft_transaction_type_fk varchar(255)
);
CREATE TYPE mmt_record AS
(
    mmt_seq varchar(255),
    mmt_company_fk varchar(255)
);
CREATE TYPE fts_record AS
    (
        fts_company_fk varchar(255),
        newbal_date timestamp(6),
        amount integer,
        ledger_acc_fk varchar(255),
        reporting_type varchar(255)
    );
