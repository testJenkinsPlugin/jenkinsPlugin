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

    document.getElementById("folderChooser").setIsFolderChooser(true);
    document.getElementById("folderChooser").setGroup("folderChooser");
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

function userCheckFile(group)
{
    switch (group)
    {
        case "folderChooser":
            var path = getUserChoose("folderChooser");
            if (path != null && path!="")
            {
                if (path != document.getElementById("projectRootFolder").value)
                {
                    document.getElementById("projectRootFolder").value = path;
                    rootFolderChange();
                }
            }
            break;
        
        default:
            addUserSelectFile(group);
    }
}

function addUserSelectFile(group)
{
    var path = getUserChoose(group);
    if (path == "")
    {
        return;
    }

    var divgroupId = document.getElementById(group).parentNode.parentNode.id;
    var appletNumber = getElementNumber(group);

    if (document.getElementById("files_" + appletNumber).tagName.toLowerCase() == "input")
    {
        if (!isPathValid(path))
        {
            return;
        }
        path = path.substr(document.getElementById("projectRootFolder").value.length);
        document.getElementById("files_" + appletNumber).value = path;
        return;
    }

    var selectionGroups = document.getElementById(divgroupId).getElementsByTagName("select");

    for (var i = 0; i < selectionGroups.length; i++)
    {
        if (selectionGroups[i].id.lastIndexOf(appletNumber)!=-1 && selectionGroups[i].id.indexOf("files") != -1)
        {
            tryAddToSelectionBox(selectionGroups[i].id, path);
        }
    }
}

function getElementNumber(id)
{
    return id.substring(id.lastIndexOf('_')+1);
}

function isPathValid(path)
{
    if (document.getElementById("projectRootFolder").value == "")
    {
        alert("Please set root project folder before adding files.");
        return false;
    }
    if (path.indexOf(document.getElementById("projectRootFolder").value) == -1)
    {
        alert("Sorry, but you choose file which not exist in project folders.");
        return false;
    }
    return true;
}

function tryAddToSelectionBox(selectionBoxId, path)
{
    if (!isPathValid(path))
    {
        return;
    }
    
    path = path.substr(document.getElementById("projectRootFolder").value.length);
    addToSelectionBox(selectionBoxId, path)
}

function rootFolderChange()
{
    var newPath = document.getElementById("projectRootFolder").value;
    var applets = document.getElementsByTagName("applet");
    for (var i=0; i<applets.length; i++)
    {
        document.getElementById(applets[i].id).setStartPath(newPath);
    }
}

function cleacSelectionGroup(groupId)
{
    var selectElement = document.getElementById(groupId);
    for (var option in selectElement)
    {
        selectElement.remove(option);
    }
}

function getUserChoose(control)
{
    var result = document.getElementById(control).getPath();
    document.getElementById(control).setPath("");
    return result;
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
        setAppletsId("projectsToBuild");
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

function setAppletsId(viewId)
{
    var elms = document.getElementById(viewId).getElementsByTagName("applet");
    var newPath = document.getElementById("projectRootFolder").value;
    for (var i = 0; i < elms.length; i++)
    {
        document.getElementById(elms[i].id).setGroup(elms[i].id);
        document.getElementById(elms[i].id).setStartPath(newPath);
    }
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
    document.getElementById(hiddenInputId).value += path + ";";
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
    var appletId = "applet_" + getElementNumber(checkBox.id);
    if (checkBox.checked)
    {
        document.getElementById(appletId).style.visibility = "visible";
    }
    
    if (!checkBox.checked)
    {
        var selectionGroupId = "files_" + getElementNumber(checkBox.id);
        cleacSelectionGroup(selectionGroupId);
        document.getElementById(appletId).style.visibility = "hidden";
    }
}

function isValidForm()
{
    var projectName = document.getElementById("projectName").value;
    if (projectName == "")
    {
        alert("Please, enter your project name");
        return false;
    }
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