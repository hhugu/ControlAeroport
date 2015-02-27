package ciutat;

import java.io.File;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.Finger;

public class Bus extends Cotxe{

	public Bus(ControlTrafic controlador, String idBus, Carrer carrer, Finger parking) {
		super(controlador, idBus, carrer, parking);
		try {
    		imgCotxeH = ImageIO.read(new File("Imagenes/bus/busH.png"));
			imgCotxeV = ImageIO.read(new File("Imagenes/bus/busV.png"));
			imgCotxeHB = ImageIO.read(new File("Imagenes/bus/busHB.png"));
			imgCotxeVB = ImageIO.read(new File("Imagenes/bus/busVB.png"));
		} catch (Exception e) {
		}
    	        
        speed = 30;
	}

}
