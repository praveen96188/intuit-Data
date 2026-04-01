package com.intuit.sbd.payroll.psp.processes;

/**
 * Interface for all PSP process flows and processes that make up other processes or process flows.
 * <p/>
 * Processes indicate their outcome in a ProcessResult return object.
 *
 * @author Dawn Haddan
 * @author Wiktor Kozlik
 */
public interface IProcess
{
    /**
     * Executes the process.
     */
    public ProcessResult execute();
}

