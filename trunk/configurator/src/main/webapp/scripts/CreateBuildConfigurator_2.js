var $$ =  (function()
{
var type;
var scroll=0;
var isCommited;
var isAdmin;
var projectNumber=0;

	var myFunction = function()
	{

	},
	loadViews = function(projectName)
	{
	    buildConfiguration.loadViews(projectName,
	    function(t)
	    {
	        if (t.responseObject().html.length > 0)
	        {
		        var iDiv = document.createElement("div");
		        iDiv.innerHTML = t.responseObject().html;
		        $("#projectsToBuild").appendChild(iDiv);
		        projectNumber = iDiv.childNodes.length;
	    	}
	    });
	},
	setContent = function(name)
	{
	        buildConfiguration.getConfiguration(name, function(t){
	        $("#projectName").value = t.responseObject().projectName;
	        $("#typeSCM").value = t.responseObject().scm;
	        $("#preScript").value = t.responseObject().preScript;
	        $("#postScript").value = t.responseObject().postScript;

	        if(t.responseObject().rejectionReason)
	            $("#reasonLabel").innerHTML = "Reason of rejection:  "+t.responseObject().rejectionReason;

	        var bmcValue = t.responseObject().buildMachineConfiguration;
	        if ($("#build_machine_configuration"))
	        {
	            $("#build_machine_configuration").value = "";
	            var bmc = $("#node");
	            for (var i=0; i<bmc.length; i++)
	            {
	                $('#'+bmc[i].id).checked = false;
	            }
	        }

	        for (var i=0; i<bmcValue.length; i++)
	        {
	            if ($('#'+bmcValue[i]) != null)
	            {
	                $('#'+bmcValue[i]).checked = true;
	                addToHidden("build_machine_configuration", bmcValue[i]);
	            }
	        }

	        var mail = t.responseObject().email;
	        if (mail.length != 0)
	        {
	            $('email').disabled = false
	            $('email').value = mail;
	            $('isEmail').checked = true;
	        }
	        $("#configEmail").value = t.responseObject().configEmail;

	        var scripts = t.responseObject().scripts;
	        for (var i=0; i<scripts.length; i++)
	        {
	            addToSelectionBox("files_script", scripts[i])
	        }
	        
	        if(t.responseObject().creator != null)
	        {
	            buildConfiguration.getFullNameCreator(t.responseObject().creator, function(t){
	            $("#userLabel").innerHTML = "Created by:  "+t.responseObject();
	            })
	        }
	    })
	},
	addToHidden = function(hiddenInputId, value)
	{
	    var hiddenValue = $('#'+hiddenInputId).value;
	    if (!hiddenValue.split(';').contains(value))
	    {
	        $('#'+hiddenInputId).value += value + ";";
	    }
	}
	init = function()
	{
		var parameters = document.location.search.substr(1);
	    if (parameters.length > 0)
	    {
	        var projectName = getParameterValue("name");
	        type = getParameterValue("type");
	        $("#formType").value = type.toUpperCase();
	        loadViews(projectName);
	        setContent(projectName);
	        $("#projectName").disabled = true;
	        if (type == "ApproveReject")
	        {
	            $("#titlePage").innerHTML = "Approve/reject build configuration";
	            $("#save").value='Approve';
	            $("#reject").hide();
	        }
	        if(type == "view")
	        {
	            $("#save").hide();
	            $("#reject").hide();
	        }
	    }
	    else
	    {
	        $("#formType").value = "CREATE";
	        projectNumber = 0;
	    }

	    buildConfiguration.loadCreateNewBuildConfiguration(function(t)
	    {
	        if ($("#formType").value == "CREATE")
	        {
	            addView();
	        }
	    });

	    buildConfiguration.deleteNotUploadFile(
	    	$("#files_hidden_script").value.split(';'), 
	    	function(t) {}
	    );

	    $("#files_hidden_script").value = "";

	    buildConfiguration.isCommited(function(t){
	        isCommited = t.responseObject();
	    });

	    buildConfiguration.isCurrentUserAdministrator(function(t){
	        isAdmin = t.responseObject();
	    });
	},

	myFunction2 = function()
	{

	};

	return
	{
		init: inti,
		myFunction: myFunction,
		myFunction2: myFunction2

	}

})();

