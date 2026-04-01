package com.intuit.sbd.payroll.psp.bedl;

import org.w3c.dom.Element;

/**
 * A wrapper for a DataRelation DOM node from the generated bedl file
 */
public class BedlRelation {
    public BedlRelation(Element pRelation) {
        relation = pRelation;

        Element source = (Element) relation.getElementsByTagName("Source").item(0);
        Element target = (Element) relation.getElementsByTagName("Target").item(0);

        sourceUpperBoundMultiplicity = getMultiplicity(source, "UpperBound");
        targetUpperBoundMultiplicity = getMultiplicity(target, "UpperBound");

        sourceLowerBoundMultiplicity = getMultiplicity(source, "LowerBound");
        targetLowerBoundMultiplicity = getMultiplicity(target, "LowerBound");

        sourceType = ((Element) source.getElementsByTagName("Type").item(0)).getAttribute("Type");
        targetType = ((Element) target.getElementsByTagName("Type").item(0)).getAttribute("Type");

        sourceClassName = resolveClassName(source);
        targetClassName = resolveClassName(target);

        sourceClassNameForFk = resolveClassName(source, sourceUpperBoundMultiplicity);
        targetClassNameForFk = resolveClassName(target, targetUpperBoundMultiplicity);

        direction = Direction.valueOf(relation.getAttributes().getNamedItem("Direction").getNodeValue());
        
        relationType = relationType.valueOf(relation.getAttributes().getNamedItem("Type").getNodeValue());
    }

    private String resolveClassName(Element classElement) {
        String className = ((Element) classElement.getElementsByTagName("Type").item(0)).getAttribute("Type");
        if (classElement.hasAttribute("Role") &&
                classElement.getAttribute("Role").length() > 0) {
            className = classElement.getAttribute("Role");
        }
        return className;

    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public String getSourceClassNameForFk() {
        return sourceClassNameForFk;
    }

    public Multiplicity getSourceUpperBoundMultiplicity() {
        return sourceUpperBoundMultiplicity;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public String getTargetClassNameForFk() {
        return targetClassNameForFk;
    }

    public Multiplicity getTargetUpperBoundMultiplicity() {
        return targetUpperBoundMultiplicity;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public Multiplicity getSourceLowerBoundMultiplicity() {
        return sourceLowerBoundMultiplicity;
    }

    public void setSourceLowerBoundMultiplicity(Multiplicity sourceLowerBoundMultiplicity) {
        this.sourceLowerBoundMultiplicity = sourceLowerBoundMultiplicity;
    }

    public Multiplicity getTargetLowerBoundMultiplicity() {
        return targetLowerBoundMultiplicity;
    }

    public void setTargetLowerBoundMultiplicity(Multiplicity targetLowerBoundMultiplicity) {
        this.targetLowerBoundMultiplicity = targetLowerBoundMultiplicity;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public enum Multiplicity {
        Zero,
        One,
        Many
    }

    public enum Direction {
        SourceToTarget,
        TargetToSource,
        Bidirectional
    }

    public enum RelationshipSide {
        Source,
        Target
    }

    public enum RelationType {
        Association,
        Composition
    }

    private Multiplicity getMultiplicity(Element parentElement, String attributeName) {
        /**
         *
      <Relation Type="Association" Direction="Bidirectional">
      <Source>
        <Type Type="ACHEnrollment" />
        <Multiplicity LowerBound="0" UpperBound="1" />
      </Source>
      <Target>
        <Type Type="CompanyAgency" />
        <Multiplicity UpperBound="1" />
      </Target>
    </Relation>
         */
        Element multiplicityElement = (Element) parentElement.getElementsByTagName("Multiplicity").item(0);
        if(multiplicityElement == null){
            if (attributeName.equals("UpperBound"))
                return Multiplicity.One;
            else
                return Multiplicity.Zero;
        }

        if (multiplicityElement.hasAttribute(attributeName) &&
                multiplicityElement.getAttribute(attributeName).equals("*")) {
            return Multiplicity.Many;
        } else if (multiplicityElement.hasAttribute(attributeName) &&
                multiplicityElement.getAttribute(attributeName).equals("0")) {
            return Multiplicity.Zero;
        } else {
            return Multiplicity.One;
        }

    }

    private String resolveClassName(Element classElement, Multiplicity upperBoundMultiplicity) {
        String className = ((Element) classElement.getElementsByTagName("Type").item(0)).getAttribute("Type");
        if (!upperBoundMultiplicity.equals(Multiplicity.Many) &&
                classElement.hasAttribute("Role") &&
                classElement.getAttribute("Role").length() > 0) {
            className = classElement.getAttribute("Role");
        }
        return className;
    }

    /*
     <Relation Type="Association" Direction="SourceToTarget">
     <Source>
     <Type Type="CompanyEvent" />
     <Multiplicity UpperBound="*" />
     </Source>
     <Target>
     <Type Type="Company" />
     <Multiplicity UpperBound="1" />
     </Target>
     </Relation>
     */
    private Element relation;
    private String sourceClassName = null;
    private String targetClassName = null;
    private String sourceType = null;
    private String targetType = null;
    private Multiplicity sourceUpperBoundMultiplicity = null;
    private Multiplicity targetUpperBoundMultiplicity = null;
    private Direction direction;
    private String sourceClassNameForFk = null;
    private String targetClassNameForFk = null;
    private Multiplicity sourceLowerBoundMultiplicity = null;
    private Multiplicity targetLowerBoundMultiplicity = null;
    private RelationType relationType;
}
