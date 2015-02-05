package aeroport;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import aeroport.Finger.Estat;

public class Avio extends Thread {

	private Aeroport aeroport;
	private static final int ACCELERACIO = 5;
    private int cmLong, cmWidth, speed, posicio = 0;
    private volatile int cmPosition;
    private Image imgAvioH, imgAvioV;
    private volatile Carrer carrerActual;
    private Finger finger;
    private String idAvio;
    private Direction direction;
    private CrossRoad cruceActual = null, proximCruce = null;
    private ArrayList<String> rutaAlFinger, rutaDespegue;
    private EstatAvio estat;
	public static enum EstatAvio {ATERRANT, GOFINGER, ATURAT, GOPISTA, DESPEGANT, VOLANT};

    
    public Avio(Aeroport aeroport, String idAvio, Carrer way, Direction direction, Finger finger, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue) {
        this.aeroport = aeroport;
    	this.idAvio = idAvio;
        try {
			imgAvioH = ImageIO.read(new File("Imagenes/avioH.png"));
			imgAvioV = ImageIO.read(new File("Imagenes/avioV.png"));
		} catch (IOException e) {
		}
        
        cmLong = 1000;
        cmWidth = 1000;
        
        speed = Aeroport.VELOCITAT_DE_VOL;
        
        this.finger = finger;
        this.rutaAlFinger = rutaAlFinger;
        this.rutaDespegue = rutaDespegue;
        
        cmPosition = 0;

        setWay(way);
        setDirection(direction);
    }
	
