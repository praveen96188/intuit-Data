package com.intuit.sbd.payroll.psp.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class IndustryTypeTests {

    @Test
    public void verifyQuickbooksIndustryHasEquivalentStandardIndustry() {
        List<String> industryTypes = removedLegacyQuickbooksIndustryList();
        industryTypes.parallelStream().forEach((industryName) -> {
            assertTrue(String.format("Industry Name match not found for %s", industryName), IndustryType.QUICKBOOKS_INDUSTRY_STANDARD_INDUSTRY_NAME_MAP.containsKey(industryName));
        });
    }

    public List<String> removedLegacyQuickbooksIndustryList() {
        List<String> industryTypes = new ArrayList<>();
        industryTypes.add("Accounting or Bookkeeping");
        industryTypes.add("Advertising or Public Relations");
        industryTypes.add("Agriculture, Ranching, or Farming");
        industryTypes.add("Art, Writing, or Photography");
        industryTypes.add("Automotive Sales or Repair");
        industryTypes.add("Church or Religious Organization");
        industryTypes.add("Construction General Contractor");
        industryTypes.add("Construction Trades (Plumber, Electrician, HVAC,...)");
        industryTypes.add("Design, Architecture, Engineering");
        industryTypes.add("Hair Salon, Beauty Salon, or Barber Shop");
        industryTypes.add("Information Technology (Computers, Software)");
        industryTypes.add("Insurance Agency or Broker");
        industryTypes.add("Lawn Care or Landscaping");
        industryTypes.add("Legal Services");
        industryTypes.add("Lodging (Hotel, Motel)");
        industryTypes.add("Manufacturing");
        industryTypes.add("Medical, Dental, or Health Service");
        industryTypes.add("Non-Profit");
        industryTypes.add("Professional Consulting");
        industryTypes.add("Real Estate Brokerage or Developer");
        industryTypes.add("Rental");
        industryTypes.add("Repair Maintenance");
        industryTypes.add("Restaurant, Caterer, or Bar");
        industryTypes.add("Retail Shop or Online Commerce");
        industryTypes.add("Transportation, Trucking, or Delivery");
        industryTypes.add("Wholesale Distribution and Sales");
        industryTypes.add("General Product-based Business");
        return industryTypes;
    }
}
