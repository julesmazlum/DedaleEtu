package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import java.time.Instant;

public class MyCollectShareDataBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = -568863390879327961L;
	
	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectShareDataBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyCollectAgent) this.myAgent).getMyMap();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyCollectAgent) this.myAgent).getListTreasureData();
		
		/* Gestion des agents */
		HashMap<String, String> agentTypes = ((MyCollectAgent) this.myAgent).getAgentTypes();
		String receiverCARTE = ((MyCollectAgent) this.myAgent).getAgentNameToSendTo();
		Tuple3<String, Location, Instant> tanker = ((MyCollectAgent) this.myAgent).getTanker();
		
		/* Gestion du message */
		List<Serializable> message = new ArrayList<>();
		
		
		/*
		 * Envoyer la carte à la personne observée
		 */
		
		//ajout de la carte en message[0]
		message.add((Serializable) myMap.getSerializableGraph());
		System.out.println(color + agentName+" : Je lui envoie donc toute la carte.");
		
		//ajout de la liste de type en message[1]
		message.add(listTreasureData);
		System.out.println(color + agentName+" : J'envoie également les données sur les ressources");
		
		//ajout de la liste des type des agents en message[2]
		message.add(agentTypes);
		System.out.println(color + agentName+" : J'envoie également le type des agents.");
		
		
		//ajout de tanker en message[3]
		if(tanker.getSecond()!=null) {
			System.out.println(color + agentName+" : J'ai la position du tanker je lui envoie : " + tanker.getSecond());
			System.out.println(color + agentName+" : J'ai le nom du tanker je lui envoie : "+ tanker.getFirst());
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
		((MyCollectAgent) this.myAgent).setAgentNameToSendTo(null);
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