	public void run(){
		estat = EstatAvio.ATERRANT;
		calcularPuntEntrada();

		while(!Aeroport.isEnd()){
			if (!Aeroport.isPaused()) {
				avançar();
				try {
					Thread.sleep(Aeroport.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				
				if (estat != EstatAvio.ATURAT){
					if(estat == EstatAvio.ATERRANT) aterrar();
					else if(estat == EstatAvio.GOFINGER) calcularProximCarrer(rutaAlFinger);
					else if(estat == EstatAvio.GOPISTA)	calcularProximCarrer(rutaDespegue);
					else if(estat == EstatAvio.DESPEGANT) despegar();
					else if(estat == EstatAvio.VOLANT) aeroport.borrarAvio(this);
				}
								
				if(carrerActual instanceof Finger){
					int temp = cmPosition;
					direction = Direction.BACKWARD;
					cmPosition = carrerActual.getCmLong(); 
					
					try {
						aeroport.canviarEstatFinger(finger, Estat.ocupat);
						Thread.sleep(6500);
						cmPosition = temp;
						estat = EstatAvio.GOPISTA;
						aeroport.canviarEstatFinger(finger, Estat.buit);
						posicio = 0;
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	private void despegar() {
		speed += ACCELERACIO;
		
		if (speed >= Aeroport.VELOCITAT_DE_VOL) estat = EstatAvio.VOLANT;		
	}

	private void calcularPuntEntrada() {
		if (direction == Direction.BACKWARD) cmPosition = carrerActual.getCmLong()+5000;			
		else if(direction == Direction.FORWARD) cmPosition = -5000;		
	}

	private void aterrar(){
		speed -= ACCELERACIO;
		
		if (speed <= Aeroport.VELOCITAT_SEGURITAT) estat = EstatAvio.GOFINGER;
	}

	private void avançar() {
		if (direction == Direction.BACKWARD) cmPosition -= speed;
		else if(direction == Direction.FORWARD) cmPosition += speed;

		if (estat != EstatAvio.VOLANT && estat != EstatAvio.ATERRANT && estat != EstatAvio.DESPEGANT) {
			if (cmPosition < 0)	direction = Direction.FORWARD;
			else if (cmPosition > carrerActual.cmLong) direction = Direction.BACKWARD;
		}
	}
	
    private void calcularProximCarrer(ArrayList<String> ruta) {
		avançar();
    	
    	if (carrerActual.insideAnyCrossRoad(cmPosition)) {
			cruceActual = carrerActual.intersectedCrossRoad(cmPosition);
			if (posicio < ruta.size()) {
				if (cruceActual.getCarrer(carrerActual).getId().equals(ruta.get(posicio))) {
					Carrer carrerAnterior = carrerActual;
					carrerActual = cruceActual.getCarrer(carrerActual);
					
					if (posicio + 1 < ruta.size()) proximCruce = carrerActual.getNextCrossRoad(ruta.get(posicio+1), carrerActual);

					direction = obtenirDireccio(cruceActual, proximCruce);
					
					if(carrerActual.getId().equals("pista")) {
						this.estat = EstatAvio.DESPEGANT;
						direction = aeroport.direccioDespegue();
					}
					
					cmPosition = carrerActual.getCmPosition(
											       carrerAnterior.getCmPosX(this.cmPosition, this.direction),
												   carrerAnterior.getCmPosY(this.cmPosition, this.direction),
												   this.direction);
					posicio++;
				}
			}
		}
	}
    
	private Direction obtenirDireccio(CrossRoad cruceActual, CrossRoad proximCruce) {
		if(carrerActual instanceof HCarrer){
			if (cruceActual.getIniX() < proximCruce.getIniX()) return Direction.FORWARD;
			else return Direction.BACKWARD;
		}else if(carrerActual instanceof VCarrer){
			if (cruceActual.getIniY() < proximCruce.getIniY()) return Direction.FORWARD;
			else return Direction.BACKWARD;
		}
		return null;
	}
	
	public synchronized void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		int iniX, iniY, finX, finY;
		
   		finY=(int)(((this.cmWidth)/factorY));
   		finX=(int)(((this.cmLong)/factorX));
   		
   		if(carrerActual instanceof HCarrer){
   			
   			iniY=(int)((((this.carrerActual.cmFinY+this.carrerActual.cmIniY)/2-400)/factorY)+offsetY);
   	   		iniX=(int)((((this.carrerActual.cmIniX+this.cmPosition))/factorX)+offsetX);
   	   		
	   		if (direction==Direction.BACKWARD) {
	   			g.drawImage(this.imgAvioH, iniX, iniY, -finX, finY, null);
			}
			else if(direction==Direction.FORWARD){
				g.drawImage(this.imgAvioH, iniX, iniY, finX, finY, null);
			}
	   		g.drawString(idAvio, iniX, iniY);
   		}else if(carrerActual instanceof VCarrer || carrerActual instanceof Finger){
   			
   			iniY=(int)((((this.carrerActual.cmIniY+this.cmPosition))/factorY)+offsetY);
   	   		iniX=(int)((((this.carrerActual.cmIniX+this.carrerActual.cmFinX)/2-400)/factorX)+offsetX);
   	   		
   			if (direction==Direction.BACKWARD) {
	   			g.drawImage(this.imgAvioV, iniX, iniY, finY, -finX, null);
			}
			else if(direction==Direction.FORWARD){
				g.drawImage(this.imgAvioV, iniX, iniY, finY, finX, null);
			}
	   		g.drawString(idAvio, iniX, iniY);
   		}
    }
	
    public static enum Direction {

        FORWARD(1), BACKWARD(-1);
        private int increment;

        private Direction(int increment) {
            this.increment = increment;
        }

        public int getIncrement() {
            return this.increment;
        }
    }
    
    /*
     * GETTERS Y SETTERS
     */
    
    public Direction getDirection() {
		return direction;
	}

	public Carrer getWay() {
		return this.carrerActual;
	}

	public int getCmPosition() {
	        return this.cmPosition;
	}

	public int getLongInCm() {
		return this.cmLong;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getIdCar() {
		return idAvio;
	}

	public void setCmPosition(int cmPosition) {
		this.cmPosition = cmPosition;
	}

	public void setWay(Carrer way) {
		this.carrerActual = way;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getWidthInCm() {
		return this.cmWidth;
	}

	public void setPositionInCm(int cmPos) {
        this.cmPosition = cmPos;
		
	}

}