package com.zzz.registration.testalternate;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import com.uhg.umvs.bene.domain.common.Day;
import com.uhg.umvs.bene.domain.common.Month;
import com.uhg.umvs.bene.domain.common.SecurityQuestion;
import com.uhg.umvs.bene.domain.common.SponsorRelationship;
import com.uhg.umvs.bene.domain.ldap.UserAccountEntry;
import com.uhg.umvs.bene.domain.reference.SecurityQuestionReference;
import com.uhg.umvs.bene.service.eligibility.EligibilityService;
import com.uhg.umvs.bene.service.email.EmailService;
import com.uhg.umvs.bene.service.email.MessageContext;
import com.uhg.umvs.bene.service.email.MessageTemplate;
import com.uhg.umvs.bene.service.reference.ReferenceService;
import com.uhg.umvs.bene.service.security.SecurityService;
import com.uhg.umvs.bene.web.registration.RegistrationForm;
import com.uhg.umvs.bene.web.registration.RegistrationHelperImpl;
import com.uhg.umvs.bene.web.registration.RegistrationModel;

public class AlternateRegistrationHelperImplTest
{
    private RegistrationForm mockForm = createMock(RegistrationForm.class);

    private RegistrationModel mockModel = createMock(RegistrationModel.class);

    private ReferenceService mockReferenceService = createMock(ReferenceService.class);

    private SecurityService mockSecurityService = createMock(SecurityService.class);

    private EmailService mockEmailService = createMock(EmailService.class);
    
    private MessageTemplate mockEmailMessageTemplate = createMock(MessageTemplate.class);
    
    private EligibilityService mockEligibilityService = createMock(EligibilityService.class);
    
    private Object[] mocks = { mockForm, mockModel, mockReferenceService,
            mockSecurityService, mockEmailService, mockEmailMessageTemplate, mockEligibilityService };
    
    
    private RegistrationHelperImpl createHelper()
    {
        RegistrationHelperImpl h = new RegistrationHelperImpl();
        h.setReferenceService(mockReferenceService);
        h.setSecurityService(mockSecurityService);
        h.setEmailMessageTemplate(mockEmailMessageTemplate);
        h.setEligibilityService(mockEligibilityService);
        h.setEmailService(mockEmailService);
        return h;
    }
    

//    @Test
//    public void testInitializeForm() throws Exception
//    {
//        RegistrationHelperImpl h = createHelper();
//        reset(mocks);
//        expectLastCall();
//        replay(mocks);
//        h.initialize(mockForm);
//        verify(mocks);
//    }

    @Test
    public void testInitializeModel() throws Exception
    {
        List<Month> months = new ArrayList<Month>();
        months.add(createMock(Month.class));
        List<Day> days = new ArrayList<Day>();
        days.add(createMock(Day.class));
        List<SecurityQuestion> secqs = new ArrayList<SecurityQuestion>();
        secqs.add(createMock(SecurityQuestion.class));
        List<SponsorRelationship> sponrels = new ArrayList<SponsorRelationship>();
        sponrels.add(createMock(SponsorRelationship.class));
        
        
        RegistrationHelperImpl h = createHelper();
        reset(mocks);
        expect(mockReferenceService.readAllMonths(Locale.getDefault())).andReturn(months);
        mockModel.setMonths(months); expectLastCall();
        expect(mockReferenceService.readAllDays()).andReturn(days);
        mockModel.setDays(days); expectLastCall();
        expect(mockReferenceService.readAllSecurityQuestions()).andReturn(secqs);
        mockModel.setSecurityQuestions(secqs); expectLastCall();
        expect(mockReferenceService.readAllSponsorRelationships()).andReturn(sponrels);
        mockModel.setSponsorRelationships(sponrels); expectLastCall();
        replay(mocks);
        h.initialize(mockModel);
        verify(mocks);
    }
        
//    @Test
//    public void testSendConfirmationEmail() throws Exception    
//    {
//        RegistrationMessageContext msgctx = new RegistrationMessageContext();
//        msgctx.setTo("a@b.com");
//        msgctx.setFrom("b@c.com");        
//        msgctx.asMap().put("form",mockForm);
//        msgctx.setIndividual(true);
//        
//        RegistrationHelperImpl h = createHelper();
//
//        reset(mocks);        
//        expect(mockForm.getEmail()).andReturn("a@b.com");
//        expect(mockEmailMessageTemplate.getFrom()).andReturn("b@c.com");
//        mockEmailService.composeAndSend(msgctx, mockEmailMessageTemplate);
//
//        replay(mocks);
//        h.sendConfirmationEmail(mockForm, mockModel);
//        verify(mocks);
//
//    }

