Contribution Guidelines
=======================
Great to have you here. Here are a few ways you can help make our project better!

- [Process for Contributing Code](#Process-for-Contributing-Code)
	- [Local Setup](#Local-Setup)
		- [Fork and Clone](#Fork-and-Clone)
		- [Commit](#Commit)
		- [Pull Request](#Pull-Request)
- [Code Quality Expectations](#Code-Quality-Expectations)
	- [Tests](#Tests)
	- [Coverage](#Coverage)
	- [Documentation](#Documentation)
	- [Code Style](#Code-Style)
- [SLA](#SLA)
- [Contact Information](#Contact-Information)
	- [Team](#Team)
	- [Contact](#Contact)

# Process for Contributing Code

## Local Setup

Make sure git knows your real name and email address:

```text
$ git config --global user.name "Jon Doe"
$ git config --global user.email "jon.doe@example.com"
```

### Fork and Clone

From the GitHub UI, fork the project into your user space or another organization.  Following the steps below, clone locally and add the upstream remote.

```text
$ git clone git@github.intuit.com:payroll-dtpayroll/dt-payroll-svc.git
$ cd <project>

## If you have SSH keys set up, then add the SSH URL as an upstream.
$ git remote add upstream git@github.intuit.com/payroll-dtpayroll/dt-payroll-svc.git

## If you want to type in your password when fetching from upstream, then add the HTTPS URL as an upstream.
$ git remote add upstream https://github.intuit.com/payroll-dtpayroll/dt-payroll-svc.git
```

If later you want to switch your remote upstream from `https` to `ssh` or vice versa you can edit it using the [`git remote set-url`](https://help.github.com/articles/changing-a-remote-s-url/) command.

### Commit

Writing good commit logs is important.  A commit log should describe what
changed and why.  Make sure that commit message contains the JIRA or GitHub issue associated with the change.  Make sure to use GitHubs [special syntax](https://help.github.com/articles/closing-issues-via-commit-messages/) for closing issues via commit messages.


### Pull Request
Go to https://github.intuit.com/payroll-dtpayroll/dt-payroll-svc and select your fork.
Click the 'Pull Request' button and fill out the form.  Be sure to @mention code owners and/or maintainers.

# Code Quality Expectations

>### Tests
>All new Java methods should have correlated JUnit tests.
>### Coverage
>Ensure that code coverage does not fall below 80%.
>### Documentation
>Code should be well documented. What it is doing should be self explanatory based on coding conventions, however why the >code is doing something should be documented well.
>
>* Java code should have JavaDoc
>* `pom.xml` should have comments
>* Unit tests should have comments and failure messages
>* Integration tests should have comments and failure messages
>
>### Code Style
>We try to follow [Google's Coding Standards](https://google.github.io/styleguide/javaguide.html).  The easiest way is to just format based on existing code you see.  We don't enforce this, just a guideline.  
>Some [Good Coding Practices](https://wiki.intuit.com/display/UIP/Coding+Standards) are mentioned here as well. 

# SLA

>The pull request review SLA is 72 hours.  If there are comments
>to address, apply your changes in a separate commit and push that to your
>feature branch.  Post a comment in the pull request afterwards; GitHub does
>not send out notifications when you add commits.

# Contact Information

>## Team 

>QBDT Payroll
>
>```
>Dev Manager: Syed Iqbal Nasim @snasim
>Architect: Karthikeyan Muthurangam @kmuthurangam
>```

>## Contact

>Here is how you can contact our team:

>* Slack: #desktop-payroll
>* JIRA: https://jira.intuit.com/browse/PSP