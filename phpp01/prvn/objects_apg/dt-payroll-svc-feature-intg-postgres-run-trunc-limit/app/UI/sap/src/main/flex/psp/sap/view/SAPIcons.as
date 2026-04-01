package psp.sap.view
{
	[Bindable]
	public class SAPIcons
	{	
		// plus and minus icons	
        [Embed(source="./assets/images/expand.png")]
        public var expand:Class;               
        [Embed(source="./assets/images/collapse.png")]
        public var collapse:Class;
        [Embed(source="./assets/images/add.png")]
        public var addRow:Class;
        [Embed(source="./assets/images/delete.png")]
        public var deleteRow:Class;

        // green arrows
        [Embed(source="./assets/images/back_16.png")]
        public var backGreenArrow16:Class;        
        [Embed(source="./assets/images/forwd_16.png")]
        public var fowardGreenArrow16:Class;        
        [Embed(source="./assets/images/down_16.png")]
        public var downGreenArrow16:Class; 
        
        // old arrows
        [Embed(source="./assets/images/arrow_down.png")]
        public var downGreenArrow:Class;
        [Embed(source="./assets/images/arrow_up.png")]
        public var upGreenArrow:Class;       
        
        // arrows with bar
        [Embed(source="./assets/images/up_arrow.png")]
        public var upArrowWithBar:Class;        
        [Embed(source="./assets/images/down_arrow.png")]
        public var downArrowWithBar:Class;

		// resize icons
		[Embed(source="./assets/images/sizeNS.gif")]
        public var sizeNSCursorSymbol:Class;
        [Embed(source="./assets/images/sizeNESW.gif")]
        public var sizeNESWCursorSymbol:Class;        
        [Embed(source="./assets/images/sizeWE.gif")]
        public var sizeWECursorSymbol:Class;
        [Embed(source="./assets/images/sizeNWSE.gif")]
        public var sizeNWSECursorSymbol:Class;                                
        [Embed(source="./assets/images/sizeAll.gif")]
        public var sizeAllCursorSymbol:Class;
        
         // export results
        [Embed(source="./assets/images/copy_16.png")]
        public var exportIcon:Class;       
        
         // print
        [Embed(source="./assets/images/print_16.png")]
        public var printIcon:Class;
        
        // asterisk
        [Embed(source="./assets/images/asterisk_black.png")]
		public var blackAsterisk:Class;		
		[Embed(source="./assets/images/asterisk_red.png")]
		public var redAsterisk:Class;
		
		// note icons (todo: these need to be consistant)
		[Embed(source="./assets/images/queue_notes.png")]
		public var queueNote:Class;
		[Embed(source="./assets/images/queue_has_notes.png")]
		public var queueHasNote:Class;
		[Embed(source="./assets/images/note_add.png")]
		public var addNote:Class;
		[Embed(source="./assets/images/note.png")]
		public var note:Class;
        
        // misc
        [Embed(source="./assets/images/accept.png")]
        public var greenCircleWithCheckMark:Class;
        [Embed(source="./assets/images/cancel.png")]
        public var redHexagonWithWhiteX:Class;
        [Embed(source="./assets/images/phone.png")]        
        public var communicationPrefIcon_Phone:Class;
		[Embed(source="./assets/images/email.png")]        
        public var communicationPrefIcon_Email:Class;        
        [Embed(source="./assets/images/logo_intuit.gif")]        
        public var intuitLogo:Class;
        [Embed(source="./assets/images/error.png")]        
        public var yellowTriangleExclamation:Class;
        [Embed(source="./assets/images/queue_error.png")]        
        public var redCircleExclamation:Class;
        [Embed(source="./assets/images/flag_red.png")]        
        public var redFlag:Class;
        [Embed(source="./assets/images/magnifier.png")]        
        public var magnifier:Class;
        [Embed(source="./assets/images/bullet_black.png")]        
        public var blackBullet:Class;
        [Embed(source="./assets/images/docs_16.png")]        
        public var docs:Class;

        [Embed(source="./assets/images/lock.png")]        
        public var lock:Class;
        [Embed(source="./assets/images/lock_unlock.png")]
        public var unLock:Class;
        [Embed(source="./assets/images/user_gray.png")]
        public var user:Class;
        [Embed(source="./assets/images/user_comment.png")]
        public var userComment:Class;
        [Embed(source="./assets/images/money_dollar.png")]
        public var moneyDollar:Class;
        [Embed(source="./assets/images/help.png")]
        public var help:Class;

        [Embed(source="./assets/images/table_relationship.png")]
        public var relationship:Class;

		[Embed(source="./assets/images/user_add.png")]
        public var userAdd:Class;


        [Embed(source="./assets/images/drop_down.png")]
        public var dropDown:Class;

        [Embed(source="./assets/images/history.png")]
        public var history:Class;

        [Embed(source="./assets/images/copy.png")]
        public var copy:Class;

        [Embed(source="./assets/images/bell.png")]
        public var bell:Class;


        [Embed("./assets/images/loader.swf")]
        public static var spinner:Class;

	}
}