window.onload = function()
{
    $$.Init();
}

window.onbeforeunload = function (e) {
    if (document.activeElement.href.indexOf("BuildConfigurator") == -1 && !isCommited && isAdmin)
    {
        return 'Are you sure you want leave this page when configurations it not synchronized with your svn repository?';
    }
}



document.addEventListener('keyup', function (e)
{
    if(e.keyCode == 46)
    {
        if (document.activeElement.tagName != "SELECT")
        {
            return;
        }
        var selectionGroups = document.getElementsByTagName("select");
        var selectionValue;
        for (var i=0; i<selectionGroups.length; i++)
        {
            if (selectionGroups[i].id.indexOf("files") == -1)
            {
                continue;
            }
            if (selectionGroups[i].selectedIndex != -1)
            {
                if (selectionGroups[i].id == "files_script")
                {
                    selectionValue = selectionGroups[i][selectionGroups[i].selectedIndex].value;
                    buildConfiguration.deleteNotUploadFile(selectionValue, function(t) {});
                }
                var hiddenInput = $("#files_hidden_" + getElementNumber(selectionGroups[i].id));
                selectionValue = selectionGroups[i][selectionGroups[i].selectedIndex].value;
                deleteFromHidden(hiddenInput, selectionValue);

                selectionGroups[i].remove(selectionGroups[i].selectedIndex);
            }
        }
    }
}, false);

function getParameterValue(parameter)
{
    var parameters = decodeURIComponent(document.location.search.substr(1));
    parameters = parameters.replace(/%20/g, " ");
    parameters = parameters.split('&');
    if (parameters.length == 0)
        return;
    var variabel = parameters[0];
    if (variabel.indexOf(parameter) != -1)
    {
        return variabel.split('=')[1];
    }
    variabel = parameters[1];
    if (variabel.indexOf(parameter) != -1)
    {
        return variabel.split('=')[1];
    }
}

function getElementNumber(id)
{
    return id.substring(id.lastIndexOf('_')+1);
}

function cleacSelectionGroup(groupId)
{
    var selectElement = document.getElementById(groupId);
    for (var option in selectElement)
    {
        selectElement.remove(option);
    }
}

function addView()
{
    $("#label-add-view").className = "label-add-view-hidden";
    buildConfiguration.getView(
    function(t)
    {
        var iDiv = document.createElement("div");
        iDiv.innerHTML = t.responseObject().html;
        $("#projectsToBuild").appendChild(iDiv);
        var currentDivId = iDiv.firstElementChild.id;
        $("#localDirectoryPath_" + currentDivId).value = 
        	"Development" + ((projectNumber > 0) ? projectNumber : "");
        projectNumber++;
    });
}



function addBuilder(button)
{
    buildConfiguration.getBuilderView(
    function(t)
    {
        var iDiv = document.createElement("div");
        iDiv.innerHTML = t.responseObject().html;

        var divs = button.parentNode.parentNode.id;
        $("#builders_" + divs).appendChild(iDiv);
    });
}

function selectionBoxIndexChange(selectionBox)
{
    var selections = document.getElementsByTagName("select");
    for (var i=0; i<selections.length; i++)
    {
        if (selections[i].id == selectionBox.id || selections[i].id.indexOf("files") == -1)
        {
            continue;
        }
        
        if (selections[i].selectedIndex != -1)
        {
            selections[i].selectedIndex  = -1;
        }
    }
}

function fieldsethidden(id)
{
    var number = getElementNumber(id);
    var addfield = "files_" + number;
    var checkBox = $("#isVersionFiles_"+number);
    
    if (checkBox.checked)
    {
        $("#div-fieldset_"+number).removeClass('hidden');
        document.getElementById(addfield).removeClass('hidden');
    } 
    else
    {
        $("#div-fieldset_"+number).addClass('hidden');
        document.getElementById(addfield).addClass('hidden');
    } 
}

