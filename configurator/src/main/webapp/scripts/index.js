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