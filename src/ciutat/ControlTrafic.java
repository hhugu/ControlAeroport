package ciutat;

import java.awt.Graphics;
import java.util.ArrayList;

import aeroport.Carrer;
import aeroport.Finger;
import aeroport.Mapa;
import aeroport.Finger.EstatFinguer;

public class ControlTrafic {
	
	private static final int MAX_COTXES = 10;
	private ArrayList <Carrer> carrers;
	private ArrayList <Cotxe> cotxes;
	private int j = 0;


	public ControlTrafic(Mapa mapa){
		carrers = mapa.getCarrers();
		cotxes = new ArrayList<Cotxe>();
	}
	
	/**
	 * Helper per obtenir el primer finger buit. Un cop obtes el reserva per l'avio.
	 * @return - fingerDisponible - Finger
	 */
	private Finger getPrimerFingerBuit(){
		Finger fingerDisponible;
		for (int i = 1; i < carrers.size(); i++) {
			fingerDisponible = (Finger) carrers.get(i);
			if (fingerDisponible.estaDisponible()) {
				this.canviarEstatFinger(fingerDisponible, EstatFinguer.reservat);
				return (Finger) carrers.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Helper per canviar l'estat a un figuer. Si el finguer es buida notifica a qui espera que n'hi ha un de buit.
	 * @param finger - Finger
	 * @param estat - EstatFinger
	 */
	public synchronized void canviarEstatFinger(Finger finger, EstatFinguer estat){
		for (int i = 1; i < carrers.size(); i++) {
			if (carrers.get(i).equals(finger)) {
				((Finger) carrers.get(i)).setEstat(estat);
			}
		}
	}

	public void paintCotxes(Graphics g, float factorX, float factorY, int offsetX, int offsetY){
		for (int i = 0; i < cotxes.size(); i++) {
			cotxes.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}

	public void addCotxe(){
		int random=(int)(Math.random()*6);		
		if (random > 3) {
			for (int i = 3; i < random; i++) {
				Finger finger = getPrimerFingerBuit();
				Bus bus = new Bus(this, "bus"+j, carrers.get(0), finger);
				bus.start();
				cotxes.add(bus);
				j++;
			}
		}else{
			for (int i = 0; i < random; i++) {
				if (cotxes.size() < MAX_COTXES) {
					Finger finger = getPrimerFingerBuit();
					Cotxe cotxe = new Cotxe(this, "cotxe" + j, carrers.get(0), finger);
					cotxe.start();
					cotxes.add(cotxe);
					j++;
				}
			}
		}
	}

	public void deleteCotxe(Cotxe cotxe) {
		cotxes.remove(cotxe);
	}
	
	/**
	 * Metode per sabre si si quan avanci l'avio tocara a un altre.
	 * @param cotxeQueDemanaPermis - Avio
	 * @return boolean - True si pot avançar, false si tocara a cualque avio.
	 */
	public boolean pucAvançar(Cotxe cotxeQueDemanaPermis){
		Cotxe cotxeQueSeMou;
		for (int i = 0; i < cotxes.size(); i++) {
			cotxeQueSeMou = cotxes.get(i);
			if (!cotxeQueDemanaPermis.equals(cotxeQueSeMou) && cotxeQueSeMou.getEstat() != null) {
				if(cotxeQueDemanaPermis.posicioQueOcupare().intersects(cotxeQueSeMou.posicioQueOcup())) return false;
			}
		}
		return true;
	}
}
