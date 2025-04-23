package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyExploreAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import java.time.Instant;

public class MyExploreShareMapBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = -568863390879327961L;
	
	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreShareMapBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyExploreAgent) this.myAgent).getListe_pos_ressources();
		
		/* Gestion des agents */
		Map<String, MapRepresentation> liste_agent_map = ((MyExploreAgent) this.myAgent).getListe_agent_map();
		HashMap<String, String> agent_types = ((MyExploreAgent) this.myAgent).getAgent_types();
		String receiverCARTE = ((MyExploreAgent) this.myAgent).getAgentNameToSendTo();
		Location tankLoc = ((MyExploreAgent) this.myAgent).getTankLoc();
		String tanker = ((MyExploreAgent) this.myAgent).getTanker();
		
		/* Gestion des messages */
		List<Serializable> message = new ArrayList<>();

		/*
		 * Envoyer la carte à la personne observée
		 */
		
		//ajout de la carte en message[0]
		if(agent_types.get(receiverCARTE).equals("agentExplo")) {
			System.out.println(color + agentName+" : "+receiverCARTE+" est un agent explo.");
			if (!liste_agent_map.containsKey(receiverCARTE)) {
				System.out.println(color + agentName+" : "+receiverCARTE+" n'est pas dans le dictionnaire je lui envoie toute ma carte");
				liste_agent_map.put(receiverCARTE, new MapRepresentation(false));
				message.add((Serializable) myMap.getSerializableGraph());
			} else {
				System.out.println(color + agentName+" : "+receiverCARTE+" est dans le dictionnaire je lui envoie la carte depuis la dernière fois");
				MapRepresentation map = liste_agent_map.get(receiverCARTE);
				message.add((Serializable) map.getSerializableGraph());
				liste_agent_map.put(receiverCARTE, new MapRepresentation(false));
			}
		}
		if(agent_types.get(receiverCARTE).equals("agentCollect")) {
			System.out.println(color + agentName+" : "+receiverCARTE+" est un agent collect");
			message.add((Serializable) myMap.getSerializableGraph());
			System.out.println(color + agentName+" : Je lui envoie donc toute la carte");
		}
		
		//ajout de la liste de type en message[2]
		message.add(liste_pos_ressources);
		
		//message[3]
		message.add(agent_types);
		
		System.out.println(color + agentName+" : J'envoie également la positions des ressources");
		
		//ajout de tankLoc en message[3]
		if(tankLoc!=null) {
			Couple<Location, String> tank = new Couple<>(tankLoc, tanker);
			System.out.println(color + agentName+" : J'ai la position du tank je lui envoie : " + tankLoc);
			System.out.println(color + agentName+" : J'ai le nom du tank je lui envoie : "+ tanker);
			message.add(tank);
		}
		
		
		
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("DATA");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(receiverCARTE,AID.ISLOCALNAME));
		try {					
			msg.setContentObject((java.io.Serializable) message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		System.out.println(color + agentName+" : J'ai envoyé les données.");
		
		
		
		exit = 1;
		finished = true;
		((MyExploreAgent) this.myAgent).setAgentNameToSendTo(null);
		return;
		
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
    public int onEnd() {
       return exit;
    }

}
