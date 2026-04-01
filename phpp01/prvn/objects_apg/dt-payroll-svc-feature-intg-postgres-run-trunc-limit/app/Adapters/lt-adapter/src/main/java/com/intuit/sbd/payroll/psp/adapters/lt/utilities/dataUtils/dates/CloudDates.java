package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates;



/**
 * Usage:   QBDTWS (Cloud) uses date formats that differ from other adapters.  There are also requirements for specific
 *          types of dates (ie DOB and Hire Dates).  Although implied, a DOB should be earlier than a hire date - this
 *          class creates these dates with those restrictions in mind.
 *
 *          In addition to Hire Date and DOB, the public helper method returns a data filled with random data that can
 *          be used as-is.
 */
public class CloudDates {

    /**
     * Gets a random date and then ensures the Year is between 1975 and 1960 (to ensure it comes before hire date)
     * @return a populated PspDateDTO
     */
    public DateDTO generateDateOfBirth(){

        DateDTO dobDTO = this.getRandomDate();

        int birthYear = (int)((1975.00 - 1960.00) * Math.random()) + 1960;
        dobDTO.setYear(birthYear);

        return dobDTO;
    }


    /**
     * Gets a random date and ensures the Year is between 1995 and 2009 (to ensure is comes after dob)
     * @return fully populated PspDateDTO
     */
    public DateDTO generateHireDate(){
        DateDTO dobDTO = this.getRandomDate();

        int hireYear = (int)((2009.00 - 1995.00) * Math.random()) + 1995;
        dobDTO.setYear(hireYear);

        return dobDTO;
    }


    /**
     * Public helper method that creates a PspDateDTO and populates it with random data
     * @return fully populated PspDateDTO
     */
    public DateDTO getRandomDate(){

        DateDTO dateDTO = new DateDTO();

        dateDTO.setMonth((int)(12.0 * Math.random()) + 1);
        dateDTO.setDay((int)(28.0 * Math.random()) +1);
        int randomYear = (int)((2009.00 - 1960.00) * Math.random()) + 1960;
        dateDTO.setYear(randomYear);

        return dateDTO;
    }
}
