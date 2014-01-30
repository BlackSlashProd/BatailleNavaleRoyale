package navalroyal.serveur;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

import navalroyal.serveur.grille.Grille;
import navalroyal.serveur.persistant.StatsJoueur;

/*
 * Classe representant une partie.
 */
public class Partie extends Thread {
	private Vector<ThreadClient> joueurs;
	private Vector<ThreadClient> aQuiDeJouer;
	private Vector<ThreadClient> spectateurs;
	private boolean enCours;
	private int numeroPartie;
	private boolean timerEnCours;
	private final int maxjoueurs;
	private final int minjoueurs;
	private Grille grille;
	private Vector<ShipPlacement> threadsPlacement;
	private int[] tailleShip;
	private ThreadClient gagnant;
	private Serveur serveur;
	private Vector<String> historique;
	
	public Partie(int n, Serveur s) {
		numeroPartie = n;
		serveur = s;
		joueurs = new Vector<ThreadClient>();
		aQuiDeJouer = new Vector<ThreadClient>();
		spectateurs = new Vector<ThreadClient>();
		enCours = false;
		timerEnCours = false;
		threadsPlacement = new Vector<ShipPlacement>();
		setName("Partie no "+numeroPartie);
		gagnant = null;
		historique = new Vector<String>();
		minjoueurs = s.MINJOUEURS;
		maxjoueurs = s.MAXJOUEURS;
		if(serveur.DEBUG){
			tailleShip = new int[1];
			tailleShip[0] = 1;
		}else{
			tailleShip = s.SHIPS;
		}
	}
	
	public synchronized Boolean addJoueur(ThreadClient tc){
		if(!enCours){
			if(joueurs.size() >= maxjoueurs){
				System.out.println("[PARTIE "+numeroPartie+"] ERREUR : Partie deja pleine !");
				return false;
			}else{
				joueurs.add(tc);
				return true;
			}
		}else{
			System.out.println("[PARTIE "+numeroPartie+"] ERREUR : Impossible d'ajouter un joueur, la partie est en cours !");
			return false;
		}
	}
	public synchronized void removeJoueur(ThreadClient tc){
		joueurs.removeElement(tc);
		try{
			aQuiDeJouer.removeElement(tc);
		}catch(NullPointerException n){}
	}
	public synchronized void addSpectateur(ThreadClient ts) {
		spectateurs.add(ts);
	}
	public synchronized void removeSpectateur(ThreadClient ts) {
		spectateurs.remove(ts);
	}
	
	public boolean isEnCours()                          { return enCours;       }
	public int getNumeroPartie()                        { return numeroPartie;  }
	public int[] getTailleShip()                        { return tailleShip;    }
	public Vector<ThreadClient> getJoueurs()            { return joueurs;       }
	public Vector<ThreadClient> getPriorite()           { return aQuiDeJouer;   }
	public int getNbJoueurs()                           { return joueurs.size();}
	public int getMinJoueurs()                          { return minjoueurs;    }
	public int getMaxJoueurs()                          { return maxjoueurs;    }
	public Grille getGrille()                           { return grille;        }
	public synchronized Vector<String> getHistorique()  { return historique;    }
	public Vector<ThreadClient> getSpectateurs()        { return spectateurs;   }
	public synchronized void addToHistorique(String cmd){historique.add(cmd);   }
	
