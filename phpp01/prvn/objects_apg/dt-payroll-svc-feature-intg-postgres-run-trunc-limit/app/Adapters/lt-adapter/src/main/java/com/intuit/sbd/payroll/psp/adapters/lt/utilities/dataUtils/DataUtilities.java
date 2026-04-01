package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils;
import java.util.HashMap;

/**
 * This is a utility class for generating random data.  It's arguments and return types have been specifically designed
 * to work with SOATest.  Within SOATest, it's not always possible to pass in arguments, so with one exception
 * (getRandomString) none of the methods require an argument.  Also, all return either a string or and xml object since
 * those are the two return types most easily used with SOATest <br/><br/> The one method that requires an arguement
 * <code>getRandomString(int)</code> is passed an int and returns a random numeric string the size of the int passed
 * in.
 */
public class DataUtilities {

    /**
     * Given an integer, this method returns a string of random numbers that is the length of the integer.  For
     * instance, if <code>totalDigits</code> contained a "3", the method would return a string of numbers 3 digits
     * long.
     *
     * @param totalDigits - length of the desired string of random numbers
     * @return string of numbers <code>totalDigits</code> in length.
     */
    public String getRandomString(int totalDigits) {

        String rndNumb = "";
        Integer n;

        //Create the random numbers
        for (int x = 0; x < totalDigits; x++) {
            n = (int) (9.0 * Math.random()) + 1;
            rndNumb = rndNumb + n.toString();
        }

        return rndNumb;

    }

    /**
     * Used to generate 16 digit random numbers
     * @return 16 digit random string
     */
    public String getLargeRandomNumber(){
        return getRandomString(16);
    }


    /**
     * Given a length, this will return a string of random characters, the length specified
     * @param totalDigits - length of the returned string
     * @return string of random charachers
     */
    public String getRandomStringChar(int totalDigits){

        StringBuilder stringOut = new StringBuilder();

        //Create the random chars and append to string
        for (int x = 0; x < totalDigits; x++) {
            char n = (char)((int)'A'+Math.random()*((int)'Z'-(int)'A'+1));
            stringOut.append(n);
        }

        return stringOut.toString();
    }


    /**
     * Creates an address by concatinating a random street number and a street name and type chosen from lists.  The
     * address is returned as a string.
     *
     * @return the randomly generated address
     */
    public String getRandomStreetAddress() {

        String[] streetList = {"Main", "First", "Second", "Third", "Forth", "Sierra", "Rainbow", "Garnet", "Saphire", "Rose",
                "Vista", "Northwoods", "Donner", "Jiboom", "Sqwaw", "Tahoe", "Graymalkin", "Disney", "Stalion", "Nova",
                "Mustang", "Colt", "Castle", "West", "North", "East", "South", "Shoreline", "Center", "Red",
                "Blue", "Orange", "Maple", "Dogwood", "Aspen", "Pine", "Juniper", "Oak", "Redwood", "Spruce",
                "Heavenly", "Homewood", "Alpine Meadows", "Northstar", "Diamond Peak", "Sugar Bowl", "Boreal",
                "Ranch", "Cottage Springs", "Intuit"};
        String[] extensionList = {"Pl", "Ct", "Ave", "Dr", "Rd", "Alley", "Blvd", "Cir", "Ln"};
        String number = Integer.toString((int) (1 + (Math.random() * 10000)));

        String street = streetList[((int) ((Math.random() * streetList.length)))];
        String extension = extensionList[((int) ((Math.random() * extensionList.length)))];

        String address = number + " " + street + " " + extension;

        return address;
    }


