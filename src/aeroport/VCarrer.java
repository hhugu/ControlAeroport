package aeroport;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import aeroport.Avio.Direction;

public class VCarrer extends Carrer {

	public VCarrer(String idWay, int cmWayWidth, int cmWayMark, int cmLong,	int cmPosIniX, int cmPosIniY, Direction direccio) {

		super(idWay, cmWayWidth, cmWayMark, cmLong, cmPosIniX, cmPosIniY, direccio);

		this.cmFinX = this.cmIniX + this.cmWidth;
		this.cmFinY = this.cmIniY + this.cmLong;
	}

	@Override
	public void addCrossRoad(CrossRoad cr) {
		crossRoads.add(cr);

	}

	@Override
	public void paint(Graphics g, float factorX, float factorY, int offsetX,
			int offsetY) {
		int wayWidth;
		int wayMark;
		int xIni, yIni, xFin, yFin;
		Graphics2D g2d;
		wayMark = (int) (((float) this.cmMark) / factorY);

		if (wayMark <= 0) {
			return; // ===========================================>>
		}
		wayWidth = (int) (((float) this.cmWidth) / factorX);
		xIni = (int) ((this.cmIniX / factorX) + offsetX);
		yIni = (int) ((this.cmIniY / factorY) + offsetY);
		xFin = (int) ((this.cmFinX / factorX) + offsetX);
		yFin = (int) ((this.cmFinY / factorY) + offsetY);

		// Road
		g2d = (Graphics2D) g;
		GradientPaint gp5 = new GradientPaint(xIni, 0,
				Color.decode("0x404040"), xIni + (wayWidth / 2.9F), 0,
				Color.decode("0x606060"), true);
		g2d.setPaint(gp5);
		g.fillRect(xIni, yIni, xFin - xIni, yFin - yIni);
		g.setColor(Color.decode("0x505050"));
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