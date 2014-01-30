package navalroyal.serveur;

import java.io.IOException;

/*
 * Classe cree lors de la preparation de la grille d'une partie.
 * S'occupe d'envoyer les commandes "SHIP" au client auquel elle est
 * rattachee et attend la reponse (PUTSHIP) du client.
 * Determine l'ordre de jeu des joueurs (le plus rapide a placer jouer en 1er)
 * Lorsque tous les sous-marins du joueur sont places, on lui envoie la
 * commande "ALLYOURBASE". Si la grille est prete (ie tous les threads
 * ShipPlacement ont termines sauf celui la) on reveille la partie
 * pour que la boucle de jeu se lance.
 */
public class ShipPlacement extends Thread {
	private Partie partie;
	private ThreadClient joueur;
	
	public ShipPlacement(ThreadClient j, Partie p) {
		partie = p;
		joueur = j;
		setName("ShipPlacement du Joueur no "+joueur.getNumeroClient()+" : "+joueur.getNomClient());
	}
	public ThreadClient getJoueur() { return joueur; }
	public Partie getPartie()       { return partie; }
	
	public void run(){
		for(int i=0; i<partie.getTailleShip().length; i++){
			try {
				joueur.sendCommande("SHIP/"+partie.getTailleShip()[i]+"/");
				joueur.setLastShip(partie.getTailleShip()[i]);
				synchronized (this) {
					this.wait();
				}
				// placement de Ship recu de la part du client
				// on passe au Ship suivant
			} catch (IOException e) {
				// retenter l'envoi de la cmd en cas d'echec
				i--;
				continue;
			} catch (InterruptedException e) {}
		}
		// ajout du Joueur dans le tableau de priorite (pour savoir dans quel ordre jouent les joueurs)
		synchronized (partie) {
			partie.getPriorite().add(joueur);
		}
		try {
			joueur.sendCommande("HEYLISTEN/(serveur) Sous-marins en place, en attente des autres joueurs.");
			joueur.sendCommande("ALLYOURBASE/");
		} catch (IOException e) {}
		if(partie.getGrille().estInitialisee()){
			System.out.println("[PLACEMENT J"+joueur.getNumeroClient()+"] Grille initialisee, debut de la partie");
			synchronized (partie) {
				partie.notify();
			}
		}
		System.out.println("[PLACEMENT J"+joueur.getNumeroClient()+"] Fin thread "+getName());
	}
}
