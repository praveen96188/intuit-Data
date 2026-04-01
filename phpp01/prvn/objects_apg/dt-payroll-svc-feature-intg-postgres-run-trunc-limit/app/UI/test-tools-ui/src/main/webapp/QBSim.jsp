<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- saved from url=(0072)https://ewsmain1.quickbooks.com/sub/dev/crissim.jsp?browserType=embedded -->
<html class="translated-ltr">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>browserType=embedded</title>
    <script language="JAVASCRIPT">
        <!--
        var newTitle;
        var pName;
        function stripServer() {
            var oldVal = window.document.URLform.URLbox.value;
            var pos = oldVal.indexOf("?");
            window.document.URLform.URLbox.value = oldVal.substring(pos + 1, oldVal.length);
            return false;
        }
        function pasteIntoBox() {
            window.document.URLform.URLbox.value = window.clipboardData.getData("Text");
            window.document.URLform.URLbox.focus();
            return false;
        }

        function pasteIntoBoxByIndex(index) {
            window.document.getElementById('pValue' + index).value = window.clipboardData.getData("Text");
            window.document.getElementById('pValue' + index).focus();
            return false;
        }

        function changeTitleFromUrl() {
            window.document.title = window.document.URLform.URLbox.value;
            return false;
        }

        function signupDDResponse() {
            clearAllFields();
            window.document.thisForm.pName0.value = 'SERVICEKEY';
            window.document.thisForm.pValue0.value = '';
            window.document.thisForm.pName1.value = 'Subtype';
            window.document.thisForm.pValue1.value = '15';
            window.document.thisForm.pName2.value = 'FeatureStatus_DD';
            window.document.thisForm.pValue2.value = 'VERIFIED';
            window.document.thisForm.pName3.value = 'PayrollUserID';
            window.document.thisForm.pValue3.value = '';
            var params = document.getElementById("parameters");
            params.style.visibility = "visible";
            var submit = document.getElementById("submit");
            submit.style.visibility = "visible";
        }

        function signupAssistedResponse() {
            clearAllFields();
            window.document.thisForm.pName0.value = 'SERVICEKEY';
            window.document.thisForm.pValue0.value = '';
            window.document.thisForm.pName1.value = 'Subtype';
            window.document.thisForm.pValue1.value = '4';
            window.document.thisForm.pName2.value = 'FeatureStatus_DD';
            window.document.thisForm.pValue2.value = 'VERIFIED';
            window.document.thisForm.pName3.value = 'FeatureStatus_TFP';
            window.document.thisForm.pValue3.value = 'VERIFIED';
            window.document.thisForm.pName4.value = 'PayrollUserID';
            window.document.thisForm.pValue4.value = '';
            var params = document.getElementById("parameters");
            params.style.visibility = "visible";
            var submit = document.getElementById("submit");
            submit.style.visibility = "visible";
        }

        function clearAllFields() {
            var formelems = window.document.thisForm.elements;
            if (formelems != null) {
                for (i = 0; i < formelems.length; i++) {
                    var formelem = formelems.item(i);
                    if (!formelem.disabled && formelem.type == "text") {
                        formelem.value = "";
                    }
                }
            }
        }
        function execCmd(cmd) {
            window.document.location.href = cmd;
            return false;
        }
        function changeTitle() {
            newTitle = "";
            readForm();
            if (newTitle == "") {
                newTitle = "NO PARAMS";
            }
            window.document.title = newTitle;
            return false;
        }
        function sendResponseToQBViaCommand_FromURL() {
            newTitle = window.document.URLform.URLbox.value;
            execCmd("qbks2://qbw:service_signup_response?" + newTitle);
            return false;
        }
        function sendResponseToQBViaCommand() {
            newTitle = "";
            readForm();
            if (newTitle == "") {
                newTitle = "NOPARAMS";
            }
            execCmd("qbks2://qbw:service_signup_response?" + newTitle);
            return false;
        }
        function putValue(name, value) {
            if (name == "pName") {
                pName = value;
                return;
            }
            if (name == "pValue" && pName.length > 0 && value.length >= 0) {
                if (newTitle.length > 0) {
                    newTitle += "&";
                }
                newTitle += pName + "=" + value;
            }
        }
        function readForm() {
            var formelems = window.document.thisForm.elements;
            if (formelems != null) {
                for (i = 0; i < formelems.length; i++) {
                    var formelem = formelems.item(i);
                    if (!formelem.disabled && formelem.type != "submit" && formelem.type != "button") {
                        if (formelem.type == "select-multiple") {
                            var seloptions = formelem.options;
                            if (seloptions != null) {
                                for (j = 0; j < seloptions.length; j++) {
                                    var selopt = seloptions.item(j);
                                    if (selopt.selected) {
                                        putValue(formelem.name, selopt.value);
                                    }
                                }
                            }
                        } else {
                            if (formelem.type == "checkbox" || formelem.type == "radio") {
                                if (formelem.checked) {
                                    putValue(formelem.name, formelem.value);
                                }
                            } else {
                                putValue(formelem.name, formelem.value);
                            }
                        }
                    }
                }
            }
        }

        function onServiceSubmit() {
            changeTitle();
            sendResponseToQBViaCommand();
        }

        function closeBrowser() {
            execCmd('qbks://qbw:closebrowser')
        }

        function toggleCRISstuff() {
            var oldStuff = document.getElementById("oldCrisStuff");
            var params = document.getElementById("parameters");
            var submit = document.getElementById("submit");
            if(oldStuff.style.display == "block") {
                oldStuff.style.display = "none";

                if(document.getElementById("pName0").value == null || document.getElementById("pName0").value == "") {
                    params.style.visibility = "hidden";
                    submit.style.visibility = "hidden";
                }
            } else {
                oldStuff.style.display = "block";
                params.style.visibility = "visible";
                submit.style.visibility = "visible";
            }
        }
        //-->
    </script>
    <style type="text/css">
        <!--
        BODY, H1, H2, TABLE, TR, TH, TD, FORM, INPUT, BUTTON {
            font-family: MS Sans Serif, Tahoma, Verdana, Arial;
            font-size: 10pt;
        }

        .tinyfont {
            font-family: MS Sans Serif, Tahoma, Verdana, Arial;
            font-size: 8pt;
        }
        -->
    </style>
