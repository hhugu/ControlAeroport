package aeroport;

import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;

import aeroport.Cotxe.Direction;

public class Cotxe extends Thread{
	public static enum Direction {FORWARD, BACKWARD}
	private int cmLong, cmWidth;
    private Image imgCotxeH, imgCotxeV, imgCotxeHB, imgCotxeVB;
    private String id;
    
    private ControlTrafic control;
    private volatile int cmPosition, speed;
    private volatile Carrer carrerActual;
    private CrossRoad cruceActual, proximCruce;
    private Direction direccio;
    
    public Cotxe(){
    	try {
    		imgCotxeH = ImageIO.read(new File("Imagenes/CotxeH.png"));
			imgCotxeV = ImageIO.read(new File("Imagenes/CotxeV.png"));
			imgCotxeHB = ImageIO.read(new File("Imagenes/CotxeHB.png"));
			imgCotxeVB = ImageIO.read(new File("Imagenes/CotxeVB.png"));
		} catch (Exception e) {
		}
    }
}
