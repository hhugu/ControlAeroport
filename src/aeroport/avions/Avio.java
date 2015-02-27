package aeroport.avions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.CrossRoad;
import aeroport.Finger;
import aeroport.HCarrer;
import aeroport.VCarrer;
import aeroport.Finger.EstatFinguer;

public class Avio extends Thread {
	
   protected int ACCELERACIO = 10;
   protected int VEL_PISTA = 40;
   protected int VEL_VOL = 460;
    public static enum Estat {ATERRANT, GOFINGER, DINS_FINGER, GOPISTA, DESPEGANT, VOLANT};    
	public static enum Direction {FORWARD, BACKWARD}
	protected int cmLong, cmWidth;
    protected Image imgAvioH, imgAvioV, imgAvioHB, imgAvioVB;
    private String idAvio;

	private Estat estat;
	private Aeroport aeroport;
	private Finger finger;
    private ArrayList<String> rutaAlFinger, rutaDespegue;
    private int posicio = 0;
    
    private volatile int cmPosition;
	protected volatile int speed;
    private volatile Carrer carrerActual;
    private CrossRoad cruceActual, proximCruce;
    private Direction direccio;

    public Avio(Aeroport aeroport, String idAvio, Carrer way, Direction direction, Finger finger, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue) {
        this.aeroport = aeroport;
    	this.idAvio = idAvio;
        try {
			imgAvioH = ImageIO.read(new File("Imagenes/avio/avioH.png"));
			imgAvioV = ImageIO.read(new File("Imagenes/avio/avioV.png"));
			imgAvioHB = ImageIO.read(new File("Imagenes/avio/avioHB.png"));
			imgAvioVB = ImageIO.read(new File("Imagenes/avio/avioVB.png"));
		} catch (IOException e) {
		}
        
        cmLong = cmWidth = 1000;
        
        speed = VEL_VOL;
        
        this.finger = finger;
        this.rutaAlFinger = rutaAlFinger;
        this.rutaDespegue = rutaDespegue;
        
        cmPosition = 0;

        carrerActual = way;
        setDirection(direction);
    }
	
	public void run(){
		setWay(carrerActual);		
		estat = Estat.ATERRANT;
		calcularEntrada();

		while(!Aeroport.isEnd()){
			if (!Aeroport.isPaused()) {
				avançar();
				
				if(estat == Estat.ATERRANT) aterrar();
				else if(estat == Estat.GOFINGER) calcularProximCarrer(rutaAlFinger);
				else if(estat == Estat.DINS_FINGER) descarregarCarregar();
				else if(estat == Estat.GOPISTA)	calcularProximCarrer(rutaDespegue);
				else if(estat == Estat.DESPEGANT) despegar();
				else if(estat == Estat.VOLANT){ aeroport.borrarAvio(this); carrerActual.alliberarCarrer(this);; }
			}
		}
	}
	
	private void calcularEntrada() {
		if (direccio == Direction.BACKWARD) cmPosition = carrerActual.getCmLong()+8000;			
		else if(direccio == Direction.FORWARD) cmPosition = -8000;		
	}
	
