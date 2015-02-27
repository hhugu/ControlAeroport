package ciutat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.CrossRoad;
import aeroport.Finger;
import aeroport.HCarrer;
import aeroport.VCarrer;
import aeroport.Finger.EstatFinguer;
import aeroport.avions.Avio;
import aeroport.avions.Avio.Direction;

public class Cotxe extends Thread{
	public static enum Estat {CAPALPARKING, DINSELPARKING, CAPACASES}
	private int cmLong, cmWidth;
    protected Image imgCotxeH;
	protected Image imgCotxeV;
	protected Image imgCotxeHB;
	protected Image imgCotxeVB;
    private String idCotxe;
    
    private ControlTrafic controlador;
    protected volatile int cmPosition, speed;
    private volatile Carrer carrerActual, carrerPrincipal;
    private CrossRoad cruceActual;
    private aeroport.avions.Avio.Direction direccio;
    private Estat estat;
	private Finger parking;
    
    public Cotxe(ControlTrafic controlador, String idCotxe, Carrer carrer, Finger parking){
    	this.controlador = controlador;
    	this.idCotxe = idCotxe;
    	
    	try {
    		imgCotxeH = ImageIO.read(new File("Imagenes/cotxe/cotxeH.png"));
			imgCotxeV = ImageIO.read(new File("Imagenes/cotxe/cotxeV.png"));
			imgCotxeHB = ImageIO.read(new File("Imagenes/cotxe/cotxeHB.png"));
			imgCotxeVB = ImageIO.read(new File("Imagenes/cotxe/cotxeVB.png"));
		} catch (Exception e) {
		}
    	
    	cmLong = cmWidth = 1000;
        
        speed = 50;

        carrerPrincipal = carrerActual = carrer;
        this.parking = parking;
        setDirection(Direction.FORWARD);
        
    }
    
    public void run(){
		setWay(carrerActual);		
		estat = Estat.CAPALPARKING;
		calcularEntrada();

		while(!Aeroport.isEnd()){
			if (!Aeroport.isPaused()) {
				avançar();
				
				if (estat == Estat.CAPALPARKING) cambiarDeCarrer(parking);
				else if (estat == Estat.DINSELPARKING) recollirPassatgers();
				else if (estat == Estat.CAPACASES) {
					cambiarDeCarrer(carrerPrincipal);
					if (cmPosition > carrerActual.getCmLong()) {
						controlador.deleteCotxe(this);
					}
				}
			}
		}
	}

	private void recollirPassatgers() {
		cmPosition = carrerActual.getCmLong();
		controlador.canviarEstatFinger(parking, EstatFinguer.ocupat);
		while(cmPosition > 0) avançar();
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
		}
		
		direccio = Direction.FORWARD;
		estat = Estat.CAPACASES;
		controlador.canviarEstatFinger(parking, EstatFinguer.buit);

	}

	private void avançar() {
		if (controlador.pucAvançar(this)) {
			if (direccio == Direction.BACKWARD) cmPosition -= speed;
			else if (direccio == Direction.FORWARD)	cmPosition += speed;
			
			if (estat != Estat.CAPACASES) {
				if (cmPosition < 0)	direccio = Direction.FORWARD;
				else if (cmPosition > carrerActual.getCmLong()) direccio = Direction.BACKWARD;
			}
		}
		try {
			Thread.sleep(Aeroport.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}
	
	private void cambiarDeCarrer(Carrer proximCarrer){
		if (carrerActual.insideAnyCrossRoad(cmPosition)){
			cruceActual = carrerActual.intersectedCrossRoad(cmPosition);
			if (cruceActual.getCarrer(carrerActual).equals(proximCarrer)) {
				Carrer carrerAnterior = carrerActual;
				setWay(cruceActual.getCarrer(carrerActual));
				
				if (carrerActual instanceof Finger) estat = Estat.DINSELPARKING;

				cmPosition = carrerActual.getCmPosition(
					       carrerAnterior.getCmPosX(cmPosition, direccio),
						   carrerAnterior.getCmPosY(cmPosition, direccio),
						   direccio);
			}
		}
	}
	
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
	
	public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {		
		g.setColor(Color.RED);
		int iniX = 0, iniY = 0, finX, finY;
		
   		finY=(int)(((this.cmWidth)/factorY));
   		finX=(int)(((this.cmLong)/factorX));
   		
   		if(carrerActual instanceof HCarrer){
   			
   	   		iniX=(int)((((this.carrerActual.getCmIniX()+this.cmPosition))/factorX)+offsetX);
   	   		iniY=(int)((((this.carrerActual.getCmFinY()+this.carrerActual.getCmIniY())/2-400)/factorY)+offsetY);
   	   		
	   		if (direccio==Direction.BACKWARD) {
	   			g.drawImage(this.imgCotxeHB, iniX, iniY, finX, finY, null);
			}
			else if(direccio==Direction.FORWARD){
				g.drawImage(this.imgCotxeH, iniX, iniY, finX, finY, null);
			}
	   		
	   		g.drawString(idCotxe, iniX, iniY);

	   		
   		}else if(carrerActual instanceof VCarrer || carrerActual instanceof Finger){
   			
   			iniY=(int)((((this.carrerActual.getCmIniY()+this.cmPosition))/factorY)+offsetY);
   	   		iniX=(int)((((this.carrerActual.getCmIniX()+this.carrerActual.getCmFinX())/2-400)/factorX)+offsetX);
   	   		
   			if (direccio==Direction.BACKWARD) {
	   			g.drawImage(this.imgCotxeVB, iniX, iniY, finY, finX, null);
			}
			else if(direccio==Direction.FORWARD){
				g.drawImage(this.imgCotxeV, iniX, iniY, finY, finX, null);
			}
	   		g.drawString(idCotxe, iniX, iniY);
   		}
	}

	private void calcularEntrada() {
		cmPosition = carrerActual.getEntryPoint(direccio);
	}

	public void setWay(Carrer carrer) {
		carrerActual = carrer;
	}

	public void setDirection(aeroport.avions.Avio.Direction direccio) {
		this.direccio = direccio;
	}
	
	public Estat getEstat() {
		return this.estat;
	}
}
