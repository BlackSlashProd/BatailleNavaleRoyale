package navalroyal.serveur.grille;

/*
 * Classe representant une partie d'un sous-marin d'un joueur
 */
public class ShipPart implements ICoordonnees {
	private Boolean estDetruit;
	private int x;
	private char y;
	
	public ShipPart(int x, char y){
		estDetruit(false);
		setCoords(x, y);
	}
	public Boolean estDetruit()         {return estDetruit;}
	public void estDetruit(Boolean b)   {estDetruit = b;   }
	public void setCoords(int x, char y){setX(x);setY(y);  }
	public int getX()                   {return x;         }
	public void setX(int x)             {this.x = x;       }
	public char getY()                  {return y;         }
	public void setY(char y)            {this.y = y;       }
}