	/**
	 * Fa que l'avio es mogui per el carrer en funcio de la direccio que du i la velocitat a la que va.
	 * Si la direccio es BACKWARD disminuirà el cmPosition, si es FORWARD augmenta el cmPosition.
	 * Si per cualsevol cosa arriba al principi o al final del carrer canvia de diraccio.
	 * @author hugu
	 */
	private void avançar() {
		if (aeroport.pucAvançar(this)) {
			if (direccio == Direction.BACKWARD)	cmPosition -= speed;
			else if (direccio == Direction.FORWARD) cmPosition += speed;
			
			if (estat != Estat.VOLANT && estat != Estat.ATERRANT && estat != Estat.DESPEGANT) {
				if (cmPosition < 0)	direccio = Direction.FORWARD;
				else if (cmPosition > carrerActual.getCmLong()) direccio = Direction.BACKWARD;
			}
		}
		try {
			Thread.sleep(Aeroport.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Va reduint la velocitat en funcio de l'acceleracio que te l'avio. Quan la seva velocitat
	 * es menor que la maxima per circular dins l'aeroport cambia l'estat de l'avio a GOFINGER.
	 */
	private void aterrar(){
		if (speed - ACCELERACIO > VEL_PISTA) speed -= ACCELERACIO;
		else speed = VEL_PISTA;
		if (speed == VEL_PISTA) estat = Estat.GOFINGER;
	}

	/**
	 * Va agumentant la velocitat en funcio de l'acceleracio que te l'avio. Quan la seva velocitat
	 * es major que la velocitat de vol cambia l'estat de l'avio a VOLANT.
	 */
	private void despegar() {
		speed += ACCELERACIO;
		
		if (speed > VEL_VOL) estat = Estat.VOLANT;		
	}
	
	/**
	 * Cambia l'estat del seu finger a ocupat. Simula una estada al finger, cambia l'estat de l'avio a 
	 * GOPISTA i el del finger a buit. 
	 */
	private void descarregarCarregar() {
		aeroport.canviarEstatFinger(finger, EstatFinguer.ocupat);
		
		direccio = Direction.FORWARD;
		while((cmPosition + cmLong + 300) <= carrerActual.getCmLong()) avançar();					
		try {
			Thread.sleep((long)(Math.random()*10000+1000));
			
			while(aeroport.getCarrerCritic().getAvions().size() >= 4) Thread.sleep(Aeroport.SLEEP_TIME);
			
			cmPosition = finger.getCmLong() - cmLong;
			direccio = Direction.BACKWARD;
			estat = Estat.GOPISTA;
			aeroport.canviarEstatFinger(finger, EstatFinguer.buit);
			posicio = 0;
		} catch (InterruptedException e) {
		}		
	}
	
	/*
	 * MOURER-SE PER ELS CARRERS 
	 */
	
	/**
	 * Calcula si el carrer que cruza amb el carrer on esta es el proxim que ha d'agafar, si es aixo l'agafa.
	 * Calcula amb quina direccio ha d'entrar al nou carre i a quina posicio. Si entra a un finger canvia
	 * l'estat de l'avio a DINS_FINGER i si es la pista a DESPEGANT.
	 * @param ruta
	 */
    private void calcularProximCarrer(ArrayList<String> ruta) {
    	
    	if (carrerActual.insideAnyCrossRoad(cmPosition)) {
			cruceActual = carrerActual.intersectedCrossRoad(cmPosition);
			if (posicio < ruta.size()) {
				if (cruceActual.getCarrer(carrerActual).getId().equals(ruta.get(posicio))) {
					Carrer carrerAnterior = carrerActual;
					setWay(cruceActual.getCarrer(carrerActual));
					
					carrerAnterior.alliberarCarrer(this);

					if (posicio + 1 < ruta.size()) proximCruce = carrerActual.getNextCrossRoad(ruta.get(posicio+1), carrerActual);

					direccio = obtenirDireccio(cruceActual, proximCruce);

					if (carrerActual instanceof Finger) estat = Estat.DINS_FINGER;
					
					if(carrerActual.getId().equals("pista")) {
						this.estat = Estat.DESPEGANT;
						direccio = aeroport.direccioDespegue();
					}
					
					cmPosition = carrerActual.getCmPosition(
											       carrerAnterior.getCmPosX(this.cmPosition, this.direccio),
												   carrerAnterior.getCmPosY(this.cmPosition, this.direccio),
												   this.direccio);
					posicio++;
				}
			}
		}
	}
    
    /**
     * Calcula la direccio que haura de dur l'avio en el carrer nou depenent del proxim a agafar.
     * Si esta mes envant o mes abaix la direccio es FORWARD, si esta mes enrrera o mes amunt la 
     * direccio es BACKWARD.
     * @param cruceActual - Crossroad
     * @param proximCruce - Crossroad
     * @return direccio - Direction
     */
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
	
	/*
	 * PINTAR
	 */
	
	/**
	 * Pinta l'avio depenent del zoom, la posicio de visualitzacio, del carrer (Horizontal o Vertical) on està
	 *  i la direccio que du (BACKWARD o FORWARD).
	 * @param g - Graphics
	 * @param factorX - float
	 * @param factorY - float
	 * @param offsetX - int
	 * @param offsetY - int
	 */
	public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {		
		g.setColor(Color.RED);
		int iniX = 0, iniY = 0, finX, finY;
		
   		finY=(int)(((this.cmWidth)/factorY));
   		finX=(int)(((this.cmLong)/factorX));
   		
   		if(carrerActual instanceof HCarrer){
   			
   	   		iniX=(int)((((this.carrerActual.getCmIniX()+this.cmPosition))/factorX)+offsetX);
   	   		iniY=(int)((((this.carrerActual.getCmFinY()+this.carrerActual.getCmIniY())/2-400)/factorY)+offsetY);
   	   		
	   		if (direccio==Direction.BACKWARD) {
	   			g.drawImage(this.imgAvioHB, iniX, iniY, finX, finY, null);
			}
			else if(direccio==Direction.FORWARD){
				g.drawImage(this.imgAvioH, iniX, iniY, finX, finY, null);
			}
	   		
	   		g.drawString(idAvio, iniX, iniY);
	
   		}else if(carrerActual instanceof VCarrer || carrerActual instanceof Finger){
   			
   			iniY=(int)((((this.carrerActual.getCmIniY()+this.cmPosition))/factorY)+offsetY);
   	   		iniX=(int)((((this.carrerActual.getCmIniX()+this.carrerActual.getCmFinX())/2-400)/factorX)+offsetX);
   	   		
   			if (direccio==Direction.BACKWARD) {
	   			g.drawImage(this.imgAvioVB, iniX, iniY, finY, finX, null);
			}
			else if(direccio==Direction.FORWARD){
				g.drawImage(this.imgAvioV, iniX, iniY, finY, finX, null);
			}
	   		g.drawString(idAvio, iniX, iniY);
   		}
    }
	
	/*
	 * POSICIONS DE L'AVIO 
	 */
	
	public Rectangle posicioQueOcupare() {
		int iniX = 0, iniY = 0;
		if (carrerActual instanceof HCarrer) {
			if (direccio == Direction.FORWARD) {
				iniX = carrerActual.getCmPosIniX() + cmPosition + cmLong;
				iniY = (carrerActual.getCmFinY() + carrerActual.getCmIniY())/2 -400;
			} else if (direccio == Direction.BACKWARD) {
				iniX = carrerActual.getCmPosIniX() + cmPosition - cmLong;
				iniY = (carrerActual.getCmFinY() + carrerActual.getCmIniY())/2 -400;
			}
		}else if(carrerActual instanceof VCarrer || carrerActual instanceof Finger){
			if (direccio == Direction.FORWARD) {
				iniX = (carrerActual.getCmFinX() + carrerActual.getCmIniX())/2 -400;
				iniY =  carrerActual.getCmPosIniY() + cmPosition + cmLong;
			}else if(direccio == Direction.BACKWARD){
				iniX = (carrerActual.getCmFinX() + carrerActual.getCmIniX())/2 -400;
				iniY =  carrerActual.getCmPosIniY() + cmPosition - cmLong;
			}
		}
		return new Rectangle(iniX, iniY, cmLong, cmWidth);
	}
	
	public Rectangle posicioQueOcup() {
		int iniX = 0, iniY = 0;
   		
		if (carrerActual instanceof HCarrer) {
			iniX = carrerActual.getCmPosIniX() + cmPosition;
			iniY = (carrerActual.getCmFinY() + carrerActual.getCmIniY())/2 -400;
		}else if(carrerActual instanceof VCarrer || carrerActual instanceof Finger){
			iniX = (carrerActual.getCmFinX() + carrerActual.getCmIniX())/2 -400;
			iniY =  carrerActual.getCmPosIniY() + cmPosition;
		}
		return new Rectangle(iniX, iniY, cmLong, cmWidth);
	}
	    
    /*
     * GETTERS Y SETTERS
     */
    
    public Direction getDirection() {
		return direccio;
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

	public void setWay(Carrer carrer) {
		carrer.ocuparCarrer(this);	
		carrerActual = carrer;
	}

	public void setDirection(Direction direction) {
		this.direccio = direction;
	}

	public int getWidthInCm() {
		return this.cmWidth;
	}

	public void setPositionInCm(int cmPos) {
        this.cmPosition = cmPos;
		
	}
	
	public float getCmLong(){
		return this.cmLong;
	}
	
	public Estat getEstat(){
		return estat;
	}

}