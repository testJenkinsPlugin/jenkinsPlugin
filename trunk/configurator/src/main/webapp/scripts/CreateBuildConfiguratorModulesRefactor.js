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
            jQuery("#formType").val(type.toUpperCase());
            loadViews(projectName);
            setContent(projectName);
            jQuery("#projectName").prop('disabled', true);

            if (type == "ApproveReject") {
                jQuery("#titlePage").html("Approve/reject build configuration");
                jQuery('#save').val("Approve");
                jQuery('#reject').show();
            }
            if (type == "view") {
                jQuery('#save').hide();
                jQuery('#spanReject').show();
            }
        }
        else {
            jQuery("#formType").val('CREATE');
            projectNumber = 0;
        }
        buildConfiguration.loadCreateNewBuildConfiguration(function (t) {
            if (jQuery("#formType").val() == "CREATE") {
                addView();
            }
        });

        buildConfiguration.isCurrentUserAdministrator(function (t) {
            isAdmin = t.responseObject();
        });

        jQuery("#cancelButton").click(function () {
            location.href = '../BuildConfigurator';
        })
    };


    function setContent(name) // used in initPage only
    {
        buildConfiguration.getConfiguration(name, function (t) {
            jQuery("#projectName").val(t.responseObject().projectName);
            jQuery("#typeSCM").val(t.responseObject().scm);
            jQuery("#preScript").val(t.responseObject().preScript);
            jQuery("#postScript").val(t.responseObject().postScript);
            setScriptTypeSelect(t.responseObject().scriptType);

            if (t.responseObject().rejectionReason != "")
                jQuery("#reasonLabel").html("Reason of rejection:  " + t.responseObject().rejectionReason);

            var bmcValue = t.responseObject().buildMachineConfiguration;
            if (jQuery("#build_machine_configuration") != null) {
                jQuery("#build_machine_configuration").val("");
                var bmc = document.getElementsByName("node");
                for (var i = 0; i < bmc.length; i++) {
                    jQuery("#" + bmc[i].id).prop('checked', false);
                }
            }

            for (var i = 0; i < bmcValue.length; i++) {
                if (jQuery(bmcValue[i]) != null) {
                    document.getElementById(bmcValue[i]).checked = true;
                    addToHidden("build_machine_configuration", bmcValue[i]);
                }
            }

            var mail = t.responseObject().email;
            if (mail.length != 0) {
                jQuery("#email").prop('disabled', false);
                jQuery('#email').val(mail);
                jQuery('#isEmail').prop('checked', true);
            }
            jQuery("#configEmail").val(t.responseObject().configEmail);

           /* var scripts = t.responseObject().scripts;
            for (var i = 0; i < scripts.length; i++) {
                addToSelectionBox("files_script", scripts[i])
            }
*/
            if (t.responseObject().creator != null) {
                buildConfiguration.getFullNameCreator(t.responseObject().creator, function (t) {
                    jQuery("#userLabel").html("Created by:  " + t.responseObject());
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
        var variable = parameters[0];
        if (variable.indexOf(parameter) != -1) {
            return variable.split('=')[1];
        }
        variable = parameters[1];
        if (variable.indexOf(parameter) != -1) {
            return variable.split('=')[1];
        }
    }

    var  getElementNumber = function (id) {
        return id.substring(id.lastIndexOf('_') + 1);
    }

    var getProjectId = function (element) {
        return jQuery(element).closest("div[name=projectToBuild]").attr("id");
    }



    function cleacSelectionGroup(groupId) {
        var selectElement = jQuery("#" + groupId)[0];
        for (var option in selectElement) {
            selectElement.remove(option);
        }
    }

    var addView = function () {
        jQuery("#label-add-view").hide();
        buildConfiguration.getView(
            function (t) {
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;
                jQuery("#projectsToBuild").append(iDiv);
                var currentDivId = iDiv.firstElementChild.id;

                var scm = jQuery("#typeSCM")[0];
                // var scm = document.getElementById("typeSCM");
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

                setLocalFolderPath(defaultLocalPath, currentDivId);
                projectNumber++;
                // addBuilderOnDefault(currentDivId);
                addBuilder(currentDivId);
                setDefaultCredentials(currentDivId)
            });
    }
    function setDefaultCredentials(currentDivId){
        var cred_select = jQuery("#credentials_" + currentDivId)[0];
        var default_cred_value = jQuery("#def_cred").val();
        for (var i = 0; i < cred_select.options.length; i++) {
            if (cred_select.options[i].value === default_cred_value) {
                cred_select.selectedIndex = i;
                cred_select.options[i].selected = 'selected';
                break;
            }
        }

    }

    function setLocalFolderPath(defaultLocalPath, currentDivId) {
        var projectSelector = "div[name=projectToBuild][id=" + currentDivId + "]";
        defaultLocalPath = defaultLocalPath + ((projectNumber > 0) ? projectNumber : "");
        jQuery(projectSelector).find("[name=localDirectoryPath]").val(defaultLocalPath);
    }

    function setScriptTypeSelect(scriptTypeSelected) {

        var script_select = jQuery("#scriptTypeSelect")[0];
        for (var i = 0; i < script_select.options.length; i++) {
            if (script_select.options[i].value === scriptTypeSelected) {
                script_select.selectedIndex = i;
                script_select.options[i].selected = 'selected';
                break;
            }
        }

    }

    var setCurrentCredentialsAsDefault = function (element) {
        var projectId = getProjectId(element);
        var projectSelector = "div[name=projectToBuild][id=" + projectId + "]"
        var credentials_select = jQuery(projectSelector).find("[name=credentials]")[0];
        var def_cred_value = credentials_select.options[credentials_select.selectedIndex].value;
        jQuery("#def_cred").val(def_cred_value);
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

    var addBuilder = function (projectId) {
        buildConfiguration.getBuilderView(
            function (t) {
                var projectSelector = "div[name=projectToBuild][id=" + projectId + "]"
                var iDiv = document.createElement("div");
                iDiv.innerHTML = t.responseObject().html;
                jQuery(projectSelector).find("[class=builders]").append(iDiv);
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

    var otherCheckBoxChange = function (checkBox) {
        var textboxId = "userConfig_" + getElementNumber(checkBox.id);
        textboxDisabled(checkBox, textboxId);
    }

    var emailCheckBoxChange = function (checkBox) {
        textboxDisabled(checkBox, "email");
    }

    function textboxDisabled(checkBox, textboxId) {
        if (!checkBox.checked) {
            // document.getElementById("email_Error").className = "error-block none";
            jQuery("#email_Error").attr('class', 'error-block none');

            jQuery("#email").attr('class', "email-notification");
            jQuery("#" + textboxId).prop("disabled", true);
            jQuery("#" + textboxId).val("");
        }
        if (checkBox.checked) {
            jQuery("#" + textboxId).prop("disabled", false);

        }
    }

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
            var inputValue = jQuery("#" + hiddenInputId).val();
            jQuery("#" + hiddenInputId).val(inputValue + ";" + value + ";");
        }
        else {
            var inputValue = jQuery("#" + hiddenInputId).val();
            jQuery("#" + hiddenInputId).val(inputValue + value + ";");
        }
    }

    var versionFileCheckBoxChange = function (checkBox) {
        var pathInput = "path_input_" + getElementNumber(checkBox.id);
        var addButton = "add_button_" + getElementNumber(checkBox.id);
        var addfield = "files_" + getElementNumber(checkBox.id);

        if (checkBox.checked) {
            var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
            jQuery("#" + pathInput).attr("class", "textbox");;
            jQuery("#" + pathInput).val("");
            jQuery("#" + hiddenInput).val("");
            jQuery("#" + addButton).css("visibility", "visible");
            jQuery("#" + addfield).css("visibility", "visible");
            jQuery("#div-fieldset_" + getElementNumber(checkBox.id)).show();
        }

        if (!checkBox.checked) {
            var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
            document.getElementById(hiddenInput).value = "";
            var selectionGroupId = "files_" + getElementNumber(checkBox.id);
            cleacSelectionGroup(selectionGroupId);
            jQuery("#path_input_" + getElementNumber(checkBox.id)).attr("class", "textbox hidden");
            jQuery("#" + addButton).css("visibility", "hidden");
            jQuery("#" + addfield).css("visibility", "hidden");
            jQuery("#div-fieldset_" + getElementNumber(checkBox.id)).hide();
        }
    }

    var isValidForm = function () {
        var projectName = jQuery("#projectName")[0];
        var pathFolder = document.getElementsByName("localDirectoryPath");
        var pathUrl = document.getElementsByName("projectUrl");
        var pathArt = document.getElementsByName("pathToArtefacts");
        var pathVer = document.getElementsByName("versionFilesPath");
        var build = document.getElementsByName("projectToBuild");
        var patBuild = document.getElementsByName("fileToBuild");
        if (projectName.value == "") {
            jQuery("#projectErrorText").html(" Please, enter your project name");
            jQuery("#projectError").attr("class", "error-block empty");
            projectName.focus();
            return false;
        }
        if (projectName.className == "textbox wrong") {
            projectName.focus();
            return false;
        }
        if (build.length == 0) {
            jQuery("#label-add-view").show();
            return false;
        }
        if (jQuery("#email_Error").attr("class") == "error-block") {
            jQuery("#email").focus();
            return false;
        }
        if (jQuery("#configEmail_Error").attr("class") == "error-block") {
            jQuery("#configEmail").focus();
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
            jQuery("#formType").val("APPROVED");
            return true;
        }
        buildConfiguration.isNameFree(projectName.value, function (t) {
            if (t.responseObject() != false) {
                document.getElementById("save").onclick = null;
                setFormResultDialog("create");
                document.getElementById('save').click();
            }
            else {
                jQuery("#fieldHelp").html("Configuration with name '" + projectName.value + "' already exists. Please select another name.");
                jQuery("#fieldHelp").attr("class", "field-help-error");
            }
        });
        return false;
    }

    function checkingPath(path) {
        if (path.length == 0)
            return true;
        for (var i = 0; i < path.length; i++) {
            if (path[i].className == "textbox wrong") {
                path[i].focus();
                return false;
            }
        }
        return true;
    }

    var rejectDiv = function () {
        jQuery("#rejectDiv").attr("class", "reject-div help-top");
        jQuery("#overlay").attr("class" ,(scroll != 0 ? 'overlay help-top' : 'overlay'));
        jQuery("#textReject").focus();
    }

    var rejectionSubmit = function ()
    {
        return true;
    }

   var deleteFromHidden  = function (hidden, value) {
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
        jQuery("#formResultHidden").val(result);
    }

    var closeButtonClick = function (button) {
        var divId = button.id.replace('close_', '');
        var element = jQuery("#" + divId)[0];
        element.outerHTML = "";
        delete element;
    }

    var addPath = function (button) {
        var number = getElementNumber(button.id);
        var path = jQuery("#path_input_" + number).val();
        var error = jQuery("#path_error_" + number).attr("class");
        var errorInput = jQuery("#path_error_" + (number - 1)).attr("class");
        if ((errorInput == "error-block") || (path.length <= 0) || (error == "error-block")) {
            return;
        }

        var selectionId = "files_" + number;

        if (!checkPathRepeat(path, selectionId)) {
            addToSelectionBox(selectionId, path);
            jQuery("#path_input_" + number).val("");
        }
        else {
            jQuery("#path_error_" + number).attr("class", "error-block");
            jQuery("#coincide_" + number).html(" such pass already exists");
            return;
        }
    }

    var addPathFiles = function (button) {
        var number = getElementNumber(button.id);
        var path = jQuery("#path_input_" + number).val();
        var error = jQuery("#path_error_" + number).attr("class");

        if ((path.length <= 0) || (error == "error-block")) {
            return;
        }

        var selectionId = "files_" + number;
        if (!checkPathRepeat(path, selectionId)) {
            addToSelectionBox(selectionId, path);
            jQuery("#path_input_" + number).val("");
        }
        else {
            jQuery("#path_error_" + number).attr("class", "error-block");
            jQuery("#coincide_" + number).html(" path has already added");
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

    var imageHelp = function (id) {
        var number = getElementNumber(id);
        if (jQuery("#block_help_" + number).attr("class") == "help-view") {
            jQuery("#block_help_" + number).attr("class", "block-help-view");
            // jQuery("#text_help_" + number).attr("class", "helptext-block");
            jQuery("#text_help_" + number).css("display", "block");
        }
        else {
            jQuery("#block_help_" + number).attr("class","help-view");
            jQuery("#text_help_" + number).attr("class", "helptext");
        }
    }

    var validateMail = function (mail) {
        var name = mail.name;
        var mailValue = mail.value;
        var mails = mailValue.split(' ');
        var checking = true;
        for (var i = 0; i < mails.length; i++) {
            checking = checkMail(mails[i]);
            if (!checking)
                break;
        }
        if (checking || mail.value == "") {
            jQuery("#" + name + "_Error").attr("class", "error-block none");
            if (name == "email") {
                jQuery("#"+name).attr("class","email-notification");
            }
            else {
                jQuery("#" + name).attr("class", "textbox");
            }
        }
        else {
            jQuery("#" + name + "_Error").attr("class", "error-block");
            if (name == "email") {
                jQuery("#" + name).attr("class", "email-notification wrong");
            }
            else {
                jQuery("#" + name).attr("class", "textbox wrong");
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

    var checkURL = function (element) {
        var regURL = /(((git|ssh|http(s)?)|(git@[\w\.]+))(:\/?)([\w\.\@:\/\-\~]+)(\.git)?)$/;
        var projectId = getProjectId(element);
        var projectSelector = "div[name=projectToBuild][id=" + projectId + "]"
        var url = jQuery(projectSelector).find("[name=projectUrl]").val();
        if (regURL.test(url)) {
            jQuery(projectSelector).find("[name=projectUrl-block]").attr("class", "error-block none");
            jQuery(projectSelector).find("[name=projectUrl]").attr("class", "textbox");
        }
        else {
            jQuery(projectSelector).find("[name=projectUrl-block]").attr("class", "error-block");
            jQuery(projectSelector).find("[name=projectUrl]").attr("class", "textbox wrong");
        }
    }


    var checkPTB = function (element) {
        var regPath = /^([a-zA-Z]:\\)?[^\x00-\x1F"<>\|:\*\?/]+\.[a-zA-Z]{3,5}$/i;
        var projectId = getProjectId(element);
        var projectSelector = "div[name=projectToBuild][id=" + projectId + "]"
        var path = jQuery(projectSelector).find("[name=fileToBuild]").val();
        if (regPath.test(path)) {
            jQuery(projectSelector).find("[name=fileToBuild-block]").attr("class", "error-block none");
            jQuery(projectSelector).find("[name=fileToBuild]").attr("class", "textbox");
        }
        else {
            jQuery(projectSelector).find("[name=fileToBuild-block]").attr("class", "error-block");
            jQuery(projectSelector).find("[name=fileToBuild]").attr("class", "textbox wrong");
        }
    }



    var checkPath = function (id) {
        var path = jQuery("#" + id).val();
        var number = getElementNumber(id);

        var regPath;
        if ((document.getElementById(id).name == "pathToArtefacts") ||
            (document.getElementById(id).name == "versionFilesPath")) {
            regPath = /^(?![*?])(?:[^\\/:"*?<>|\r\n]+?(?:\/?|\/\*{0,2})*?|\/\*\.\*$)*?$/;// Allow Ant wildcards valid folder/file structure only
        }
        else if (document.getElementById(id).name == "localDirectoryPath") {																		// Change also correctArtifactPaths at JobManagerGenerator
            regPath = /(^\.[A-Za-z0-9]*$)|^(?:(?!\.)[^\\/:*?"<>|\r\n]+\/?)*$/;				// Match only one . or valid folder structure (zero-length - ok)
        }
        else {
            regPath = /^([a-zA-Z]:)?(\\[^<>:"/\\|?*]+)+\\?$/i;
        }

        if (regPath.test(path) || path == "") {
            jQuery("#path_error_" + number).attr("class", "error-block none");
            jQuery("#" + id).attr("class", "textbox");
        }
        else {
            if (document.getElementById(id).name == "pathToArtefacts" || document.getElementById(id).name == "versionFilesPath") {
                jQuery("#coincide_" + number).html(" Not correct path");
            }
            jQuery("#path_error_" + number).attr("class","error-block");
            jQuery("#" + id).attr("class", "textbox wrong");
        }
    }



    var OkReject = function () {
        var reason = jQuery("#textReject").val();
        if (reason != null) {
            if (reason.length == 0) {
                jQuery("#textReject").focus();
                return false;
            }
            jQuery("#rejectionReason").val(reason);
            jQuery("#formType").val("REJECT");
            document.getElementById("rejectSubmit").click();
            rejectionSubmit();
        }
        return;
    }

    var CancelReject = function () {
        jQuery("#rejectDiv").attr("class","div-none");
        jQuery("#overlay").attr("class", "div-none");
        jQuery("#textReject").val("");
    }

    var validateProject = function (project) {
        var regPath = /^[^\\\/\?\*\#\%\"\>\<\:\|]*$/i;
        var cl = jQuery("#projectError").attr("class");
        var classes = cl.split(" ");
        if ((classes.length == 2) && (project.value.length == 0))
            return;
        if (regPath.test(project.value) || (project.value.length == 0)) {
            jQuery("#projectError").attr("class", "error-block none");
            jQuery("#" + project.id).attr("class", "textbox");
            jQuery("#projectErrorText").html("");
        }
        else {
            jQuery("#projectError").attr("class", "error-block");
            jQuery("#projectErrorText").html(" Not correct name");
            jQuery("#" + project.id).attr("class", "textbox wrong");

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


    function validAllView() {
        // var view = jQuery(".project-container");
        var view = jQuery("div[name=projectToBuild]");

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
        bMCChange: bMCChange,
        getElementNumber:getElementNumber,
        deleteFromHidden:deleteFromHidden

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
                var hiddenInput = jQuery("#files_hidden_" + configurator.getElementNumber(selectionGroups[i].id))[0];
                selectionValue = selectionGroups[i][selectionGroups[i].selectedIndex].value;
                configurator.deleteFromHidden(hiddenInput, selectionValue);

                selectionGroups[i].remove(selectionGroups[i].selectedIndex);
            }
        }
    }
}, false);





