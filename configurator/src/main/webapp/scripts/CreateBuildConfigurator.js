var type;
   
window.onload = function()
{
    var parameters = document.location.search.substr(1);
    if (parameters.length != 0)
    {
        var projectName = getParameterValue("name");
        type = getParameterValue("type");
        document.getElementById("formType").value = type;
        buildConfiguration.getConfiguration(projectName, function(t) {
        setContent(t.responseObject());});
        if (type == "ApproveReject")
        {
            document.getElementById('reject').style.visibility='visible';
            document.getElementById('save').value='Approve';
            document.getElementById("titlePage").innerHTML = "Approve/reject build configuration";
        }
    }
    else
    {
        document.getElementById("formType").value = "create";
    }
    document.getElementById("folderChooser").setIsFolderChooser(true);
    document.getElementById("folderChooser").setGroup(1);
    document.getElementById("fileToBuildChooser").setGroup(2);
    document.getElementById("pathToArtefactsChooser").setGroup(3);
    document.getElementById("versionFileChooser").setGroup(4);
    document.getElementById("fileHidden").value = "";
    document.getElementById("artefactsHidden").value = "";
    document.getElementById("versionFileHidden").value = "";
    document.getElementById("projectRootFolder").value = "";
    buildConfiguration.deleteNotUploadFile(document.getElementById("scriptsHidden").value.split(';'), function(t) {});
    document.getElementById("scriptsHidden").value = "";
}

document.addEventListener('keyup', function (e)
{
    var activeElement = document.activeElement.id;
    if(activeElement == "projectName" || activeElement == "url" || activeElement == "projectRootFolder" || activeElement=="email" || activeElement=="otherConfig")
        return;
    if(e.keyCode == 46)
    {
        if (document.getElementById("Files").selectedIndex != -1)
            deleteSelectionItem(document.getElementById("Files"), document.getElementById("fileHidden"));
        if (document.getElementById("Artefacts").selectedIndex != -1)
            deleteSelectionItem(document.getElementById("Artefacts"), document.getElementById("artefactsHidden"));
        if (document.getElementById("VersionFile").selectedIndex != -1)
            deleteSelectionItem(document.getElementById("VersionFile"), document.getElementById("versionFileHidden"));
        if (document.getElementById("Scripts").selectedIndex != -1)
        {
            var selectionValue = document.getElementById("Scripts")[document.getElementById("Scripts").selectedIndex].value;
            buildConfiguration.deleteNotUploadFile(selectionValue, function(t) {});
            deleteSelectionItem(document.getElementById("Scripts"), document.getElementById("scriptsHidden"));
        }
    }
}, false);

function setContent(response)
{
    document.getElementById("projectName").value = response.projectName;
    document.getElementById("projectName").disabled = true;
    document.getElementById("url").value = response.url;
    setRadioButtonSelection(document.getElementsByName("sourceControlTool"), response.sourceControlTool);
    setRadioButtonSelection(document.getElementsByName("buildMachineConfiguration"), response.buildMachineConfiguration);
    setRadioButtonSelection(document.getElementsByName("configuration"), response.configuration); 
    if (response.configuration == "Other")
        document.getElementById("otherConfig").disabled = false;
    document.getElementById("otherConfig").value = response.userConfiguration;
    secCheckBox(response.builders);
    secCheckBox(response.platforms);
    document.getElementById("email").value = response.email;
    if (response.email != "")
    {
        document.getElementById("isEmail").checked = true;
        document.getElementById("email").disabled = false;
    }
    setFilePath(response.files, "Files", "fileHidden");
    setFilePath(response.artefacts, "Artefacts", "artefactsHidden");
    setFilePath(response.versionFile, "VersionFile", "versionFileHidden");
    if (response.versionFile.length!=0)
        if(response.versionFile[0]!="")
            document.getElementById("isVersionFiles").checked = true;
    setFilePath(response.scripts, "Scripts", "scriptsHidden");
}

function setFilePath(path, selectionBoxId, hidenField)
{
    for (var p in path)
    {
        if (!isNaN(p))
            addToSelectionBox(selectionBoxId,path[p],hidenField);
    }
}

function secCheckBox(checkBoxList)
{
    var inputs = document.getElementsByTagName("input");
    for (var inp in inputs)
    {
        if (inputs[inp].type == "checkbox")
        {
            if (checkBoxList.indexOf(inputs[inp].name)!=-1)
                inputs[inp].checked = true;
        }
    }
}

function setRadioButtonSelection(radioButton, value)
{
    for (var rad in radioButton)
    {
        if (radioButton[rad].value == value)
        {
            radioButton[rad].checked  = true;
            return;
        }
    }
}

