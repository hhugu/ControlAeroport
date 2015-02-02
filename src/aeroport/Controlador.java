package aeroport;

import java.awt.Graphics;
import java.util.ArrayList;

import aeroport.Avio.Direction;
import aeroport.Finger.Estat;

public class Controlador implements Runnable{
	private Mapa mapa;
	private ArrayList <Avio> avions = new ArrayList<Avio>();
	private ArrayList <Carrer> carrers;
	private ArrayList<Finger> fingers;
	private ArrayList<String> rutaAlFingerOest, rutaDespegueOest, rutaAlFingerEst, rutaDespegueEst;
	private int maxAvions= 40, random;
	
	public Controlador(ArrayList<Carrer> carrers, ArrayList<Finger> fingers, Mapa mapa){
		this.carrers = carrers;
		this.fingers = fingers;
		this.mapa = mapa;
		crearRutes();
	}
	
	public void run() {
		Direction direccio = null;
		int vent;
		if(!Aeroport.isPaused()){
			for(int i=0; i<maxAvions; i++){
				vent = mapa.getVent();
				if(vent == 0) direccio = Direction.FORWARD;
				else if(vent ==1) direccio = Direction.BACKWARD;
				
				ArrayList<String> rutaAlFinger = triarRutaAlFinger(vent);
				ArrayList<String> rutaDespegue = triarRutaDespegue(vent);
				Finger finger = afegirFingerARuta(rutaAlFinger);
				
				try {
					addAvio(i+"", carrers.get(0), direccio, (ArrayList<String>)(rutaAlFinger.clone()), (ArrayList<String>)rutaDespegue.clone(), finger);
					rutaAlFinger.remove(rutaAlFinger.size()-1);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}

	/*
	 * RUTES
	 */
	private void crearRutes(){
		//Ruta Oest: Aterra i despega enrrera
		rutaAlFingerOest = new ArrayList<String>();
		rutaAlFingerOest.add(("iniciPista"));
		rutaAlFingerOest.add(("h1"));
		rutaAlFingerOest.add(("v1"));
		rutaAlFingerOest.add(("goFingers"));
		
		rutaDespegueOest = new ArrayList<String>();
		rutaDespegueOest.add(("goFingers"));
		rutaDespegueOest.add(("fiPista"));
		rutaDespegueOest.add(("pista"));
		//Ruta Est: Aterra i despega envant
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
	
	private Finger afegirFingerARuta(ArrayList<String> ruta) {
		Finger finger = getPrimerFingerBuit();
		while(finger == null){
			try {
				finger = getPrimerFingerBuit();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ruta.add((finger.idWay));

		return finger;
	}
	
	private ArrayList<String> triarRutaAlFinger(int vent) {
		switch(vent){
		case 0: return rutaAlFingerEst;
		case 1: return rutaAlFingerOest;
		}
		return null;
	}
	
	private ArrayList<String> triarRutaDespegue(int vent) {
		switch(vent){
		case 0: return rutaDespegueEst;
		case 1: return rutaDespegueOest;
		}
		return null;
	}
	
	/*
	 * AVIONS
	 */
	public ArrayList<Avio> getAvions(){
		return avions;
	}
	
	private void addAvio(String idavio, Carrer way, Direction direction, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue, Finger finger){
		Avio avio = new Avio(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegue);
		avions.add(avio);
		avio.start();
	}
	
	public void paintAvions(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		for(int i=0; i < avions.size(); i++){
			avions.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}
	
	public void borrarAvio(Avio avio){
		avions.remove(avio);
	}
	
	/*
	 * FINGERS
	 */
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
	
	/*
	 * VENT
	 */
	public Direction direccioDespegue(){
		//oest enrrera
		if(mapa.getVent() == 0) return Direction.FORWARD;
		else if(mapa.getVent() == 1) return Direction.BACKWARD;
		
		return null;
	}
}
