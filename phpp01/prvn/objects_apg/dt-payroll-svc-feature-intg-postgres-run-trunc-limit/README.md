# PSP

## Guidelines for branching and deployment to preprod environments

### Branching 
* All feature branches to be named _feature-{featurename}_
* All features to be protected by feature-flag, wherever applicable
* PRs to be raised against the branch _develop_
* Integration testing to be on _develop_ branch
* _develop_ branch will then be merged to _master_ and regression to be done on QAL or E2E environments before prod deployment.

### RTB Guidelines
To implement and release RTB programs follow these guidelines https://wiki.intuit.com/display/EMSPD/PSP+RTB+Guidelines.

### Deployment to Preprod environments

There are flags added in msaas-config.yaml for QAL and E2E environments. By default, they have a value of false, You can change the flag to true to enable deployment to the respective environment. 

Please reach out to the slack channel #psp or any PSP team members to avoid conflict in deployment to preprod environments.

## Deployment to Prod
* Raise a PR from _develop_ to _master_
* _master_ branch will get deployed to all preprod environments and the pipelines stops to get approval for prod deployment. Only project admins will have access to approve Prod deployment.
* Please reach out to Praveenkumar Hoolimath(@praveenkumarh635) or Dhanendra Jain(@djain5) for prod deployment. 

Devportal Link: https://devportal.intuit.com/app/dp/resource/6847740825712235356/msaasresource

## Docker Image
<div style="margin-left: 20px;">
<code>docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc/psp-base-img</code><br/>&#8627;
<div style="margin-left: 20px;">
<code>docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc</code><br/>&#8627;
</div>
</div>

Refer PSP base image content [here](https://github.intuit.com/payroll-services-platform/psp-docker-image/tree/master/psp-base-images)

## Local Setup

### About
The local setup script has the following features:-
1. Installs the appropriate versions of `maven`, `java`, `sqlplus`, `tomcat` and `podman` if are not already installed.
2. Creates the local podman database container. Also archives the database image locally so that it doesn't have to be downloaded each time the laptop reboots.
3. Performs local Tomcat configuration changes and builds the codebase.
4. We can skip some of the above steps, like building the codebase if we want by using the `--skip` flag (example: `./start_local.sh --skip mvn,sql` skips building the codebase and sqlplus installation and only creates the database container). For more details, run `./start_local.sh --help`.
5. The script is idempotent, so we can run it everytime we want to build the codebase or create the database container.
6. In order to change the value of the artifact, modify the value of `artifact_name` (default value is `CdmAdapter`) in `./start_local.sh`.

The script uses the following default locations for storing the corresponding files
1. `~/` - database image local archive `oracle_psp_image.tar`
2. `~/Downloads/binaries/` - `apache-maven-3.6.3` and `apache-tomcat-9.0.63`.
3. `/opt/oracle/instantclient_12_1` - `instantclient`
4. `~/etc/` - `tnsnames.ora`
5. `/Library/Java/JavaVirtualMachines/` - `amazon-corretto-8.jdk`
### Prerequisites
[Add git ssh keys](https://wiki.intuit.com/pages/viewpage.action?spaceKey=qbwin&title=Local+Dev+Setup+-+Server+Side#LocalDevSetupServerSide-InstallGit)
### Steps
1. Clone this repository from github
```shell
git clone git@github.intuit.com:payroll-dtpayroll/dt-payroll-svc.git
cd dt-payroll-svc
```
2. Run the `start_local.sh` script from the repository root and follow the on-screen prompts.
```shell
./start_local.sh
```
3. Start Tomcat in debug mode ([configuration](https://drive.google.com/file/d/1X0Upt6li5Iq_XDstOUCdqVdL7o96r4_F/view?usp=sharing)).