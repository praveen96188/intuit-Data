package test.mock {
    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.service.interfaces.IUserService;

    public class MockUserService extends MockAsyncService implements IUserService {

        public function expectsSapLogout(pCorpId:String):Mock {
            return expects("sapLogout").withArgs(pCorpId);
        }

        public function sapLogout(pCorpId:String, responder:IResponder):void {
            record("sapLogout", pCorpId);
            sendAsyncResult(responder, "sapLogout");
        }

        public function expectsGetAllRoles(domainId:String):Mock {
            return expects("getAllRoles").withArgs(domainId);
        }

        public function getAllRoles(domainId:String, responder:IResponder):void {
            record("getAllRoles", domainId);
            sendAsyncResult(responder, "getAllRoles");
        }

        public function expectsGetAllRoleObjects():Mock {
            return expects("getAllRoleObjects").withArgs();
        }

        public function getAllRoleObjects(responder:IResponder):void {
            record("getAllRoleObjects");
            sendAsyncResult(responder, "getAllRoleObjects");
        }

        public function expectsGetAllOperations():Mock {
            return expects("getAllOperations").withArgs();
        }

        public function getAllOperations(responder:IResponder):void {
            record("getAllOperations");
            sendAsyncResult(responder, "getAllOperations");
        }

        public function expectsGetAllUsersByOperation(operationId:String):Mock {
            return expects("getAllUsersByOperation").withArgs(operationId);
        }

        public function getAllUsersByOperation(operationId:String, responder:IResponder):void {
            record("getAllUsersByOperation", operationId);
            sendAsyncResult(responder, "getAllUsersByOperation");
        }

        public function expectsAddNewUserData(corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection):Mock {
            return expects("addNewUserData").withArgs(corpId, firstName, lastName, roleIds);
        }

        public function addNewUserData(corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void {
            record("addNewUserData", corpId, firstName, lastName, roleIds);
            sendAsyncResult(responder, "addNewUserData");
        }

        public function expectsUpdateUserData(uniqueId:String, corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection):Mock {
            return expects("updateUserData").withArgs(uniqueId, corpId, firstName, lastName, roleIds);
        }

        public function updateUserData(uniqueId:String, corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void {
            record("updateUserData", uniqueId, corpId, firstName, lastName, roleIds);
            sendAsyncResult(responder, "updateUserData");
        }

        public function expectsGetUsersInDomain(domainId:String):Mock {
            return expects("getUsersInDomain").withArgs(domainId);
        }

        public function getUsersInDomain(domainId:String, responder:IResponder):void {
            record("getUsersInDomain", domainId);
            sendAsyncResult(responder, "getUsersInDomain");
        }

        public function expectsSearchUsersInDomain(pDomainId:String, pFirstName:String, pLastName:String, pCorpId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):Mock {
            return expects("searchUsersInDomain").withArgs(pDomainId, pFirstName, pLastName, pCorpId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending, responder);
        }

        public function searchUsersInDomain(pDomainId:String, pFirstName:String, pLastName:String, pCorpId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
            record("searchUsersInDomain", pDomainId, pFirstName, pLastName, pCorpId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending, responder);
            sendAsyncResult(responder, "searchUsersInDomain");
        }

        public function expectsRemoveUser(corpId:String):Mock {
            return expects("removeUser").withArgs(corpId);
        }

        public function removeUser(corpId:String, responder:IResponder):void {
            record("removeUser", corpId);
            sendAsyncResult(responder, "removeUser");
        }

        public function expectsUpdatePreference(corpId:String, key:String, value:String):Mock {
            return expects("updatePreference").withArgs(corpId, key, value);
        }

        public function updatePreference(corpId:String, key:String, value:String, responder:IResponder):void {
            record("updatePreference", corpId, key, value);
            sendAsyncResult(responder, "updatePreference");
        }

        public function expectsUpdatePreferences(corpId:String, settings:ArrayCollection):Mock {
            return expects("updatePreferences").withArgs(corpId, settings);
        }

        public function updatePreferences(corpId:String, settings:ArrayCollection, responder:IResponder):void {
            record("updatePreferences", corpId, settings);
            sendAsyncResult(responder, "updatePreferences");
        }

        public function expectsGetUserSettings(corpId:String):Mock {
            return expects("getUserSettings").withArgs(corpId);
        }

        public function getUserSettings(corpId:String, responder:IResponder):void {
            record("getUserSettings", corpId);
            sendAsyncResult(responder, "getUserSettings");
        }

        public function expectsResetSettings(corpId:String):Mock {
            return expects("resetSettings").withArgs(corpId);
        }

        public function resetSettings(corpId:String, responder:IResponder):void {
            record("resetSettings", corpId);
            sendAsyncResult(responder, "resetSettings");
        }

        public function expectsUnlockUser(corpId:String, responder:IResponder):Mock {
            return expects("unlockUser").withArgs(corpId, responder);
        }

        public function unlockUser(corpId:String, responder:IResponder):void {
            record("unlockUser", corpId, responder);
            sendAsyncResult(responder, "unlockUser");
        }

    }
}