package aeroport;

import java.awt.Graphics;
import java.util.ArrayList;

import aeroport.Avio.Direction;
import aeroport.Finger.Estat;

public class Controlador implements Runnable{
	private ArrayList <Avio> avions = new ArrayList<Avio>();
	private ArrayList <Carrer> carrers;
	private ArrayList<Finger> fingers;
	private ArrayList<String> rutaAlFingerOest, rutaDespegueOest, rutaAlFingerEst, rutaDespegueEst;
	private int maxAvions= 3, random;
	
	public Controlador(ArrayList<Carrer> carrers, ArrayList<Finger> fingers){
		this.carrers = carrers;
		this.fingers = fingers;
		
		crearRutes();
	}
	
	public void run() {
		Direction direccio;
		if(!Aeroport.isPaused()){
			for(int i=0; i<maxAvions; i++){
				random = (int) (Math.random()*1); //Cambiar a 2 cuan pugui
				if (random == 0) direccio = Direction.BACKWARD;
				else direccio = Direction.FORWARD;
				
				ArrayList<String> rutaAlFinger = triarRutaAlFinger(random);
				Finger finger = afegirFingerARuta(rutaAlFinger);
				
				try {
					addAvio(i+"", carrers.get(0), direccio, (ArrayList<String>)(rutaAlFinger.clone()), finger);
					rutaAlFingerOest.remove(rutaAlFingerOest.size()-1);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private ArrayList<String> triarRutaAlFinger(int vent) {
		switch(vent){
		case 0: return rutaAlFingerOest;
		case 1: return rutaAlFingerEst;
		}
		return null;
	}

	private synchronized Finger afegirFingerARuta(ArrayList<String> ruta) {
		Finger finger = getPrimerFingerBuit();
		while(finger == null){
			try {
				System.out.println("Estan tots plens");
				finger = getPrimerFingerBuit();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ruta.add((finger.idWay));

		return finger;
	}

	private void crearRutes(){
		rutaAlFingerOest = new ArrayList<String>();
		rutaAlFingerOest.add(("iniciPista"));
		rutaAlFingerOest.add(("h1"));
		rutaAlFingerOest.add(("v1"));
		rutaAlFingerOest.add(("goFingers"));
		
		rutaDespegueOest = new ArrayList<String>();
		rutaDespegueOest.add(("goFingers"));
		rutaDespegueOest.add(("fiPista"));
		rutaDespegueOest.add(("pista"));
		
		rutaAlFingerEst = new ArrayList<String>();
		rutaAlFingerEst.add("fiPista");
		rutaAlFingerEst.add("h2");
		rutaAlFingerEst.add("iniciPista");
		rutaAlFingerEst.add(("h1"));
		rutaAlFingerEst.add(("v1"));
		rutaAlFingerEst.add(("goFingers"));
		
		rutaDespegueEst = new ArrayList<String>();
		rutaDespegueEst.add(("goFingers"));
		rutaDespegueEst.add(("fiPista"));
		rutaDespegueEst.add("h2");
		rutaDespegueEst.add("iniciPista");
		rutaDespegueEst.add(("pista"));
		
	}

	public ArrayList<Avio> getAvions(){
		return avions;
	}
	
	private void addAvio(String idavio, Carrer way, Direction direction, ArrayList<String> rutaAlFinger, Finger finger){
		Avio avio = new Avio(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegueOest);
		avions.add(avio);
		avio.start();
	}

	private Finger getPrimerFingerBuit(){
		Finger finger;
		for (int i = 0; i < fingers.size(); i++) {
			finger = fingers.get(i);
			if (finger.estaDisponible()) {
				this.canviarEstatFinger(finger, Estat.reservat);
				return fingers.get(i);
			}
		}
		return null;
	}
	
	public void canviarEstatFinger(Finger finger, Estat estat){
		for (int i = 0; i < fingers.size(); i++) {
			if (fingers.get(i).equals(finger)) {
				fingers.get(i).setEstat(estat);
			}
		}
	}
	
	public void paintAvions(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		for(int i=0; i < avions.size(); i++){
			avions.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}
}