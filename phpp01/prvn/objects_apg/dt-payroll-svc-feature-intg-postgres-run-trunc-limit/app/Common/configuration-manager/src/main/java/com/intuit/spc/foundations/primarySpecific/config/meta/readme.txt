These files have been manually changed to support substitutionGroup:

(Everytime a new configuration provider is added, you must change ModuleDescriptor.java and the 
configuration provider class itself!!!)

ModuleDescriptor.java -- added Xml, properties, and Custom
ConfigProvider.java -- made abstract
Custom.java -- made it extend ConfigProvider
Xml.java -- made it extend ConfigProvider
Properties.java -- made it extend ConfigProvider