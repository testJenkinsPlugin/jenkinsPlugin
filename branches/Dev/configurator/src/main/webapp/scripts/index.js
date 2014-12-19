function setDeletion(name)
{
    var result = confirm("Are you sure you want delete '" + name + "' configuration");
    if (!result)
        return;
    var labelName = name+"Label";
    buildConfiguration.setForDeletion(name, function(t) {});
    document.getElementById(labelName).innerHTML = "For Deletion";
    var label = document.getElementById(labelName);
    label.style.backgroundColor = "#000000";
}

function deletePermanently(name)
{
    var result = confirm("Are you sure you want delete '" + name + "' configuration permanently");
    if (!result)
        return;
    buildConfiguration.deleteConfigurationPermanently(name, function(t) {});
}

function exportToXml()
{
    document.getElementById("loading-div-background").style.visibility='visible';
    document.getElementById("loading-div").style.visibility='visible';
    buildConfiguration.exportToXml(function(t) 
    {
        document.getElementById("loading-div-background").style.visibility='hidden';
        document.getElementById("loading-div").style.visibility='hidden';

        if (!t.responseObject().success)
        {
            alert(t.responseObject().errorMassage);
        }
    });
}

function createJob(name)
{
    buildConfiguration.createJob(name, function(t) { alert("Job was successfully created.");});
}