    /**
     * Randomly picks a city name from the hard coded values assigned to the <code>cityList</code> array and returns it
     * as a string
     *
     * @return the city name
     */
    public String getRandomCity() {

        String[] cityList = {"New York", "Los Angeles", "Chicago", "Houston", "Philadelphia", "Phoenix", "San Diego", "Dallas",
                "San Antonio", "Detroit", "San Jose", "Indianapolis", "San Francisco", "Jacksonville", "Columbus",
                "Austin", "Memphis", "Baltimore", "Milwaukee", "Boston", "El Paso", "Nashville", "Denver", "Seattle",
                "Washington", "Charlotte", "Fort Worth", "Portland", "Las Vegas", "Tucson", "Oklahoma City",
                "New Orleans", "Cleveland", "Long Beach", "Albuquerque", "Kansas City", "Fresno", "Virginia Beach",
                "Atlanta", "Sacramento", "Mesa", "Oakland", "Tulsa", "Omaha", "Minneapolis", "Colorado Springs",
                "Miami", "Honolulu", "Saint Louis", "Wichita"};

        String city = cityList[((int) ((Math.random() * cityList.length)))];

        return city;

    }

    /**
     * Randomly selects a name from the hard coded values assigned to the <code>nameList</code> array and returns it as
     * a string. NOTE: this method can also be called to get a middle name since the next time it's called it will
     * select a different name from the list
     *
     * @return the chosen name
     */
    public String getFirstName() {
        String[] nameList = {"JAMES", "JOHN", "ROBERT", "MICHAEL", "WILLIAM", "DAVID", "RICHARD", "CHARLES", "JOSEPH", "THOMAS",
                "CHRISTOPHER", "DANIEL", "PAUL", "MARK", "DONALD", "GEORGE", "KENNETH", "STEVEN", "EDWARD", "BRIAN", "RONALD",
                "ANTHONY", "KEVIN", "JASON", "BARBARA", "JENNIFER", "SUSAN", "DOROTHY", "NANCY", "BETTY", "SANDRA", "CAROL",
                "SHARON", "LAURA", "KIMBERLY", "JESSICA", "CYNTHIA", "MELISSA", "AMY", "REBECCA", "KATHLEEN", "MARTHA", "AMANDA",
                "CAROLYN", "MARIE", "CATHERINE", "ANN", "DIANE", "JULIE", "TERESA"};

        String name = nameList[((int) (Math.random() * nameList.length))];

        return name;

    }


    /**
     * Randomly selects a name from the hard coded values assigned to the <code>nameList</code> array and returns it as
     * a string
     *
     * @return the chosen name
     */
    public String getLastName() {
        String[] nameList = {"SMITH", "JOHNSON", "WILLIAMS", "JONES", "BROWN", "DAVIS", "MILLER", "WILSON", "MOORE", "TAYLOR",
                "ANDERSON", "THOMAS", "JACKSON", "WHITE", "HARRIS", "MARTIN", "THOMPSON", "GARCIA", "MARTINEZ", "ROBINSON",
                "CLARK", "RODRIGUEZ", "LEWIS", "LEE", "WALKER", "HALL", "ALLEN", "YOUNG", "HERNANDEZ", "KING", "WRIGHT", "LOPEZ",
                "HILL", "SCOTT", "GREEN", "ADAMS", "BAKER", "GONZALEZ", "NELSON", "CARTER", "MITCHELL", "PEREZ", "ROBERTS", "TURNER",
                "PHILLIPS", "CAMPBELL", "PARKER", "EVANS", "EDWARDS", "COLLINS"};

        String name = nameList[((int) (Math.random() * nameList.length))];

        return name;

    }


    /**
     * Creates a random email address by generating a random number and concatinating the <code>"@domain.com"</code> on
     * the end and returns it as a string.  The domian is randomly selected from the hard coded values assigned to the
     * <code>domainList</code> array
     *
     * @return the email address
     */
    public String getEmailAddress() {
        String[] domainList = {"yahoo", "google", "aol", "earthlink", "gmail", "hotmail"};

        String user = Integer.toString((int) (100 + (Math.random() * 90000)));

        String email = user + "@" + domainList[((int) (Math.random() * domainList.length))] + ".com";

        return email;
    }


