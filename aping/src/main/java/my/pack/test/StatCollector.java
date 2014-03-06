package my.pack.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import my.pack.model.HorseStatBean;
import my.pack.model.StartPrice;
import my.pack.util.AccountConstants;
import my.pack.util.ApplicationConstants;
import my.pack.util.CouchbaseConnector;
import net.spy.memcached.PersistTo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.entities.ExchangePrices;
import com.betfair.aping.entities.MarketBook;
import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.MarketFilter;
import com.betfair.aping.entities.PriceProjection;
import com.betfair.aping.entities.Runner;
import com.betfair.aping.entities.StartingPrices;
import com.betfair.aping.entities.TimeRange;
import com.betfair.aping.enums.MarketProjection;
import com.betfair.aping.enums.MarketSort;
import com.betfair.aping.enums.MatchProjection;
import com.betfair.aping.enums.OrderProjection;
import com.betfair.aping.enums.PriceData;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.HttpClientSSO;
import com.couchbase.client.CouchbaseClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StatCollector {

	private static final String MARKET_COUNTRY = "GB";

	private static final String SESSION_TOKEN = "sessionToken";

	private static final Logger log = LoggerFactory
			.getLogger(StatCollector.class);
	private static final ObjectMapper om = new ObjectMapper();
	private static final CouchbaseClient cbClient = CouchbaseConnector
			.getClient("horses");
	private static final ApiNgJsonRpcOperations rpcOperator = ApiNgJsonRpcOperations
			.getInstance();
	final static String ssoId = getSessionToken();

	public static void main(String[] args) {
		try {
			final int RUNNERS_CNT = 3;
			final String MARKET_CNT = "20";
			final int MARKET_CNT_FOR_MARKET_BOOK = 5;

			try {
				Thread.sleep(10000L);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			HashMap<String, Integer> marketCounters = new HashMap<String, Integer>();
			List<MarketCatalogue> marketCatalogues = listMarketCatalogue(
					rpcOperator, MARKET_CNT, ssoId);
			long startTime = marketCatalogues.get(0).getDescription()
					.getMarketTime().getTime();
			waitFirstMarket(startTime);
			Queue<String> allMarketIds = new LinkedList<String>();
			for (MarketCatalogue marketCatalogue : marketCatalogues) {
				String marketId = marketCatalogue.getMarketId();
				allMarketIds.add(marketId);
				if (marketCounters.get(marketId) == null) {
					marketCounters.put(marketId, 0);
				}
				// System.out.println(marketCatalogue.getMarketName());
				// System.out.println(marketCatalogue.getDescription()
				// .getMarketTime().getTime());
			}
			// Prepare params for listMarketBook
			List<String> marketIds = new ArrayList<String>();
			for (int k = 0; k < MARKET_CNT_FOR_MARKET_BOOK; k++) {
				// TODO: if poll return null???
				marketIds.add(allMarketIds.poll());
			}
			boolean observ = true;
			while (observ) {
				try {
					PriceProjection priceProjection = new PriceProjection();
					Set<PriceData> priceData = new HashSet<PriceData>();
					priceData.add(PriceData.EX_ALL_OFFERS);
					priceData.add(PriceData.EX_TRADED);
					priceData.add(PriceData.SP_AVAILABLE);
					priceData.add(PriceData.SP_TRADED);
					priceProjection.setPriceData(priceData);

					final String appKey = AccountConstants.APP_KEY;
					List<MarketBook> listMarketBook = null;
					try {
						listMarketBook = rpcOperator.listMarketBook(marketIds,
								priceProjection, OrderProjection.ALL,
								MatchProjection.NO_ROLLUP, MARKET_COUNTRY,
								appKey, ssoId);
						int activeMarketsCount = marketIds.size();
						for (MarketBook marketBook : listMarketBook) {
							if (marketBook.getStatus().equalsIgnoreCase(
									"suspended")
									|| marketBook.getStatus().equalsIgnoreCase(
											"closed")) {
								activeMarketsCount--;
							}
						}
						while (activeMarketsCount < marketIds.size()) {
							marketIds.remove(0);
							if (allMarketIds.peek() != null) {
								String poll = allMarketIds.poll();
								log.info("New makret is monitoring: {}", poll);
								marketIds.add(poll);
								activeMarketsCount++;
							} else {
								// TODO: stop observer or something else
								observ = false;
							}
						}
					} catch (APINGException e) {
						e.printStackTrace();
					}
					// for (MarketBook marketBook : listMarketBook) {
					// System.out.print(marketBook.getMarketId() + ", ");
					// }
					for (MarketBook marketBook : listMarketBook) {
						String marketId = marketBook.getMarketId();
						Integer cnt = marketCounters.get(marketId);
						String num = String.valueOf(cnt);
						marketCounters.put(marketId, ++cnt);
						List<Runner> runners = marketBook.getRunners();
						int realCntOfRunners = 0;
						if (runners.size() < RUNNERS_CNT) {
							realCntOfRunners = runners.size();
						}
						for (int i = 0; i < realCntOfRunners; i++) {
							Runner runner = runners.get(i);
							Long selectionId = runner.getSelectionId();
							Double totalMatched = runner.getTotalMatched();
							ExchangePrices ex = runner.getEx();
							StartingPrices sp = runner.getSp();
							StartPrice startPrice = null;
							if (sp != null) {
								startPrice = new StartPrice(sp.getActualSP(),
										sp.getFarPrice(), sp.getNearPrice());
							}
							// TODO: use Calendar!!!
							long timestamp = System.currentTimeMillis();
							HorseStatBean horse = new HorseStatBean(
									totalMatched, ex, startPrice, timestamp);
							try {
								String doc = om.writeValueAsString(horse);
								// TODO: think about async
								cbClient.set(
										marketId + "_" + selectionId + "_"
												+ num, doc, PersistTo.ZERO)
										.get();
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					log.error("Exception while observ: ", e);
				}
			}
		} finally {
			cbClient.shutdown();
		}
	}

	private static void waitFirstMarket(long startTime) {
		long HOUR_IN_MILLIS = 60L * 60L * 1000L;
		long timeToStart = startTime - System.currentTimeMillis();
		if (timeToStart > HOUR_IN_MILLIS) {
			long sleepTime = timeToStart - HOUR_IN_MILLIS;
			log.info("Sleeping time - {}", sleepTime);
			// keep alive every 10 seconds
			long period = 10000L;
			int cntOfKeepAlivedRequests = (int) (sleepTime / 10000L);
			while (cntOfKeepAlivedRequests > 0) {
				try {
					Thread.sleep(period);
					MarketFilter filter = new MarketFilter();
					filter.setInPlayOnly(true);
					rpcOperator.listEventTypes(filter,
							AccountConstants.APP_KEY, ssoId);
					log.info("Remain {} periods", cntOfKeepAlivedRequests);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (APINGException e) {
					log.error("Exception while keepAlived: {}", e);
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				cntOfKeepAlivedRequests--;
			}
			// working sleep
			// try {
			// Thread.sleep(timeToStart - HOUR_IN_MILLIS);
			// } catch (InterruptedException e1) {
			// e1.printStackTrace();
			// }
		}
	}

	public static List<MarketCatalogue> listMarketCatalogue(
			ApiNgJsonRpcOperations rpcOperator, String maxResult,
			String sessionToken) {
		final String ssoId = sessionToken;
		final String appKey = AccountConstants.APP_KEY;

		MarketFilter filter = new MarketFilter();
		Set<String> eventTypeIds = new HashSet<String>();
		eventTypeIds.add(ApplicationConstants.HORSE_EVENT_TYPE);
		filter.setEventTypeIds(eventTypeIds);
		Set<String> marketCountries = new HashSet<String>();
		marketCountries.add(MARKET_COUNTRY);
		filter.setMarketCountries(marketCountries);
		TimeRange time = new TimeRange();
		time.setFrom(new Date());
		filter.setMarketStartTime(time);
		Set<String> marketTypeCodes = new HashSet<String>();
		marketTypeCodes.add("WIN");
		// marketTypeCodes.add("PLACE");
		filter.setMarketTypeCodes(marketTypeCodes);
		Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
		// marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);
		marketProjection.add(MarketProjection.MARKET_DESCRIPTION);
		marketProjection.add(MarketProjection.MARKET_START_TIME);
		final String MAX_RESULT = maxResult;
		List<MarketCatalogue> listMarketCatalogue = null;
		try {
			listMarketCatalogue = rpcOperator.listMarketCatalogue(filter,
					marketProjection, MarketSort.FIRST_TO_START, MAX_RESULT,
					appKey, ssoId);
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
		// return sessionToken;
		return "3L+/wQZyysileYL7TaV4kIVGaYNzh4pJyzeJGO9EAG0=";
		// return "eVeCnQuQnR6DwWPQ5jo10P2o/z1h8UHSIT0M9nvChnM=";
		// return "OpUSjth8F6Xvk+ZMjHgp1gVoxi4NzXd33D8sYOtt70o=";
		// return "rttaPMbihBtf2Qh/II3TfjKkK1eToBY3Q3bL8Y2NoTg=";
		// return "uZlBpp+PxF1YkKVtpGHLbYoviTh3MZwQm/E9Xxm3yWI=";
	}
}
