package my.pack.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import my.pack.util.AccountConstants;
import my.pack.util.ApplicationConstants;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoClass {

	private static final String SESSION_TOKEN = "sessionToken";

	private static final Logger log = LoggerFactory.getLogger(DemoClass.class);
	private static final ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) {
		final int RUNNERS_CNT = 3;
		ApiNgJsonRpcOperations rpcOperator = ApiNgJsonRpcOperations
				.getInstance();

		List<MarketCatalogue> marketCatalogues = demoListMarketCatalogue(rpcOperator);

		MarketCatalogue marketCatalogue = marketCatalogues.get(0);
		List<RunnerCatalog> runnersCatalog = marketCatalogue.getRunners();
		HashMap<Long, String> runnersIdNames = new HashMap<Long, String>();
		for (int i = 0; i < RUNNERS_CNT; i++) {
			runnersIdNames.put(runnersCatalog.get(i).getSelectionId(),
					runnersCatalog.get(i).getRunnerName());
		}
		System.out.println(marketCatalogue.getMarketName() + ": "
				+ marketCatalogue.getEvent().getName());
		String marketId = marketCatalogue.getMarketId();

		// Prepare params for listMarketBook
		List<String> marketIds = new ArrayList<String>();
		marketIds.add(marketId);

		PriceProjection priceProjection = new PriceProjection();
		Set<PriceData> priceData = new HashSet<PriceData>();
		priceData.add(PriceData.EX_BEST_OFFERS);
		// priceData.add(PriceData.EX_ALL_OFFERS);
		priceProjection.setPriceData(priceData);

		final String ssoId = getSessionToken();
		final String appKey = AccountConstants.APP_KEY;
		List<MarketBook> listMarketBook = null;
		try {
			listMarketBook = rpcOperator.listMarketBook(marketIds,
					priceProjection, OrderProjection.ALL,
					MatchProjection.NO_ROLLUP, "UK", appKey, ssoId);
		} catch (APINGException e) {
			e.printStackTrace();
		}
		MarketBook marketBook = listMarketBook.get(0);
		List<Runner> runners = marketBook.getRunners();
		Runner runner = runners.get(0);
		System.out.println("Runner Name: "
				+ runnersIdNames.get(runner.getSelectionId()));
		ExchangePrices ex = runner.getEx();
		List<PriceSize> availableToBack = ex.getAvailableToBack();
		System.out.println();
		System.out.println("Available to back");
		for (PriceSize priceSize : availableToBack) {
			System.out.println(priceSize.getPrice() + ": "
					+ priceSize.getSize());
		}
		List<PriceSize> availableToLay = ex.getAvailableToLay();
		System.out.println();
		System.out.println("Available to lay");
		for (PriceSize priceSize : availableToLay) {
			System.out.println(priceSize.getPrice() + ": "
					+ priceSize.getSize());
		}
		List<PriceSize> tradedVolume = ex.getTradedVolume();
		System.out.println();
		System.out.println("Traded volume");
		for (PriceSize priceSize : tradedVolume) {
			System.out.println(priceSize.getPrice() + ": "
					+ priceSize.getSize());
		}

		System.out.println("Total matched: " + runner.getTotalMatched());
	}

	public static List<MarketCatalogue> demoListMarketCatalogue(
			ApiNgJsonRpcOperations rpcOperator) {
		final String ssoId = getSessionToken();
		final String appKey = AccountConstants.APP_KEY;

		MarketFilter filter = new MarketFilter();
		Set<String> eventTypeIds = new HashSet<String>();
		eventTypeIds.add(ApplicationConstants.HORSE_EVENT_TYPE);
		filter.setEventTypeIds(eventTypeIds);
		TimeRange time = new TimeRange();
		time.setFrom(new Date());
		filter.setMarketStartTime(time);
		Set<String> marketTypeCodes = new HashSet<String>();
		marketTypeCodes.add("WIN");
		// marketTypeCodes.add("PLACE");
		filter.setMarketTypeCodes(marketTypeCodes);
		Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
		marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);
		marketProjection.add(MarketProjection.EVENT);

		final String MAX_RESULT = "1";
		List<MarketCatalogue> listMarketCatalogue = null;
		try {
			listMarketCatalogue = rpcOperator.listMarketCatalogue(filter,
					marketProjection, MarketSort.FIRST_TO_START, MAX_RESULT,
					appKey, ssoId);
			System.out.println(listMarketCatalogue);
		} catch (APINGException e) {
			e.printStackTrace();
		}
		return listMarketCatalogue;
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