	public void run() {
		// la partie est lancee avec minjoueurs
		// attente de nouveaux joueurs pendant 30 secondes ou jusqu'a maxjoueurs
		Timer timer;
		if(serveur.DEBUG){
			timer = new Timer(5, this);
		}else{
			timer = new Timer(30, this);
		}
		timerEnCours = true;
		timer.start();
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// on n'accepte plus de nouveaux joueurs dans cette partie
		timerEnCours = false;
		enCours = true;
		
		while(enCours){
			if(joueurs.size() >= minjoueurs){ // si des clients se deconnectent pendant le timer
				/* 
				 * Debut partie :
				 */
				System.out.println("[PARTIE "+numeroPartie+"] Debut de la partie avec "+joueurs.size()+" joueurs.");
				// Broadcast les joueurs : envoie de la commande PLAYERS/ avec la liste des joueurs.
				String cmd = "/";
				for(ThreadClient pl: joueurs){
					if(pl.estConnecte()){
						cmd += pl.getNomClient()+"/";
					}else{
						// le joueur n'est pas connecte (erreur IO) !
						joueurs.removeElement(pl);
					}
				}
				for(ThreadClient pl: joueurs){
					try {
						pl.sendCommande("PLAYERS"+cmd);
					} catch (IOException e) {
						System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+pl.getNomClient()+" est deconnecte. On le retire de la partie.");
						deconnecteJoueur(pl);
					}
				}
				for(ThreadClient pl: spectateurs){
					try {
						pl.sendCommande("PLAYERS"+cmd);
					} catch (IOException e) {
						System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+pl.getNomClient()+" est deconnecte. On le retire de la partie.");
						deconnecteJoueur(pl);
					}
				}
				
				/*
				 * Creation de la grille de jeu
				 */
				grille = new Grille(this);
				/*
				 * Preparatifs : placement des vaisseaux.
				 */
				for(ThreadClient pl: joueurs){
					ShipPlacement sp = new ShipPlacement(pl, this);
					threadsPlacement.add(sp);
					sp.start();
				}
				synchronized (this) {
					// on attend que la grille soit initialisee avec les Ships des joueurs
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				// initialisation du Drone de chaque joueur avec des coordonnees aleatoires
				for(ThreadClient pl: joueurs){
					pl.initDrone();
				}
				ThreadClient joueurCourrant = aQuiDeJouer.firstElement();
				System.out.println("[PARTIE "+numeroPartie+"] Le Joueur no "+joueurCourrant.getNumeroClient()+" ("+joueurCourrant.getNomClient()+") commence la partie.");
				/* 
				 * Deroulement partie
				 */
				while(!finDePartie()){
					try{
						for(ThreadClient joueur : aQuiDeJouer){
							joueurCourrant = joueur;
							if(joueur.estVivant()){
								joueur.sendCommande("YOURTURN/"+joueur.getDrone().getX()+"/"+joueur.getDrone().getY()+"/"+joueur.getNbActionsPossibles()+"/");
								// envoie aux specateurs de la position actuelle du drone du joueur
								for(ThreadClient spec : spectateurs){
									try{
										spec.sendCommande("PLAYERMOVE/"+joueur.getNomClient()+"/"+joueur.getDrone().getX()+"/"+joueur.getDrone().getY()+"/");
									}catch(IOException io){}
								}
								synchronized (this) {
									// on attend que le joueur joue son tour (commande ACTION)
									joueur.aMonTourDeJouer(true);
									wait();
									joueur.aMonTourDeJouer(false);
								}
							}
						}
					}catch(IOException e){
						System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+joueurCourrant.getNomClient()+" est deconnecte. On le retire de la partie.");
						deconnecteJoueur(joueurCourrant);
					}catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				/*
				 *  Fin partie :
				 *  A la fin de la partie on envoie donc le message AWINNERIS ou DRAWGAME.
				 *  A ce moment la partie est supprimee, les joueurs restent "estConnecte" et "estEnJeu".
				 *  Si le joueur envoie BYE, le thread client sera detruit. 
				 *  Si il envoie PLAYAGAIN, "estEnJeu" sera mis a false et il sera a nouveau elligible.
				 */
				System.out.println("[PARTIE "+numeroPartie+"] La partie se termine");
				// Broadcast les joueurs.
				if(gagnant == null){
					// DrawGame :(
					for(ThreadClient j: joueurs){
						try {
							j.sendCommande("DRAWGAME/");
						} catch (IOException e) {
							System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+j.getNomClient()+" est deconnecte. On le retire de la partie.");
							deconnecteJoueur(j);
						}
					}
					for(ThreadClient j: spectateurs){
						try {
							j.sendCommande("DRAWGAME/");
						} catch (IOException e) {
							System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+j.getNomClient()+" est deconnecte. On le retire de la partie.");
							deconnecteJoueur(j);
						}
					}
				}else{
					// We got a Winner!
					for(ThreadClient j: joueurs){
						try {
							j.sendCommande("AWINNERIS/"+gagnant.getNomClient()+"/");
						} catch (IOException e) {
							System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+j.getNomClient()+" est deconnecte. On le retire de la partie.");
							deconnecteJoueur(j);
						}
					}
					for(ThreadClient j: spectateurs){
						try {
							j.sendCommande("AWINNERIS/"+gagnant.getNomClient()+"/");
						} catch (IOException e) {
							System.out.println("[PARTIE "+numeroPartie+"] ERREUR : le joueur "+j.getNomClient()+" est deconnecte. On le retire de la partie.");
							deconnecteJoueur(j);
						}
					}
					// mise a jour des statistiques de victoire/defaite des joueurs enregistres
					for(ThreadClient j: joueurs){
						StatsJoueur s = j.getStatistiques();
						if(s != null){
							if(gagnant == j){
								s.incrementeNbVictoires();
							}else{
								s.incrementeNbDefaites();
							}
							// enregistrement des statistiques du joueur
							try {
								FileOutputStream fichier = new FileOutputStream(j.getPath()+j.getNomClient()+".ser");
								ObjectOutputStream oos = new ObjectOutputStream(fichier);
								oos.writeObject(s);
								oos.flush();
								oos.close();
							}catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			enCours = false;
			joueurs.removeAllElements();
			aQuiDeJouer.removeAllElements();
			spectateurs.removeAllElements();
			historique.removeAllElements();
			gagnant = null;
			serveur.getParties().removeElement(this);
			System.out.println("[PARTIE "+numeroPartie+"] Fin du Thread");
			/*
			 * j'ai choisi de ne pas attendre la reponse (BYE/PLAYAGAIN) du joueur mais de mettre
			 * fin a la partie des qu'elle est finie. Les joueurs qui envoient BYE (ou qui ferment
			 * leur client) sont deconnectes du serveur, tandis que ceux qui envoient PLAYAGAIN
			 * sont ajoutes a une nouvelle partie.
			 */
		}
	}

