package navalroyal.serveur.persistant;

import java.io.Serializable;

/*
 * Classe serializable qui permet de creer un compte utilisateur.
 * Sauvegarde le nom, le mot de passe, et les statistiques
 * (nombre de victoires et de defaites) d'un client.
 * Les fichiers crees sont de la forme nomclient.ser et sont
 * enregistres dans le dossier "persistant" a la racine du projet.
 */

public class StatsJoueur implements Serializable {
	private static final long serialVersionUID = -8351339518960797271L;
	private String nom;
	private String password;
	private int nbVictoires;
	private int nbDefaites;
	
	public StatsJoueur(String n, String p){
		nom = n;
		password = p;
		nbVictoires = 0;
		nbDefaites = 0;
	}

	public String getNom()                   {return nom;              }
	public void setNom(String nom)           {this.nom = nom;          }
	public String getPassword()              {return password;         }
	public void setPassword(String password) {this.password = password;}
	public int getNbVictoires()              {return nbVictoires;      }
	public void incrementeNbVictoires()      {nbVictoires++;           }
	public int getNbDefaites()               {return nbDefaites;       }
	public void incrementeNbDefaites()       {nbDefaites++;            }
}
