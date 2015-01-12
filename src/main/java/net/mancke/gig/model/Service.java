package net.mancke.gig.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Service {
	
	private String image;
	private List<String> ports = Collections.EMPTY_LIST;
	private List<String> links = Collections.EMPTY_LIST;
	private List<String> volumes = Collections.EMPTY_LIST;
	private Map<String, String> environment = Collections.EMPTY_MAP;
	
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public List<String> getPorts() {
		return ports;
	}
	public void setPorts(List<String> ports) {
		this.ports = ports;
	}
	public List<String> getLinks() {
		return links;
	}
	public void setLinks(List<String> links) {
		this.links = links;
	}
	public List<String> getVolumes() {
		return volumes;
	}
	public void setVolumes(List<String> volumes) {
		this.volumes = volumes;
	}
	public Map<String, String> getEnvironment() {
		return environment;
	}
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
}
