#### Steps to build image local
- Stop if container is already running
    - docker stop psp-postgres-db-container
- Remove container
    - docker rm psp-postgres-db-container
- Remove Image
    - docker rmi psp-postgres-db
- cd into psp-docker-image
- Build Image
    - docker build -t psp-postgres-db -f psp-postgres-db-image/Dockerfile .
- Start Container
    - docker run -d --name psp-postgres-db-container -p 5432:5432 psp-postgres-db

#### If Port is already in use then
- sudo lsof -i :5432
- sudo kill -9
- sudo lsof -i :5432

#### Pull Image from Artifactory
- Pull Image
  - docker pull docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc/psp-postgres-db-image:0.0.3
- Create Container
  - docker run -d --name psp-postgres-db-container -p 5432:5432 -v ~/data/postgresql/data:/var/lib/postgresql/data docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc/psp-postgres-db-image:0.0.3;