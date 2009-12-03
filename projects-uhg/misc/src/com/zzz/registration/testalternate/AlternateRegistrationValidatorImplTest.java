package com.zzz.registration.testalternate;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.validation.Errors;

import com.uhg.umvs.bene.domain.common.MilitaryIndividual;
import com.uhg.umvs.bene.web.registration.RegistrationForm;
import com.uhg.umvs.bene.web.registration.RegistrationFormImpl;
import com.uhg.umvs.bene.web.registration.RegistrationModel;
import com.uhg.umvs.bene.web.registration.RegistrationValidatorHelper;
import com.uhg.umvs.bene.web.registration.RegistrationValidatorImpl;

public class AlternateRegistrationValidatorImplTest
{
    
    private MilitaryIndividual mockMilGuy = createMock(MilitaryIndividual.class);
    private RegistrationForm mockForm = createMock(RegistrationForm.class);
    private RegistrationModel mockModel = createMock(RegistrationModel.class);
    private RegistrationValidatorHelper mockHelper = createMock(RegistrationValidatorHelper.class);
    
    private Errors mockErrors = createMock(Errors.class);

    private Object[] mocks = { mockForm, mockModel, mockHelper, mockErrors, mockMilGuy };
    

    @Test
    public void testSupports()
    {
        RegistrationValidatorImpl v = new RegistrationValidatorImpl();
        assertTrue(v.supports(RegistrationFormImpl.class));
        assertFalse(v.supports(RegistrationForm.class));
    }
    
    @Test
    public void testValidate()
    {
        RegistrationValidatorImpl v = new RegistrationValidatorImpl();

        reset(mocks);
        replay(mocks);
        try
        {
            v.validate(mockForm, mockErrors);
            fail();
        }
        catch (UnsupportedOperationException ex)
        {
            // success
        }
        verify(mocks);
    }
    
    void runInvalidDemographicsTest(Map<String,Object> formdata, String field, String expectederror) throws Exception
    {
        RegistrationValidatorImpl v = createValidator();
        reset(mocks);
        TestUtils.prepareGetterExpects(mockForm, formdata);
        mockErrors.rejectValue(field, expectederror);
        expectLastCall();
        expect(mockErrors.getErrorCount()).andReturn(1);
        replay(mocks);
        v.validateDemographics(mockForm, mockModel, mockErrors);
        verify(mocks);
        
    }
    
    @Test
    public void testValidateDemographics() throws Exception
    {


        // test valid path
        RegistrationValidatorImpl v = createValidator();
        reset(mocks);
        TestUtils.prepareGetterExpects(mockForm, TestData.demographicsAlpha);
        expect(mockErrors.getErrorCount()).andReturn(0);
        expect(mockHelper.doEligiblityCheck(mockForm)).andReturn(mockMilGuy);
        mockModel.setSponsor(mockMilGuy);
        expectLastCall();
        replay(mocks);
        v.validateDemographics(mockForm, mockModel, mockErrors);
        verify(mocks);
        
        // missing first name
        runInvalidDemographicsTest(TestUtils.copyClearing(TestData.demographicsAlpha, "firstName"),"firstName", "registration.firstname.required");

        // missing last name
        runInvalidDemographicsTest(TestUtils.copyClearing(TestData.demographicsAlpha,"lastName"),"lastName", "registration.lastname.required");

        // missing sponsor ssn
        runInvalidDemographicsTest(TestUtils.copyClearing(TestData.demographicsAlpha,"sponsorSsn"),"sponsorSsn", "registration.sponsorssn.required");

        // missing ssn
        runInvalidDemographicsTest(TestUtils.copyClearing(TestData.demographicsAlpha,"ssn"),"ssn", "registration.ssn.required");

        // missing sponsor relationship id
        runInvalidDemographicsTest(TestUtils.copyClearing(TestData.demographicsAlpha,"sponsorRelationshipId"),"sponsorRelationshipId", "registration.sponsorrelationshipid.required");

        // invalid ssn
        Map<String,Object> badssn = TestUtils.copyClearing(TestData.demographicsAlpha,"ssn");
        badssn.put("ssn", "A");
        runInvalidDemographicsTest(badssn,"ssn", "registration.ssn.pattern");

        // invalid sponsor ssn
        Map<String,Object> badsponsorssn = TestUtils.copyClearing(TestData.demographicsAlpha,"sponsorSsn");
        badsponsorssn.put("sponsorSsn", "AAA-BB-CCCC");
        runInvalidDemographicsTest(badsponsorssn,"sponsorSsn", "registration.sponsorssn.pattern");

        

    }
    
