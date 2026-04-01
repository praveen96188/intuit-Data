package psp.sap.service.interfaces
{
import mx.collections.ArrayCollection;
import mx.rpc.IResponder;

public interface IUserService extends IPSPService
	{

		function sapLogout(pCorpId:String, responder:IResponder):void;

		function sapssoLogout(responder:IResponder):void;

		function getAllRoles(domainId:String, responder:IResponder):void;

		function getAllRoleObjects(responder:IResponder):void;

		function getAllOperations(responder:IResponder):void;

		function getAllUsersByOperation(operationId:String, responder:IResponder):void;

		function addNewUserData(corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void;

		function updateUserData(uniqueId:String, corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void;

		function getUsersInDomain(domainId:String, responder:IResponder):void;

        function searchUsersInDomain(pDomainId:String, pFirstName:String, pLastName:String, pCorpId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void;

		function removeUser(corpId:String, responder:IResponder):void;

		function updatePreference(corpId:String, key:String, value:String, responder:IResponder):void;

		function updatePreferences(corpId:String, settings:ArrayCollection, responder:IResponder):void;

		function getUserSettings(corpId:String, responder:IResponder):void;

		function resetSettings(corpId:String, responder:IResponder):void;

        function unlockUser(corpId:String, responder:IResponder):void;

	}
}