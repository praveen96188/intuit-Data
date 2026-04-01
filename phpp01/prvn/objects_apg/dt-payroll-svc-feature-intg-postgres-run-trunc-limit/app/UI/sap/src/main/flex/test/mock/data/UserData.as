package test.mock.data
{
	import flash.utils.*;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.enums.OperationsEnum;
	import psp.sap.model.User;
	import psp.sap.model.UserOperation;
	import psp.sap.model.UserRole;
	
	import test.sap.service.UserRolesData;
	
	public class UserData
	{
		public static function getOperationData():ArrayCollection {
			return UserData.getOperations(2);
		}
		
		private static function getOperations(number:int):ArrayCollection {
			var operations:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i < number; i++){
				var operation:UserOperation = new UserOperation();
				operation.description = "operation description " + i;
				operation.domainId = "operation domain Id " + i;
				operation.name = "operation name " + i;
				operation.operationId = "operation id " + i;
				operation.selected = false;
				
				operations.addItem(operation);
			}
			
			return operations;
		}
		
		public static function getRoleData():ArrayCollection {
			return UserData.getRoles(3);
		}
		
		private static function getRoles(number:int):ArrayCollection {
			var roles:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i < number; i++){
				var role:UserRole = new UserRole();
				role.description = "role description " + i;
				role.name = "role name " + i;
				role.operations = getOperations(2);
				role.roleId = "role id " + i;
				role.systemId = "role system id " + i;
				role.selected = false;
				
				roles.addItem(role);
			}
			return roles;
		}
		
		public static function getUsers():ArrayCollection {
			return UserData.buildUsers(4);
		}
		
		private static function buildUsers(number:int):ArrayCollection {
			var users:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i < number; i++){				
				users.addItem(UserData.buildUser());
			}
			
			return users;
		}
		
		public static function buildUser():User {
			var user:User = new User();
			user.corpId = "123";
			user.firstName = "First";
			user.grantedOperations = new ArrayCollection();
			user.lastName = "Last";
			user.roleIds = new ArrayCollection(["role"]);
			user.userName = "userName";
			return user;			
		}
		
		public static function getRoleNames():ArrayCollection {
			var roleNames:ArrayCollection = new ArrayCollection();
			roleNames.addItem("role1");
			roleNames.addItem("role2");
			roleNames.addItem("role3");
			roleNames.addItem("role4");
			roleNames.addItem("role5");
			
			return roleNames;
		}
		
		public static function getUser(role:String):User {
			var user:User = new User();
			var permissions:ArrayCollection = UserRolesData[UserRolesData.ROLE_OPERATIONS[UserRolesData.ROLES.getItemIndex(role)]] as ArrayCollection;
			var grantedPermissions:ArrayCollection = new ArrayCollection();
			for each(var permission:String in permissions) {
				var operation:UserOperation = new UserOperation();
				operation.domainId = "DDUI";
				operation.operationId = permission;
				grantedPermissions.addItem(operation);
			}
			user.grantedOperations = grantedPermissions;
			
			return user;
		}				
		
		public static function getSuperDuperUser():User {
			var user:User = new User();
			
			var grantedPermissions:ArrayCollection = new ArrayCollection();
			
			var classInfo:XML = describeType(getDefinitionByName("psp.sap.application.enums.OperationsEnum"));
			for each (var v:XML in classInfo..constant) {
				var permission:String = OperationsEnum[v.@name];
				
				var operation:UserOperation = new UserOperation();
				operation.domainId = "DDUI";
				operation.operationId = permission;
				grantedPermissions.addItem(operation);
			}
									
			user.grantedOperations = grantedPermissions;
			
			return user;			
		} 

	}
}