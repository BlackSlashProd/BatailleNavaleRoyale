package navalroyal.serveur.grille;

import navalroyal.serveur.ThreadClient;

/*
 * Classe representant le drone d'un joueur
 */

public class Drone implements ICoordonnees{
	private int x;
	private char y;
	private ThreadClient joueur;
	
	public Drone(){}
	
	public void setCoords(int x, char y){
		setX(x);
		setY(y);
	}
	public int getX()                     {return x;     }
	public void setX(int x)               {this.x = x;   }
	public char getY()                    {return y;     }
	public void setY(char y)              {this.y = y;   }
	public ThreadClient getJoueur()       {return joueur;}
	public void setJoueur(ThreadClient j) {joueur = j;   }
}