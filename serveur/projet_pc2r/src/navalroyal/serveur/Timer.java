package navalroyal.serveur;

import java.io.IOException;

/*
 * Classe utilisee lors du lancement d'une partie. La partie est lancee avec
 * Partie.minjoueurs et le Timer attend pendant 30 secondes l'arrivee de
 * nouveaux joueurs dans la partie. A la fin du Timer, la partie reprend
 */
public class Timer extends Thread {
	private int tempsRestant;
	private Partie partie;
	
	public Timer(int t, Partie p){
		tempsRestant = t;
		partie = p;
	}
	
	private void broadcast(String cmd){
		for(ThreadClient tc : partie.getJoueurs()){
			try {
				tc.sendCommande("HEYLISTEN/(serveur) " + cmd);
			} catch (IOException e) {
				System.out.println("[CLIENT "+tc.getNumeroClient()+"] Erreur IO pendant le timer, on retire le Client du serveur");
				partie.deconnecteJoueur(tc);
			}
		}
		for(ThreadClient tc : partie.getSpectateurs()){
			try {
				tc.sendCommande("HEYLISTEN/(serveur) " + cmd);
			} catch (IOException e) {
				System.out.println("[CLIENT "+tc.getNumeroClient()+"] Erreur IO pendant le timer, on retire le Client du serveur");
				partie.deconnecteJoueur(tc);
			}
		}
	}
	
	public void run(){
		broadcast("En attente d'autres joueurs.");
		while(tempsRestant > 0){
			if(partie.getNbJoueurs() >= partie.getMaxJoueurs()){
				// la partie a atteint maxjoueurs, elle peut commencer
				break;
			}
			if(tempsRestant%10 == 0 || tempsRestant < 4){
				broadcast("Debut de la partie dans "+tempsRestant+" secondes");
			}
			try {
				System.out.println("[PARTIE "+partie.getNumeroPartie()+"] Partie en attente de "+(partie.getMaxJoueurs()-partie.getNbJoueurs())+" joueur(s) supplementaire(s). ("+tempsRestant+" sec restantes)");
				sleep(1000);
				tempsRestant--;
			} catch (Exception e) {
			}
		}
		System.out.println("[PARTIE "+partie.getNumeroPartie()+"] Fin du timer");
		synchronized (partie) {
			partie.notify();
		}
	}
}
