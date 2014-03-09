package my.pack.algo;

import my.pack.util.CouchbaseConnector;

import com.couchbase.client.CouchbaseClient;

/**
 * Extract prices from horses and generate file for GNUPlot
 * 
 * @author VLD
 * 
 */
public class PlotPreproccessor {
	private static final CouchbaseClient cbClient = CouchbaseConnector
			.getClient("horses");

	public static void main(String[] args) {
		
	}
	
}
