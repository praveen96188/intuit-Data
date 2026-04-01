package com.intuit.sbd.payroll.psp.batchjobs.utils;

import java.util.ArrayList;
/** This class is used as a response object of the UTFXmlCompare class.
 * @author Zack Norcross
 * @version 1.0
 */
public class CompareResults
{
    private boolean nodesMatch = false;
    private String[] mismatchReasons = new String[]{};

    /** Default empty constructor
     */
    public CompareResults() {
    }

    /** This method sets the status variable.
     * @param set A boolean to set the status of the function.<br>
     * <b>true = matching nodes | false = nodes do not match</b>
     */
    public void setStatus(boolean set)
    {
        nodesMatch = set;
    }

    /** This method gets the status variable.
     * @return boolean
     */
    public boolean getStatus()
    {
        return nodesMatch;
    }

    /** This method sets the reasons array.
     * @param Reasons An ArrayList containing the reasons the nodes did not match.<br>
     * <b>The list will be null if no failures exist.</b>
     */
    public void setReasons(ArrayList Reasons)
    {
        mismatchReasons = new String[Reasons.size()];
        for(int i = 0; i<Reasons.size(); i++)
        {
            mismatchReasons[i] = (String)Reasons.get(i);
        }
    }

    /** This method gets the reasons variable.
     * @return String[]
     */
    public String[] getReasons()
    {
        return	mismatchReasons;
    }

    public String toString(){
        String retStr;
        retStr = "Status = " + nodesMatch + "\n";
        if(mismatchReasons!= null){
            for (int i = 0; i < mismatchReasons.length; i++) {
                retStr += "\nReason " + i + " = " + mismatchReasons[i];
            }
        }
        return retStr;
    }
}