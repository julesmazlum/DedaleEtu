package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
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

public class MyExploreShareDataBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = -568863390879327961L;
	
	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreShareDataBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyExploreAgent) this.myAgent).getListTreasureData();
		
		/* Gestion des agents */
		Map<String, MapRepresentation> listAgentMap = ((MyExploreAgent) this.myAgent).getListAgentMap();
		HashMap<String, String> agentTypes = ((MyExploreAgent) this.myAgent).getAgentTypes();
		String receiverCARTE = ((MyExploreAgent) this.myAgent).getAgentNameToSendTo();
		Tuple3<String, Location, Instant> tanker = ((MyExploreAgent) this.myAgent).getTanker();
		
		/* Gestion des messages */
		List<Serializable> message = new ArrayList<>();

		/*
		 * Envoyer les donées à la personne observée
		 */
		
		//ajout de la carte en message[0]
		// Si c'est un agent explo
		if(agentTypes.get(receiverCARTE).equals("agentExplo")) {
			System.out.println(color + agentName+" : "+receiverCARTE+" est un agent explo.");
			if (!listAgentMap.containsKey(receiverCARTE)) {
				System.out.println(color + agentName+" : "+receiverCARTE+" n'est pas dans le dictionnaire je lui envoie toute ma carte.");
				listAgentMap.put(receiverCARTE, new MapRepresentation(false));
				message.add((Serializable) myMap.getSerializableGraph());
			} else {
				System.out.println(color + agentName+" : "+receiverCARTE+" est dans le dictionnaire je lui envoie la carte depuis la dernière fois.");
				MapRepresentation map = listAgentMap.get(receiverCARTE);
				message.add((Serializable) map.getSerializableGraph());
				listAgentMap.put(receiverCARTE, new MapRepresentation(false));
			}
		}
		//Si c'est un agent collect
		if(agentTypes.get(receiverCARTE).equals("agentCollect")) {
			System.out.println(color + agentName+" : "+receiverCARTE+" est un agent collect.");
			message.add((Serializable) myMap.getSerializableGraph());
			System.out.println(color + agentName+" : Je lui envoie donc toute la carte.");
		}
		
		//ajout de la des ressources en message[1]
		message.add(listTreasureData);
		System.out.println(color + agentName+" : J'envoie également la positions des ressources");
		
		//ajout de la liste de type d'agents en message[2]
		message.add(agentTypes);
		System.out.println(color + agentName+" : J'envoie également le type des agents");
		
		
		//ajout si possible de tanker en message[3]
		if(tanker.getSecond()!=null) {
			System.out.println(color + agentName+" : J'ai la position du tank je lui envoie : " + tanker.getSecond());
			message.add(tanker);
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
