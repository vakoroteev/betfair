package my.pack.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class MarketBean {

	@JsonProperty("mrktStart")
	private Long marketStartTime;
	@JsonProperty("monitorStart")
	private Long startMonitoringTime;
	@JsonProperty("monitorEnd")
	private Long endMonitoringTime;
	@JsonProperty("hid")
	private ArrayList<Long> horsesId;
	@JsonProperty("cnt")
	private Integer cntOfProbes;

	public MarketBean() {

	}

	public MarketBean(Long marketStartTime, Long startMonitoringTime,
			Long endMonitoringTime, ArrayList<Long> horsesId,
			Integer cntOfProbes) {
		super();
		this.marketStartTime = marketStartTime;
		this.startMonitoringTime = startMonitoringTime;
		this.endMonitoringTime = endMonitoringTime;
		this.horsesId = horsesId;
		this.cntOfProbes = cntOfProbes;
	}

	public Long getMarketStartTime() {
		return marketStartTime;
	}

	public void setMarketStartTime(Long marketStartTime) {
		this.marketStartTime = marketStartTime;
	}

	public Long getStartMonitoringTime() {
		return startMonitoringTime;
	}

	public void setStartMonitoringTime(Long startMonitoringTime) {
		this.startMonitoringTime = startMonitoringTime;
	}

	public Long getEndMonitoringTime() {
		return endMonitoringTime;
	}

	public void setEndMonitoringTime(Long endMonitoringTime) {
		this.endMonitoringTime = endMonitoringTime;
	}

	public ArrayList<Long> getHorsesId() {
		return horsesId;
	}

	public void setHorsesId(ArrayList<Long> horsesId) {
		this.horsesId = horsesId;
	}

	public Integer getCntOfProbes() {
		return cntOfProbes;
	}

	public void setCntOfProbes(Integer cntOfProbes) {
		this.cntOfProbes = cntOfProbes;
	}

}
