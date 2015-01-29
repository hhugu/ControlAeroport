package aeroport;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import aeroport.Avio.Direction;

public class Finger extends Carrer {
	
	private Avio avio;
	private Estat estat;
	public enum Estat {ocupat, buit, reservat};
	
	//String idWay, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY	
	public Finger(String idWay, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY, Direction direccio) {
		super(idWay, cmWayWidth, cmWayMark, cmLong, cmPosIniX, cmPosIniY, direccio);
        this.cmFinX = this.cmIniX + this.cmWidth;
        this.cmFinY = this.cmIniY + this.cmLong;
        
        this.estat=Estat.buit;
    }
	
	public Avio getAvio() {
		return avio;
	}
	public void setAvio(Avio avio) {
		this.avio = avio;
	}
	
	public Estat getEstat() {
		return estat;
	}

	public void setEstat(Estat estat) {
		this.estat = estat;
	}
	
	public boolean estaDisponible(){
		return estat == Estat.buit;
	}
	
	public void addCrossRoad(CrossRoad cr) {
		this.crossRoads.add(cr);
	}

	public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int xIni, yIni, xFin, yFin;
        
        xIni = (int) ((this.cmIniX / factorX) + offsetX);
        yIni = (int) ((this.cmIniY / factorY) + offsetY);
        xFin = (int) ((this.cmFinX / factorX) + offsetX);
        yFin = (int) ((this.cmFinY / factorY) + offsetY);
        
        g.setColor(Color.DARK_GRAY);
        g.drawRect(xIni, yIni, xFin - xIni, yFin - yIni);
    }
	
	public CrossRoad inFrontCrossRoad(Avio avio) {
		int minDistance;
		int actualDistance;
		int crossRoadPos;
		CrossRoad inFrontCR, actualCR;
		Iterator<CrossRoad> itr;

		inFrontCR = null;
		minDistance = this.getCmLong() + 1;
		itr = this.crossRoads.iterator();
		while (itr.hasNext()) {
			actualCR = itr.next();

			crossRoadPos = actualCR.getFinY();
			if (avio.getDirection() == Direction.FORWARD) {
				crossRoadPos = actualCR.getIniY();
			}
			actualDistance = avio.getDirection().getIncrement()
					* (crossRoadPos - this.getCmPosY(avio.getCmPosition(),
							avio.getDirection()));

			if ((actualDistance < minDistance) && (actualDistance > 0)) {
				minDistance = actualDistance;
				inFrontCR = actualCR;
			}
		}

		return inFrontCR;
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

		Iterator<CrossRoad> itr = this.crossRoads.iterator();
		while (itr.hasNext()) {
			cr = itr.next();

			if (this.insideThisCrossRoad(cmPosY, cr)) {
				return cr; // ================================================>>
			}
		}
		return null;
	}

	@Override
	public boolean insideThisCrossRoad(int cmPosY, CrossRoad crossRoad) {
		return ((cmPosY >= crossRoad.getIniY()) && (cmPosY <= crossRoad
				.getFinY()));
	}

	public int getCmPosX(int cmPosition, Direction direction) {
		if (direction == Direction.FORWARD) {
			return this.cmIniX + (this.cmWidth / 4); // ======================>>
		}

		return this.cmFinX - (this.cmWidth / 4);
	}

	public int getCmPosY(int cmPosition, Avio.Direction direction) {
		int cmPosY;

		cmPosY = this.cmIniY + cmPosition;
		if (cmPosY < this.cmIniY || cmPosY > this.cmFinY) {
			return -1; // Fuera de la via ====================================>>
		}
		return cmPosY;
	}

	public int getCmPosition(int cmPosX, int cmPosY, Avio.Direction direction) {
		int cmPosition;

		cmPosition = cmPosY - this.cmIniY;
		if (cmPosY < this.cmIniY || cmPosY > this.cmFinY) {
			return -1; // ============== Off road ============================>>
		}

		return cmPosition;
	}

	@Override
	public int distanceToCrossRoadInCm(CrossRoad cr, Avio Avio) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean posIsInside(int cmPosition, Direction direction) {
		// TODO Auto-generated method stub
		return false;
	}
}
