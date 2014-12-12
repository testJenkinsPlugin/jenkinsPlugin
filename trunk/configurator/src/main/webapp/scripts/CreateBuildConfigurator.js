var type;

window.onload = function()
{
    var parameters = document.location.search.substr(1);
	
    buildConfiguration.loadCreateNewBuildConfiguration(function(t) {});
    if (parameters.length != 0)
    {
        var projectName = getParameterValue("name");
        type = getParameterValue("type");
        document.getElementById("formType").value = type.toUpperCase();
        loadViews(projectName);
        setContent(projectName);
        document.getElementById("projectName").disabled = true;
        if (type == "ApproveReject")
        {
            document.getElementById("titlePage").innerHTML = "Approve/reject build configuration";
            document.getElementById('save').value='Approve';
            document.getElementById('reject').style.visibility='visible';
        }
    }
    else
    {
        document.getElementById("formType").value = "CREATE";
    }

    buildConfiguration.deleteNotUploadFile(document.getElementById("files_hidden_script")
        .value.split(';'), function(t) {});
    document.getElementById("files_hidden_script").value = "";
}



function setContent(name)
{
    buildConfiguration.getConfiguration(name, function(t){
        document.getElementById("projectName").value = t.responseObject().projectName;
		
        var bmcValue = t.responseObject().buildMachineConfiguration;
        var bmc = document.getElementsByName("buildMachineConfiguration");
        for (var i=0; i<bmc.length; i++)
        {
            if (bmc[i].value == bmcValue)
            {
                bmc[i].checked  = "selected";
            }
        }

        var mail = t.responseObject().email;
        if (mail.length != 0)
        {
            document.getElementById('email').disabled = false
            document.getElementById('email').value = mail;
            document.getElementById('isEmail').checked = true;
        }

        var scripts = t.responseObject().scripts;
        for (var i=0; i<scripts.length; i++)
        {
            addToSelectionBox("files_script", scripts[i])
        }
		if(t.responseObject().creator != null)
		{
			buildConfiguration.getFullNameCreator(t.responseObject().creator, function(t){
			
			document.getElementById("userLabel").innerHTML = "Created by:  "+t.responseObject();
			})
		}
    })
	
}

