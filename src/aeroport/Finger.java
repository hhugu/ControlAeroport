package aeroport;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import aeroport.avions.Avio;
import aeroport.avions.Avio.Direction;

public class Finger extends Carrer {
	
	private Avio avio;
	private EstatFinguer estat;
	public enum EstatFinguer {ocupat, buit, reservat};
	
	//String idWay, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY	
	public Finger(String idWay, int cmWayWidth, int cmLong, int cmPosIniX, int cmPosIniY) {
		super(idWay, true, cmWayWidth, cmLong, cmPosIniX, cmPosIniY);
        this.cmFinX = this.cmIniX + this.cmWidth;
        this.cmFinY = this.cmIniY + this.cmLong;
        
        this.estat=EstatFinguer.buit;
    }
	
	/**
	 * Metode per sabre si el finguer esta buit.
	 * @return boolean - True si esta buit, false si esta ocupat o reservat
	 */
	public boolean estaDisponible(){
		return estat == EstatFinguer.buit;
	}
	
	public void addCrossRoad(CrossRoad cr) {
		this.crossroads.add(cr);
	}

	@Override
	public boolean insideAnyCrossRoad(int cmPosition) {
		return this.intersectedCrossRoad(cmPosition) != null;
	}

	@Override
	public CrossRoad intersectedCrossRoad(int cmPosition) {
		CrossRoad cr;
		int cmPosY;

		cmPosY = this.getCmPosY(cmPosition, Direction.FORWARD);

		Iterator<CrossRoad> itr = this.crossroads.iterator();
		while (itr.hasNext()) {
			cr = itr.next();

			if (this.insideThisCrossRoad(cmPosY, cr)) return cr;
		}
		return null;
	}

	@Override
	public boolean insideThisCrossRoad(int cmPosY, CrossRoad crossRoad) {
		return ((cmPosY >= crossRoad.getIniY()) && (cmPosY <= crossRoad.getFinY()));
	}

	public int getCmPosX(int cmPosition, Direction direction) {
		if (direction == Direction.FORWARD) return this.cmIniX + (this.cmWidth / 4); 

		return this.cmFinX - (this.cmWidth / 4);
	}

	public int getCmPosY(int cmPosition, Avio.Direction direction) {
		int cmPosY;

		cmPosY = this.cmIniY + cmPosition;
		if (cmPosY < this.cmIniY || cmPosY > this.cmFinY) return -1; // Fuera de la via
		return cmPosY;
	}

	public int getCmPosition(int cmPosX, int cmPosY, Avio.Direction direction) {
		int cmPosition;

		cmPosition = cmPosY - this.cmIniY;
		if (cmPosY < this.cmIniY || cmPosY > this.cmFinY) return -1; // Fuera de la via

		return cmPosition;
	}

	/**
	 * Metode que pinta el finger en funcio del tros de mapa que es visualitza i el zoom.
	 * @param g - Graphics
	 * @param factorX - float
	 * @param factorY - float
	 * @param offsetX - int
	 * @param offsetY - int
	 */
	public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int xIni, yIni, xFin, yFin;
        
        xIni = (int) ((this.cmIniX / factorX) + offsetX);
        yIni = (int) ((this.cmIniY / factorY) + offsetY);
        xFin = (int) ((this.cmFinX / factorX) + offsetX);
        yFin = (int) ((this.cmFinY / factorY) + offsetY);
        
        g.setColor(Color.DARK_GRAY);
        g.drawRect(xIni, yIni, xFin - xIni, yFin - yIni);   
    }
	
	/*
	 * GETTERS I SETTERS
	 */
	
	public Avio getAvio() {
		return avio;
	}
	public void setAvio(Avio avio) {
		this.avio = avio;
	}
	
	public EstatFinguer getEstat() {
		return estat;
	}

	public void setEstat(EstatFinguer estat) {
		this.estat = estat;
	}
}
