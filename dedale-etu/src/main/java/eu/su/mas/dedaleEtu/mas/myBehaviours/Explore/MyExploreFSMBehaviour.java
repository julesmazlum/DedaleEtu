package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyExploreFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_HOME = "Home";
	private static final String STATE_COMMUNICATION = "Communication";
	private static final String STATE_OBSERVE = "Observe";
	private static final String STATE_SEND = "Send";


    public MyExploreFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
        SimpleBehaviour home = new MyExploreExploreBehaviour(myagent);
        SimpleBehaviour communication = new MyExploreCommunicationBehaviour(myagent);
        SimpleBehaviour observe = new MyExploreObserveBehaviour(myagent);
        SimpleBehaviour send = new MyExploreShareDataBehaviour(myagent);

        // ajouter les états à la machine
        registerFirstState(home, STATE_HOME);
        registerState(communication, STATE_COMMUNICATION);
        registerState(observe, STATE_OBSERVE);
        registerState(send, STATE_SEND);

        // définir les transitions
        registerTransition(STATE_HOME, STATE_OBSERVE, 1);
        registerTransition(STATE_OBSERVE, STATE_COMMUNICATION, 1);
        registerTransition(STATE_OBSERVE, STATE_SEND, 2);
        registerTransition(STATE_COMMUNICATION, STATE_HOME, 1);
        registerTransition(STATE_SEND, STATE_COMMUNICATION, 1);

    }
}