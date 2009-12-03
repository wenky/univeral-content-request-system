/*
 * TODO:  DEERS-less call for eligibility aka doDemographics (?facade impl vs actual impl?)
 * TODO:  initialize does test account check and removal from LDAP so flow completes without error 
 */


package com.zzz.registration.testalternate;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.test.MockParameterMap;

import com.uhg.umvs.bene.domain.common.MilitaryIndividual;
import com.uhg.umvs.bene.test.AbstractActionTest;
import com.uhg.umvs.bene.web.registration.RegistrationAction;
import com.uhg.umvs.bene.web.registration.RegistrationFormImpl;
import com.uhg.umvs.bene.web.registration.RegistrationHelper;
import com.uhg.umvs.bene.web.registration.RegistrationModel;
import com.uhg.umvs.bene.web.registration.RegistrationModelImpl;
import com.uhg.umvs.bene.web.registration.RegistrationValidatorHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
        "/conf/spring/web/common/action-beans.xml",
        "/conf/spring/web/common/model-beans.xml", 
        "/conf/spring/web/common/validator-beans.xml",
        "/conf/spring/web/registration/action-beans.xml",
        "/conf/spring/web/registration/model-beans.xml",
        "/conf/spring/web/registration/validator-beans.xml",
        "/conf/spring/web/registration/mock-helper-beans.xml" })
public class AlternateRegistrationActionImplTest extends AbstractActionTest
{
    @Resource
    private RegistrationAction action;

    @Resource
    private RegistrationHelper mockHelper;

    @Resource
    private RegistrationValidatorHelper mockValidatorHelper;
    
    private MilitaryIndividual mockMilGuy = createMock(MilitaryIndividual.class);

    private Object[] mocks;

    @Before
    public void setupMocks()
    {
        mocks = new Object[] { mockHelper, mockValidatorHelper};
    }

    protected void assertCurrentStateEquals(String desiredState)
    {
        String curstate = getFlowExecution().getActiveSession().getState().getId();
        super.assertCurrentStateEquals(desiredState);
        // CEM: why doesn't my flow scopes have these in them? I assume the framework would be doing this for me...
        //assertNotNull(getFlowExecution().getConversationScope().get("registrationForm"));
        //assertNotNull(getFlowExecution().getConversationScope().get("registrationModel"));
    }

    @Override
    protected String getFlowDefinitionClassPath()
    {
        return "/conf/spring/web/registration/flow/registration-flow.xml";
    }

    @Override
    protected void configureFlowBuilderContext(MockFlowBuilderContext context)
    {
        super.configureFlowBuilderContext(context);
    }

    @Override
    protected void registerMockFlowBeans(ConfigurableBeanFactory flowBeanFactory)
    {
        flowBeanFactory.registerSingleton("registrationAction", action);
        flowBeanFactory.registerSingleton("registrationHelper", mockHelper);
        flowBeanFactory.registerSingleton("registrationValidatorHelper", mockValidatorHelper);
    }

    @Test
    public void testRegistration() throws Exception
    {
        // prepare test data
        MockExternalContext context = new MockExternalContext();

        // prepare mock objects
        reset(mocks);
        //prepareInitialize
        mockHelper.initialize(new RegistrationFormImpl(), new RegistrationModelImpl());
        expectLastCall();        
        //prepareDemographics        
        RegistrationFormImpl form = new RegistrationFormImpl();
        TestUtils.populateBean(form, TestData.demographicsBeta);
        expect(mockValidatorHelper.doEligiblityCheck(form)).andReturn(mockMilGuy);
        mockHelper.populateModelForDemographics(eq(form), isA(RegistrationModel.class));
        expectLastCall();
        //prepareAccount
        expect(mockValidatorHelper.isAccountAvailable((String)TestData.accountBeta.get("username"))).andReturn(true);
        mockHelper.populateModelForAccount(eq(form), isA(RegistrationModel.class));
        expectLastCall();        
        //prepareAccountConfirmation();
        RegistrationFormImpl cyaform = new RegistrationFormImpl();
        RegistrationModelImpl cyamodel = new RegistrationModelImpl();
        mockHelper.createUserAccount(cyaform, cyamodel);
        expectLastCall();
        //preparations complete
        

        // execute test (possible to do: side trip to example?)
        replay(mocks);
        startFlow(context);
        //Preamble
        assertCurrentStateEquals("displayPreamble");
        signalEvent("start", context); // click "Begin Registration" link
        //Demographics Form
        assertCurrentStateEquals("displayDemographics");
        MockParameterMap demographicsParameters = TestUtils.convertToParameterMap(TestData.demographicsBeta);
        signalEvent("next", demographicsParameters, context);
        //Demographics Confirmation
        assertCurrentStateEquals("displayDemographicsConfirmation");
        signalEvent("next",context);  // click "Continue" button to proceed
        //Account Form
        assertCurrentStateEquals("displayAccount");
        MockParameterMap accountParameters = TestUtils.convertToParameterMap(TestData.accountBeta);
        signalEvent("next", accountParameters, context);
        //Account Confirm/Cya Form
        assertCurrentStateEquals("displayAccountConfirmation");
        MockParameterMap cyaParameters = TestUtils.convertToParameterMap(TestUtils.createMap("cya1", "true","cya2", "true"));
        signalEvent("next", cyaParameters, context);
        //Thank You For Smoking...
        assertCurrentStateEquals("displayThankyou");
        signalEvent("next",context); // click "login" link 
        assertFlowExecutionEnded();

        // verify results
        verify(mocks);
    }


}
