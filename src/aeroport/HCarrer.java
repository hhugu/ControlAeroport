package aeroport;


import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import aeroport.Avio.Direction;

public class HCarrer extends Carrer {

	public HCarrer(String idWay, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY, Direction direccio) {
        super(idWay, cmWayWidth, cmWayMark, cmLong, cmPosIniX, cmPosIniY, direccio);

        this.cmFinX = this.cmIniX + this.cmLong;
        this.cmFinY = this.cmIniY + this.cmWidth;
    }

	@Override
	public void addCrossRoad(CrossRoad cr) {
		crossRoads.add(cr);
	}
	
	@Override
    public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int wayWidth;
        int wayMark;
        int xIni, yIni, xFin, yFin;
        Graphics2D g2d;
        wayMark = (int) ((float) this.cmMark / factorY);

        if (wayMark <= 0) {
            return;
        }

        wayWidth = (int) ((float) this.cmWidth / factorY);
        xIni = (int) (((float) this.cmIniX / factorX) + offsetX);
        yIni = (int) (((float) this.cmIniY / factorY) + offsetY);
        xFin = (int) (((float) this.cmFinX / factorX) + offsetX);
        yFin = (int) (((float) this.cmFinY / factorY) + offsetY);


        // Road
        g2d = (Graphics2D) g;
        GradientPaint gp5 =
                new GradientPaint(0, yIni + 2, Color.decode("0x404040"), 0, yIni + wayWidth / 2.9F, Color.decode("0x606060"), true);
        g2d.setPaint(gp5);
        g.fillRect(xIni, yIni, xFin - xIni, yFin - yIni);
        g.setColor(Color.BLACK);
        g.drawRect(xIni, yIni, xFin - xIni, yFin - yIni);
	}

	@Override
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

			crossRoadPos = actualCR.getIniX();
			if (avio.getDirection() == Direction.FORWARD) {
				crossRoadPos = actualCR.getFinX();
			}
			actualDistance = avio.getDirection().getIncrement() * (crossRoadPos - this.getCmPosX(avio.getCmPosition(), avio.getDirection()));

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
	public boolean insideThisCrossRoad(int cmPosX, CrossRoad crossRoad) {
		return ((cmPosX >= crossRoad.getIniX()) && (cmPosX <= crossRoad.getFinX()));
	}

	@Override
	public CrossRoad intersectedCrossRoad(int cmPosition) {
		CrossRoad cr;
		int cmPosX;

		cmPosX = this.getCmPosX(cmPosition, Direction.FORWARD);
		Iterator<CrossRoad> itr = this.crossRoads.iterator();
		while (itr.hasNext()) {
			cr = itr.next();

			if (this.insideThisCrossRoad(cmPosX, cr)) {
				return cr; // ==============================================>>
			}
		}

		return null;
	}

	public int getCmPosX(int cmPosition, Avio.Direction direction) {
		int cmPosX;

		cmPosX = this.cmIniX + cmPosition;
		if (cmPosX < this.cmIniX || cmPosX > this.cmFinX) {
			return -1; // ============== Off road ============================>>
		}

		return cmPosX;
	}


	public int getCmPosY(int cmPosition, Avio.Direction direction) {
		if (direction == Avio.Direction.FORWARD) {
			return this.cmFinY - (this.cmWidth / 4);
		} else {
			return this.cmIniY + (this.cmWidth / 4);
		}
	}


	public int getCmPosition(int cmPosX, int cmPosY, Avio.Direction direction) {
		int cmPosition;

		cmPosition = cmPosX - this.cmIniX;
		if (cmPosX < this.cmIniX || cmPosX > this.cmFinX) {
			return -1; // ============== Off road ============================>>
		}

		return cmPosition;
	}
	
	@Override
	public int distanceToCrossRoadInCm(CrossRoad cr, Avio Avio) {
		return 0;
	}

	@Override
	public boolean posIsInside(int cmPosition, Direction direction) {
		return false;
	}
}