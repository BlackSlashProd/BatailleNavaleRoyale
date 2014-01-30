package navalroyal.serveur.grille;

import java.util.Vector;

import navalroyal.serveur.ThreadClient;

/*
 * Classe representant un sous-marin (constitue de parties ou "ShipPart")
 * d'un joueur.
 */
public class Ship {
	private Vector<ShipPart> parts;
	private boolean estPret;
	private ThreadClient joueur;

	public Ship(ThreadClient j) {
		parts = new Vector<ShipPart>();
		estPret = false;
		joueur = j;
	}
	
	public void addPart(int x, char y)   {parts.add(new ShipPart(x,y));}
	public void addPart(ShipPart sp)     {parts.add(sp);               }
	public Vector<ShipPart> getParts()   {return parts;                }
	public int getTaille()               {return parts.size();         }
	public boolean estPret()             {return estPret;              }
	public void estPret(boolean b)       {estPret = b;                 }
	public ThreadClient getProprietaire(){return joueur;               }
	
	public boolean estDetruit(){
		boolean res = false;
		for(ShipPart p: parts){
			if(!p.estDetruit()){
				return false;
			}else{
				res = true;
			}
		}
		return res;
	}
}