    @Test
    public void testCreateUserAccountWithMap() throws Exception
    {
        Map<String,Object> testuserdata = TestData.userAlpha;
        testuserdata = TestUtils.copyExcluding(testuserdata, "securityQuestionId");

        // -> Style one (autoset with map from above)
        SecurityQuestionReference securityQuestion = new SecurityQuestionReference();
        securityQuestion.setId(1);
        securityQuestion.setName("Favorite Pet?");

        // create user bean
        UserAccountEntry user = new UserAccountEntry();
        TestUtils.populateBeanLoose(user, testuserdata);
        
        // instantiate helper
        RegistrationHelperImpl h = createHelper();
        reset(mocks);
        TestUtils.prepareGetterExpectsLoose(mockForm, testuserdata);
        // this is too complicated for simple expects...
        expect(mockModel.getSecurityQuestion()).andReturn(securityQuestion);
        mockSecurityService.createUserAccount(user);
        mockEmailService.composeAndSend(EasyMock.isA(MessageContext.class), EasyMock.eq(mockEmailMessageTemplate));
        
        replay(mocks);
        h.createUserAccount(mockForm, mockModel);
        verify(mocks);
    }

    @Test
    public void testCreateUserAccountWithVarargs() throws Exception
    {
        
        String firstName = "Mark";
        String lastName  = "Blogenfist";
        String ssn = "111-22-3333";
        String sponsorSsn = "444-55-6666";
        String email = "a@b.com";
        String username = "markblo7";
        String password = "1@24887a";
        Integer securityQuestionId = 11;
        String securityQuestion = "Favorite Pet?";
        String securityQuestionResponse = "Mr Peepers";

        Integer dobmon = 10;
        Integer dobday = 11;
        String dobyear = "1971";
        Date mdy = new Date();
        mdy.setDate(dobday); mdy.setMonth(dobmon); mdy.setYear(Integer.parseInt(dobyear));

        SecurityQuestionReference securityQuestionRef = new SecurityQuestionReference();
        securityQuestionRef.setId(1);
        securityQuestionRef.setName("Favorite Pet?");

        
        // -> Style two (autoset with varargs)
        UserAccountEntry user = new UserAccountEntry();
        TestUtils.populateBean(user,
                "firstName",firstName,
                "lastName",lastName,
                "dob",mdy,
                "ssn",ssn,
                "sponsorSsn",sponsorSsn,
                "email","a@b.com",
                "username",username,
                "password",password,
                "securityQuestion",securityQuestion,
                "securityQuestionResponse",securityQuestionResponse);
        RegistrationHelperImpl h = createHelper();
        reset(mocks);
        TestUtils.prepareGetterExpects(mockForm,
                "firstName",firstName,
                "lastName",lastName,
                "dob",mdy,
                "ssn",ssn,
                "sponsorSsn",sponsorSsn,
                "email","a@b.com",
                "username",username,
                "password",password,
                "securityQuestionResponse",securityQuestionResponse);
        // this is too complicated for simple expects...
        expect(mockModel.getSecurityQuestion()).andReturn(securityQuestionRef);
        mockSecurityService.createUserAccount(user);
        mockEmailService.composeAndSend(EasyMock.isA(MessageContext.class), EasyMock.eq(mockEmailMessageTemplate));
        
        replay(mocks);
        h.createUserAccount(mockForm, mockModel);
        verify(mocks);

    }
    
}