function otherCheckBoxChange(checkBox)
{
    var textboxId = "userConfig_" + getElementNumber(checkBox.id);
    textboxDisabled(checkBox,textboxId);
}

function emailCheckBoxChange(checkBox)
{
    textboxDisabled(checkBox,"email");
}

function textboxDisabled(checkBox, textboxId)
{
    if (!checkBox.checked)
    {
        $("#email_Error").className = "error-none";
        $("#email").className = "email-notification";
        $('#'+textboxId).disabled = true;
        $('#'+textboxId).value = "";
    }
    if (checkBox.checked)
    {
        $('#'+textboxId).disabled = false;
    }
}

function uploadPrebuildScript()
{
    var file = $("#scriptsButtonPrebuild").files[0];
    if (file.size > 1048576)
    {
        $("#scriptErrorPrebuildText").innerHTML = " You can't upload file which size is more than 1MB";
        $("#scriptErrorPrebuild").className = "error-block script-error";
        $("#scriptsButtonPrebuild").value = "";
        return;
    }
    if (!validateExtension(file.name))
    {
        $("#scriptErrorPrebuildText").innerHTML = " You can't upload file with this extension. Please choose file with '.bat, .nant, .powershell, .shell, .ant, .maven' extension only.";
        $("#scriptErrorPrebuild").className = "error-block script-error";
        $("#scriptsButtonPrebuild").value = "";
        return;
    }
    $("#scriptErrorPrebuild").className = "div-none";
    $("#scriptsButtonPrebuild").disabled = true;
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
        $("#scriptsButtonPrebuild").disabled = false;
        $("#scriptsButtonPrebuild").value = "";
    };
}



function upload()
{
    var file = $("#scriptsButton").files[0];
    if (file.size > 1048576)
    {
        $("#scriptErrorText").innerHTML = " You can't upload file which size is more than 1MB";
        $("#scriptError").className = "error-block script-error";
        $("#scriptsButton").value = "";
        return;
    }
    if (!validateExtension(file.name))
    {
        $("#scriptErrorText").innerHTML = " You can't upload file with this extension. Please choose file with '.bat, .nant, .powershell, .shell, .ant, .maven, .python' extension only.";
        $("#scriptError").className = "error-block script-error";
        $("#scriptsButton").value = "";
        return;
    }
    $("#scriptError").className = "div-none";
    $("#scriptsButton").disabled = true;
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
        $("#scriptsButton").disabled = false;
        $("#scriptsButton").value = "";
    };
}

