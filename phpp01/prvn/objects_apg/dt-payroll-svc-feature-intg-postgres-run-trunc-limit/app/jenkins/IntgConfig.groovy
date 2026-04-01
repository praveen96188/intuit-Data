
def getSuiteOne(){
    return ["PSP", "PSE", "agency-rules", "configuration", "psp-codegen", "Common", "configuration-manager",
            "Gateways", "salestax-gateway", "email-client", "domain", "domain-secondary", "processes-core",
            "payroll-services-api", "utils", "iam-gateway", "dd-gateway", "account-service-gateway", "payroll-services-api-impl",
            "filter", "common-gateway", "email-gateway", "ers-gateway", "amo-gateway", "echo-sign-gateway", "iop-gateway",
            "pgp", "iop", "efe-gateway", "Adapters", "ade-adapter", "brm-gateway", "workers-comp-gateway", "tfs-gateway", "ofx",
            "qbdt-adapter", "batch-jobs", "cdm-adapter", "testutils", "iop-tests", "payroll-services-api-tests",
            "qbdtws-adapter", "amo-gateway-tests", "aia-gateway", "workerscomp-gateway-tests", "email-gateway-tests"]
}

def getSuiteTwo(){
    return ["batch-jobs-tests"]
}

def getSuiteThree(){
    return ["processes-core-tests", "test-suite", "ews-adapter", "cloud-migration-adapter", "brm-adapter", "UI", "sap", "sap-adapter",
            "data-adapter", "keynote-adapter", "rtb", "lt-adapter", "test-adapter", "test-tools-ui"]
}

def getSuiteFour(){
    return ["qbdt-adapter-tests", "ptc-adapter", "dis-adapter", "ivr-adapter", "qboe-adapter","cdm-adapter-tests", "ade-adapter-tests"]
}

def getSuiteFive(){
    return ["domain-tests"]
}

def getAllTest(){
    return (getSuite(1) + getSuite(2) + getSuite(3) + getSuite(4) + getSuite(5))
}

def getSuite(Integer suiteId){
    switch(suiteId) {
        case 1:
            return getSuiteOne()
        case 2:
            return getSuiteTwo()
        case 3:
            return getSuiteThree()
        case 4:
            return getSuiteFour()
        case 5:
            return getSuiteFive()
        default:
            throw new Exception("Suite not found")
    }
}

return this

