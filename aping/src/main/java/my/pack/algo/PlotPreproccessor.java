package my.pack.algo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import my.pack.model.HorseStatBean;
import my.pack.model.MarketBean;
import my.pack.util.CouchbaseHandler;

import com.betfair.aping.entities.ExchangePrices;
import com.betfair.aping.entities.PriceSize;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Extract prices from horses and generate file for GNUPlot
 * 
 * @author VLD
 * 
 */
public class PlotPreproccessor {

	private static final String MARKET_ID = "1.113191773";
	private static final CouchbaseHandler cbClient = new CouchbaseHandler(
			"horses");
	private static final ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) {
		String marketDoc = cbClient.get("m_" + MARKET_ID);
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
		ArrayList<Long> horsesId = market.getHorsesId();
		Long horseId = horsesId.get(0);
		Integer cntOfProbes = market.getCntOfProbes();
		String horseDocId = MARKET_ID + horseId + "_";
		HashMap<Double, BufferedWriter> backBw = createOutputFiles(horseDocId,
				cntOfProbes, true);
		HashMap<Double, BufferedWriter> layBw = createOutputFiles(horseDocId,
				cntOfProbes, false);
		for (int i = 0; i < cntOfProbes; i++) {
			String horseDoc = cbClient.get(horseDocId + i);
			try {
				HorseStatBean horse = om.readValue(horseDoc,
						HorseStatBean.class);
				long timestamp = horse.getTimestamp();
				ExchangePrices ex = horse.getEx();
				/**
				 * Columns: 1st - timestamp, 2nd - avb, 3rd - avl, 4th - tot
				 * mat, 5th - startPrice
				 */
				String raw = String.valueOf(timestamp) + " ";
				List<PriceSize> availableToBack = ex.getAvailableToBack();
				for (PriceSize priceSize : availableToBack) {
					BufferedWriter bw = backBw.get(priceSize.getPrice());
					bw.write(raw + priceSize.getSize());
					bw.newLine();
				}
				List<PriceSize> availableToLay = ex.getAvailableToLay();
				for (PriceSize priceSize : availableToLay) {
					BufferedWriter bw = layBw.get(priceSize.getPrice());
					bw.write(raw + priceSize.getSize());
					bw.newLine();
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			for (Entry<Double, BufferedWriter> en : backBw.entrySet()) {
				en.getValue().close();
			}
			for (Entry<Double, BufferedWriter> en : layBw.entrySet()) {
				en.getValue().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		cbClient.shutdown();
	}

	/**
	 * 
	 * @param horseDocId
	 * @param cntOfProbes
	 * @param type
	 *            - back = true, lay = false;
	 * @return
	 */
	private static HashMap<Double, BufferedWriter> createOutputFiles(
			String horseDocId, Integer cntOfProbes, boolean type) {
		// create directory
		String filePath = "C:\\bf\\" + horseDocId + "\\";
		if (type) {
			filePath += "back\\";
		} else {
			filePath += "lay\\";
		}
		new File(filePath).mkdirs();
		String horseDoc = cbClient.get(horseDocId + (cntOfProbes - 1));
		HorseStatBean horse = null;
		HashMap<Double, BufferedWriter> map = null;
		try {
			horse = om.readValue(horseDoc, HorseStatBean.class);
			ExchangePrices ex = horse.getEx();
			map = new HashMap<Double, BufferedWriter>();
			List<PriceSize> priceSizes = null;
			if (type) {
				priceSizes = ex.getAvailableToBack();
			} else {
				priceSizes = ex.getAvailableToLay();
			}
			for (PriceSize priceSize : priceSizes) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
						filePath + priceSize.getPrice())));
				map.put(priceSize.getPrice(), bw);
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

}
