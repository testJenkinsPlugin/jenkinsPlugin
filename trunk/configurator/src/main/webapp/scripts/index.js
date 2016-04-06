var nameAction;
var isAdmin;


function setDeletion(name)
{
    document.getElementById("rejectDiv").className = "reject-div";
    document.getElementById("overlay").className = "overlay";
    document.getElementById("helpReject").innerHTML = "Are you sure you want delete '" + name + "' configuration";
    nameAction = name+"?setDeletion";  
}

function deletePermanently(name)
{
    document.getElementById("rejectDiv").className = "reject-div";
    document.getElementById("overlay").className = "overlay";
    document.getElementById("helpReject").innerHTML = "Are you sure you want delete '" + name + "' configuration permanently";
    nameAction = name+"?deletePermanently"; 
}

function restore(name)
{
    document.getElementById("rejectDiv").className = "reject-div";
    document.getElementById("overlay").className = "overlay";
    document.getElementById("helpReject").innerHTML = "Are you sure you want restore '" + name + "' configuration";
    nameAction = name+"?restore"; 
}

function OkReject()
{
    document.getElementById("rejectDiv").className = "div-none";
    document.getElementById("overlay").className = "div-none";
    if(document.getElementById("CancelReject").className == "div-none")
        document.getElementById("CancelReject").className = "button-reject";
    if(nameAction != null)
    {
        var mas = nameAction.split('?');
        nameAction = null;
        switch(mas[1])
        {
            case("setDeletion"):{buildConfiguration.setForDeletion(mas[0], function(t){location.reload();}); break;}
            case("deletePermanently"):{buildConfiguration.deleteConfigurationPermanently(mas[0], function(t){location.reload();});break;}
            case("restore"):{buildConfiguration.restoreConfiguration(mas[0], function(t){location.reload();});break;}
            case("createJob"):{buildConfiguration.createJob(mas[0], function(t){location.reload();}); break;}
            case("deleteJob"):{buildConfiguration.deleteJob(mas[0], function(t){location.reload();}); break;}
        }
    }
}

function CancelReject()
{
    document.getElementById("overlay").className = "div-none";
}

function createJob(name)
{
    document.getElementById("rejectDiv").className = "reject-div";
    document.getElementById("overlay").className = "overlay";
    document.getElementById("CancelReject").className = "div-none";
    var message;
    var s = document.getElementsByName(name)[0].innerHTML.trim();
    switch(document.getElementsByName(name)[0].innerHTML.trim())
    {
        case("Create Job"):{message = "Job will be created."; break;}
        case("Update Job"):{message = "Job will be updated."; break;}
    }
    document.getElementById("helpReject").innerHTML = message;
    nameAction = name+"?createJob"; 
}

function deleteJob(name)
{
    name = name.replace("delete_", "");
    document.getElementById("rejectDiv").className = "reject-div";
    document.getElementById("overlay").className = "overlay";
    document.getElementById("helpReject").innerHTML = "Are you sure you want delete '" + name + "' job?";
    nameAction = name+"?deleteJob"; 
}