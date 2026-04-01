import com.intuit.dev.patterns.jenkins_lib.git.GitOperations
@Library(value = 'msaas-shared-lib') _

def configureMaintenanceUp(Map config, String env, Integer gitTimeout) {

    def maintenance_folder="psp-maintenance"
    GitOperations git = new GitOperations(this)
    git.commit(config, env, gitTimeout) {
        steps.sh label: "bringing up maintenance ", script: """
          sh ${maintenance_folder}/maintenance-up.sh ${env}
      """
    }
}

def configureMaintenanceDown(Map config, String env, Integer gitTimeout) {

    def maintenance_folder="psp-maintenance"
    GitOperations git = new GitOperations(this)
    git.commit(config, env, gitTimeout) {
        steps.sh label: "bringing down maintenance ", script: """
          sh ${maintenance_folder}/maintenance-down.sh ${env}
      """
    }

}

def bringMaintenanceDown(def steps, Map config, String env, String credentialId, String gitUsername, String gitPassword, String argocdToken){
    steps.dir("deployment-${env}-${steps.env.GIT_COMMIT}") {
        if(config.argocd_tracking_revision) {
            steps.git url: "https://${config.deploy_repo}", credentialsId: credentialId, branch: config.argocd_tracking_revision.trim()
        } else {
            steps.git url: "https://${config.deploy_repo}", credentialsId: credentialId
        }

        echo "Bringing down maintenance"

        def maintenance = load "psp-maintenance/maintenance.groovy"
        maintenance.maintenanceDown(this, env, gitUsername, gitPassword, argocdToken)

    }
}
// add service-maintenance pods to already generated rendermanifest file in prod
// & commit to deployment repo prod env branch (environments/prd-usw2-eks)
def bringMaintenanceUp(def steps, Map config, String env, String credentialId, String gitUsername, String gitPassword, String argocdToken){
    steps.dir("deployment-${env}-${steps.env.GIT_COMMIT}") {
        if(config.argocd_tracking_revision) {
            steps.git url: "https://${config.deploy_repo}", credentialsId: credentialId, branch: config.argocd_tracking_revision.trim()
        } else {
            steps.git url: "https://${config.deploy_repo}", credentialsId: credentialId
        }

        echo "Bringing Up maintenance"

        def maintenance = load "psp-maintenance/maintenance.groovy"
        maintenance.maintenanceUp(this, env, gitUsername, gitPassword, argocdToken)

    }
}

return this