</head>
<body>
<table border="1" cellpadding="10" cellspacing="0">
    <tbody>
    <tr>
        <td valign="top">
        <b>1. Choose a signup type</b>
        <br/>
        <button type="BUTTON" name="signupDDresp" onclick="signupDDResponse()">DD Signup</button>
        &nbsp;
        <button type="BUTTON" name="signupAssistedresp" onclick="signupAssistedResponse()">Assisted Signup</button>
        <br/>
        <br/>
        <div id="parameters" style="visibility: hidden;">
        <b>2. Update missing values and/or add additional parameters</b>
            <form name="thisForm" id="thisForm" onsubmit="return changeTitle()">
                <table>
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                        <th></th>
                    </tr>
                    <tr>
                        <td><label for="pName0"><u style="ACCELERATOR:true">0</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="0"
                                                                                                       name="pName"
                                                                                                       id="pName0">
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue0"></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName0.value=&#39;&#39;;window.document.thisForm.pValue0.value=&#39;&#39;;window.document.thisForm.pName0.focus();">
                                Cl
                            </button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName0.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue0.value=&#39;&#39;;window.document.thisForm.pValue0.focus();">
                                SK
                            </button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(0)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName1"><u style="ACCELERATOR:true">1</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="1"
                                                                                                       name="pName"
                                                                                                       id="pName1"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue1" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName1.value=&#39;&#39;;window.document.thisForm.pValue1.value=&#39;&#39;;window.document.thisForm.pName1.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName1.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue1.value=&#39;&#39;;window.document.thisForm.pValue1.focus();">SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(1)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName2"><u style="ACCELERATOR:true">2</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="2"
                                                                                                       name="pName"
                                                                                                       id="pName2"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue2" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName2.value=&#39;&#39;;window.document.thisForm.pValue2.value=&#39;&#39;;window.document.thisForm.pName2.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName2.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue2.value=&#39;&#39;;window.document.thisForm.pValue2.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(2)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName3"><u style="ACCELERATOR:true">3</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="3"
                                                                                                       name="pName"
                                                                                                       id="pName3"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue3" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName3.value=&#39;&#39;;window.document.thisForm.pValue3.value=&#39;&#39;;window.document.thisForm.pName3.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName3.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue3.value=&#39;&#39;;window.document.thisForm.pValue3.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(3)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName4"><u style="ACCELERATOR:true">4</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="4"
                                                                                                       name="pName"
                                                                                                       id="pName4"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue4" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName4.value=&#39;&#39;;window.document.thisForm.pValue4.value=&#39;&#39;;window.document.thisForm.pName4.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName4.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue4.value=&#39;&#39;;window.document.thisForm.pValue4.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(4)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName5"><u style="ACCELERATOR:true">5</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="5"
                                                                                                       name="pName"
                                                                                                       id="pName5"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue5" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName5.value=&#39;&#39;;window.document.thisForm.pValue5.value=&#39;&#39;;window.document.thisForm.pName5.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName5.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue5.value=&#39;&#39;;window.document.thisForm.pValue5.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(5)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName6"><u style="ACCELERATOR:true">6</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="6"
                                                                                                       name="pName"
                                                                                                       id="pName6"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue6" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName6.value=&#39;&#39;;window.document.thisForm.pValue6.value=&#39;&#39;;window.document.thisForm.pName6.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName6.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue6.value=&#39;&#39;;window.document.thisForm.pValue6.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(6)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName7"><u style="ACCELERATOR:true">7</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="7"
                                                                                                       name="pName"
                                                                                                       id="pName7"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue7" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName7.value=&#39;&#39;;window.document.thisForm.pValue7.value=&#39;&#39;;window.document.thisForm.pName7.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName7.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue7.value=&#39;&#39;;window.document.thisForm.pValue7.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(7)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName8"><u style="ACCELERATOR:true">8</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="8"
                                                                                                       name="pName"
                                                                                                       id="pName8"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue8" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName8.value=&#39;&#39;;window.document.thisForm.pValue8.value=&#39;&#39;;window.document.thisForm.pName8.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName8.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue8.value=&#39;&#39;;window.document.thisForm.pValue8.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(8)">P</button>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="pName9"><u style="ACCELERATOR:true">9</u>:</label>&nbsp;<input type="TEXT"
                                                                                                       accesskey="9"
                                                                                                       name="pName"
                                                                                                       id="pName9"
                                                                                                       value="" >
                        </td>
                        <td><input type="TEXT" name="pValue" id="pValue9" value=""></td>
                        <td>
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName9.value=&#39;&#39;;window.document.thisForm.pValue9.value=&#39;&#39;;window.document.thisForm.pName9.focus();">
                                Cl</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON"
                                    onclick="window.document.thisForm.pName9.value=&#39;SERVICEKEY&#39;;window.document.thisForm.pValue9.value=&#39;&#39;;window.document.thisForm.pValue9.focus();">
                                SK</button>
                            &nbsp;
                            <button class="tinyfont" type="BUTTON" onclick="pasteIntoBoxByIndex(9)">P</button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
        <div id="submit" style="visibility: hidden;">
            <br/>
            <b>3. Submit to QB</b>
            <br/>
            <button type="RESET">Reset</button>
            &nbsp;
            <button type="BUTTON" onclick="onServiceSubmit()">a. Submit Signup</button>
            &nbsp;
            <button type="BUTTON" onclick="closeBrowser()">b. Close Browser</button>
        </div>
        </td>
    </tr>
    </tbody>
