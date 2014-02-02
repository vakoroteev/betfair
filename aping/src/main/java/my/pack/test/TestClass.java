package my.pack.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import my.pack.util.AccountConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.entities.ExchangePrices;
import com.betfair.aping.entities.MarketBook;
import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.MarketFilter;
import com.betfair.aping.entities.PriceProjection;
import com.betfair.aping.entities.PriceSize;
import com.betfair.aping.entities.Runner;
import com.betfair.aping.entities.RunnerCatalog;
import com.betfair.aping.entities.TimeRange;
import com.betfair.aping.enums.MarketProjection;
import com.betfair.aping.enums.MarketSort;
import com.betfair.aping.enums.MatchProjection;
import com.betfair.aping.enums.OrderProjection;
import com.betfair.aping.enums.PriceData;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.HttpClientSSO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestClass {

	private static final String SESSION_TOKEN = "sessionToken";

	private static final Logger log = LoggerFactory.getLogger(TestClass.class);
	private static final ObjectMapper om = new ObjectMapper();

	// TODO: Added documentation to different Projections
	public static void main(String[] args) throws JsonProcessingException,
			IOException, APINGException {
		String sessionToken = getSessionToken();
		final String applicationKey = AccountConstants.APP_KEY;
		String maxResult = "1";

		Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
		marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);

		MarketFilter marketFilter = new MarketFilter();

		TimeRange time = new TimeRange();
		time.setFrom(new Date());

		Set<String> countries = new HashSet<String>();
		countries.add("GB");

		Set<String> typesCode = new HashSet<String>();
		typesCode.add("WIN");

		Set<String> eventTypeIds = new HashSet<String>();
		eventTypeIds.add("7");

		marketFilter = new MarketFilter();
		marketFilter.setEventTypeIds(eventTypeIds);
		marketFilter.setMarketStartTime(time);
		marketFilter.setMarketCountries(countries);
		marketFilter.setMarketTypeCodes(typesCode);

		ApiNgJsonRpcOperations connector = ApiNgJsonRpcOperations.getInstance();

		List<MarketCatalogue> listMarketCatalogue = connector
				.listMarketCatalogue(marketFilter, marketProjection,
						MarketSort.LAST_TO_START, maxResult, applicationKey,
						sessionToken);
		MarketCatalogue marketCatalogue = listMarketCatalogue.get(0);
		String marketId = marketCatalogue.getMarketId();
		List<RunnerCatalog> runners = marketCatalogue.getRunners();
		HashMap<Long, String> runnersIdName = new HashMap<Long, String>();

		for (RunnerCatalog runnerCatalog : runners) {
			runnersIdName.put(runnerCatalog.getSelectionId(),
					runnerCatalog.getRunnerName());
		}

		System.out.println("Market name: " + listMarketCatalogue);

		List<String> marketIds = new ArrayList<String>();
		marketIds.add(marketId);

		PriceProjection priceProjection = new PriceProjection();
		Set<PriceData> priceData = new HashSet<PriceData>();
		priceData.add(PriceData.EX_ALL_OFFERS);
		priceProjection.setPriceData(priceData);
		List<MarketBook> listMarketBook = connector.listMarketBook(marketIds,
				priceProjection, OrderProjection.ALL,
				MatchProjection.NO_ROLLUP, "US", applicationKey, sessionToken);
		MarketBook marketBook = listMarketBook.get(0);
		System.out.println("Market Book: " + marketBook);
		Runner runner = marketBook.getRunners().get(0);
		ExchangePrices ex = runner.getEx();
		List<PriceSize> availableToBack = ex.getAvailableToBack();
		for (PriceSize priceSize : availableToBack) {
			System.out.println(priceSize.getPrice() + " - "
					+ priceSize.getSize() + ";");
		}
		List<PriceSize> availableToLay = ex.getAvailableToLay();
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
