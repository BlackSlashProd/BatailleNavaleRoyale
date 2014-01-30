package navalroyal.serveur.grille;

import java.util.Vector;

import navalroyal.serveur.Partie;

/*
 * Classe representant la grille d'une partie sur laquelle sont places
 * les sous-marins de tous les joueurs.
 */
public class Grille {
	private boolean estInitialisee;
	public final char ymin = 'A';
	public final char ymax = 'P';
	public final int xmin = 0;
	public final int xmax = 15;
	private Vector<Ship> ships; // vaisseaux de tous les joueurs
	private Partie partie;
	
	public Grille(Partie p){
		estInitialisee = false;
		ships = new Vector<Ship>();
		partie = p;
	}

	public boolean estInitialisee() {
		if(!estInitialisee){
			boolean res = false;
			if(ships.size() == partie.getNbJoueurs() * partie.getTailleShip().length){
				for(Ship s : ships){
					if(s.estPret()){
						res = true;
					}else{
						res = false;
						break;
					}
				}
			}
			estInitialisee = res;
		}else{
			// grille deja initialisee (prete pour jouer)
			return true;
		}
		return estInitialisee;
	}
	public boolean coordValide(char y)                {return (y >= ymin && y <= ymax);}
	public boolean coordValide(int x)                 {return (x >= xmin && x <= xmax);}
	public synchronized void addShip(Ship s)          {ships.add(s);                   }
	public synchronized void addShips(Vector<Ship> s) {ships.addAll(s);                }
	public synchronized Vector<Ship> getShips()       {return ships;                   }
}