</table>
<button type="button" onclick="toggleCRISstuff()">Show/Hide Old CRIS Stuff</button>
<div id="oldCrisStuff" style="display: none">
<hr>
<button type="BUTTON" accesskey="i" onclick="changeTitle()"><b>Change T <u
        style="ACCELERATOR:true">i</u> tle</b>
</button>
&nbsp;
<button type="BUTTON" accesskey="q" onclick="sendResponseToQBViaCommand()"><b>Send To <u
        style="ACCELERATOR:true">Q</u>uickBooks</b></button>
<br><br>
<button type="BUTTON" accesskey="c" class="tinyfont" onclick="clearAllFields()"><u
        style="ACCELERATOR:true">C</u>lear All
</button>
&nbsp;
<br/>
<b>Complete list of incoming GET and POST parameters:</b><br><br>
<table border="1" cellpadding="3" cellspacing="0">
    <tbody>
    <tr>
        <th>Name</th>
        <th>Value</th>
    </tr>
    <tr>
        <td>browser-type</td>
        <td>embedded</td>
    </tr>
    </tbody>
</table>
<hr>
<form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
    <input type="text" size="50" name="qbcmd" value="qbks://qbw:setup_direct_deposit">&nbsp;
    <button type="submit">Execute QBCommand</button>