document.addEventListener('keyup', function (e)
{
    if(e.keyCode == 46)
    {
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
                var hiddenInput = document.getElementById("files_hidden_" + getElementNumber(selectionGroups[i].id));
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
    buildConfiguration.getView(
    function(t)
    {
        var iDiv = document.createElement("div");
        iDiv.innerHTML = t.responseObject().html;
        document.getElementById("projectsToBuild").appendChild(iDiv);
        setAppletsId(t.responseObject().viewId);
    });
}

function loadViews(projectName)
{
    buildConfiguration.loadViews(projectName,
    function(t)
    {
        if (t.responseObject().html.length == 0)
        {
            return;
        }
        var iDiv = document.createElement("div");
        iDiv.innerHTML = t.responseObject().html;
        document.getElementById("projectsToBuild").appendChild(iDiv);
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
        document.getElementById("builders_" + divs).appendChild(iDiv);
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
        document.getElementById("mailError").style.display = "";
		document.getElementById("email").style.border = "";	
        document.getElementById(textboxId).disabled = true;
        document.getElementById(textboxId).value = "";
    }
    if (checkBox.checked)
    {
        document.getElementById(textboxId).disabled = false;
    }
}

function upload()
{
    var file = document.getElementById("scriptsButton").files[0];
    if (file.size > 1048576)
    {
        alert ("You can't upload file which size is more than 1MB");
        document.getElementById("scriptsButton").value = "";
        return;
    }
    if (!validateExtension(file.name))
    {
        alert ("You can't upload file with this extension. Please choose file with '.bat, .nant, .powershell, .shell, .ant, .maven' extension only.");
        document.getElementById("scriptsButton").value = "";
        return;
    }
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
	var hiddenValue = document.getElementById(hiddenInputId).value;
	if (hiddenValue.length > 0 && hiddenValue.lastIndexOf(";") != hiddenValue.length - 1)
	{
		document.getElementById(hiddenInputId).value += ";" + path + ";";
	}
	else
	{
		document.getElementById(hiddenInputId).value += path + ";";
	}
}

function validateExtension(filename)
{
    var ext = filename.substring(filename.lastIndexOf('.')+1);
    var extensions = new Array ("bat","nant","powershell","shell","ant","maven");
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
        document.getElementById(pathInput).style.visibility = "visible";
        document.getElementById(addButton).style.visibility = "visible";
        document.getElementById(pathInput).value = "";
        var hiddenInput = "files_hidden_" + getElementNumber(checkBox.id);
        document.getElementById(hiddenInput).value = "";
		document.getElementById("div-fieldset_"+getElementNumber(checkBox.id)).style.visibility = "visible";
		document.getElementById(addfield).style.visibility = "visible";
		document.getElementById("div-fieldset_"+getElementNumber(checkBox.id)).style.height = 60;   
    }
    if (!checkBox.checked)
    {
        var selectionGroupId = "files_" + getElementNumber(checkBox.id);
        cleacSelectionGroup(selectionGroupId);
        document.getElementById(pathInput).style.visibility = "hidden";
        document.getElementById(addButton).style.visibility = "hidden";
		document.getElementById("div-fieldset_"+getElementNumber(checkBox.id)).style.visibility = "hidden";
		document.getElementById(addfield).style.visibility = "hidden";
		document.getElementById("div-fieldset_"+getElementNumber(checkBox.id)).style.height = 0;
    }
}

function isValidForm()
{
    var projectName = document.getElementById("projectName").value;
	var pathFolder = document.getElementsByName("projectFolderPath");
	var pathUrl = document.getElementsByName("projectUrl");
	var pathArt = document.getElementsByName("pathToArtefacts");
	var pathVer = document.getElementsByName("versionFilesPath");
    if (projectName == "")
    {
        alert("Please, enter your project name");
        return false;
    }
	if(document.getElementById("mailError").className == "error-block")
	{
		document.getElementById("email").focus();
		return false;
	}
	if (!checkingPath(pathUrl))
		return false;
	if (!checkingPath(pathFolder))
		return false;
	if (!checkingPath(pathArt))
		return false;
	if (!checkingPath(pathVer))
		return false;	
    if (type == "edit")
        return true;
    if (type == "ApproveReject")
    {
        document.getElementById("formType").value = "APPROVED";
        return true;
    }
    buildConfiguration.isNameFree(projectName, function(t) {
        if (t.responseObject() != false)
        {
            document.getElementById("save").onclick = null;
            setFormResultDialog("create");
            document.getElementById('save').click();
        }
        else
            alert("Configuration with name '" + projectName + "' has already exists. Please select another name.");
    });
    return false;
}

function checkingPath(path)
{
   for(var i=0;i<path.length;i++)
	{
	    if(path[i].className == "textbox-error")
		{
			path[i].focus();
			return false;
		}
		else
			return true;
	}
}

function rejectConfiguration()
{
    var reason = prompt("Please type reasons of rejection:", "");
    if (reason != null)
    {
        if (reason.length == 0)
        {
            alert ("You must type reasons of rejection.");
            return false;
        }
        document.getElementById("rejectionReason").value = reason;
		alert(document.getElementById("rejectionReason").value);
        document.getElementById("formType").value = "REJECT";
        return true;
    }
    return false;
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

function urlTypeChange(radiobutton)
{
    var hiddenId = "sourceControlTool_" + getElementNumber(radiobutton.name);
    document.getElementById(hiddenId).value = radiobutton.value;
}

function setFormResultDialog(result)
{
    document.getElementById("formResultHidden").value = result;
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
    var path = document.getElementById("path_input_" + number).value;
	var error = document.getElementById("path_error_" + number).className;
    if((path.length <= 0)||(error == "error-block"))
    {
        return;
    }
    addToSelectionBox("files_" + number, path);
    document.getElementById("path_input_" + number).value = "";
}

function imageHelp(id)
{  
	var number = getElementNumber(id);
	
	if(document.getElementById("block_help_"+number).className == "help-view")
	{
		document.getElementById("block_help_"+number).className = "block-help-view";
		document.getElementById("text_help_"+number).className = "helptext-block";
	}
	else
	{
		document.getElementById("block_help_"+number).className = "help-view";
		document.getElementById("text_help_"+number).className = "helptext";
	}
}

function validateMail(mail)
{
	var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
    if(pattern.test(mail.value)||mail.value=="")
	{
		document.getElementById("mailError").className = "error-none";
		document.getElementById("email").className = "textbox";
	}
	else
	{
		document.getElementById("mailError").className = "error-block";
		document.getElementById("email").className = "textbox-error";
	}
}

function checkURL(id,add)
{
    var url = document.getElementById(id).value;
	var number = getElementNumber(id);
    var regURL = /^(?:(?:https?|ftp|telnet):\/\/(?:[a-z0-9_-]{1,32}(?::[a-z0-9_-]{1,32})?@)?)?(?:(?:[a-z0-9-]{1,128}\.)+(?:com|net|org|mil|edu|arpa|ru|gov|biz|info|aero|inc|name|[a-z]{2})|(?!0)(?:(?!0[^.]|255)[0-9]{1,3}\.){3}(?!0|255)[0-9]{1,3})(?:\/[a-z0-9.,_@%&?+=\~\/-]*)?(?:#[^ \'\"&<>]*)?$/i;
    if(regURL.test(url) || url == "")
	{
	    document.getElementById("url_error_"+number+add).className = "error-none";
		document.getElementById(id).className = "textbox";
	}
	else
	{
		document.getElementById("url_error_"+number+add).className = "error-block";
		document.getElementById(id).className= "textbox-error";
	}
}

function checkPath(id)
{
	var path = document.getElementById(id).value;
	var number = getElementNumber(id);
    var regPath = /^([a-zA-Z]:)?(\\[^<>:"/\\|?*]+)+\\?$/i;
    if(regPath.test(path) || path == "")
	{
	    document.getElementById("path_error_"+number).className = "error-none";
		document.getElementById(id).className = "textbox";
	}
	else
	{
		document.getElementById("path_error_"+number).className = "error-block";
		document.getElementById(id).className= "textbox-error";
	}
}

