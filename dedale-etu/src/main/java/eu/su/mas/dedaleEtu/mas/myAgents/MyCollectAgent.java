package eu.su.mas.dedaleEtu.mas.myAgents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myBehaviours.Collect.MyCollectFSMBehaviour;
import jade.core.behaviours.Behaviour;
import java.util.HashMap;


public class MyCollectAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	
	private String tanker = null;
	private MapRepresentation myMap;
	private MapRepresentation myMap2;
	private Location tankLoc = null;
	private boolean isMapExplored = false;
	private String agentNameToSendTo = null;
	private HashMap<String, String> agent_types = new HashMap<>();
	private Map<String, MapRepresentation> liste_agent_map = new HashMap<>();
	private HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = new HashMap<>();
	private String goToTres = null;


	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
				

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		lb.add(new MyCollectFSMBehaviour(this));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	protected void beforeMove(){
		super.beforeMove();
		//System.out.println("I migrate");
	}

	protected void afterMove(){
		super.afterMove();
		//System.out.println("I migrated");
	}
	
	public MapRepresentation getMyMap() {
		return myMap;
	}
	
	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}


	public String getAgentNameToSendTo() {
		return agentNameToSendTo;
	}

	public void setAgentNameToSendTo(String agentNameToSendTo) {
		this.agentNameToSendTo = agentNameToSendTo;
	}


	public Map<String, MapRepresentation> getListe_agent_map() {
		return liste_agent_map;
	}


	public void setListe_agent_map(Map<String, MapRepresentation> liste_agent_map) {
		this.liste_agent_map = liste_agent_map;
	}


	public Location getTankLoc() {
		return tankLoc;
	}


	public void setTankLoc(Location tankLoc) {
		this.tankLoc = tankLoc;
	}


	public void setIsMapExplored(boolean isMapExplored) {
		this.isMapExplored = isMapExplored;
	}
	
	public boolean getIsMapExplored() {
		return this.isMapExplored;
	}


	public HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> getListe_pos_ressources() {
		return liste_pos_ressources;
	}


	public void setListe_pos_ressources(HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources) {
		this.liste_pos_ressources = liste_pos_ressources;
	}


	public String getTanker() {
		return tanker;
	}


	public void setTanker(String tanker) {
		this.tanker = tanker;
	}
	
	public HashMap<String, String> getAgent_types() {
		return agent_types;
	}


	public void setAgent_types(HashMap<String, String> agent_types) {
		this.agent_types = agent_types;
	}


	public String getGoToTres() {
		return goToTres;
	}


	public void setGoToTres(String goToTres) {
		this.goToTres = goToTres;
	}


	public MapRepresentation getMyMap2() {
		return myMap2;
	}


	public void setMyMap2(MapRepresentation myMap2) {
		this.myMap2 = myMap2;
	}
	
	
	
	
	
}
