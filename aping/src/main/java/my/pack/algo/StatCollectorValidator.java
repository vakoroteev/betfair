package my.pack.algo;

import java.io.IOException;

import my.pack.model.HorseStatBean;
import my.pack.model.MarketBean;
import my.pack.util.CouchbaseHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StatCollectorValidator {

	private static final CouchbaseHandler cbClient = new CouchbaseHandler(
			"horses");

	private static final Logger log = LoggerFactory
			.getLogger(StatCollectorValidator.class);

	private static final ObjectMapper om = new ObjectMapper();

	private static final String DES_DOC = "des1";
	private static final String VIEW_NAME = "getAllMarkets";

	private static final long DELTA = 10000L;

	public static void main(String[] args) {
		// Paginator scroll = cbClient.executeView(false, DES_DOC, VIEW_NAME);
		// while (scroll.hasNext()) {
		// ViewResponse resp = scroll.next();
		// for (ViewRow viewRow : resp) {
		// }
		// }
		validateMarket("m_1.113193494");
	}

	// WARNING: check only 1st horse
	private static void validateMarket(String marketDocId) {
		String marketDoc = cbClient.get(marketDocId);
		MarketBean market = null;
		try {
			market = om.readValue(marketDoc, MarketBean.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		market.getMarketStartTime();
		market.getEndMonitoringTime();
		if (market.getMarketStartTime().longValue() < market
				.getEndMonitoringTime().longValue()) {
			market.setValidated("OK");
		} else {
			market.setValidated("WARN");
		}
		Long horseId = market.getHorsesId().get(0);
		int cntOfProbes = market.getCntOfProbes();
		String monitoredDocId = marketDocId.substring(2) + "_" + horseId + "_";
		Long timestampPrev = null;
		Long timestampNext = null;
		String horseDoc = cbClient.get(monitoredDocId + "0");
		if (horseDoc != null) {
			try {
				HorseStatBean horse = om.readValue(horseDoc,
						HorseStatBean.class);
				timestampNext = timestampPrev = horse.getTimestamp();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// TODO: less or less-equal?
		long delta = 0;
		for (int i = 1; i < cntOfProbes; i++) {
			try {
				horseDoc = cbClient.get(monitoredDocId + cntOfProbes);
				if (horseDoc != null) {
					HorseStatBean horse = om.readValue(horseDoc,
							HorseStatBean.class);
					timestampNext = horse.getTimestamp();
					if (timestampNext - timestampPrev > DELTA) {
						delta += (timestampNext - timestampPrev);
						System.out.println();
						log.info("{} - {}", timestampNext, timestampPrev);
					}
					timestampPrev = timestampNext;
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				log.error("Excetpiont: {}", e);
			}
		}
		market.setMissedTime(delta);
		try {
			cbClient.set(marketDocId, om.writeValueAsString(market));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		cbClient.shutdown();
	}
}
