package navalroyal.serveur;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import navalroyal.serveur.persistant.StatsJoueur;

/*
 * Classe principale (contient le main).
 * Gere la connexion des clients au serveur et leur ajout dans une partie.
 */
public class Serveur extends Thread{
	// pour des tests rapides du serveur, passer DEBUG a true.
	public final boolean DEBUG = false;
	// nombre de clients maximum connectes au serveur
	public final int     MAXCLIENTS = 32;
	// nombre et taille des sous-marins de chaque joueur.
	public final int[]   SHIPS = {1,1,2,2,3,3,4};
	// nombre de joueurs minimum dans une partie.
	public final int     MINJOUEURS = 2;
	// nombre de joueurs maximum par partie.
	public final int     MAXJOUEURS = 4;
	
	private int port = 0;
	private ServerSocket ecoute;
	private int nbclients = 0;
	private Vector<ThreadClient> clients = new java.util.Vector<ThreadClient>();
	private Vector<Partie> parties = new Vector<Partie>();
	private int nbPartiesEnCours = 0;
	
	public static void main(String[] args) {
		int port = 0;
		if(args.length > 1){
			System.out.println("[MAIN] Usage : serveur <port>");
			System.exit(1);
		}
		if(args.length != 1){
			System.out.println("[MAIN] Port non precise : utilisation du port 2012 par defaut.");
			port = 2012;
		}else{
			port = Integer.parseInt(args[0]);
			System.out.println("[MAIN] Port passe en parametre : "+port);
		}
		new Serveur(port).start();
		new Serveur(2092).start();
	}
	