function addToSelectionBox(selectionBoxId, path)
{
    var x = document.getElementById(selectionBoxId);
    if (x.length!=0)
    {
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


function validateExtension(filename)
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
}

function versionFileCheckBoxChange(checkBox)
{
    var pathInput = "path_input_" + getElementNumber(checkBox.id);
    var addButton = "add_button_" + getElementNumber(checkBox.id);
    var addfield = "files_" + getElementNumber(checkBox.id);

    if (checkBox.checked)
    {
        var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
        document.getElementById(pathInput).style.visibility = "visible";
        document.getElementById(addButton).style.visibility = "visible";
        document.getElementById(pathInput).value = "";
        document.getElementById(hiddenInput).value = "";
        $("#div-fieldset_"+getElementNumber(checkBox.id)).style.visibility = "visible";
        document.getElementById(addfield).style.visibility = "visible";
        $("#div-fieldset_"+getElementNumber(checkBox.id)).style.height = 60;   
    }
    
    if (!checkBox.checked)
    {
        var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
        document.getElementById(hiddenInput).value = "";
        var selectionGroupId = "files_" + getElementNumber(checkBox.id);
        cleacSelectionGroup(selectionGroupId);
        document.getElementById(pathInput).style.visibility = "hidden";
        document.getElementById(addButton).style.visibility = "hidden";
        $("#div-fieldset_"+getElementNumber(checkBox.id)).style.visibility = "hidden";
        document.getElementById(addfield).style.visibility = "hidden";
        $("#div-fieldset_"+getElementNumber(checkBox.id)).style.height = 0;
        $("#path_error_"+getElementNumber(checkBox.id)).className = "error-none";
        $("#path_input_"+getElementNumber(checkBox.id)).className = "textbox";
    }
}

function isValidForm()
{
    var projectName = $("#projectName");
    var pathFolder = document.getElementsByName("localDirectoryPath");
    var pathUrl = document.getElementsByName("projectUrl");
    var pathArt = document.getElementsByName("pathToArtefacts");
    var pathVer = document.getElementsByName("versionFilesPath");
    var build = document.getElementsByName("projectToBuild");
    var patBuild = document.getElementsByName("fileToBuild");
    if (projectName.value == "")
    {
        $("#projectErrorText").innerHTML = " Please, enter your project name";
        $("#projectError").className = "error-block empty";
        projectName.focus();
        return false;
    }
    if(projectName.className == "textbox-error")
    {
        projectName.focus();
        return false;
    }
    if(build.length == 0)
    {
        $("#label-add-view").className = "label-add-view";
        return false;
    }
    if($("#email_Error").className == "error-block")
    {
        $("#email").focus();
        return false;
    }
    if($("#configEmail_Error").className == "error-block")
    {
        $("#configEmail").focus();
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
    if (type == "ApproveReject")
    {
        $("#formType").value = "APPROVED";
        return true;
    }
    buildConfiguration.isNameFree(projectName.value, function(t) {
        if (t.responseObject() != false)
        {
            $("#save").onclick = null;
            setFormResultDialog("create");
            $('save').click();
        }
        else
        {
            $("#fieldHelp").innerHTML = "Configuration with name '" + projectName.value + "' already exists. Please select another name.";
            $("#fieldHelp").className = "field-help-error";
        }
    });
    return false;
}

function checkingPath(path)
{
   if(path.length == 0)
        return true;
   for(var i=0;i<path.length;i++)
    {
        if(path[i].className == "textbox-error")
        {
            path[i].focus();
            return false;
        }
    }
    return true;
}

function rejectDiv()
{
    $("#rejectDiv").className = "reject-div help-top";
    $("#overlay").className = (scroll != 0 ? 'overlay help-top' : 'overlay');
    $("#textReject").focus();
}

function rejectionSubmit()
{
    return true;
}

function deleteFromHidden(hidden, value)
{
    hidden.value = hidden.value.replace(value,"");
    hidden.value = hidden.value.replace(";;",";");
    if (hidden.value[0] == ";")
    {
        hidden.value = hidden.value.substr(1, hidden.value.length);
    }
    if (hidden.value.lastIndexOf(";") == hidden.value.length - 1)
    {
        hidden.value = hidden.value.substr(0,hidden.value.length - 1);
    }
}

function setFormResultDialog(result)
{
    $("#formResultHidden").value = result;
}

function closeButtonClick(button)
{
    var divId = button.id.replace('close_','');
    var element = document.getElementById(divId);
    element.outerHTML = "";
    delete element;
}

function addPath(button)
{
    var number = getElementNumber(button.id);
    var path = $("#path_input_" + number).value;
    var pathInput = $("#localDirectoryPath_" + (number-1)).value;
    var error = $("#path_error_" + number).className;
    var errorInput = $("#path_error_" + (number-1)).className;
    if((pathInput.length <= 0)||(errorInput == "error-block")||(path.length <= 0)||(error == "error-block"))
    {
        return;
    }
    
    var selectionId = "files_" + number;
    
    if (!checkPathRepeat(path, selectionId))
    {
        addToSelectionBox(selectionId, path);
        $("#path_input_" + number).value = "";
    }
    else
    {
        $("#path_error_" + number).className = "error-block";
        $("#coincide_" + number).innerHTML = " such pass already exists";
        return;
    }
}

function addPathFiles(button)
{
    var number = getElementNumber(button.id);
    var path = $("#path_input_" + number).value;
    var error = $("#path_error_" + number).className;

    if((path.length <= 0)||(error == "error-block"))
    {
        return;
    }

    var selectionId = "files_" + number;
    if (!checkPathRepeat(path, selectionId))
    {
        addToSelectionBox(selectionId, path);
        $("#path_input_" + number).value = "";
    }
    else
    {
        $("#path_error_" + number).className = "error-block";
        $("#coincide_" + number).innerHTML = " path has already added";
        return;
    }
}

function checkPathRepeat(path, selectionBoxId)
{
    var options = document.getElementById(selectionBoxId).options;
    if (options == null)
    {
        return false;
    }
    for (var i=0; i<options.length; i++)
    {
        if (options[i].value == path)
        {
            return true;
        }
    }
    return false;
}

function trimSlash(value)
{
    while (value[value.length-1] == "\\")
    {
        value = value.substr(0, value.length-1);
    }
    return value;
}

function imageHelp(id)
{  
    var number = getElementNumber(id);
    if($("#block_help_"+number).className == "help-view")
    {
        $("#block_help_"+number).className = "block-help-view";
        $("#text_help_"+number).className = "helptext-block";
    }
    else
    {
        $("#block_help_"+number).className = "help-view";
        $("#text_help_"+number).className = "helptext";
    }
}

function validateMail(mail)
{
    var name = mail.name;
    var mailValue = mail.value;
    var mails = mailValue.split(' ');
    var cheking = true;
    for(var i = 0; i < mails.length;i++)
    {
        cheking = checkMail(mails[i]);
        if (!cheking)
            break;
    }
    if(cheking || mail.value=="")
    {
        document.getElementById(name + "_Error").className = "error-none";
        if (name == "email")
        {
            document.getElementById(name).className = "email-notification";
        }
        else
        {
            document.getElementById(name).className = "textbox";
        }
    }
    else
    {
        document.getElementById(name + "_Error").className = "error-block";
        if (name == "email")
        {
            document.getElementById(name).className = "email-notification-error";
        }
        else
        {
            document.getElementById(name).className = "textbox-error";
        }
    }
}

function checkMail(mailp)
{
    var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
    if(pattern.test(mailp))
        return true;
    else 
        return false;
}

function checkURL(id,add)
{
    var url = document.getElementById(id).value;
    var number = getElementNumber(id);
    var regURL = /^(?:(?:https?|ftp|telnet):\/\/(?:[a-z0-9_-]{1,32}(?::[a-z0-9_-]{1,32})?@)?)?(?:(?:[a-z0-9-]{1,128}\.)+(?:com|net|org|mil|edu|arpa|ru|gov|biz|info|aero|inc|name|[a-z]{2})|(?!0)(?:(?!0[^.]|255)[0-9]{1,3}\.){3}(?!0|255)[0-9]{1,3})(?:\/[a-z0-9.,_@%&?+=\~\/-]*)?(?:#[^ \'\"&<>]*)?$/i;
    if(regURL.test(url))
    {
        $("#url_error_"+number+add).className = "error-none";
        document.getElementById(id).className = "textbox";
    }
    else
    {
        $("#url_error_"+number+add).className = "error-block";
        document.getElementById(id).className= "textbox-error";
    }
}

function checkPath(id)
{
    var path = document.getElementById(id).value;
    var number = getElementNumber(id);
    
    var regPath;
    if ((document.getElementById(id).name == "pathToArtefacts")||
        (document.getElementById(id).name == "versionFilesPath"))     
    {
        regPath = /^(?![*?])(?:[^\\/:"*?<>|\r\n]+?(?:\/?|\/\*{0,2})*?|\/\*\.\*$)*?$/;// Allow Ant wildcards valid folder/file structure only
    }
    else if(document.getElementById(id).name == "localDirectoryPath")
    {																		// Change also correctArtifactPaths at JobManagerGenerator
    	regPath = /^\.$|^(?:(?!\.)[^\\/:*?"<>|\r\n]+\/?)*$/;				// Match only one . or valid folder structure (zero-length - ok)
    }
    else
    {
        regPath = /^([a-zA-Z]:)?(\\[^<>:"/\\|?*]+)+\\?$/i;
    }

    if(regPath.test(path) || path == "")
    {
        $("#path_error_"+number).className = "error-none";
        document.getElementById(id).className = "textbox";
    }
    else
    {
        if (document.getElementById(id).name == "pathToArtefacts" || document.getElementById(id).name == "versionFilesPath")
        {
            $("#coincide_" + number).innerHTML = " Not correct path";
        }
        $("#path_error_"+number).className = "error-block";
        document.getElementById(id).className= "textbox-error";
    }
}

function checkPTB(id)
{
    var path = document.getElementById(id).value;
    var number = getElementNumber(id);
    var regPath = /^([a-zA-Z]:\\)?[^\x00-\x1F"<>\|:\*\?/]+\.[a-zA-Z]{3,5}$/i;
    if(regPath.test(path))
    {
        $("#ptb_error_"+number).className = "error-none";
        document.getElementById(id).className = "textbox";
    }
    else
    {
        $("#ptb_error_"+number).className = "error-block";
        document.getElementById(id).className= "textbox-error";
    }
}

function OkReject()
{
    var reason = $("#textReject").value;
    if (reason != null)
    {
        if (reason.length == 0)
        {
            $("#textReject").focus();
            return false;
        }
        $("#rejectionReason").value = reason;
        $("#formType").value = "REJECT";
        $("#rejectSubmit").click();
    }
    return;
}

function СancelReject()
{
    $("#rejectDiv").className = "div-none";
    $("#overlay").className = "div-none";
    $("#textReject").value = "";
}

function validateProject(project)
{
    var regPath = /^[^\\\/\?\*\#\%\"\>\<\:\|]*$/i;
    var cl = $("#projectError").className;
    var classes = cl.split(" ");
    if((classes.length == 2)&&(project.value.length == 0))
        return;
    if(regPath.test(project.value) || (project.value.length == 0))
    {
        $("#projectError").className = "error-none";
        document.getElementById(project.id).className = "textbox";
        $("#projectErrorText").innerHTML = "";
        $("#fieldHelp").className = "";
    }
    else
    {
        $("#projectError").className = "error-block";
        $("#projectErrorText").innerHTML = " Not correct name";
        document.getElementById(project.id).className= "textbox-error";
    }
}

function bMCChange(checkBox)
{
    if (checkBox.checked)
    {
        addToHidden("build_machine_configuration", checkBox.id);
    }
    if (!checkBox.checked)
    {
        deleteFromHidden($("#build_machine_configuration"), checkBox.id);
    }
}

function checkName(id)
{
    var path = document.getElementById(id).value;
    var number = getElementNumber(id);
    var regPath = /[/ ? " : < >]$/i;

    if(!regPath.test(path) || path.length == 0)
    {
        $("#name_error_"+number).className = "error-none";
        document.getElementById(id).className = "textbox";
    }
    else
    {
        $("#name_error_"+number).className = "error-block";
        document.getElementById(id).className= "textbox-error";
    }
}

function validAllView()
{
    var view = document.getElementsByClassName("div-add-project-to-build-view");
    var textboxes;
    for (var i=0; i<view.length; i++)
    {
        textboxes = view[i].getElementsByClassName("textbox");
        for (var j=0; j<textboxes.length; j++)
        {
            if ((textboxes[j].name != "credentials")&&(textboxes[j].name != "branchName")&&
                (textboxes[j].name != "preScript")&&(textboxes[j].name != "postScript")){
               textboxes[j].onblur();
            }    
            
        }
    }
}