</form>
<table>
    <tr>
        <td>
            <table>
                <tbody>
                <tr>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=Migrating">
                            <button type="submit">Migrate</button>
                        </form>
                    </th>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=SystemError">
                            <button type="submit">Turnaway</button>
                        </form>
                    </th>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=UserCancelled">
                            <button type="submit">UserCancelled</button>
                        </form>
                    </th>
                </tr>
                <tr>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=UserError">
                            <button type="submit">CCAuthFailed</button>
                        </form>
                    </th>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=SubtypeResync">
                            <button type="submit">SubtypeSync</button>
                        </form>
                    </th>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:SetOTLStatus?ResponseStatus=Reactivating">
                            <button type="submit">Reactivating</button>
                        </form>
                    </th>
                </tr>
                <tr>
                    <th>
                        <form onsubmit="return execCmd(qbcmd.value)" name="qbcmdtest">
                            <input type="hidden" size="50" name="qbcmd" value="qbks://qbw:closebrowser">
                            <button type="submit">CloseBrowser</button>
                        </form>
                    </th>
                </tr>
                </tbody>
            </table>
        </td>
        <td valign="top">... or paste new title string here:
            <form name="URLform"><label for="URLbox">N <u
                    style="ACCELERATOR:true">e</u> w
                Title</label> :<br><textarea accesskey="e" id="URLbox" rows="10"
                                             cols="25">browserType=embedded</textarea><br>
                <button type="text" accesskey="l" onclick="changeTitleFromUrl()"><b>Change
                    Tit <u style="ACCELERATOR:true">l</u>
                    e</b></button>
                &nbsp;
                <button type="BUTTON" onclick="sendResponseToQBViaCommand_FromURL()"><b>Send To QuickBooks</b></button>
                <br><br>
                <button class="tinyfont" type="text" accesskey="p" title="Click here to paste in contents of clipboard"
                        onclick="pasteIntoBox()"><u style="ACCELERATOR:true">P</u>
                    aste</button>
                &nbsp;
                <button type="text" class="tinyfont"
                        onclick="URLbox.value=&#39;&#39;;window.document.URLform.URLbox.focus();">Clear
                </button>
                &nbsp;
                <button class="tinyfont" type="text" onclick="stripServer()">Strip stuff before "?"</button>
            </form>
        </td>
    </tr>
</table>
<hr>
<p>crissim - Silly overly complicated CRIS-Web simulator<br>Change TITLE of this page by either typing in name/value
    pairs in table on the left or pasting desired title string into box on the right.<br><br></p>
</div>
</body>
</html>