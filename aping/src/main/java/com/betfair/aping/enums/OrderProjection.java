package com.betfair.aping.enums;

public enum OrderProjection {
	/**
	 * EXECUTABLE and EXECUTION_COMPLETE orders
	 */
	ALL,
	/**
	 * An order that has a remaining unmatched portion
	 */
	EXECUTABLE,
	/**
	 * An order that does not have any remaining unmatched portion
	 */
	EXECUTION_COMPLETE;
}