    /**
     * Creates a random phone number by concatinating a random 3 digit number (Area Code), a hyphen, another random 3
     * digit number (prefix), another hyphen, and a random 4 digit number(Suffix) - which is returned as a String.  This
     * method uses the <code>getRandomString(int)</code> method within this class.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {

        String phoneNumber = getRandomString(3) + "-" + getRandomString(3) + "-" + getRandomString(4);

        return phoneNumber;

    }


    /**
     * Uses the <code>getRandomString(int)</code> method to return a 9 digit, numeric string.  Althougth this seems
     * redundant since it's just being passed a single parameter - it's used because passing parameters from SOATest can
     * be difficult from the method tool and impossible from a scripted field.  By passing this a 9, the numeric string
     * returned is suitable for use as: <ul> <li>SSN</li> <li>FEIN</li> <li>Bank Account Number</li> </ul> Or, it can be
     * used as an Id, such as: <ul> <li>EmployeeId</li> <li>TransactionId</li> <li>BatchId</li> <li>AccountId</li>
     * </ul>
     *
     * @return a 9 digit numeric string
     */
    public String getRandomId() {

        return getRandomString(9);
    }


    /**
     * Generates a string, suitable for use as a company number by concatinating the word <i>"Test"</i> with a 10 digit
     * numeric string.  Although the 9 digit number created by <code>getRandomId</code> is suiteable for a company
     * number or Id, it's easier to read in the database if there is an alpha appended to it.  Also, this diferenciates
     * our companies from others, such as QBOE.  The 10 digits should also ensure enough variety to avoid a condition of
     * the same name being generated
     *
     * @return an alpha numeric string in the form of: <code>test1234567890</code>
     */
    public String getCompanyID() {
        String id = "Test" + getRandomString(10);

        return id;
    }


    /**
     * Returns a <code>HashMap<String,String></code> containing a random state and a valid zip code for that state.
     *          Key:    State
     *          Value:  Zip Code
     * @return The HashMap containing the randomized info
     */
    public HashMap<String, String> getStateAndZip() {
        HashMap<String, String> stateZip = new HashMap<String, String>() {
            {
                put("AL", "35236");   put("GA", "30006");   put("MD", "21401");   put("NJ", "07105");   put("RI", "02906");
                put("AK", "99509");   put("HI", "96813");   put("MA", "02120");   put("NM", "87106");   put("SC", "29204");
                put("AZ", "85011");   put("ID", "83301");   put("MI", "48104");   put("NM", "87106");   put("SD", "57104");
                put("AR", "72204");   put("IL", "60621");   put("MN", "55104");   put("NM", "87106");   put("TN", "37207");
                put("CA", "96161");   put("IN", "46202");   put("MS", "39203");   put("NC", "27406");   put("TX", "57211");
                put("CO", "80301");   put("IA", "50316");   put("MO", "63107");   put("ND", "58102");   put("UT", "84106");
                put("CT", "06058");   put("KS", "66103");   put("MT", "59105");   put("OH", "43701");   put("VT", "05401");
                put("DE", "19801");   put("KY", "40206");   put("NE", "68101");   put("OK", "73101");   put("VA", "22032");
                put("DC", "20001");   put("LA", "70126");   put("NV", "89502");   put("OR", "97045");   put("WA", "98006");
                put("FL", "32114");   put("ME", "04102");   put("NH", "03801");   put("PA", "15204");   put("WV", "25311");
                put("WI", "53209");
                put("WY", "82071");
            }
        };

        //Select a random state from the states in the KeySet
        String state = (String) stateZip.keySet().toArray()[((int) (Math.random() * stateZip.size()))];

        //Create the HashMap to return
        HashMap<String, String> returnStateZip = new HashMap<String, String>();

        //Insert the state and zip
        returnStateZip.put(state, stateZip.get(state));

        return returnStateZip;
    }


}
