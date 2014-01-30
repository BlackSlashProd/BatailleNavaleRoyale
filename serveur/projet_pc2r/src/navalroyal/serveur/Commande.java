package navalroyal.serveur;

import java.util.Vector;

/*
 * Classe permettant de parser et de verifier la validite d'une commande
 * recue par le serveur.
 */
public class Commande {
	
	private String cmdbrute;
	private String cmd;
	private Vector<String> args;
	private Boolean isValide;
	
	// liste des commandes que le serveur peut recevoir
	private final String [] commande = {"CONNECT","PUTSHIP","ACTION","BYE","PLAYAGAIN","TALK","REGISTER","LOGIN","SPECTATOR","SPECTATE"};
	
	public Commande(String c) {
		cmdbrute = c;
		isValide = false;
		args = new Vector<String>();
	}
	
	public String getCommande()          {return cmd;     }
	public Vector<String> getArguments() {return args;    }
	public Boolean isValide()            {return isValide;}
	
	public Boolean validate() {
		Boolean res = false;
		String[] argstmp = cmdbrute.split("/");
		cmd = argstmp[0];
		// remplissage du Vecteur des arguments
		for(int i=1; i<argstmp.length; i++){
			args.add(argstmp[i]);
		}
		// verification de la commande
		for(int i=0; i<commande.length; i++){
			if(cmd.equalsIgnoreCase(commande[i])){
				res = true;
				isValide = true;
			}
		}
		return res;
	}
	
	public String printArguments() {
		String res = "";
		for(String s: args){
			res += s.toString() + "/";
		}
		return res;
	}
}
