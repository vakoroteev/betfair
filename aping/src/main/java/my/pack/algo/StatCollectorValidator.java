package my.pack.algo;

import my.pack.util.CouchbaseConnector;

import com.couchbase.client.CouchbaseClient;

//TODO: create view - emit all markets
//TODO: create view - emit all non validate markets
//TODO: create view - emit markets by time range
//TODO: create view - emit all non processed markets
public class StatCollectorValidator {

	private static final CouchbaseClient cbClient = CouchbaseConnector
			.getClient("horses");

	public static void main(String[] args) {
		// get all non-validating markets
		// start validating
		
	}
	
	private static void validateMarket(String marketId) {
		// TODO: get all documents or only one horse?
		// check if diff(timeStamp) < 10s
	}

}
