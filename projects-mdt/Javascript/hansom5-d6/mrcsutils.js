function setLocal(key,value)
{
    document.applets["localsession"].setKey(applicationname,key,value);
}

function getLocal(key)
{
    var value = document.applets["localsession"].getKey(applicationname,key);
    getLocal = value;
}

function storeCredentials(username,password,docbase)
{
    setLocal(applicationname+".username",username);
    setLocal(applicationname+".password",password);
    setLocal(applicationname+".docbase",docbase);
}

function clearCredentials()
{
    
}
