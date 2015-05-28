package com.currencycloud.client;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.MatchRule;
import com.currencycloud.client.exception.*;
import com.currencycloud.client.model.ErrorMessage;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "UnnecessaryBoxing"})
public class ErrorTest extends BetamaxTestSupport {

    private String loginId = "rjnienaber@gmail.com";
    private String apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb65f02f129fd87eafa45ded8ae257528f0";

    @Test
    @Betamax(tape = "contains_full_details_for_api_error", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testContainsFullDetailsForApiError() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb57528f0";
        BadRequestException error = testFailedLogin("auth_invalid_user_login_details", 400, BadRequestException.class);
        assertThat(error.getErrorMessages().get("api_key").get(0).getCode(), equalTo("api_key_length_is_invalid"));
        assertThat(error.getErrorMessages().get("api_key").get(0).getMessage(), equalTo("api_key should be 64 character(s) long"));
        assertThat(error.getErrorMessages().get("api_key").get(0).getParams().get("length"), instanceOf(Integer.class));
        assertThat((Integer)error.getErrorMessages().get("api_key").get(0).getParams().get("length"), equalTo(new Integer(64)));
    }

    @Test
    @Betamax(tape = "is_raised_on_a_bad_request", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnABadRequest() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb57528f0";
        BadRequestException error = testFailedLogin("auth_invalid_user_login_details", 400, BadRequestException.class);
        ErrorMessage errorMessage = error.getErrorMessages().get("api_key").get(0);
        assertThat(errorMessage.getCode(), equalTo("api_key_length_is_invalid"));
        assertThat(errorMessage.getMessage(), equalTo("api_key should be 64 character(s) long"));
        assertThat(errorMessage.getParams().get("length"), instanceOf(Integer.class));
        assertThat((Integer)errorMessage.getParams().get("length"), equalTo(new Integer(64)));
    }

    @Test
    @Betamax(tape = "is_raised_on_incorrect_authentication_details", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnIncorrectAuthenticationDetails() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "efb5ae2af84978b7a37f18dd61c8bbe139b403009faea83484405a3dcb64c4d8";
        AuthenticationException error = testFailedLogin("auth_failed", 401, AuthenticationException.class);
        assertThat(error.getErrorMessages().get("username").size(), equalTo(1));
        assertThat(error.getErrorMessages().get("username").get(0).getCode(), equalTo("invalid_supplied_credentials"));
        assertThat(error.getErrorMessages().get("username").get(0).getMessage(), equalTo("Authentication failed with the supplied credentials"));
        assertThat(error.getErrorMessages().get("username").get(0).getParams(), anEmptyMap());
    }

/*
  it 'is raised on unexpected error' do
    allow(HTTParty).to receive(:post).and_raise(Timeout::Error)

    error = nil
    begin
      CurrencyCloud.session
      raise 'Should have failed'
    rescue CurrencyCloud::UnexpectedError => error
    end

        expected_error = %Q{CurrencyCloud::UnexpectedError
---
platform: #{error.platform}
request:
  parameters:
    login_id: rjnienaber@gmail.com
    api_key: ef0fd50fca1fb14c1fab3a8436b9ecb65f02f129fd87eafa45ded8ae257528f0
  verb: post
  url: https://devapi.thecurrencycloud.com/v2/authenticate/api
inner_error: Timeout::Error
}

    expect(error.to_s).to eq(expected_error)
    expect(error.inner_error).to_not be_nil
    expect(error.inner_error.class).to eq(Timeout::Error)
  end
*/

    @Test
    @Betamax(tape = "is_raised_on_a_forbidden_request", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnAForbiddenRequest() throws Exception {
        ForbiddenException error = testFailedLogin("auth_failed", 403, ForbiddenException.class);
        assertThat(error.getErrorCode(), equalTo("auth_failed"));
        assertThat(error.getHttpStatusCode(), equalTo(403));


        ErrorMessage errorMessage = error.getErrorMessages().get("username").get(0);
        assertThat(errorMessage.getCode(), equalTo("invalid_supplied_credentials"));
        assertThat(errorMessage.getMessage(), equalTo("Authentication failed with the supplied credentials"));
        assertThat(errorMessage.getParams(), is(anEmptyMap()));
    }

    @Test
    @Betamax(tape = "is_raised_when_a_resource_is_not_found", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedWhenAResourceIsNotFound() throws Exception {
        client.setAuthToken("656485646b068f6e9c81e3d885fa54f5");

        try {
            client.retrieveBeneficiary("081596c9-02de-483e-9f2a-4cf55dcdf98c");
            assertThat("Should fail.", false);
        } catch (NotFoundException error) {
            assertThat(error.getErrorCode(), equalTo("beneficiary_not_found"));
            assertThat(error.getHttpStatusCode(), equalTo(404));
            assertThat(error.getErrorMessages().size(), equalTo(1));

            ErrorMessage errorMessage = error.getErrorMessages().get("id").get(0);
            assertThat(errorMessage.getCode(), equalTo("beneficiary_not_found"));
            assertThat(errorMessage.getMessage(), equalTo("Beneficiary was not found for this id"));
            assertThat(errorMessage.getParams(), is(anEmptyMap()));
        }
    }

    @Test
    @Betamax(tape = "is_raised_on_an_internal_server_error", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnAnInternalServerError() throws Exception {
        InternalApplicationException error = testFailedLogin("internal_application_error", 500, InternalApplicationException.class);
        ErrorMessage errorMessage = error.getErrorMessages().get("base").get(0);
        assertThat(errorMessage.getCode(), equalTo("internal_application_error"));
        assertThat(errorMessage.getMessage(), equalTo("A general application error occurred"));
        assertThat(errorMessage.getParams(), hasEntry("request_id", (Object)2771875643610572878L));
    }

    @Test
    @Betamax(tape = "is_raised_when_too_many_requests_have_been_issued", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedWhenTooManyRequestsHaveBeenIssued() throws Exception {
        loginId = "rjnienaber@gmail.com2";
        TooManyRequestsException error = testFailedLogin("too_many_requests", 429, TooManyRequestsException.class);
        ErrorMessage errorMessage = error.getErrorMessages().get("base").get(0);
        assertThat(errorMessage.getCode(), equalTo("too_many_requests"));
        assertThat(errorMessage.getMessage(), equalTo("Too many requests have been made to the api. Please refer to the Developer Center for more information"));
        assertThat(errorMessage.getParams(), is(anEmptyMap()));
    }

    // todo: handling of timout errors

    private <E extends ApiException> E testFailedLogin(String errorCode, int httpStatusCode, Class<E> exceptionClass) {
        try {
            client.authenticate(loginId, apiKey);
            throw new AssertionError("Should have failed");
        } catch (ApiException error) {
            assertThat(error, instanceOf(exceptionClass));
            assertThat(error.getHttpStatusCode(), equalTo(httpStatusCode));
            assertThat(error.getErrorCode(), equalTo(errorCode));
            assertThat(error.getErrorMessages().size(), equalTo(1));
            return exceptionClass.cast(error);
        }
    }
}