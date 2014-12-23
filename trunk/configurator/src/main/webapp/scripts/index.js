var scroll=0;
var nameAction;

window.onscroll = function()
{
    var aside = document.getElementById("fieldHelpView");
    scroll = document.documentElement.getBoundingClientRect().top;
    aside.className = (document.documentElement.getBoundingClientRect().top != 0 ? 'field-help-view-fixed' : '');
}

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
            document.getElementById("rejectDiv").className = "reject-div";
            document.getElementById("overlay").className = "overlay";
            document.getElementById("СancelReject").className = "div-none";
            document.getElementById("helpReject").innerHTML = t.responseObject().errorMassage;
        }
    });
}

function OkReject()
{
    document.getElementById("rejectDiv").className = "div-none";
    document.getElementById("overlay").className = "div-none";
    if(document.getElementById("СancelReject").className == "div-none")
        document.getElementById("СancelReject").className = "button-reject";
    if(nameAction != null)
    {
        var mas = nameAction.split('?');
        switch(mas[1])
        {
            case("setDeletion"):{buildConfiguration.setForDeletion(mas[0], function(t){});location.reload();break;}
            case("deletePermanently"):{buildConfiguration.deleteConfigurationPermanently(mas[0], function(t){});location.reload();break;}
            case("restore"):{buildConfiguration.restoreConfiguration(mas[0], function(t){});location.reload();break;}
        }
        nameAction = null;
    }
}

function СancelReject()
{
    document.getElementById("overlay").className = "div-none";
}