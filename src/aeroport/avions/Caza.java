package aeroport.avions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import aeroport.Aeroport;
import aeroport.Carrer;
import aeroport.Finger;
import aeroport.Finger.EstatFinguer;
import aeroport.avions.Avio.Direction;
import aeroport.avions.Avio.Estat;

public class Caza extends Avio {
	
   protected static final int ACCELERACIO = 20;
   protected static final int VEL_PISTA = 50;
   protected static final int VEL_VOL = 1000;

    public Caza(Aeroport aeroport, String idAvio, Carrer way, Direction direction, Finger finger, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue) {
    	super(aeroport, idAvio, way, direction, finger, rutaAlFinger, rutaDespegue);

        try {
			imgAvioH = ImageIO.read(new File("Imagenes/caza/cazaH.png"));
			imgAvioV = ImageIO.read(new File("Imagenes/caza/cazaV.png"));
			imgAvioHB = ImageIO.read(new File("Imagenes/caza/cazaHB.png"));
			imgAvioVB = ImageIO.read(new File("Imagenes/caza/cazaVB.png"));
		} catch (IOException e) {
		}
  
        speed = VEL_VOL;
        
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
    
    /**
     * Augmenta o disminueix el cmPosition en funcio de la direccio perque l'avio quedi dafora de la pista i 
     * paresqui que entra a aquesta.
     */
	private void calcularEntrada() {
		if (direccio == Direction.BACKWARD) cmPosition = carrerActual.getCmLong() + 8000;			
		else if(direccio == Direction.FORWARD) cmPosition = - 8000;		
	}

	/**
	 * Va reduint la velocitat en funcio de l'acceleracio que te l'avio. Quan la seva velocitat
	 * es menor que la maxima per circular dins l'aeroport cambia l'estat de l'avio a GOFINGER.
	 */
	private void aterrar(){
		speed -= ACCELERACIO;

		if (speed <= VEL_PISTA) estat = Estat.GOFINGER;
	}

	/**
	 * Va agumentant la velocitat en funcio de l'acceleracio que te l'avio. Quan la seva velocitat
	 * es major que la velocitat de vol cambia l'estat de l'avio a VOLANT.
	 */
	private void despegar() {
		speed += ACCELERACIO;
		
		if (speed > VEL_VOL && (cmPosition <= 0 || cmPosition >= carrerActual.getCmLong())) estat = Estat.VOLANT;		
	}
	
	/**
	 * Cambia l'estat del seu finger a ocupat. Simula una estada al finger, cambia l'estat de l'avio a 
	 * GOPISTA i el del finger a buit. 
	 */
	protected void descarregarCarregar() {
		aeroport.canviarEstatFinger(finger, EstatFinguer.ocupat, this);
		
		direccio = Direction.FORWARD;
		while((cmPosition + cmLong + 300) <= carrerActual.getCmLong()) avançar();					
		try {
			Thread.sleep((long)(Math.random()*10000+1000));
			
			while(aeroport.getCarrerCritic().getAvions().size() >= 4) Thread.sleep(Aeroport.SLEEP_TIME);
			
			cmPosition = finger.getCmLong() - cmLong;
			direccio = Direction.BACKWARD;
			estat = Estat.GOPISTA;
			aeroport.canviarEstatFinger(finger, EstatFinguer.buit, this);
			posicio = 0;
		} catch (InterruptedException e) {
		}		
	}
}