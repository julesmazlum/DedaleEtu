package eu.su.mas.dedaleEtu.mas.myBehaviours.Tanker;

import java.io.IOException;
import java.util.ArrayList;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.myAgents.MyTankerAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;



public class MyTankerCommunicationBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyTankerCommunicationBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	public void action() {
		
		/* Affichage */
		String agentName = ((MyTankerAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		
		/* Réception demande de type */
		MessageTemplate msgASKTYPE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ASK-TYPE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedTYPE=this.myAgent.receive(msgASKTYPE);
		//S'il y a bien qlq demandant le type
		if (msgReceivedTYPE!=null) {
			String asker=null;
			try {
				asker = (String) msgReceivedTYPE.getContentObject();
				System.out.println(color + agentName+ " : "+asker + " me demande mon type.");
				
				//Envoie du type à asker
				ACLMessage msgREPLYTYPE = new ACLMessage(ACLMessage.INFORM);
				msgREPLYTYPE.setProtocol("REPLY-TYPE");
				msgREPLYTYPE.setSender(this.myAgent.getAID());
				String receiver = asker;
				msgREPLYTYPE.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				
				ArrayList<String> sltype = new ArrayList<>();
				sltype.add(agentName);
				sltype.add("agentTanker");
				
				try {					
					msgREPLYTYPE.setContentObject(sltype);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msgREPLYTYPE);
				System.out.println(color + agentName+ " : Je lui envoie mon type : "+sltype);
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		
		/* Réception demande de me déplacer */
		MessageTemplate msgMOVE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedMOVE=this.myAgent.receive(msgMOVE);
		if (msgReceivedMOVE!=null) {
			Location posSender = null;
			
			try {
				posSender = (Location) msgReceivedMOVE.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			System.out.println(color + agentName + " : Je dois me déplacer et pas en "+posSender.toString());
			((MyTankerAgent) this.myAgent).setPosSender(posSender);
		}
		
		
		exit = 1;
		finished = true;
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
