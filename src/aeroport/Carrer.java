package aeroport;

import java.awt.Graphics;
import java.util.ArrayList;

import aeroport.Avio.Direction;

public abstract class Carrer {

	protected String idWay;
    protected int cmLong;
    protected int cmIniX;
    protected int cmIniY;
    protected int cmFinX;
    protected int cmFinY;
    protected int cmWidth;
    protected ArrayList<Avio> avions = new ArrayList<Avio>();
    protected ArrayList<CrossRoad> crossroads = new ArrayList<CrossRoad>();
    protected ArrayList<Integer> forwardEntryPoint = new ArrayList<Integer>();
    protected ArrayList<Integer> backwardEntryPoint = new ArrayList<Integer>();
    protected boolean nomesUnAvio;

    public Carrer(String idWay, boolean nomesUnAvio, int cmWayWidth, int cmLong, int cmPosIniX, int cmPosIniY) {
        this.idWay = idWay;
        this.cmLong = cmLong;
        this.cmWidth = cmWayWidth;
        this.cmIniX = cmPosIniX;
        this.cmIniY = cmPosIniY;
        this.nomesUnAvio = nomesUnAvio;
        
        this.createDefaultEntryPoints();
    }
    
    /**
     * Metode que retorna el cruce on hi ha el proxim carrer a agafar. Ho fa comparant els id's dels carrers.
     * @param proximCarrer - String
     * @param carerActual - Carrer
     * @return
     */
    public CrossRoad getNextCrossRoad(String proximCarrer, Carrer carrerActual){
    	for (int i = 0; i < crossroads.size(); i++) {
			if (crossroads.get(i).getCarrer(carrerActual).getId().equals(proximCarrer)) {
				return crossroads.get(i);
			}
		}
    	return null;
    }

    public String getId() {
        return this.idWay;
    }
    public int getCmLong() {
        return this.cmLong;
    }

    public int getCmPosIniX() {
        return this.cmIniX;
    }

    public int getCmPosIniY() {
        return this.cmIniY;
    }
    
    public int getCmIniX() {
		return cmIniX;
	}

	public int getCmIniY() {
		return cmIniY;
	}

	public int getCmFinX() {
		return cmFinX;
	}

	public int getCmFinY() {
		return cmFinY;
	}

    public int getEntryPoint(Direction direction) {
        int cmPosition = 0;
        switch (direction) {
            case FORWARD: cmPosition = this.forwardEntryPoint.get(0); break;
            
            case BACKWARD: cmPosition = this.backwardEntryPoint.get(0); break;
            
            default: throw new AssertionError(direction.name());
        }
        return cmPosition;
    }

    private void createDefaultEntryPoints() {
        this.forwardEntryPoint.add(0);
        this.backwardEntryPoint.add(this.cmLong);
    }
    
    public ArrayList<Avio> getAvions(){
    	return avions;
    }

    public abstract void addCrossRoad(CrossRoad cr);

    public abstract boolean insideAnyCrossRoad(int cmPosition);

    public abstract CrossRoad intersectedCrossRoad(int cmPosition);

    public abstract boolean insideThisCrossRoad(int cmPosition, CrossRoad crossRoad);
    
    public abstract int getCmPosX(int cmPosition, Direction direction);
    
    public abstract int getCmPosY(int cmPosition, Direction direction);

    public boolean carrerIntersection(Carrer way) {
        Carrer vWay, hWay;
        hWay = vWay = null;
        
        if (way instanceof VCarrer) {
            vWay = way;
        }

        if (way instanceof HCarrer) {
            hWay = way;
        }

        if (this instanceof VCarrer) {
            vWay = this;
        }

        if (this instanceof HCarrer) {
            hWay = this;
        }

        if ((vWay == null) || (hWay == null)) {
            return false; 
        }

        return (hWay.cmIniX <= vWay.cmFinX)
                && (vWay.cmIniX <= hWay.cmFinX)
                && (hWay.cmIniY <= vWay.cmFinY)
                && (vWay.cmIniY <= hWay.cmFinY);
    }

    public abstract void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY);

    @Override
    public String toString() {
        return "Carrer: " + this.getId();
    }

	public abstract int getCmPosition(int cmPosX, int cmPosY, Direction direction);
	
	/**
	 * Afageix l'avio al carrer. Si al carrer nomes hi pot anar un avio els que volen entrar queden esperant
	 * fins que els notifiquin que poden entrar.
	 * @param avio - Avio
	 */
	public synchronized void ocuparCarrer(Avio avio){
		if(nomesUnAvio){
			while(avions.size() >= 1){
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}			
		avions.add(avio);
	}
	
	/**
	 * Borra l'avio del carrer. Si al carrer nomes hi pot haver un avio notifica als que esperen que poden entrar.
	 * @param avio - Avio
	 */
	public synchronized void alliberarCarrer(Avio avio){
		if (nomesUnAvio) {
			notify();
		}
		avions.remove(avio);
	}
}