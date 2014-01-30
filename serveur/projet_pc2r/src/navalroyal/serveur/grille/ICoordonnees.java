package navalroyal.serveur.grille;

/*
 * Tout objet qui se place dans la grille du jeu doit implementer cette
 * interface.
 */
public interface ICoordonnees {
	public void setCoords(int x, char y);
	public int  getX();
	public void setX(int x);
	public char getY();
	public void setY(char y);
}
