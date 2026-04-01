# aws-rds
AWS RDS

How to use AWS Cloudformation to create RDS

#1. Create RDS depenedecy resources

```
Usage ./create_rds_dependency.sh <AppEnv> <DBType> <Region> <VPC>
      where <AppEnv> should be qa, e2e, prf, stg, sbx or prod
            <DBType> should be clusterdb or reportdb
            <Region> should be us-west-2, us-east-2, etc
            <VPC> should be vpc-1, vpc-2, etc

For example,
./create_rds_dependency_w_cli.sh e2e clusterdb us-west-2 vpc-2
```

#2. Create a RDS db

```
Usage: ./create_rds.sh \<AppEnv> \<DBType> \<DBName> \<Region> \<VPC>
       where \<AppEnv> should be qa, e2e, e2es, stg, sbx, prf, prod, prods, prodm, prodx or prodn
             e2es means small-size e2e
             prods means small-size prod
             prodm means median-size prod
             prodx means large-size prod
             prodn means new prod (with less half of regular prod storage)
             <DBType> should be clusterdb or reportdb
             <Region> should be us-west-2, us-east-2, etc
             <VPC> should be vpc-1, vpc-2, etc

For example,
./create_rds.sh qa clusterdb pspuatuw us-west-2 vpc-2
```
