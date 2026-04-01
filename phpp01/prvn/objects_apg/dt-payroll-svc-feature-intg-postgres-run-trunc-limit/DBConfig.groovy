//Generates and commits config map in deployment repo
import com.intuit.dev.patterns.jenkins_lib.git.GitOperations
def commitDBConfigFile(Map config, String env, Integer gitTimeout, String monolithDb, String auditDb) {
    GitOperations git = new GitOperations(this)
    git.commit(config, env, gitTimeout) {
        if(config.argocd_tracking_revision?.trim()) {
            steps.sh label: "Checkout branch '${config.argocd_tracking_revision}'", script: "git checkout ${config.argocd_tracking_revision}"
        }
        String ev="";
        String region="usw2";
        if( env =="prf-usw2-eks" ){
            ev = "prf";
        }
        if( env == "qal-usw2-eks"){
            ev = "qal";
        }
        if( env == "e2e-usw2-eks"){
            ev = "e2e";
        }
        if( env == "e2e-use2-eks"){
            ev = "e2e";
            region ="use2";
        }
        if( env == "prd-usw2-eks"){
            ev = "prd";
        }
        if( env == "prd-use2-eks"){
            ev = "prd";
            region = "use2";
        }
        if( env == "stg-usw2-eks"){
            ev = "stg";
        }
        if( env == "ds2-usw2-eks"){
            ev = "ds2";
        }
        steps.sh label: "committing config-map", script: """
          cd environments/${env};
          rm -rf Config-Map.yaml
          touch Config-Map.yaml
          echo "apiVersion: v1\nkind: ConfigMap\nmetadata:\n  name: database-config\n  namespace: payroll-dtpayrollsvc-${region}-${ev}\n  annotations:\n    argocd.argoproj.io/hook: PreSync\ndata:\n  monolithDb: ${monolithDb}\n  auditDb: ${auditDb}" > Config-Map.yaml
          git add Config-Map.yaml
      """
    }
}
return this
