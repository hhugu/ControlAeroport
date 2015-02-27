package aeroport.avions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.Finger;

public class AvioComercial extends Avio{

	 protected static final int ACCELERACIO = 5;
	 protected static final int VEL_PISTA = 25;
	 protected static final int VEL_VOL = 500;
	
	 public AvioComercial(Aeroport aeroport, String idAvio, Carrer way,Direction direction, Finger finger, 
			ArrayList<String> rutaAlFinger,	ArrayList<String> rutaDespegue) {
		super(aeroport, idAvio, way, direction, finger, rutaAlFinger, rutaDespegue);
		
		try {
			imgAvioH = ImageIO.read(new File("Imagenes/avioComercial/comercialH.png"));
			imgAvioV = ImageIO.read(new File("Imagenes/avioComercial/comercialV.png"));
			imgAvioHB = ImageIO.read(new File("Imagenes/avioComercial/comercialHB.png"));
			imgAvioVB = ImageIO.read(new File("Imagenes/avioComercial/comercialVB.png"));
		} catch (IOException e) {
		}
	}
}
