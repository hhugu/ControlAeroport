package aeroport.avions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.Finger;

public class Caza extends Avio{
	
	 protected static final int ACCELERACIO = 20;
	 protected static final int VEL_PISTA = 50;
	 protected static final int VEL_VOL = 1000;
	 
	public Caza(Aeroport aeroport, String idAvio, Carrer way, Direction direction, Finger finger, ArrayList<String> rutaAlFinger,
			ArrayList<String> rutaDespegue) {
		super(aeroport, idAvio, way, direction, finger, rutaAlFinger, rutaDespegue);
		
		try {
			imgAvioH = ImageIO.read(new File("Imagenes/caza/cazaH.png"));
			imgAvioV = ImageIO.read(new File("Imagenes/caza/cazaV.png"));
			imgAvioHB = ImageIO.read(new File("Imagenes/caza/cazaHB.png"));
			imgAvioVB = ImageIO.read(new File("Imagenes/caza/cazaVB.png"));
		} catch (IOException e) {
		}
	}
}
