package com.intuit.sbd.payroll.psp.bedl;

import org.w3c.dom.Element;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;

import java.util.List;
import java.util.ArrayList;

/**
 * A wrapper for a DataType DOM node from the generated bedl file
 */
public class BedlDataType extends BedlDataEntity {
    public BedlDataType(BedlProcessor pBedlProcessor, Element pDataEntity) {
        super(pBedlProcessor, pDataEntity);
    }

    @Override
    public boolean getIsComplexObject() {
        return true;
    }
}