/**
 * Created by Oleksandr on 3/31/2016.
 */
var type;
var scroll = 0;
var isAdmin;
var projectNumber = 0;
var configurator = (function () {
    var initPage = function () {


        var parameters = document.location.search.substr(1);
        if (parameters.length != 0) {
            var projectName = getParameterValue("name");
            type = getParameterValue("type");
            document.getElementById("formType").value = type.toUpperCase();
            loadViews(projectName);
            setContent(projectName);
            document.getElementById("projectName").disabled = true;
            if (type == "ApproveReject") {
                document.getElementById("titlePage").innerHTML = "Approve/reject build configuration";
                document.getElementById('save').value = 'Approve';
                document.getElementById('reject').style.visibility = 'visible';
            }
            if (type == "view") {
                document.getElementById('save').className = "confirm-button-hidden";
                document.getElementById('reject').className = "reject-button-none";
                document.getElementById('spanReject').className = "spanReject";
            }
        }
        else {
            document.getElementById("formType").value = "CREATE";
            projectNumber = 0;
        }
        buildConfiguration.loadCreateNewBuildConfiguration(function (t) {
            if (document.getElementById("formType").value == "CREATE") {
                document.getElementById("addProjectToBuild").click();
            }
        });

        buildConfiguration.deleteNotUploadFile(document.getElementById("files_hidden_script")
            .value.split(';'), function (t) {
        });
        document.getElementById("files_hidden_script").value = "";

        buildConfiguration.isCurrentUserAdministrator(function (t) {
            isAdmin = t.responseObject();
        });


    };


    function setContent(name) // used in initPage only
    {
        buildConfiguration.getConfiguration(name, function (t) {
            document.getElementById("projectName").value = t.responseObject().projectName;
            document.getElementById("typeSCM").value = t.responseObject().scm;
            document.getElementById("preScript").value = t.responseObject().preScript;
            document.getElementById("postScript").value = t.responseObject().postScript;
            setScriptTypeSelect(t.responseObject().scriptType);

            if (t.responseObject().rejectionReason != "")
                document.getElementById("reasonLabel").innerHTML = "Reason of rejection:  " + t.responseObject().rejectionReason;

            var bmcValue = t.responseObject().buildMachineConfiguration;
            if (document.getElementById("build_machine_configuration") != null) {
                document.getElementById("build_machine_configuration").value = "";
                var bmc = document.getElementsByName("node");
                for (var i = 0; i < bmc.length; i++) {
                    document.getElementById(bmc[i].id).checked = false;
                }
            }

            for (var i = 0; i < bmcValue.length; i++) {
                if (document.getElementById(bmcValue[i]) != null) {
                    document.getElementById(bmcValue[i]).checked = true;
                    addToHidden("build_machine_configuration", bmcValue[i]);
                }
            }

            var mail = t.responseObject().email;
            if (mail.length != 0) {
                document.getElementById('email').disabled = false
                document.getElementById('email').value = mail;
                document.getElementById('isEmail').checked = true;
            }
            document.getElementById("configEmail").value = t.responseObject().configEmail;

            var scripts = t.responseObject().scripts;
            for (var i = 0; i < scripts.length; i++) {
                addToSelectionBox("files_script", scripts[i])
            }

            if (t.responseObject().creator != null) {
                buildConfiguration.getFullNameCreator(t.responseObject().creator, function (t) {
                    document.getElementById("userLabel").innerHTML = "Created by:  " + t.responseObject();
                })
            }
        })
    }


    function getParameterValue(parameter) {
        var parameters = decodeURIComponent(document.location.search.substr(1));
        parameters = parameters.replace(/%20/g, " ");
        parameters = parameters.split('&');
        if (parameters.length == 0)
            return;
        var variabel = parameters[0];
        if (variabel.indexOf(parameter) != -1) {
            return variabel.split('=')[1];
        }
        variabel = parameters[1];
        if (variabel.indexOf(parameter) != -1) {
            return variabel.split('=')[1];
        }
    }

    function getElementNumber(id) {
        return id.substring(id.lastIndexOf('_') + 1);
    }

    function cleacSelectionGroup(groupId) {
        var selectElement = document.getElementById(groupId);
        for (var option in selectElement) {
            selectElement.remove(option);
        }
    }

    var addView = function () {
        document.getElementById("label-add-view").className = "label-add-view-hidden";
        buildConfiguration.getView(
            function (t) {
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;
                document.getElementById("projectsToBuild").appendChild(iDiv);
                var currentDivId = iDiv.firstElementChild.id;

                var scm = document.getElementById("typeSCM");
                var scmValue = scm.options[scm.selectedIndex].value;

                var defaultLocalPath;
                switch (scmValue) {
                    case 'Git':
                        defaultLocalPath = ".";
                        break;
                    case 'Subversion':
                        defaultLocalPath = "Development";
                        break;
                    default:
                        defaultLocalPath = "";
                        break;
                }
                // setting default local path
                defaultLocalPath = defaultLocalPath + ((projectNumber > 0) ? projectNumber : "");
                document.getElementById("localDirectoryPath_" + currentDivId).value = defaultLocalPath;
                projectNumber++;
                addBuilderOnDefault(currentDivId);


                // setting default credentials on view creating
                var cred_select = document.getElementById("credentials_" + currentDivId);
                var default_cred_value = document.getElementById("def_cred").value;
                for (var i = 0; i < cred_select.options.length; i++) {
                    if (cred_select.options[i].value === default_cred_value) {
                        cred_select.selectedIndex = i;
                        cred_select.options[i].selected = 'selected';
                        break;
                    }
                }


            });
    }

    function setScriptTypeSelect(scriptTypeSelected) {

        var script_select = document.getElementById("scriptTypeSelect");
        for (var i = 0; i < script_select.options.length; i++) {
            if (script_select.options[i].value === scriptTypeSelected) {
                script_select.selectedIndex = i;
                script_select.options[i].selected = 'selected';
                break;
            }
        }

    }

    var setCurrentCredentialsAsDefault = function (credentials_select_id) {
        var credentials_select = document.getElementById("credentials_" + credentials_select_id);
        var def_cred_value = credentials_select.options[credentials_select.selectedIndex].value;
        document.getElementById("def_cred").value = def_cred_value;
    }


    var loadViews = function (projectName) {
        buildConfiguration.loadViews(projectName,
            function (t) {
                if (t.responseObject().html.length == 0) {
                    return;
                }
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;
                document.getElementById("projectsToBuild").appendChild(iDiv);
                projectNumber = iDiv.childNodes.length;
            });
    }

    function addBuilderOnDefault(parentId) {
        buildConfiguration.getBuilderView(
            function (t) {
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;
                document.getElementById("builders_" + parentId).appendChild(iDiv);
            });
    }

    var addBuilder = function (button) {
        buildConfiguration.getBuilderView(
            function (t) {
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;

                var divs = button.parentNode.parentNode.id;
                document.getElementById("builders_" + divs).appendChild(iDiv);
            });
    }

    var selectionBoxIndexChange = function (selectionBox) {
        var selections = document.getElementsByTagName("select");
        for (var i = 0; i < selections.length; i++) {
            if (selections[i].id == selectionBox.id || selections[i].id.indexOf("files") == -1) {
                continue;
            }

            if (selections[i].selectedIndex != -1) {
                selections[i].selectedIndex = -1;
            }
        }
    }

    /*   function fieldsethidden(id)
     {
     var number = getElementNumber(id);
     var addfield = "files_" + number;
     var checkBox = document.getElementById("isVersionFiles_"+number);

     if (checkBox.checked)
     {
     var hiddenInput = "files_hidden_" + number;

     document.getElementById("div-fieldset_"+number).style.visibility = "visible";
     document.getElementById(addfield).style.visibility = "visible";
     document.getElementById("div-fieldset_"+number).style.height = 60;
     }

     if (!checkBox.checked)
     {
     var selectionGroupId = "files_" + number;
     document.getElementById("div-fieldset_"+number).style.visibility = "hidden";
     document.getElementById(addfield).style.visibility = "hidden";
     document.getElementById("div-fieldset_"+number).style.height = 0;
     }
     }*/

    var otherCheckBoxChange = function (checkBox) {
        var textboxId = "userConfig_" + getElementNumber(checkBox.id);
        textboxDisabled(checkBox, textboxId);
    }

    var emailCheckBoxChange = function (checkBox) {
        textboxDisabled(checkBox, "email");
    }

    function textboxDisabled(checkBox, textboxId) {
        if (!checkBox.checked) {
            document.getElementById("email_Error").className = "error-none";
            document.getElementById("email").className = "email-notification";
            document.getElementById(textboxId).disabled = true;
            document.getElementById(textboxId).value = "";
        }
        if (checkBox.checked) {
            document.getElementById(textboxId).disabled = false;
        }
    }

    /*    function uploadPrebuildScript()
     {
     var file = document.getElementById("scriptsButtonPrebuild").files[0];
     if (file.size > 1048576)
     {
     document.getElementById("scriptErrorPrebuildText").innerHTML = " You can't upload file which size is more than 1MB";
     document.getElementById("scriptErrorPrebuild").className = "error-block script-error";
     document.getElementById("scriptsButtonPrebuild").value = "";
     return;
     }
     if (!validateExtension(file.name))
     {
     document.getElementById("scriptErrorPrebuildText").innerHTML = " You can't upload file with this extension. Please choose file with '.bat, .nant, .powershell, .shell, .ant, .maven' extension only.";
     document.getElementById("scriptErrorPrebuild").className = "error-block script-error";
     document.getElementById("scriptsButtonPrebuild").value = "";
     return;
     }
     document.getElementById("scriptErrorPrebuild").className = "div-none";
     document.getElementById("scriptsButtonPrebuild").disabled = true;
     var formdata = new FormData();
     formdata.append("sampleFile", file);
     var xhr = new XMLHttpRequest();
     xhr.open("post", "uploadFile", true);
     xhr.send(formdata);
     xhr.onload = function(e)
     {
     if (this.status == 200)
     {
     addToSelectionBox("files_script_prebuild", this.responseText);
     }
     document.getElementById("scriptsButtonPrebuild").disabled = false;
     document.getElementById("scriptsButtonPrebuild").value = "";
     };
     }*/


    /*    function upload()
     {
     var file = document.getElementById("scriptsButton").files[0];
     if (file.size > 1048576)
     {
     document.getElementById("scriptErrorText").innerHTML = " You can't upload file which size is more than 1MB";
     document.getElementById("scriptError").className = "error-block script-error";
     document.getElementById("scriptsButton").value = "";
     return;
     }
     if (!validateExtension(file.name))
     {
     document.getElementById("scriptErrorText").innerHTML = " You can't upload file with this extension. Please choose file with '.bat, .nant, .powershell, .shell, .ant, .maven, .python' extension only.";
     document.getElementById("scriptError").className = "error-block script-error";
     document.getElementById("scriptsButton").value = "";
     return;
     }
     document.getElementById("scriptError").className = "div-none";
     document.getElementById("scriptsButton").disabled = true;
     var formdata = new FormData();
     formdata.append("sampleFile", file);
     var xhr = new XMLHttpRequest();
     xhr.open("post", "uploadFile", true);
     xhr.send(formdata);
     xhr.onload = function(e)
     {
     if (this.status == 200)
     {
     addToSelectionBox("files_script", this.responseText);
     }
     document.getElementById("scriptsButton").disabled = false;
     document.getElementById("scriptsButton").value = "";
     };
     }*/

    function addToSelectionBox(selectionBoxId, path) {
        var x = document.getElementById(selectionBoxId);
        if (x.length != 0) {
            if (x[0].value == "")
                x.remove(0);
        }

        var option = document.createElement("option");
        option.text = path;
        option.value = path;
        option.setAttribute("name", "artefacts");
        x.add(option);

        var hiddenInputId = "files_hidden_" + getElementNumber(selectionBoxId);
        addToHidden(hiddenInputId, path);
    }


    function addToHidden(hiddenInputId, value) {
        var hiddenValue = document.getElementById(hiddenInputId).value;
        if (hiddenValue.length > 0 && hiddenValue.lastIndexOf(";") != hiddenValue.length - 1) {
            document.getElementById(hiddenInputId).value += ";" + value + ";";
        }
        else {
            document.getElementById(hiddenInputId).value += value + ";";
        }
    }

    /*    function validateExtension(filename)
     {
     var ext = filename.substring(filename.lastIndexOf('.')+1);
     var extensions = new Array ("bat","nant","powershell","shell","ant","maven","python");
     for (var extension in extensions)
     {
     if (extensions[extension] == ext)
     {
     return true;
     }
     }
     return false;
     }*/

    var versionFileCheckBoxChange = function (checkBox) {
        var pathInput = "path_input_" + getElementNumber(checkBox.id);
        var addButton = "add_button_" + getElementNumber(checkBox.id);
        var addfield = "files_" + getElementNumber(checkBox.id);

        if (checkBox.checked) {
            var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
            document.getElementById(pathInput).style.visibility = "visible";
            document.getElementById(addButton).style.visibility = "visible";
            document.getElementById(pathInput).value = "";
            document.getElementById(hiddenInput).value = "";
            document.getElementById("div-fieldset_" + getElementNumber(checkBox.id)).style.visibility = "visible";
            document.getElementById(addfield).style.visibility = "visible";
            document.getElementById("div-fieldset_" + getElementNumber(checkBox.id)).style.height = 60;
        }

        if (!checkBox.checked) {
            var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
            document.getElementById(hiddenInput).value = "";
            var selectionGroupId = "files_" + getElementNumber(checkBox.id);
            cleacSelectionGroup(selectionGroupId);
            document.getElementById(pathInput).style.visibility = "hidden";
            document.getElementById(addButton).style.visibility = "hidden";
            document.getElementById("div-fieldset_" + getElementNumber(checkBox.id)).style.visibility = "hidden";
            document.getElementById(addfield).style.visibility = "hidden";
            document.getElementById("div-fieldset_" + getElementNumber(checkBox.id)).style.height = 0;
            document.getElementById("path_error_" + getElementNumber(checkBox.id)).className = "error-none";
            document.getElementById("path_input_" + getElementNumber(checkBox.id)).className = "textbox";
        }
    }

    var isValidForm = function () {
        var projectName = document.getElementById("projectName");
        var pathFolder = document.getElementsByName("localDirectoryPath");
        var pathUrl = document.getElementsByName("projectUrl");
        var pathArt = document.getElementsByName("pathToArtefacts");
        var pathVer = document.getElementsByName("versionFilesPath");
        var build = document.getElementsByName("projectToBuild");
        var patBuild = document.getElementsByName("fileToBuild");
        if (projectName.value == "") {
            document.getElementById("projectErrorText").innerHTML = " Please, enter your project name";
            document.getElementById("projectError").className = "error-block empty";
            projectName.focus();
            return false;
        }
        if (projectName.className == "textbox-error") {
            projectName.focus();
            return false;
        }
        if (build.length == 0) {
            document.getElementById("label-add-view").className = "label-add-view";
            return false;
        }
        if (document.getElementById("email_Error").className == "error-block") {
            document.getElementById("email").focus();
            return false;
        }
        if (document.getElementById("configEmail_Error").className == "error-block") {
            document.getElementById("configEmail").focus();
            return false;
        }

        validAllView();
        if (!checkingPath(pathUrl))
            return false;
        if (!checkingPath(pathFolder))
            return false;
        if (!checkingPath(pathArt))
            return false;
        if (!checkingPath(pathVer))
            return false;
        if (!checkingPath(patBuild))
            return false;
        if (type == "edit")
            return true;
        if (type == "ApproveReject") {
            document.getElementById("formType").value = "APPROVED";
            return true;
        }
        buildConfiguration.isNameFree(projectName.value, function (t) {
            if (t.responseObject() != false) {
                document.getElementById("save").onclick = null;
                setFormResultDialog("create");
                document.getElementById('save').click();
            }
            else {
                document.getElementById("fieldHelp").innerHTML = "Configuration with name '" + projectName.value + "' already exists. Please select another name.";
                document.getElementById("fieldHelp").className = "field-help-error";
            }
        });
        return false;
    }

    function checkingPath(path) {
        if (path.length == 0)
            return true;
        for (var i = 0; i < path.length; i++) {
            if (path[i].className == "textbox-error") {
                path[i].focus();
                return false;
            }
        }
        return true;
    }

    var rejectDiv = function () {
        document.getElementById("rejectDiv").className = "reject-div help-top";
        document.getElementById("overlay").className = (scroll != 0 ? 'overlay help-top' : 'overlay');
        document.getElementById("textReject").focus();
    }

    var rejectionSubmit = function ()
    {
        return true;
    }

    function deleteFromHidden(hidden, value) {
        hidden.value = hidden.value.replace(value, "");
        hidden.value = hidden.value.replace(";;", ";");
        if (hidden.value[0] == ";") {
            hidden.value = hidden.value.substr(1, hidden.value.length);
        }
        if (hidden.value.lastIndexOf(";") == hidden.value.length - 1) {
            hidden.value = hidden.value.substr(0, hidden.value.length - 1);
        }
    }

    var setFormResultDialog = function (result) {
        document.getElementById("formResultHidden").value = result;
    }

    var closeButtonClick = function (button) {
        var divId = button.id.replace('close_', '');
        var element = document.getElementById(divId);
        element.outerHTML = "";
        delete element;
    }

    var addPath = function (button) {
        var number = getElementNumber(button.id);
        var path = document.getElementById("path_input_" + number).value;
        var pathInput = document.getElementById("localDirectoryPath_" + (number - 1)).value;
        var error = document.getElementById("path_error_" + number).className;
        var errorInput = document.getElementById("path_error_" + (number - 1)).className;
        if ((pathInput.length <= 0) || (errorInput == "error-block") || (path.length <= 0) || (error == "error-block")) {
            return;
        }

        var selectionId = "files_" + number;

        if (!checkPathRepeat(path, selectionId)) {
            addToSelectionBox(selectionId, path);
            document.getElementById("path_input_" + number).value = "";
        }
        else {
            document.getElementById("path_error_" + number).className = "error-block";
            document.getElementById("coincide_" + number).innerHTML = " such pass already exists";
            return;
        }
    }

    var addPathFiles = function (button) {
        var number = getElementNumber(button.id);
        var path = document.getElementById("path_input_" + number).value;
        var error = document.getElementById("path_error_" + number).className;

        if ((path.length <= 0) || (error == "error-block")) {
            return;
        }

        var selectionId = "files_" + number;
        if (!checkPathRepeat(path, selectionId)) {
            addToSelectionBox(selectionId, path);
            document.getElementById("path_input_" + number).value = "";
        }
        else {
            document.getElementById("path_error_" + number).className = "error-block";
            document.getElementById("coincide_" + number).innerHTML = " path has already added";
            return;
        }
    }

    function checkPathRepeat(path, selectionBoxId) {
        var options = document.getElementById(selectionBoxId).options;
        if (options == null) {
            return false;
        }
        for (var i = 0; i < options.length; i++) {
            if (options[i].value == path) {
                return true;
            }
        }
        return false;
    }

    /* function trimSlash(value)
     {
     while (value[value.length-1] == "\\")
     {
     value = value.substr(0, value.length-1);
     }
     return value;
     }
     */
    var imageHelp = function (id) {
        var number = getElementNumber(id);
        if (document.getElementById("block_help_" + number).className == "help-view") {
            document.getElementById("block_help_" + number).className = "block-help-view";
            document.getElementById("text_help_" + number).className = "helptext-block";
        }
        else {
            document.getElementById("block_help_" + number).className = "help-view";
            document.getElementById("text_help_" + number).className = "helptext";
        }
    }

    var validateMail = function (mail) {
        var name = mail.name;
        var mailValue = mail.value;
        var mails = mailValue.split(' ');
        var cheking = true;
        for (var i = 0; i < mails.length; i++) {
            cheking = checkMail(mails[i]);
            if (!cheking)
                break;
        }
        if (cheking || mail.value == "") {
            document.getElementById(name + "_Error").className = "error-none";
            if (name == "email") {
                document.getElementById(name).className = "email-notification";
            }
            else {
                document.getElementById(name).className = "textbox";
            }
        }
        else {
            document.getElementById(name + "_Error").className = "error-block";
            if (name == "email") {
                document.getElementById(name).className = "email-notification-error";
            }
            else {
                document.getElementById(name).className = "textbox-error";
            }
        }
    }

    function checkMail(mailp) {
        var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
        if (pattern.test(mailp))
            return true;
        else
            return false;
    }

    var checkURL = function (id, add) {
        var url = document.getElementById(id).value;
        var number = getElementNumber(id);
        var regURL = /^(?:(?:https?|ftp|telnet):\/\/(?:[a-z0-9_-]{1,32}(?::[a-z0-9_-]{1,32})?@)?)?(?:(?:[a-z0-9-]{1,128}\.)+(?:com|net|org|mil|edu|arpa|ru|gov|biz|info|aero|inc|name|[a-z]{2})|(?!0)(?:(?!0[^.]|255)[0-9]{1,3}\.){3}(?!0|255)[0-9]{1,3})(?:\/[a-z0-9.,_@%&?+=\~\/-]*)?(?:#[^ \'\"&<>]*)?$/i;
        if (regURL.test(url)) {
            document.getElementById("url_error_" + number + add).className = "error-none";
            document.getElementById(id).className = "textbox";
        }
        else {
            document.getElementById("url_error_" + number + add).className = "error-block";
            document.getElementById(id).className = "textbox-error";
        }
    }

    var checkPath = function (id) {
        var path = document.getElementById(id).value;
        var number = getElementNumber(id);

        var regPath;
        if (document.getElementById(id).name == "versionFilesPath") {
            regPath = /^(?![?])(?:[^\\/:"*?<>|\r\n]+?(?:\/?|\/\*{0,2})*?|\/\*\.\*$)*?$/;// Allow Ant wildcards valid folder/file structure only
        }
        else if (document.getElementById(id).name == "localDirectoryPath") {																		// Change also correctArtifactPaths at JobManagerGenerator
            regPath = /(^\.[A-Za-z0-9]*$)|^(?:(?!\.)[^\\/:*?"<>|\r\n]+\/?)*$/;				// Match only one . or valid folder structure (zero-length - ok)
        }
        else {
            regPath = /^([a-zA-Z]:)?(\\[^<>:"/\\|?*]+)+\\?$/i;
        }

        if (regPath.test(path) || path == "") {
            document.getElementById("path_error_" + number).className = "error-none";
            document.getElementById(id).className = "textbox";
        }
        else {
            if (document.getElementById(id).name == "pathToArtefacts" || document.getElementById(id).name == "versionFilesPath") {
                document.getElementById("coincide_" + number).innerHTML = " Not correct path";
            }
            document.getElementById("path_error_" + number).className = "error-block";
            document.getElementById(id).className = "textbox-error";
        }
    }

    var checkPTB = function (id) {
        var path = document.getElementById(id).value;
        var number = getElementNumber(id);
        var regPath = /^([a-zA-Z]:\\)?[^\x00-\x1F"<>\|:\*\?/]+\.[a-zA-Z]{3,5}$/i;
        if (regPath.test(path)) {
            document.getElementById("ptb_error_" + number).className = "error-none";
            document.getElementById(id).className = "textbox";
        }
        else {
            document.getElementById("ptb_error_" + number).className = "error-block";
            document.getElementById(id).className = "textbox-error";
        }
    }

    var OkReject = function () {
        var reason = document.getElementById("textReject").value;
        if (reason != null) {
            if (reason.length == 0) {
                document.getElementById("textReject").focus();
                return false;
            }
            document.getElementById("rejectionReason").value = reason;
            document.getElementById("formType").value = "REJECT";
            document.getElementById("rejectSubmit").click();
        }
        return;
    }

    var CancelReject = function () {
        document.getElementById("rejectDiv").className = "div-none";
        document.getElementById("overlay").className = "div-none";
        document.getElementById("textReject").value = "";
    }

    var validateProject = function (project) {
        var regPath = /^[^\\\/\?\*\#\%\"\>\<\:\|]*$/i;
        var cl = document.getElementById("projectError").className;
        var classes = cl.split(" ");
        if ((classes.length == 2) && (project.value.length == 0))
            return;
        if (regPath.test(project.value) || (project.value.length == 0)) {
            document.getElementById("projectError").className = "error-none";
            document.getElementById(project.id).className = "textbox";
            document.getElementById("projectErrorText").innerHTML = "";
            document.getElementById("fieldHelp").className = "";
        }
        else {
            document.getElementById("projectError").className = "error-block";
            document.getElementById("projectErrorText").innerHTML = " Not correct name";
            document.getElementById(project.id).className = "textbox-error";
        }
    }

    var bMCChange = function (checkBox) {
        if (checkBox.checked) {
            addToHidden("build_machine_configuration", checkBox.id);
        }
        if (!checkBox.checked) {
            deleteFromHidden(document.getElementById("build_machine_configuration"), checkBox.id);
        }
    }

    /*    function checkName(id)
     {
     var path = document.getElementById(id).value;
     var number = getElementNumber(id);
     var regPath = /[/ ? " : < >]$/i;

     if(!regPath.test(path) || path.length == 0)
     {
     document.getElementById("name_error_"+number).className = "error-none";
     document.getElementById(id).className = "textbox";
     }
     else
     {
     document.getElementById("name_error_"+number).className = "error-block";
     document.getElementById(id).className= "textbox-error";
     }
     }*/

    function validAllView() {
        var view = document.getElementsByClassName("div-add-project-to-build-view");
        var textboxes;
        for (var i = 0; i < view.length; i++) {
            textboxes = view[i].getElementsByClassName("textbox");
            for (var j = 0; j < textboxes.length; j++) {
                if ((textboxes[j].name != "credentials") && (textboxes[j].name != "branchName") &&
                    (textboxes[j].name != "preScript") && (textboxes[j].name != "postScript")) {
                    textboxes[j].onblur();
                }

            }
        }
    }


    return {
        initPage: initPage,
        addView: addView,
        setCurrentCredentialsAsDefault: setCurrentCredentialsAsDefault,
        loadViews: loadViews,
        addBuilder: addBuilder,
        selectionBoxIndexChange: selectionBoxIndexChange,
        otherCheckBoxChange: otherCheckBoxChange,
        emailCheckBoxChange: emailCheckBoxChange,
        versionFileCheckBoxChange: versionFileCheckBoxChange,
        isValidForm: isValidForm,
        rejectDiv: rejectDiv,
        rejectionSubmit: rejectionSubmit,
        setFormResultDialog: setFormResultDialog,
        closeButtonClick: closeButtonClick,
        addPath: addPath,
        addPathFiles: addPathFiles,
        imageHelp: imageHelp,
        validateMail: validateMail,
        checkURL: checkURL,
        checkPath: checkPath,
        checkPTB: checkPTB,
        OkReject: OkReject,
        CancelReject: CancelReject,
        validateProject: validateProject,
        bMCChange: bMCChange

    };
})(); //END OF CONFIGURATOR MODULE
window.onload = function () {
    configurator.initPage();
}


document.addEventListener('keyup', function (e) {
    if (e.keyCode == 46) {
        if (document.activeElement.tagName != "SELECT") {
            return;
        }
        var selectionGroups = document.getElementsByTagName("select");
        var selectionValue;
        for (var i = 0; i < selectionGroups.length; i++) {
            if (selectionGroups[i].id.indexOf("files") == -1) {
                continue;
            }
            if (selectionGroups[i].selectedIndex != -1) {
                if (selectionGroups[i].id == "files_script") {
                    selectionValue = selectionGroups[i][selectionGroups[i].selectedIndex].value;
                    buildConfiguration.deleteNotUploadFile(selectionValue, function (t) {
                    });
                }
                var hiddenInput = document.getElementById("files_hidden_" + getElementNumber(selectionGroups[i].id));
                selectionValue = selectionGroups[i][selectionGroups[i].selectedIndex].value;
                deleteFromHidden(hiddenInput, selectionValue);

                selectionGroups[i].remove(selectionGroups[i].selectedIndex);
            }
        }
    }
}, false);




