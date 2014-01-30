package navalroyal.serveur;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import navalroyal.serveur.grille.Drone;
import navalroyal.serveur.grille.Ship;
import navalroyal.serveur.grille.ShipPart;
import navalroyal.serveur.persistant.StatsJoueur;

/*
 * Classe representant un client. 
 */
public class ThreadClient extends Thread {
	private Socket client;
	private int numeroClient;
	private String nomClient;
	private boolean estConnecte;
	private boolean estEnJeu;
	private BufferedReader inchan;
	private DataOutputStream outchan;
	private String lastCommande;
	private Partie partie;
	private Vector<Ship> ships;
	private Drone drone;
	private boolean estVivant;
	private boolean aMonTourDeJouer;
	private final String path = "./persistant/";
	private StatsJoueur stats;
	private Serveur serveur;
	private boolean bombeUsed;
	private int lastShip;
	
	public ThreadClient(Socket so, int num, Serveur s) throws IOException {
		client = so;
		serveur = s;
		numeroClient = num;
		inchan = new BufferedReader(new InputStreamReader(so.getInputStream()));
		outchan = new DataOutputStream(so.getOutputStream());
		nomClient = "ANONYMOUS (not yet connected)";
		estConnecte = false;
		stats = null;
		init();
	}
	
	public void init(){
		estEnJeu = false;
		ships = new Vector<Ship>();
		estVivant = true;
		aMonTourDeJouer = false;
		bombeUsed = false;
		lastShip = serveur.SHIPS[0];
		setName("Client no "+numeroClient+". nom : "+nomClient);
	}
	
	public int getNumeroClient()              { return numeroClient;      }
	public boolean estConnecte()              { return estConnecte;       }
	public void estConnecte(boolean b)        { estConnecte = b;          }
	public boolean estEnJeu()                 { return estEnJeu;          }
	public String getNomClient()              { return nomClient;         }
	public void estEnJeu(boolean b)           { estEnJeu = b;             }
	public Vector<Ship> getShips()            { return ships;             }
	public void setPartie(Partie p)           { partie = p;               }
	public Drone getDrone()                   { return drone;             }
	public void estVivant(boolean b)          { estVivant = b;            }
	public void aMonTourDeJouer(boolean b)    { aMonTourDeJouer = b;      }
	public StatsJoueur getStatistiques()      { return stats;             }
	public String getPath()                   { return path;              }
	public void setLastShip(int taille)       { lastShip = taille;        }
	
	public int getNbActionsPossibles() {
		int res = 1;
		for(Ship s : ships){
			for(ShipPart sp : s.getParts()){
				if(sp.estDetruit()){
					res++;
				}
			}
		}
		return res;
	}
	public boolean estVivant(){
		if(estVivant){
			for(Ship s : ships){
				for(ShipPart sp : s.getParts()){
					if(!sp.estDetruit()){
						return true;
					}
				}
			}
			//tous les shippart sont detruits
			estVivant = false;
		}
		return false;
	}
	public void initDrone() {
		// initialisation du Drone du joueur avec des coordonnees aleatoires
		drone = new Drone();
		if(serveur.DEBUG){
			drone.setCoords(0, 'A');
		}else{
			Random rand = new Random();
			drone.setCoords(rand.nextInt(partie.getGrille().xmax+1), (char)(65 + rand.nextInt(partie.getGrille().xmax+1)));
		}
	}
		
