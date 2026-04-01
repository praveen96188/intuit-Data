package com.intuit.sbd.payroll.psp.iam;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.hats.common.InvalidTicketException;
import com.intuit.platform.integration.hats.common.OfflineTicketAuthorizationHeader;
import com.intuit.platform.integration.iamticket.client.IAMOfflineTicketClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;


public class AuthorizationBuilderTest {
	private static final String DEFAULT_OFFLINE_TICKET = "eyJraWQiOiJJbnR1aXQuY3RvLmlhbS5oYXRzLnB";

	private AuthorizationBuilder authorizationBuilder;
	private OfflineTicketAuthorizationHeader offlineTicketAuthorizationHeader;
	private IAMOfflineTicketClient offlineTicketClient;

	@Before
	public void setup() throws Exception {
		offlineTicketAuthorizationHeader = mock(OfflineTicketAuthorizationHeader.class);
		offlineTicketClient = mock(IAMOfflineTicketClient.class);

		whenNew(IAMOfflineTicketClient.class).withAnyArguments().thenReturn(offlineTicketClient);

		authorizationBuilder = new AuthorizationBuilder("IAM URL", "APP ID", "APP SECRET");
	}

	@Test
	@Ignore
	public void buildAuthorizationHeaderWithOfflineTicket() throws Exception {
		when(offlineTicketClient
				.getAuthHeaderForSystemOfflineTicket(Mockito.any(GetAuthHeaderForSystemOfflineTicketRequest.class)))
						.thenReturn(offlineTicketAuthorizationHeader);
		when(offlineTicketAuthorizationHeader.getAuthorizationHeader()).thenReturn(DEFAULT_OFFLINE_TICKET);

        GetAuthHeaderForSystemOfflineTicketRequest getAuthHeaderForSystemOfflineTicketRequest
                = mock(GetAuthHeaderForSystemOfflineTicketRequest.class);
		String offlineToken = authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket(getAuthHeaderForSystemOfflineTicketRequest);
		assertEquals(DEFAULT_OFFLINE_TICKET, offlineToken);
	}

	@Test(expected = RuntimeException.class)
	public void buildAuthorizationHeaderWithOfflineTicketThrowsInvalidTicketExceptiono() throws Exception {
		when(offlineTicketClient
				.getAuthHeaderForSystemOfflineTicket(Mockito.any(GetAuthHeaderForSystemOfflineTicketRequest.class)))
						.thenThrow(new InvalidTicketException("Invalid offline ticket"));

        GetAuthHeaderForSystemOfflineTicketRequest getAuthHeaderForSystemOfflineTicketRequest
                = mock(GetAuthHeaderForSystemOfflineTicketRequest.class);
		authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket(getAuthHeaderForSystemOfflineTicketRequest);
	}

	@Test(expected = RuntimeException.class)
	public void buildAuthorizationHeaderWithOfflineTicketThrowsNullException() throws Exception {
		when(offlineTicketClient
				.getAuthHeaderForSystemOfflineTicket(Mockito.any(GetAuthHeaderForSystemOfflineTicketRequest.class)))
						.thenThrow(new NullPointerException());
		authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket(null);
	}
}