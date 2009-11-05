package com.medtronic.ecm.documentum.introspection.config;

import java.util.List;

public class MrcsESignature 
{
    public Integer NoOfSigns;
    public List SigningReasons;
    public MrcsPlugin CustomSignaturePlugin;
    public String SigningReasonsQuery; // DQL Query that gets the signing reasons
    
}
