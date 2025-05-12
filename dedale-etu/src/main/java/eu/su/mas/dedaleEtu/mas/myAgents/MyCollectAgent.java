package eu.su.mas.dedaleEtu.mas.myAgents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myBehaviours.Collect.MyCollectFSMBehaviour;
import jade.core.behaviours.Behaviour;
import java.util.HashMap;


public class MyCollectAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	
	/* Gestion de la carte et ressources */
	
	private HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer,Integer>, Instant>>> listTreasureData = new HashMap<>();
	
	private String goToTres = null;
	private int capMax = -1;
	
	private MapRepresentation myMap;
	private MapRepresentation myMap2;
	private boolean isMapExplored = false;
	private String agentNameToSendTo = null;
	private int nbExplored = 0;
	
	/* Gestion des agents */
	
	private HashMap<String, String> agentTypes = new HashMap<>();
	private Tuple3<String, Location, Instant> tanker = new Tuple3<>(null, null, Instant.now());
	private int myLockPicking = -1;
	private int myStrentgh = -1;
	

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


	public void setIsMapExplored(boolean isMapExplored) {
		this.isMapExplored = isMapExplored;
	}
	
	public boolean getIsMapExplored() {
		return this.isMapExplored;
	}


	public HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer,Integer>, Instant>>> getListTreasureData() {
		return listTreasureData;
	}


	public void setListTreasureData(HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer,Integer>, Instant>>> listTreasureData) {
		this.listTreasureData = listTreasureData;
	}

	
	public HashMap<String, String> getAgentTypes() {
		return agentTypes;
	}


	public void setAgentTypes(HashMap<String, String> agentTypes) {
		this.agentTypes = agentTypes;
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


	public int getCapMax() {
		return capMax;
	}


	public void setCapMax(int capMax) {
		this.capMax = capMax;
	}


	public Tuple3<String, Location, Instant> getTanker() {
		return tanker;
	}


	public void setTanker(Tuple3<String, Location, Instant> tanker) {
		this.tanker = tanker;
	}


	public int getMyLockPicking() {
		return myLockPicking;
	}


	public void setMyLockPicking(int myLockPicking) {
		this.myLockPicking = myLockPicking;
	}


	public int getMyStrentgh() {
		return myStrentgh;
	}


	public void setMyStrentgh(int myStrentgh) {
		this.myStrentgh = myStrentgh;
	}


	public int getNbExplored() {
		return nbExplored;
	}


	public void setNbExplored(int nbExplored) {
		this.nbExplored = nbExplored;
	}
	
	
	
	
}
