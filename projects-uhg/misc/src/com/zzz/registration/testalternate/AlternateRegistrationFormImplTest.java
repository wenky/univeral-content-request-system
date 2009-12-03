package com.zzz.registration.testalternate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.uhg.umvs.bene.web.registration.RegistrationFormImpl;

public class AlternateRegistrationFormImplTest
{


    @Test
    public void testFirstName()
    {
        String expected = "RegFirst";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setFirstName(expected);
        assertEquals(expected, form.getFirstName());
    }

    @Test
    public void testLastName()
    {
        String expected = "RegLast";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setLastName(expected);
        assertEquals(expected, form.getLastName());
    }

    @Test
    public void testUsername()
    {
        String expected = "Regtestuser";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setUsername(expected);
        assertEquals(expected, form.getUsername());
    }

    @Test
    public void testPassword()
    {
        String expected = "Regpass1";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setPassword(expected);
        assertEquals(expected, form.getPassword());
    }

    @Test
    public void testPasswordConfirm()
    {
        String expected = "Regpass2";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setPasswordConfirm(expected);
        assertEquals(expected, form.getPasswordConfirm());
    }

    @Test
    public void testSecurityQuestionId()
    {
        Integer expected = 667;

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setSecurityQuestionId(expected);
        assertEquals(expected, form.getSecurityQuestionId());
    }

    @Test
    public void testSponsorRelationshipId()
    {
        Integer expected = 668;

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setSponsorRelationshipId(expected);
        assertEquals(expected, form.getSponsorRelationshipId());
    }

    
    @Test
    public void testSecurityQuestionResponse()
    {
        String expected = "RegResponse";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setSecurityQuestionResponse(expected);
        assertEquals(expected, form.getSecurityQuestionResponse());
    }

    @Test
    public void testSponsorSsn()
    {
        String expected = "112-22-3333";
        String expectedlast4 = "3333";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setSponsorSsn(expected);
        assertEquals(expected, form.getSponsorSsn());
        assertEquals(expectedlast4, form.getSponsorSsnLast4());
        
    }

    @Test
    public void testSsn()
    {
        String expected = "998-88-7777";
        String expectedlast4 = "7777";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setSsn(expected);
        assertEquals(expected, form.getSsn());
        assertEquals(expectedlast4, form.getSsnLast4());
    }


    @Test
    public void testEmail()
    {
        String expected = "a@b.com";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setEmail(expected);
        assertEquals(expected, form.getEmail());
    }

    @Test
    public void testEmailConfirm()
    {
        String expected = "a@b.com";

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setEmailConfirm(expected);
        assertEquals(expected, form.getEmailConfirm());
    }

    @Test
    public void testCya1()
    {
        boolean expected = false;

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setCya1(expected);
        assertEquals(expected, form.getCya1());
    }

    @Test
    public void testCya2()
    {
        boolean expected = false;

        RegistrationFormImpl form = new RegistrationFormImpl();
        form.setCya2(expected);
        assertEquals(expected, form.getCya2());
    }

    
}