    public void runAccountTest(Map<String,Object> accountdata, boolean expectErrors) throws Exception
    {
        RegistrationValidatorImpl v = createValidator();
        TestUtils.prepareGetterExpects(mockForm, accountdata);
        String password = (String)accountdata.get("password");
        expect(mockForm.getPassword()).andReturn(password); // extra for equals check
        
        // optional email fields but MUST match if either has entries...
        String email = (String)accountdata.get("email");
        String emailConfirm = (String)accountdata.get("emailConfirm");
        if (StringUtils.isEmpty(email)) {
            expect(mockForm.getEmail()).andReturn(email);
            expect(mockForm.getEmailConfirm()).andReturn(emailConfirm);
            if (!StringUtils.isEmpty(emailConfirm)) {
                expect(mockForm.getEmail()).andReturn(email);
                expect(mockForm.getEmailConfirm()).andReturn(emailConfirm);                
            }
        } else {
            expect(mockForm.getEmail()).andReturn(email);
            //expect(mockForm.getEmail()).andReturn(email);
            //expect(mockForm.getEmailConfirm()).andReturn(emailConfirm);                            
        }
        // success test or failtest?
        expect(mockErrors.hasErrors()).andReturn(expectErrors);
        if (!expectErrors) {
            String username = (String)accountdata.get("username");
            expect(mockForm.getUsername()).andReturn(username); // for availability check
            expect(mockHelper.isAccountAvailable(username)).andReturn(true);
            expectLastCall();
        } else {
            expectLastCall();            
        }
        replay(mocks);
        v.validateAccount(mockForm, mockModel, mockErrors);
        verify(mocks);        
    }
    
    @Test
    public void testValidateAccount() throws Exception
    {
        String passwordweak = "weakpass";
        String passwordshort = "$hort1";
        String passwordconsecutive = "$abcghi0";
        String emailConfirmViolation = "c@d.com";        

        // valid path - no email
        Map<String,Object> noemail = TestUtils.copyExcluding(TestData.accountAlpha,"email","emailConfirm");
        reset(mocks);
        runAccountTest(noemail,false);

        // valid path with email
        reset(mocks);
        runAccountTest(TestData.accountAlpha,false);

        // missing username
        reset(mocks);
        mockErrors.rejectValue("username", "registration.username.required");
        runAccountTest(TestUtils.copyClearing(noemail,"username"),true);
        
        // missing password
        reset(mocks);
        mockErrors.rejectValue("password", "registration.password.required");
        mockErrors.rejectValue("passwordConfirm", "registration.passwordconfirm.equals");
        runAccountTest(TestUtils.copyClearing(noemail,"password"),true);
        
        // missing password Confirm
        reset(mocks);
        mockErrors.rejectValue("passwordConfirm", "registration.passwordconfirm.required");
        runAccountTest(TestUtils.copyClearing(noemail,"passwordConfirm"),true);

        // missing sec question
        reset(mocks);
        mockErrors.rejectValue("securityQuestionId", "registration.securityquestionid.required");
        runAccountTest(TestUtils.copyClearing(noemail,"securityQuestionId"),true);
        
        // missing answer
        reset(mocks);
        mockErrors.rejectValue("securityQuestionResponse", "registration.securityquestionresponse.required");
        runAccountTest(TestUtils.copyClearing(noemail,"securityQuestionResponse"),true);

        // password/passwordConfirm mismatch
        Map<String,Object> passwordMismatch = TestUtils.copyClearing(noemail, "passwordConfirm");
        passwordMismatch.put("passwordConfirm", passwordweak);
        reset(mocks);
        mockErrors.rejectValue("passwordConfirm", "registration.passwordconfirm.equals");
        runAccountTest(passwordMismatch,true);
        
        // email/emailConfirm mismatch
        Map<String,Object> emailMismatch = TestUtils.copyClearing(TestData.accountAlpha, "emailConfirm");
        passwordMismatch.put("emailConfirm", emailConfirmViolation);
        reset(mocks);
        mockErrors.rejectValue("emailConfirm", "registration.emailconfirm.equals");
        runAccountTest(emailMismatch,true);

    }
    

        

    private RegistrationValidatorImpl createValidator()
    {
        RegistrationValidatorImpl v = new RegistrationValidatorImpl();
        v.setHelper(mockHelper);
        return v;
    }

}
