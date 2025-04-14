package eu.su.mas.dedaleEtu.mas.myBehaviours.Tanker;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyTankerFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_EXPLORE = "Explore";
	private static final String STATE_COM = "Communication";


    public MyTankerFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
    	SimpleBehaviour communication = new MyTankerCommunicationBehaviour(myagent);
        SimpleBehaviour explore = new MyTankerBehaviour(myagent);
        


        // ajouter les états à la machine
        registerFirstState(communication, STATE_COM);
        registerState(explore, STATE_EXPLORE);


        // définir les transitions
        registerTransition(STATE_COM, STATE_EXPLORE, 1);
        registerTransition(STATE_EXPLORE, STATE_COM, 1);




    }
}