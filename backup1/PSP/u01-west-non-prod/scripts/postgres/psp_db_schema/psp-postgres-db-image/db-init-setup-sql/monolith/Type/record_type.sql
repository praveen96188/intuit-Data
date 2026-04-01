CREATE TYPE ft_record AS
    (
    ft_seq                 varchar(255),
    ft_company_fk          varchar(255),
    ft_transaction_type_fk varchar(255)
    );
CREATE TYPE seq_company_fk_record AS
    (
    seq varchar(255),
    company_fk varchar(255)
    );
CREATE TYPE fts_record AS
    (
    fts_company_fk varchar(255),
    newbal_date timestamp(6),
    amount numeric(19,4),
    ledger_acc_fk varchar(255),
    reporting_type varchar(255)
    );