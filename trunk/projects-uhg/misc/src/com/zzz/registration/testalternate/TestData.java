package com.zzz.registration.testalternate;

import java.util.Date;
import java.util.Map;

public class TestData
{
    // alpha user
    
    public static Map<String,Object> userAlpha = TestUtils.createMap( 
            "firstName","Mark",
            "lastName","Blogenfist",
            "dob",new Date(6,14,1971),
            "ssn","111-22-3333",
            "sponsorSsn","444-55-6666",
            "email","a@b.com",
            "username","markblo7",
            "password","1@24887a",
            "securityQuestionId",11,
            "securityQuestion","Favorite Pet?",                
            "securityQuestionResponse","Mr Peepers");

    public static Map<String,Object> demographicsAlpha = TestUtils.createMap(
            "firstName","Mark",
            "lastName","Blogenfist",
            "dob",new Date(6,14,1971),
            "ssn","111-22-3333",
            "sponsorSsn","444-55-6666",
            "sponsorRelationshipId",1);
            
    public static Map<String,Object> accountAlpha = TestUtils.createMap(
            "username","markblo7",
            "password","1@24887a",
            "passwordConfirm","1@24887a",
            "securityQuestionId",11,
            "securityQuestionResponse","Mr Peepers",
            "email","a@b.com",
            "emailConfirm","a@b.com");

    //Beta user

    public static Map<String,Object> demographicsBeta = TestUtils.createMap(
            "firstName","Mary",
            "lastName","Blofield",
            "dob",new Date(9,12,1970),
            "ssn","112-23-3334",
            "sponsorSsn","445-56-6667",
            "sponsorRelationshipId",2);
            
    public static Map<String,Object> accountBeta = TestUtils.createMap(
            "username","marytest",
            "password","uhg135##",
            "passwordConfirm","uhg135##",
            "securityQuestionId",3,
            "securityQuestionResponse","mary response",
            "email","aa@bb.com",
            "emailConfirm","aa@bb.com");

    
    
}