	public void sendCommande(String co) throws IOException{
		System.out.println("[CLIENT "+numeroClient+"] Envoie de la commande \""+co+"\" au client no "+numeroClient+" : "+nomClient);
		outchan.writeBytes(co + "\n");
		outchan.flush();
		lastCommande = co;
		if(estConnecte){
			if(co.startsWith("HEYLISTEN") ||
				co.startsWith("PLAYERSHIP") ||
				co.startsWith("PLAYEROUCH") ||
				co.startsWith("PLAYERMOVE") ||
				co.startsWith("DEATH") ||
				co.startsWith("AWINNERIS") ||
				co.startsWith("PLAYERS")){
				if(!co.startsWith("HEYLISTEN/(serveur)")){
					try{
						partie.addToHistorique(co);
					}catch(NullPointerException e){}
				}
			}
		}
	}
	private void sendHistorique(){
		String lastcmd = "";
		for(String s : partie.getHistorique()){
			try {
				if(!s.equals(lastcmd)){
					System.out.println("[CLIENT "+numeroClient+"] Historique : Envoie de la commande "+s);
					outchan.writeBytes(s+"\n");
					outchan.flush();
					sleep(10);
					lastcmd = s;
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {}
		}
	}
	
	public void run() {
		try {
			System.out.println("[CLIENT "+numeroClient+"] Bonjour client " + client.getInetAddress() + ":" + client.getPort());
			while (true){
				Commande cmd;
				System.out.println("[CLIENT "+numeroClient+"] En attente d'une commande...");
				String reponse = inchan.readLine();
				cmd = new Commande(reponse);
				cmd.validate();
				if(cmd.isValide()){
					if(cmd.getArguments().size() == 0){
						System.out.println("[CLIENT "+numeroClient+"] Commande recue : "+cmd.getCommande().toUpperCase()+" sans arguments");
					}else{
						System.out.println("[CLIENT "+numeroClient+"] Commande recue : "+cmd.getCommande().toUpperCase()+", Arguments : /"+ cmd.printArguments() );
					}
					
					if(cmd.getCommande().equalsIgnoreCase("CONNECT")){
						/*
						 * connexion d'un joueur
						 * CONNECT/name/
						 */
						if(!estConnecte){
							estConnecte = true;
							try{
								nomClient = cmd.getArguments().firstElement();
							}catch(NoSuchElementException e){
								System.out.println("[CLIENT "+numeroClient+"] ERREUR : Commande CONNECT mal formee");
								sendCommande("ACCESSDENIED/");
								estConnecte = false;
								client.close();
								serveur.deconnecteClient(this);
								break;
							}
							Boolean alreadyUsed = false;
							File f = new File(path+nomClient+".ser");
							if(f.exists()){
								alreadyUsed = true;
								sendCommande("HEYLISTEN/(serveur) Nom reserve "+nomClient+" : utilisez la commande LOGIN au lieu de CONNECT pour vous connecter sous ce nom");
							}else{
								for(ThreadClient c: serveur.getClients()){
									if(c.getNumeroClient() == numeroClient){
										continue;
									}else{
										if(nomClient.equalsIgnoreCase(c.getNomClient())){
											alreadyUsed = true;
											break;
										}
									}
								}
							}
							if(alreadyUsed || nomClient.equalsIgnoreCase("serveur")){
								// le nom du client est reserve, on change son nom
								nomClient = nomClient + numeroClient;
							}
							// envoie de la commande WELCOME/nomClient/
							sendCommande("WELCOME/"+nomClient+"/");
							setName("Client no "+numeroClient+". nom : "+nomClient);
							// ajout des joueurs en attente a une partie
							serveur.gestionParties();
						} else {
							System.out.println("[CLIENT "+numeroClient+"] ERREUR : Client "+nomClient+" no "+numeroClient+" deja connecte.");
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("LOGIN")){
						/*
						 * login d'un joueur enregistre
						 * LOGIN/name/pass/
						 */
						if(!estConnecte){
							estConnecte = true;
							String nom = "";
							String pass = "";
							try{
								nom  = cmd.getArguments().elementAt(0);
								pass = cmd.getArguments().elementAt(1);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[CLIENT "+numeroClient+"] ERREUR : Commande CONNECT mal formee");
								sendCommande("ACCESSDENIED/");
								estConnecte = false;
								client.close();
								serveur.deconnecteClient(this);
								break;
							}
							// deserialization
							try {
								FileInputStream fichier = new FileInputStream(path+nom+".ser");
								ObjectInputStream ois = new ObjectInputStream(fichier);
								StatsJoueur j = (StatsJoueur) ois.readObject();
								ois.close();
								// connection
								if(j.getNom().equalsIgnoreCase(nom) && j.getPassword().equalsIgnoreCase(pass)){
									boolean alreadyConnected = false;
									for(ThreadClient c: serveur.getClients()){
										if(c.getNomClient().equalsIgnoreCase(j.getNom())){
											if(c.getNumeroClient() != numeroClient){
												alreadyConnected = true;
												break;
											}
										}
									}
									if(!alreadyConnected){
										// envoie de la commande WELCOME/nomClient/
										sendCommande("WELCOME/"+j.getNom()+"/");
										sendCommande("HEYLISTEN/(serveur) Vos statistiques : "+j.getNbVictoires()+" victoires et "+j.getNbDefaites()+" defaites.");
										nomClient = j.getNom();
										stats = j;
										setName("Client REGISTRED no "+numeroClient+". nom : "+nomClient);
										// ajout des joueurs en attente a une partie
										serveur.gestionParties();
									}else{
										System.out.println("[CLIENT "+numeroClient+"] ERREUR CONNECT : client deja connecte !");
										sendCommande("HEYLISTEN/(serveur) Le joueur "+j.getNom()+" est deja connecte !");
										sendCommande("ACCESSDENIED/");
										estConnecte = false;
										client.close();
										serveur.deconnecteClient(this);
										break;	
									}
								}else{
									System.out.println("[CLIENT "+numeroClient+"] ERREUR CONNECT : mot de passe incorrect");
									sendCommande("HEYLISTEN/(serveur) Mot de passe incorrect");
									sendCommande("ACCESSDENIED/");
									estConnecte = false;
									client.close();
									serveur.deconnecteClient(this);
									break;
								}
							}catch (IOException e) {
								System.out.println("[CLIENT "+numeroClient+"] ERREUR CONNECT : le client doit d'abord s'enregistrer");
								sendCommande("HEYLISTEN/(serveur) Vous n'etes pas enregistre sur le serveur");
								sendCommande("ACCESSDENIED/");
								estConnecte = false;
								client.close();
								serveur.deconnecteClient(this);
								break;
							}catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("REGISTER")){
						/*
						 * reservation d'un nom sur le serveur
						 * REGISTER/name/pass/
						 */
						if(!estConnecte){
							estConnecte = true;
							String nom, pass;
							try{
								nom  = cmd.getArguments().elementAt(0);
								pass = cmd.getArguments().elementAt(1);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[CLIENT "+numeroClient+"] ERREUR : Commande REGISTER mal formee");
								sendCommande("ACCESSDENIED/");
								estConnecte = false;
								client.close();
								serveur.deconnecteClient(this);
								break;
							}
							File f = new File(path+nom+".ser");
							if(f.exists() || nom.equalsIgnoreCase("serveur")){
								System.out.println("[CLIENT "+numeroClient+"] ERREUR REGISTER : ce nom est deja reserve : "+nom);
								sendCommande("HEYLISTEN/(serveur) "+nom+" est deja reserve sur le serveur");
								sendCommande("ACCESSDENIED/");
								estConnecte = false;
								client.close();
								serveur.deconnecteClient(this);
								break;
							}else{
								StatsJoueur j = new StatsJoueur(nom, pass);
								// serialization
								try {
									FileOutputStream fichier = new FileOutputStream(path+nom+".ser");
									ObjectOutputStream oos = new ObjectOutputStream(fichier);
									oos.writeObject(j);
									oos.flush();
									oos.close();
									// connection
									// envoie de la commande WELCOME/nomClient/
									nomClient = j.getNom();
									sendCommande("WELCOME/"+nomClient+"/");
									stats = j;
									setName("Client REGISTRED no "+numeroClient+". nom : "+nomClient);
									// ajout des joueurs en attente a une partie
									serveur.gestionParties();
								}catch (IOException e) {
									e.printStackTrace();
								}
							}
						}else{
							System.out.println("[CLIENT "+numeroClient+"] ERREUR : Client deja connecte");
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("SPECTATOR")){
						/*
						 * connexion en tant que spectateur
						 * SPECTATOR/   : renvoie la liste des parties : GAMESLIST/p1/.../pn/
						 * SPECTATOR/n/ : connecte à la partie n
						 */
						if(!estConnecte){
							if(cmd.getArguments().size() == 0){
								String s = "GAMESLIST";
								for(Partie p : serveur.getParties()){
									if(p.isEnCours()){
										s += "/" + p.getNumeroPartie();
										s += "/" + p.getNbJoueurs();
									}
								}
								s += "/";
								sendCommande(s);
							}else{
								int noPartie = 0;
								try{
									noPartie = Integer.parseInt(cmd.getArguments().firstElement());
								}catch(Exception e){};
								for(Partie p : serveur.getParties()){
									if(p.getNumeroPartie() == noPartie && noPartie != 0){
										p.addSpectateur(this);
										setPartie(p);
										estConnecte = true;
										estEnJeu = true;
										nomClient = "spectateur_" + numeroClient;
										setName("Client " + nomClient);
										sendCommande("WELCOME/" + nomClient + "/");
										sendHistorique();
										break;
									}
								}
							}
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("PUTSHIP")){
						/* 
						 * Placement de vaisseau.
						 * verifications : le vaisseau est dans la grille, les vaisseaux d'un meme joueur ne se chevauchent pas.
						 * PUTSHIP/x1/y1/x2/y2/.../xn/yn/
						 */
						if(estConnecte && estEnJeu && !partie.getGrille().estInitialisee() && cmd.getArguments().size()>0){
							String lastCmd = lastCommande;
							Vector<String> args = cmd.getArguments();
							Ship ship = new Ship(this);
							int x = 0;
							char y = 'A';
							if(args.size()%2 == 0){
								for(int i=0; i<args.size(); i++){
									if(i%2 == 0){
										// X
										try{
											x = Integer.valueOf(args.get(i));
										}catch(NumberFormatException ne){
											System.out.println("[CLIENT "+numeroClient+"] ERREUR : coord invalide : X doit etre un entier");
											break;
										}
										if(!partie.getGrille().coordValide(x)){
											break;
										}
									}else{
										// Y
										try{
											y = Character.toUpperCase(args.get(i).charAt(0));
										}catch(NumberFormatException ne){
											System.out.println("[CLIENT "+numeroClient+"] ERREUR : coord invalide : Y doit etre un caractere");
											break;
										}
										if(!partie.getGrille().coordValide(y)){
											break;
										}
										// verif chevauchement.
										boolean chevauchement = false;
										for(Ship stmp: ships){
											for(ShipPart sp: stmp.getParts()){
												if(sp.getX() == x && sp.getY() == y){
													// chevauchement avec un autre ship !
													System.out.println("[CLIENT "+numeroClient+"] ERREUR : chevauchement entre Ships du joueur");
													chevauchement = true;
													break;
												}
											}
										}
										if(!chevauchement){
											/* 
											 * Les coordonnees sont valides
											 * Il n'y a pas de chevauchement entre les Ship du joueur
											 */
											ship.addPart(new ShipPart(x, y));
										}
									}
								}
								if(ship.getTaille() != lastShip){
									// erreurs lors de l'ajout des PartShip : pas autant de PartShip que d'arguments a PUTSHIP
									System.out.println("[CLIENT "+numeroClient+"] ERREUR : erreur lors de l'ajout des PartShip : pas autant de PartShip que d'arguments a PUTSHIP");
									sendCommande("WRONG/");
									sendCommande(lastCmd);
								}else{
									// ajout du Ship fini, on verifie l'alignement de ses ShipParts
									boolean conforme = false;
									if(ship.getParts().size() > 1){
										int direction = 0;
										int lastdirection = 0;
										boolean aligned = false;
										for(int k=1; k<ship.getParts().size(); k++){
											int x_prev = ship.getParts().elementAt(k-1).getX();
											char y_prev = ship.getParts().elementAt(k-1).getY();
											int x_ = ship.getParts().elementAt(k).getX();
											char y_ = ship.getParts().elementAt(k).getY();
											// direction de l'aligmement
											direction = 0;
											if(x_ +1 == x_prev && y_ == y_prev){
												// en haut
												direction = 1;
											}
											if(x_ -1 == x_prev && y_ == y_prev){
												// en bas
												direction = 2;
											}
											if(x_ == x_prev && y_ +1 == y_prev){
												// a droite
												direction = 3;
											}
											if(x_ == x_prev && y_ -1 == y_prev){
												// a gauche
												direction = 4;
											}
											if(direction != 0){
												if(lastdirection == direction){
													aligned = true;
												}
												if(k==1){
													aligned = true;
												}
												lastdirection = direction;
												if(aligned){
													conforme = true;
												}else{
													conforme = false;
													break;
												}
											}else{
												conforme = false;
												break;
											}
											aligned = false;
										}
									}else{
										conforme = true;
									}
									// Ship conforme
									if(conforme){
										ships.add(ship);
										partie.getGrille().addShip(ship);
										ship.estPret(true);
										// sous-marin place sur la grille
										sendCommande("OK/");
										partie.addToHistorique("PLAYERSHIP/"+ nomClient + "/" + cmd.printArguments());
										for(ThreadClient s : partie.getSpectateurs()){
											String c = "PLAYERSHIP/" + nomClient + "/" + cmd.printArguments();
											s.sendCommande(c);
										}
										for(ShipPlacement sp : partie.getShipPlacementThreads()){
											if(sp.getPartie() == partie && sp.getJoueur() == this){
												//System.out.println("NOTIFY "+sp.getName());
												synchronized (sp) {
													sp.notify();
												}
											}
										}
									}else{
										System.out.println("[CLIENT "+numeroClient+"] ERREUR : Ship non conforme");
										sendCommande("WRONG/");
										sendCommande(lastCmd);
									}
								}
							}else{
								System.out.println("[CLIENT "+numeroClient+"] ERREUR : nombre d'arguments impaire !");
								sendCommande("WRONG/");
								sendCommande(lastCmd);
							}
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("ACTION")){
						/*
						 *  Actions
						 *  ACTION/a1/a2/.../an/n/
						 */
						if(estEnJeu && estConnecte){
							if(aMonTourDeJouer){
								if(cmd.getArguments().size() <= getNbActionsPossibles()){
									boolean laserUsed = false;
									for(String arg : cmd.getArguments()){
										char action = Character.toUpperCase(arg.charAt(0));
										switch (action) {
										case 'L':
											drone.setX(drone.getX() -1 );
											if(drone.getX() < partie.getGrille().xmin){
												drone.setX(partie.getGrille().xmin);
											}
											for(ThreadClient s : partie.getSpectateurs()){
												String c = "PLAYERMOVE/"+nomClient+"/"+drone.getX()+"/"+drone.getY()+"/";
												s.sendCommande(c);
											}
											break;
										case 'R':
											drone.setX(drone.getX() +1 );
											if(drone.getX() > partie.getGrille().xmax){
												drone.setX(partie.getGrille().xmax);
											}
											for(ThreadClient s : partie.getSpectateurs()){
												String c = "PLAYERMOVE/"+nomClient+"/"+drone.getX()+"/"+drone.getY()+"/";
												s.sendCommande(c);
											}
											break;
										case 'D':
											drone.setY((char) (drone.getY() -1));
											if(drone.getY() < partie.getGrille().ymin){
												drone.setY(partie.getGrille().ymin);
											}
											for(ThreadClient s : partie.getSpectateurs()){
												String c = "PLAYERMOVE/"+nomClient+"/"+drone.getX()+"/"+drone.getY()+"/";
												s.sendCommande(c);
											}
											break;
										case 'U':
											drone.setY((char) (drone.getY() +1));
											if(drone.getY() > partie.getGrille().ymax){
												drone.setY(partie.getGrille().ymax);
											}
											for(ThreadClient s : partie.getSpectateurs()){
												String c = "PLAYERMOVE/"+nomClient+"/"+drone.getX()+"/"+drone.getY()+"/";
												s.sendCommande(c);
											}
											break;
										case 'E':
											if(!laserUsed){
												laserUsed = true;
												boolean touche = false;
												for(ThreadClient j : partie.getJoueurs()){
													for(Ship s : j.getShips()){
														for(ShipPart sp : s.getParts()){
															if(sp.getX() == drone.getX() && sp.getY() == drone.getY()){
																// le ShipPart est au meme endroit que le drone, il est detruit
																if(!sp.estDetruit()){
																	touche = true;
																	sp.estDetruit(true);
																	try{
																		j.sendCommande("OUCH/"+drone.getX()+"/"+drone.getY()+"/");
																	}catch(IOException e){}
																	for(ThreadClient spec : partie.getSpectateurs()){
																		String c = "PLAYEROUCH/"+j.getNomClient()+"/"+drone.getX()+"/"+drone.getY()+"/";
																		spec.sendCommande(c);
																	}
																}
															}
														}
													}
												}
												if(touche){
													sendCommande("TOUCHE/"+drone.getX()+"/"+drone.getY()+"/");
												}else{
													sendCommande("MISS/"+drone.getX()+"/"+drone.getY()+"/");
												}
											}
											break;
										// EXTENSION : bombe
										case 'B':
											if(!bombeUsed){
												bombeUsed = true;
												// splash damage sur 9 cases autour de la cible
												for(ThreadClient j : partie.getJoueurs()){
													for(Ship s : j.getShips()){
														for(ShipPart sp : s.getParts()){
															for(int x = -1; x <= 1; x++){
																for(int y = -1; y <= 1; y++){
																	if(sp.getX() == (int)(drone.getX()+x) && sp.getY() == (char)(drone.getY()+y)){
																		// si la case est dans la grille
																		if( (int)(drone.getX()+x) >= (int)(partie.getGrille().xmin) &&
																			(int)(drone.getY()+y) >= (int)(partie.getGrille().ymin) &&
																			(int)(drone.getX()+x) <= (int)(partie.getGrille().xmax) &&
																			(int)(drone.getY()+y) <= (int)(partie.getGrille().ymax)){
																			// le ShipPart est dans la zone d'explosion, il est detruit
																			if(!sp.estDetruit()){
																				sp.estDetruit(true);
																				try{
																					sendCommande("TOUCHE/"+ (int)(drone.getX()+x) +"/"+ (char)(drone.getY()+y) +"/");
																					j.sendCommande("OUCH/"+ (int)(drone.getX()+x) +"/"+ (char)(drone.getY()+y) +"/");
																				}catch(IOException e){}
																				for(ThreadClient spec : partie.getSpectateurs()){
																					String c = "PLAYEROUCH/"+j.getNomClient()+"/"+ (int)(drone.getX()+x) +"/"+ (char)(drone.getY()+y) +"/";
																					spec.sendCommande(c);
																				}
																			}
																		}
																	}
																}
															}
															
														}
													}
												}
											}
											break;
										default:
											sendCommande("HEYLISTEN/(serveur) Commande invalide, tu passes ton tour !");
											System.out.println("[CLIENT "+numeroClient+"] ERREUR ACTION : commande mal formee");
											break;
										}
									}
								}else{
									sendCommande("HEYLISTEN/(serveur) Commande invalide, tu passes ton tour !");
									System.out.println("[CLIENT "+numeroClient+"] ERREUR ACTION : nombre d'actions non conforme");
								}
								// fin du traitement, on reveille la partie
								synchronized (partie) {
									partie.notify();
								}
							}else{
								sendCommande("HEYLISTEN/(serveur) Ce n'est pas votre tour !"); 
							}
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("PLAYAGAIN")){
						/*
						 * fin de partie, le joueur veut rejouer
						 * PLAYAGAIN/
						 */
						if(estEnJeu){	// Evite de sortir un client en plein milieu d'une partie.
							estEnJeu = false;
							partie.removeJoueur(this);
							// reinitialisation du joueur
							init();
							serveur.gestionParties();
						}else{
							System.out.println("[CLIENT "+numeroClient+"] ERREUR : le client est dans une partie non terminee.");
						}
					}else
					if(cmd.getCommande().equalsIgnoreCase("BYE")){
						/*
						 * fin de partie, le joueur quitte
						 * BYE/
						 */
						try{
							partie.deconnecteJoueur(this);
						}catch(NullPointerException e){}
						System.out.println("[CLIENT "+numeroClient+"] Deconnexion du client");
						serveur.deconnecteClient(this);
						client.close();
						break;
					}else
					if(cmd.getCommande().equalsIgnoreCase("TALK")){
						/*
						 * Chat. a tout moment les joueurs peuvent communiquer
						 * TALK/message/
						 */
						try{
							for(ThreadClient tc : partie.getJoueurs()){
								tc.sendCommande("HEYLISTEN/(" + nomClient + ") " + cmd.printArguments());
							}
							for(ThreadClient s : partie.getSpectateurs()){
								String c = "HEYLISTEN/(" + nomClient + ") " + cmd.printArguments();
								s.sendCommande(c);
							}
						}catch(NullPointerException e){
							System.out.println("[CLIENT "+numeroClient+"] ERREUR : le client n'est pas connecte (avec CONNECT/name/), il ne peut pas utiliser la commande TALK");
						}
					}else{
						System.out.println("[CLIENT "+numeroClient+"] ERREUR : Commande non suportee pour ce client : "+reponse);
					}
				}else{
					System.out.println("[CLIENT "+numeroClient+"] ERREUR : Commande invalide : "+reponse);
				}
			}
		} catch (IOException e) {
			estConnecte = false;
			estEnJeu = false;
			System.out.println("[CLIENT "+numeroClient+"] ERREUR : Client deconnecte");
			try{
				partie.deconnecteJoueur(this);
			}catch(NullPointerException n){}
			
			try {
				client.close();
			} catch (IOException e1) {
				System.out.println("[CLIENT "+numeroClient+"] ERREUR : IO removeJoueur");
			}
		}
	}
}
