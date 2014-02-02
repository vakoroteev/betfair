package com.betfair.aping.enums;

public enum PriceData {
	/**
	 * Amount available for the BSP auction.
	 */
	SP_AVAILABLE,
	/**
	 * Amount traded in the BSP auction.
	 */
	SP_TRADED,
	/**
	 * Only the best prices available for each runner, to requested price depth
	 */
	EX_BEST_OFFERS,
	/**
	 * 
	 */
	EX_ALL_OFFERS,
	/**
	 * Amount traded on the exchange.
	 */
	EX_TRADED;
}