	private synchronized boolean finDePartie() {
		boolean res = true;
		if(joueurs.size() == 1){
			// le joueur est seul dans la partie !
			gagnant = joueurs.firstElement();
			return true;
		}
		Vector<ThreadClient> deads = new Vector<ThreadClient>();
		for(ThreadClient j : aQuiDeJouer){
			if(!j.estVivant()){
				deads.add(j);
				for(ThreadClient j2 : joueurs){
					try {
						j2.sendCommande("DEATH/"+j.getNomClient()+"/");
					} catch (IOException e) {}
				}
				for(ThreadClient j2 : spectateurs){
					try {
						j2.sendCommande("DEATH/"+j.getNomClient()+"/");
					} catch (IOException e) {}
				}
			}
		}
		aQuiDeJouer.removeAll(deads);
		switch (aQuiDeJouer.size()) {
		case 0:
			// DrawGame
			res = true;
			gagnant = null;
			break;
		case 1:
			// Victoire du joueur
			res = true;
			gagnant = aQuiDeJouer.firstElement();
			break;
		default:
			// Partie non finie, on continue...
			res = false;
			break;
		}
		return res;
	}

	public boolean attendJoueur() {
		// partie deja commencee
		if(enCours){
			return false;
		}
		// partie pleine
		if(joueurs.size() == maxjoueurs){
			return false;
		}
		// partie pas encore pleine, on verifie le timer
		if(joueurs.size() < maxjoueurs){
			if(joueurs.size() < minjoueurs){
				return true;
			}else{
				return timerEnCours;
			}
		}
		// non atteignable
		System.out.println("[PARTIE "+numeroPartie+"] ERREUR : attendJoueur");
		return false;
	}

	public synchronized void deconnecteJoueur(ThreadClient threadClient) {
		StatsJoueur stats = threadClient.getStatistiques();
		if(stats != null){
			// enregistrement des statistiques du joueur avant la deco
			try {
				FileOutputStream fichier = new FileOutputStream(threadClient.getPath()+threadClient.getNomClient()+".ser");
				ObjectOutputStream oos = new ObjectOutputStream(fichier);
				oos.writeObject(stats);
				oos.flush();
				oos.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		try{
			threadClient.estConnecte(false);
			threadClient.estEnJeu(false);
			joueurs.removeElement(threadClient);
			aQuiDeJouer.removeElement(threadClient);
		}catch(NullPointerException n){
			
		}finally{
			serveur.deconnecteClient(threadClient);
		}
	}
	public Vector<ShipPlacement> getShipPlacementThreads(){
		return threadsPlacement;
	}
}