	public Serveur(int p) {
		port = p;
		try{
			ecoute = new ServerSocket(port, MAXCLIENTS);
		}
		catch(IOException e){
			System.out.println("[SERVEUR] ERREUR : IO Serveur");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run(){
		switch(port){
		case 2092 :
			System.out.println("[SERVEUR HTTP] Serveur en ecoute sur le port "+port);
			while(true){
				try{
					String contenu = "HTTP/1.1 200 OK\nContent-Type: text/html; charset=utf-8\n\n";
					contenu += "<!DOCTYPE html>\n<html lang=\"fr\">\n<head><meta charset=\"utf-8\">\n";
					contenu += "<title>Statistiques de NavalRoyal</title>\n</head>\n";
					contenu += "<body>\n<h1>Statistiques de NavalRoyal</h1>\n";
					String path = "./persistant/";
					System.out.println("[SERVEUR HTTP] En attente d'un client.");
					Socket client = ecoute.accept();
					nbclients++;
					System.out.println("[SERVEUR HTTP] Client "+nbclients+" accepte.");
					// recuperation des stats de tous les joueurs
					String [] joueurs = new File(path).list();
					try{
						for (int i = 0; i < joueurs.length; i++){
							try {
								FileInputStream fichier = new FileInputStream(path+joueurs[i]);
								ObjectInputStream ois = new ObjectInputStream(fichier);
								StatsJoueur j = null;
								j = (StatsJoueur) ois.readObject();
								ois.close();
								contenu += "<div style=\"background-color:#EEEEEE; margin:20px; padding:20px;\">\n";
								contenu += "<h2>Joueur : "+stringToHTMLString(j.getNom())+"</h2>\n<ul>\n<li>Nombre de Victoires : "+j.getNbVictoires();
								contenu += "</li>\n<li>Nombre de D&eacute;faites : "+j.getNbDefaites()+"</li>\n</ul>\n</div>\n";
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e){
								e.printStackTrace();
							}
						}
					}catch(NullPointerException e){
						contenu += "Erreur de lecture des fichiers de statistiques";
					}
					contenu += "</body>\n</html>\n";
					// envoi au client
					DataOutputStream outchan = new DataOutputStream(client.getOutputStream());
					outchan.writeBytes(contenu);
					outchan.flush();
					// fermeture du client
					client.close();
					nbclients--;
				}catch(IOException e){
					System.out.println("[SERVEUR HTTP] ERREUR : IO Serveur");
					e.printStackTrace();
				}
			}
			
		default :
			System.out.println("[SERVEUR] Serveur en ecoute sur le port "+port);
			try{
				while(true){
					System.out.println("[SERVEUR] En attente d'un client.");
					Socket client = ecoute.accept();
					nbclients++;
					System.out.println("[SERVEUR] Client "+nbclients+" accepte.");
					ThreadClient tc = new ThreadClient(client, nbclients, this);
					tc.start();
					clients.add(tc);
				}
			}
			catch(IOException e){
				System.out.println("[SERVEUR] ERREUR : IO Serveur");
				e.printStackTrace();
				System.exit(1);
			}
			break;
		}
	}
	
	/*
	 * Ajout des joueurs en attente de partie dans une partie.
	 * Si on ne trouve pas de place pour accueillir un joueur, on cree une
	 * nouvelle partie avec 1 joueur, qui attendra un second joueur avant
	 * de lancer le timer de 30 secondes.
	 */
	public void gestionParties(){
		if(getNbClientsPretsAJouer() > 0){
			Boolean placeTrouvee = false;
			// parcours des clients prets a jouer
			for(ThreadClient c: clients){
				if(c.estConnecte() && !c.estEnJeu()){
					// parcours des parties en cours
					for(Partie p: parties){
						if(p.attendJoueur()){
							// il reste de la place dans la partie
							p.addJoueur(c);
							c.setPartie(p);
							c.estEnJeu(true);
							placeTrouvee = true;
							System.out.println("[SERVEUR] Joueur no "+c.getNumeroClient()+" ajoute a la partie no "+p.getNumeroPartie());
							if(p.getNbJoueurs() == p.getMinJoueurs()){
								System.out.println("[SERVEUR] Partie no "+p.getNumeroPartie()+" lancee avec "+p.getNbJoueurs()+" joueurs, en attente d'autres joueurs... ");
								// lancement du timer de 30 sec puis de la partie
								p.start();
							}
							break;
						}
					}
					// toutes les parties sont pleines, on en cree une nouvelle
					if(!placeTrouvee){
						// on cree une nouvelle partie et on lui ajoute le joueur
						parties.add(new Partie(++nbPartiesEnCours, this));
						parties.lastElement().addJoueur(c);
						c.setPartie(parties.lastElement());
						c.estEnJeu(true);
						System.out.println("[SERVEUR] Joueur no "+c.getNumeroClient()+" ajoute a la nouvelle partie no "+parties.lastElement().getNumeroPartie());
					}
				}
			}
		}
	}
	
	public synchronized int getNbClients(){
		System.out.println("[SERVEUR] getNbClients="+nbclients);
		return nbclients;
	}

	public synchronized Vector<ThreadClient> getClients() {
		System.out.println("[SERVEUR] getClients");
		return clients;
	}

	public synchronized Vector<Partie> getParties() {
		System.out.println("[SERVEUR] getParties");
		return parties;
	}
	
	public synchronized void deconnecteClient(ThreadClient tc) {
		System.out.println("[SERVEUR] removeClient");
		if(clients.removeElement(tc)){
			nbclients--;
		}
	}
	
	private int getNbClientsPretsAJouer(){
		int n = 0;
		for(ThreadClient c: clients) {
			if(c.estConnecte() && !c.estEnJeu()){
				n++;
			}
		}
		System.out.println("[SERVEUR] getNbClientsPretsAJouer="+n);
		return n;
	}
	
	public String stringToHTMLString(String string) {
		StringBuffer sb = new StringBuffer(string.length());
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;
		for (int i = 0; i < len; i++){
			c = string.charAt(i);
			if (c == ' ') {
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				}
				else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			}
			else {
				lastWasBlankChar = false;
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					sb.append("&lt;br/&gt;");
				else {
					int ci = 0xffff & c;
					if (ci < 160 )
						sb.append(c);
					else {
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}
}
