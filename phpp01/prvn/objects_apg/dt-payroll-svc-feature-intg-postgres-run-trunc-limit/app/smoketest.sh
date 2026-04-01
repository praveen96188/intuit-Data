#
# Smoketest.sh 
# 
# Perform basic smoke tests for PSP services
#
# parameters:
#     configenv - the config environment such as prdqa, prdqc, prdlv, etc.
#
# exit code: 
#     == 0 smoke tests passed
#     != 0 smoke tests failed
#

#
# If one command line parameter was passed in then it's the configenv
#
if [ $# = 1 ]; then configenv=$1; else configenv=""; fi

success=0
for x in {0..24}; do
  sleep 5

  echo "Verifying qboedd-ws"
  wget -nv http://`uname -n`:8080/qboedd-ws/services -O /dev/null || continue
  echo "Verifying SAP"
  wget -nv http://`uname -n`:8080/SAP/SAP.html -O /dev/null || continue

  echo "Verifying EWSwsdl"
  wget -nv http://`uname -n`:8080/EWSAdapter/services/EWSAdapter/v1_9?wsdl -O /tmp/EWSwsdl.tmp || continue
  grep 'xmlns:tns="http://webservices.v1_9.ews.adapters.psp.payroll.sbd.intuit.com/"' /tmp/EWSwsdl.tmp > /dev/null || continue

  echo "Verifying EWSxsd"
  wget -nv http://`uname -n`:8080/EWSAdapter/services/EWSAdapter/v1_9?xsd=1 -O /tmp/EWSxsd.tmp || continue
  grep 'xmlns:tns="http://webservices.v1_9.ews.adapters.psp.payroll.sbd.intuit.com/"' /tmp/EWSxsd.tmp > /dev/null || continue

  echo "Verifying qbdt"
  wget -nv http://`uname -n`:8080/payroll/payrollwebexchange.dll -O /tmp/QBDT.tmp || continue
  grep "<OFX>" /tmp/QBDT.tmp > /dev/null || continue

  echo "Verifying BillPaymentWebServices wsdl"
  wget -nv http://`uname -n`:8080/qbdtws/services/BillPaymentWebServices?wsdl -O /tmp/BillPaymentWebServicesWSDL.tmp || continue
  grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BillPaymentWebServicesWSDL.tmp > /dev/null || continue

  echo "Verifying BillPaymentWebServices xsd"
  wget -nv http://`uname -n`:8080/qbdtws/services/BillPaymentWebServices?xsd=1 -O /tmp/BillPaymentWebServicesXSD.tmp || continue
  grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BillPaymentWebServicesXSD.tmp > /dev/null || continue

  echo "Verifying QBPayrollWebServices wsdl"
  wget -nv http://`uname -n`:8080/qbdtws/services/QBPayrollWebServices?wsdl -O /tmp/QBPayrollWebServicesWSDL.tmp || continue
  grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/QBPayrollWebServicesWSDL.tmp > /dev/null || continue;
		
  echo "Verifying QBPayrollWebServices xsd"
  wget -nv http://`uname -n`:8080/qbdtws/services/QBPayrollWebServices?xsd=1 -O /tmp/QBPayrollWebServicesXSD.tmp || continue
  grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/QBPayrollWebServicesXSD.tmp > /dev/null || continue

  echo "Verifying IVRWebServices wsdl"
  wget -nv http://`uname -n`:8080/ivradapter/services/IVRWebServices?wsdl -O /tmp/IVRWebServicesWSDL.tmp || continue
  grep 'targetNamespace="http://webservices.ivr.adapters.psp.payroll.sbd.intuit.com/"' /tmp/IVRWebServicesWSDL.tmp > /dev/null || continue
		
  echo "Verifying IVRWebServices xsd"
  wget -nv http://`uname -n`:8080/ivradapter/services/IVRWebServices?xsd=1 -O /tmp/IVRWebServicesXSD.tmp || continue
  grep 'targetNamespace="http://webservices.ivr.adapters.psp.payroll.sbd.intuit.com/"' /tmp/IVRWebServicesXSD.tmp > /dev/null || continue

  echo "Verifying BRMWebServices xsd";
  wget -nv http://`uname -n`:8080/BRMAdapter/services/BRMWebServices?xsd=1 -O /tmp/BRMWebServicesXSD.tmp || continue
  grep 'targetNamespace="http://webservices.brm.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BRMWebServicesXSD.tmp > /dev/null || continue

  echo "Validating keynote adapter"
  wget -nv http://`uname -n`:8080/Keynote-Adapter/services/KeynoteWS?wsdl -O /tmp/Keynote-AdapterWSDL.tmp > /dev/null || continue
	
  # 
  # If we know our configenv then conditionally test those services which are only deployed
  # to some environments.
  #
  if [ ! $configenv = "" ]; then
    if [ ! $configenv = "prdqa" ] && [ ! $configenv = "prdqc" ] && [ ! $configenv = "prdlv" ]; then
      echo "Verifying ddrepui-ws"
      wget -nv http://`uname -n`:8080/ddrepui-ws/services -O /dev/null || continue
      echo "Verifying test-ws"
      wget -nv http://`uname -n`:8080/test-ws/services/PSPDateWS -O /dev/null || continue
      if [ ! $configenv = "pp" ]; then
          echo "Verifying testtools"
          wget -nv http://`uname -n`:8080/testtools -O /dev/null || continue
      fi
    fi
  fi

  # 
  # We made it through all the tests, success.
  #
  success=1
  echo "Verification successful"
  break
done

#
# Clean up the temp files created during the test
#
echo "Cleanup tmp files"
rm -f /tmp/EWSwsdl.tmp /tmp/EWSxsd.tmp /tmp/QBDT.tmp /tmp/BillPaymentWebServicesWSDL.tmp /tmp/BillPaymentWebServicesXSD.tmp /tmp/QBPayrollWebServicesWSDL.tmp /tmp/QBPayrollWebServicesXSD.tmp /tmp/IVRWebServicesWSDL.tmp /tmp/IVRWebServicesXSD.tmp

if [ $success = 0 ]; then
  echo "Error verifying app servers"   
  exit 1
fi

exit 0
