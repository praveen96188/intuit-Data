package intuit.sbd.flex.framework.validation
{
	import mx.events.ValidationResultEvent;
	
	public interface IPropertyValidation
	{
		function getValidationResultEvent(property: String): ValidationResultEvent;
		
		[Bindable("isValidChanged")]
		function get isValid():Boolean;
	}
}