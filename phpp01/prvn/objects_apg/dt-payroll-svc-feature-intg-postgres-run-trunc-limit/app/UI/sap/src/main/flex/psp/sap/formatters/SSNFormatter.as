package psp.sap.formatters
{
	import mx.formatters.Formatter;
	import mx.formatters.SwitchSymbolFormatter;
	
	import psp.sap.application.SAP;

	public class SSNFormatter extends Formatter
	{
		
		private var switchSymbolFormatter:SwitchSymbolFormatter = new SwitchSymbolFormatter();
		
		public function SSNFormatter()
		{
			super();
		}
		
		
		override public function format(value:Object):String
    	{
    		return switchSymbolFormatter.formatValue(SAP.instance.configuration.ssnFormat, value.toString());
    	}
		
		
	}
}