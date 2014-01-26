package my.pack.test;

import java.io.IOException;

import my.pack.util.AccountConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.betfair.aping.ApiNGJsonRpcDemo;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.HttpClientSSO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestClass {

	private static final String SESSION_TOKEN = "sessionToken";

	private static final Logger log = LoggerFactory.getLogger(TestClass.class);
	private static final ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) throws JsonProcessingException, IOException, APINGException {
		String sessionToken = getSessionToken();
		final String applicationKey = AccountConstants.APP_KEY;
		ApiNGJsonRpcDemo jsonRpcDemo = new ApiNGJsonRpcDemo();
        jsonRpcDemo.start(applicationKey, sessionToken);
		
	}

	// TODO: how to process wrong session token - exception or errorCode
	private static String getSessionToken() {
		JsonNode jsonNode = null;
		String sessionToken = null;
		try {
			String sessionResponse = null;
			if ((sessionResponse = HttpClientSSO.getSessionTokenResponse()) != null) {
				jsonNode = om.readTree(sessionResponse);
				sessionToken = jsonNode.get(SESSION_TOKEN).toString();
				log.info("Session token: {}", sessionToken);
			} else {
				log.error("Getting null session token from BetFair");
			}
		} catch (IOException e) {
			log.error("Exception while processing session token: {}", e);
		}
		return sessionToken;
	}
}
