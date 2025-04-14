package global;


public class Global {
	
	// définir les codes de couleur ansi
	public static final String RESET = "\u001B[0m";
	public static final String RED = "\u001B[38;2;255;0;0m";
	public static final String GREEN = "\u001B[38;2;19;140;12m";
	public static final String PINK = "\u001B[38;2;255;143;199m";
	public static final String PURPLE = "\u001B[35m";
	public static final String BROWN = "\u001B[33m";
	public static final String ORANGE = "\u001B[38;5;214m";
	public static final String CYAN = "\u001B[36m";
	public static final String BLACK = "\u001B[30m";
	
	public static int temps = 1000;

	// fonction pour obtenir une couleur en fonction du nom de l'agent
	public static String getColorForAgent(String agentName) {
	    switch (agentName) {
	        case "Mario": return RED;
	        case "Luigi": return GREEN;
	        case "Peach": return PINK;
	        case "Daisy": return PURPLE;
	        case "Toad": return BROWN;
	        case "Bowser": return ORANGE;
	        default: return CYAN; // couleur par défaut
	    }
	}


}
