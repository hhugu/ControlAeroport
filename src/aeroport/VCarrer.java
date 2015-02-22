package aeroport;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import aeroport.Avio.Direction;

public class VCarrer extends Carrer {

	public VCarrer(String idWay, boolean nomesUnAvio, int cmWayWidth, int cmLong, int cmPosIniX, int cmPosIniY) {
		super(idWay, nomesUnAvio, cmWayWidth, cmLong, cmPosIniX, cmPosIniY);

		this.cmFinX = this.cmIniX + this.cmWidth;
		this.cmFinY = this.cmIniY + this.cmLong;
	}

	@Override
	public void addCrossRoad(CrossRoad cr) {
		crossroads.add(cr);

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

	@Override
	public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		int wayWidth;
		int xIni, yIni, xFin, yFin;
		Graphics2D g2d;
		
		wayWidth = (int) (((float) this.cmWidth) / factorX);
		xIni = (int) ((this.cmIniX / factorX) + offsetX);
		yIni = (int) ((this.cmIniY / factorY) + offsetY);
		xFin = (int) ((this.cmFinX / factorX) + offsetX);
		yFin = (int) ((this.cmFinY / factorY) + offsetY);

		// Road
		g2d = (Graphics2D) g;
		GradientPaint gp5 = new GradientPaint(xIni, 0, Color.decode("0x404040"), xIni + (wayWidth / 2.9F), 0, Color.decode("0x606060"), true);
		g2d.setPaint(gp5);
		g.fillRect(xIni, yIni, xFin - xIni, yFin - yIni);
		g.setColor(Color.decode("0x505050"));
		g.drawRect(xIni, yIni, xFin - xIni, yFin - yIni);
	}
}