function getParameterValue(parameter)
{
    var parameters = document.location.search.substr(1);
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

function deleteSelectionItem(selection, hidden)
{
    var selectionValue = selection[selection.selectedIndex].value;
    hidden.value = hidden.value.replace(selectionValue,"");
    if (hidden.value.lastIndexOf(";") == hidden.value.length - 1)
    {
        hidden.value = hidden.value.substr(0,hidden.value.length - 1);
    }
    if (hidden.value[0] == ";")
    {
        hidden.value = hidden.value.substr(1, hidden.value.length);
    }
    hidden.value = hidden.value.replace(";;",";");
    selection.remove(selection.selectedIndex);
}

function rootFolderChange()
{
    clearSelectionFile("Files", "fileHidden");
    clearSelectionFile("Artefacts", "artefactsHidden");
    document.getElementById("isVersionFiles").checked = false;
    clearSelectionFile("VersionFile", "versionFileHidden");
    document.getElementById("fileToBuildChooser").setStartPath(document.getElementById("projectRootFolder").value);
    document.getElementById("pathToArtefactsChooser").setStartPath(document.getElementById("projectRootFolder").value);
    document.getElementById("versionFileChooser").setStartPath(document.getElementById("projectRootFolder").value);
}

function clearSelectionFile(selectioGroup, hiddenField)
{
    document.getElementById(hiddenField).value = "";
    var selectElement = document.getElementById(selectioGroup);
    for (var option in selectElement)
    {
        selectElement.remove(option);
    }
}

function userCheckFile(group)
{
    switch (group)
    {
        case "1":
            var path = getUserChoose("folderChooser");
            if (path != "")
                if (path != document.getElementById("projectRootFolder").value)
                {
                    document.getElementById("projectRootFolder").value = path;
                    rootFolderChange();
                }
            break;

        case "2":
            var path = getUserChoose("fileToBuildChooser");
            if (path != "")
                tryAddToSelectionBox("Files" ,path, "fileHidden");
            break;

        case "3":
            var path = getUserChoose("pathToArtefactsChooser");
            if (path != "")
                tryAddToSelectionBox("Artefacts" ,path, "artefactsHidden");
            break;

        case "4":
            if (!document.getElementById("isVersionFiles").checked)
            {
                document.getElementById("isVersionFiles").checked = true;
            }
            var path = getUserChoose("versionFileChooser");
            if (path != "")
                tryAddToSelectionBox("VersionFile" ,path, "versionFileHidden");
            break;
    }
}

function tryAddToSelectionBox(selectionBoxId, path, hidenField)
{
    if (document.getElementById("projectRootFolder").value == "")
    {
        alert("Please set root project folder before adding files.");
        return;
    }
    if (!checkFilePath(path))
    {
        alert("Sorry, but you choose file which not exist in project folders.");
        return;
    }
    path = path.substr(document.getElementById("projectRootFolder").value.length+1);
    addToSelectionBox(selectionBoxId, path, hidenField);
}

function addToSelectionBox(selectionBoxId, path, hidenField)
{
    var x = document.getElementById(selectionBoxId);
    var option = document.createElement("option");
    option.text = path;
    x.add(option);
    if (document.getElementById(hidenField).value == "")
    {
        document.getElementById(hidenField).value = path;
    }
    else
    {
        document.getElementById(hidenField).value += ";" + path;
    }
}

function checkFilePath(path)
{
    if (path.indexOf(document.getElementById("projectRootFolder").value)!=-1)
        return true;
    else
        return false;
}

function getUserChoose(control)
{
    var result = document.getElementById(control).getPath();
    if (result == "")
        return "";
    document.getElementById(control).setPath("");
    return result;
}

function configurationGroupChange(radio)
{
    if(radio.id == "OTHER")
    {
        document.getElementById("otherConfig").disabled = false;
    }
    else
    {
        document.getElementById("otherConfig").value = "";
        document.getElementById("otherConfig").disabled = true;
    }
}
   
function checkBoxChange(cb)
{
    if (!document.getElementById("isVersionFiles").checked)
    {
        clearSelectionFile("VersionFile", "versionFileHidden");
    }
    if (!document.getElementById("isEmail").checked)
    {
        document.getElementById("email").disabled = true;
        document.getElementById("email").value = "";
    }
    if (document.getElementById("isEmail").checked)
    {
        document.getElementById("email").disabled = false;
    }
}

function setformResult()
{
    document.getElementById("formResultHidden").value = "cancel";
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
        if (this.status == 200) {
        addToSelectionBox("Scripts", this.responseText, "scriptsHidden");
    }
    document.getElementById("scriptsButton").disabled = false;
    document.getElementById("scriptsButton").value = "";
    };
}

function selectionBoxIndexChange(selection)
{
    var sel = document.getElementById("Files");
    if (sel != selection)
        sel.selectedIndex = -1;
    sel = document.getElementById("Artefacts");
    if (sel != selection)
        sel.selectedIndex = -1;
    sel = document.getElementById("VersionFile");
    if (sel != selection)
        sel.selectedIndex = -1;
    sel = document.getElementById("Scripts");
    if (sel != selection)
        sel.selectedIndex = -1;
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
        document.getElementById("formType").value = "reject";
        return true;
    }
    return false;
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
        document.getElementById("formType").value = "approved";
        return true;
    }
    buildConfiguration.isNameValid(projectName, function(t) {
        if (t.responseObject() != false)
        {
            document.getElementById("save").onclick = null;
            document.getElementById('save').click();
        }
        else
            alert("Configuration with name '" + projectName + "' has already exists. Please select another name.");
    });
    return false;
}