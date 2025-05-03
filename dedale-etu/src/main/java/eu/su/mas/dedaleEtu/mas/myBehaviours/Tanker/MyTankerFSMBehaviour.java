package eu.su.mas.dedaleEtu.mas.myBehaviours.Tanker;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyTankerFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_TANK = "Tank";
	private static final String STATE_COM = "Communication";


    public MyTankerFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
    	SimpleBehaviour communication = new MyTankerCommunicationBehaviour(myagent);
        SimpleBehaviour tank = new MyTankerBehaviour(myagent);
        


        // ajouter les états à la machine
        registerFirstState(communication, STATE_COM);
        registerState(tank, STATE_TANK);


        // définir les transitions
        registerTransition(STATE_COM, STATE_TANK, 1);
        registerTransition(STATE_TANK, STATE_COM, 1);




    }
}