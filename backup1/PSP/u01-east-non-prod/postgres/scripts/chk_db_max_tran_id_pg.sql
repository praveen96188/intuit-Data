select age(datfrozenxid) FROM pg_database where datname = :dbname;
