package psp.app.util
{
	import flash.external.ExternalInterface;

	public class CookieUtil
	{
		/* Javascript function to set new cookie with the given name, value, path & domain. Expiry is 30 minutes.*/
		private static const FUNCTION_SETCOOKIE:String = 
			"document.insertScript = function ()" +
			"{ " +
				"if (document.snw_setCookie==null)" +
				"{" +
					"snw_setCookie = function (name, value, minutes, path, domain)" +
					"{" + 
						"if (minutes) {"+
							"var date = new Date();"+
							"date.setTime(date.getTime()+(minutes*60*1000));"+
							"var expires = '; expires='+date.toGMTString();"+
						"}" +
						"else var expires = '';"+
						"document.cookie = name+'='+value+expires+'; path=' + path + (typeof domain != 'undefined' ? ';domain=' + domain : '');" +
					"}" +
				"}" +
			"}";
		/* Javascript function to get cookie value with the given name, value, path & domain. This method is currently not used*/
		private static const FUNCTION_GETCOOKIE:String = 
			"document.insertScript = function ()" +
			"{ " +
				"if (document.snw_getCookie==null)" +
				"{" +
					"snw_getCookie = function (name)" +
					"{" +
						"var nameEQ = name + '=';"+
						"var ca = document.cookie.split(';');"+
						"for(var i=0;i < ca.length;i++) {"+
							"var c = ca[i];"+
							"while (c.charAt(0)==' ') c = c.substring(1,c.length);"+
							"if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);"+
						"}"+
						"return null;" +
					"}" +
				"}" +
			"}";
		
		
		private static var INITIALIZED:Boolean = false;
		
		public static function init():void {
			/* Call Javascript code to initialize Get/Set cookies javascript code.*/
			ExternalInterface.call(FUNCTION_GETCOOKIE);
			ExternalInterface.call(FUNCTION_SETCOOKIE);
			INITIALIZED = true;
		}
		
		public static function setCookie(name:String, value:Object, minutes:int = 999999, path:String = '/', domain:String = null):void {
		/* Call javascript API to set the cookie */
			if(!INITIALIZED)
				init();
			
			ExternalInterface.call("snw_setCookie", name, value, minutes, path, domain);
		}
		
		public static function getCookie(name:String):String {
		/* Call javascript API to read the cookie */
			if(!INITIALIZED)
				init();
			
			return ExternalInterface.call("snw_getCookie", name);
		}
		
		public static function deleteCookie(name:String):void {
			if(!INITIALIZED)
				init();
			
			ExternalInterface.call("snw_setCookie", name, "", -1);
		}

	}
}