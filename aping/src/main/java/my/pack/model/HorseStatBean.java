package my.pack.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HorseStatBean {

	private Long timeStamp;
	private Long startedTimeStamp;
	private double startedPrise;
	private double lastTradedVolume;
	private Map<String, Double> avalToBack;
	private Map<String, Double> avalToLay;

	public HorseStatBean() {
	}

	public HorseStatBean(Long timeStamp, Long startedTimeStamp,
			double startedPrise, double lastTradedVolume,
			Map<String, Double> avalToBack, Map<String, Double> avalToLay) {
		this.timeStamp = timeStamp;
		this.startedTimeStamp = startedTimeStamp;
		this.startedPrise = startedPrise;
		this.lastTradedVolume = lastTradedVolume;
		this.avalToBack = avalToBack;
		this.avalToLay = avalToLay;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Long getStartedTimeStamp() {
		return startedTimeStamp;
	}

	public void setStartedTimeStamp(Long startedTimeStamp) {
		this.startedTimeStamp = startedTimeStamp;
	}

	public double getStartedPrise() {
		return startedPrise;
	}

	public void setStartedPrise(double startedPrise) {
		this.startedPrise = startedPrise;
	}

	public double getLastTradedVolume() {
		return lastTradedVolume;
	}

	public void setLastTradedVolume(double lastTradedVolume) {
		this.lastTradedVolume = lastTradedVolume;
	}

	public Map<String, Double> getAvalToBack() {
		return avalToBack;
	}

	public void setAvalToBack(Map<String, Double> avalToBack) {
		this.avalToBack = avalToBack;
	}

	public Map<String, Double> getAvalToLay() {
		return avalToLay;
	}

	public void setAvalToLay(Map<String, Double> avalToLay) {
		this.avalToLay = avalToLay;
	}

	@Override
	public String toString() {
		return "HorseStatBean [timeStamp=" + timeStamp + ", startedTimeStamp="
				+ startedTimeStamp + ", startedPrise=" + startedPrise
				+ ", lastTradedVolume=" + lastTradedVolume + ", avalToBack="
				+ avalToBack + ", avalToLay=" + avalToLay + "]";
	}

}
