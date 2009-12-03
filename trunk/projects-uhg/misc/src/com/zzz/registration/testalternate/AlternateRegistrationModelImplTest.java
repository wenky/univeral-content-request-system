package com.zzz.registration.testalternate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.uhg.umvs.bene.domain.common.Day;
import com.uhg.umvs.bene.domain.common.Month;
import com.uhg.umvs.bene.domain.common.SecurityQuestion;
import com.uhg.umvs.bene.domain.common.SponsorRelationship;
import com.uhg.umvs.bene.web.registration.RegistrationModelImpl;

public class AlternateRegistrationModelImplTest
{
    // CEM: appear to be mostly injection/wiring tests...

    @Test
    public void testSecurityQuestions()
    {
        List<SecurityQuestion> expected = new ArrayList<SecurityQuestion>();

        RegistrationModelImpl model = new RegistrationModelImpl();
        model.setSecurityQuestions(expected);
        assertEquals(expected, model.getSecurityQuestions());
    }

    @Test
    public void testSponsorRelationships()
    {
        List<SponsorRelationship> expected = new ArrayList<SponsorRelationship>();

        RegistrationModelImpl model = new RegistrationModelImpl();
        model.setSponsorRelationships(expected);
        assertEquals(expected, model.getSponsorRelationships());
    }

    @Test
    public void testMonths()
    {
        List<Month> expected = new ArrayList<Month>();

        RegistrationModelImpl model = new RegistrationModelImpl();
        model.setMonths(expected);
        assertEquals(expected, model.getSponsorRelationships());
    }

    @Test
    public void testDays()
    {
        List<Day> expected = new ArrayList<Day>();

        RegistrationModelImpl model = new RegistrationModelImpl();
        model.setDays(expected);
        assertEquals(expected, model.getSponsorRelationships());
    }

    
}
