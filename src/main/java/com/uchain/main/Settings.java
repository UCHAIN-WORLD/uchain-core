package com.uchain.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Getter
@Setter
public class Settings {
	public Settings(String config) {
		ResourceBundle resource = ResourceBundle.getBundle(config);
		this.nodeName = resource.getString("nodeName");
		this.bindAddress = resource.getString("bindAddress");
		this.knownPeers = resource.getString("knownPeers");
		this.agentName = resource.getString("agentName");
		this.maxPacketSize = resource.getString("maxPacketSize");
		this.localOnly = resource.getString("localOnly");
		this.appVersion = resource.getString("appVersion");
		this.maxConnections = resource.getString("maxConnections");
		this.connectionTimeout = resource.getString("connectionTimeout");
		this.upnpEnabled = resource.getString("upnpEnabled");
		this.handshakeTimeout = resource.getString("handshakeTimeout");
		this.controllerTimeout = resource.getString("controllerTimeout");

		this.server = resource.getString("server");
		this.updateEvery = resource.getString("updateEvery");
		this.timeout = resource.getString("timeout");

		this.chainSettings = new ChainSettings(resource.getString("chain_dbDir"), resource.getString("chain_forkDir"),
				resource.getString("chain_miner"), Long.valueOf(resource.getString("chain_genesis_timeStamp")),
				resource.getString("chain_genesis_publicKey"), resource.getString("chain_genesis_privateKey"));

		String initialWitness = resource.getString("initialWitness");
		int produceInterval = Integer.parseInt(resource.getString("produceInterval"));
		int acceptableTimeError = Integer.parseInt(resource.getString("acceptableTimeError").trim());
		int producerRepetitions = Integer.parseInt(resource.getString("producerRepetitions").trim());
		this.consensusSettings = new ConsensusSettings(getWitness(initialWitness),produceInterval,acceptableTimeError,producerRepetitions);
	}

	private String nodeName;
	private String bindAddress;
	private String knownPeers;
	private String agentName;
	private String maxPacketSize;
	private String localOnly;
	private String appVersion;
	private String maxConnections;
	private String connectionTimeout;
	private String upnpEnabled;
	private String handshakeTimeout;
	private String controllerTimeout;

	private String server;
	private String updateEvery;
	private String timeout;

	private ChainSettings chainSettings;
	private ConsensusSettings consensusSettings;

	private static List<Witness> getWitness(String json) {
		List<Witness> list = new ArrayList<Witness>();
		try {
			JSONArray jsonObject = JSONArray.fromObject(json);
			for (Iterator<?> iterator = jsonObject.iterator(); iterator.hasNext();) {
				JSONObject job = (JSONObject) iterator.next();
				Iterator<?> it = job.keys();
				Witness witness = new Witness();
				int i = 0;
				while (it.hasNext()) {
					if (i == 0) {
						witness.setName((String) job.get(it.next()));
					} else if (i == 1) {
						witness.setPubkey((String) job.get(it.next()));
					} else if (i == 2) {
						witness.setPrivkey((String) job.get(it.next()));
					}
					i++;
				}
				list.add(witness);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
