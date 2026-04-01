# PSP

## Guidelines for release process

### Release Guidelines 
* Do not log any sensitive fields in plaintext in the code. Sensitive fields should not appear in plaintext in splunk. Encrypt the sensitive fields and log.
* Do not release too many features in a single release. Related features can go in a single release.
* Release DB changes and code changes in separate releases. Rollback will be simple.
* PR review comments should be addressed and all PRs for the release should be approved before pre-release meeting.
* Integration tests results and automation tests results on final feature branch (No more code changes after this point) should be attached in the release wiki before pre-release meeting.
* Rollback strategy and rollback steps should be updated in the release wiki before pre-release meeting.
* DB review with Mayank/Shobhit should be completed before pre-release meeting. Detailed DB review process is explained in the next section.
* Pre Release meeting should be completed by 2PM on the previous day before the release day. Merge master PR just after pre release meeting.
* Trigger integration test and automation test suites on the master branch and attach the reports in the release wiki once the results are out.
* Any extra failures in the Integration Tests need to be checked. Check with Karthik/Iqbal/Ankush/Chethan/Ram to decide the next steps here.
* Make sure CR is approved before the release.
* Send Post Release Monitor email after 6 hours (Around 9 PM) after the release on release date and next day morning 11 AM.
* Start the release as soon as all the 11:30PM PST jobs complete. Make sure no critical jobs are running in JSS portal before starting the release. Releases should complete before the LedgerBalance job which triggers at 1:00AM PST.
* Lets make sure all the release owners are available on the release day to perform post release sanity and to support issues during PST hours. If you are unavailable(OOO) on the release day, release your changes once you are back.
* Lets involve PD Primary and Secondary in the pre-release discussion to understand the impact of the release and specific alerts configured for the release. This will help us to detect any production issues quickly.

### DB changes Guidelines
* We have prepared checklist https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+DB+Review to capture common DB errors before the release. Please go through this wiki to understand. 
* This wiki captures Validation scripts, Rollback scripts and Preprod DB logs.
* DBA Approval step has been added in the Release template https://wiki.intuit.com/display/EMSPD/Release+Checklist. So, this step will be part of our Release WIKI for any DB Releases.
* If you have DB changes for your release, clone this wiki page https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+DB+Review and name the wiki as PSP-ReleaseVersion-DBReview. Please answer all the questions in this WIKI.
* Setup DB review meeting with Mayank/Shobhit 2-3 days before the release. Address all review comments and get the approval before pre-release meeting.
