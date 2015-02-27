package ciutat;

import java.awt.Graphics;
import java.util.ArrayList;

import aeroport.Carrer;
import aeroport.Finger;
import aeroport.Mapa;
import aeroport.Finger.EstatFinguer;
import aeroport.avions.Avio;
import aeroport.avions.AvioComercial;
import aeroport.avions.Caza;

public class ControlTrafic {
	
	private static final int MAX_COTXES = 20;
	private ArrayList <Carrer> carrers;
	private ArrayList <Cotxe> cotxes;
	private int j = 0;


	public ControlTrafic(Mapa mapa){
		carrers = mapa.getCarrers();
		cotxes = new ArrayList<Cotxe>();
	}
	
	/**
	 * Helper per obtenir el primer parking buit. Un cop obtes el reserva per el cotxe.
	 * @return - parkingDisponible - Finger
	 */
	private Finger getPrimerParkingBuit(){
		Finger fingerDisponible;
		for (int i = 1; i < carrers.size(); i++) {
			fingerDisponible = (Finger) carrers.get(i);
			if (fingerDisponible.estaDisponible()) {
				this.canviarEstatParking(fingerDisponible, EstatFinguer.reservat);
				return (Finger) carrers.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Helper per canviar l'estat a un parking.
	 * @param parking - Finger
	 * @param estat - EstatFinger
	 */
	public synchronized void canviarEstatParking(Finger parking, EstatFinguer estat){
		for (int i = 1; i < carrers.size(); i++) {
			if (carrers.get(i).equals(parking)) {
				((Finger) carrers.get(i)).setEstat(estat);
			}
		}
	}

	public void paintCotxes(Graphics g, float factorX, float factorY, int offsetX, int offsetY){
		for (int i = 0; i < cotxes.size(); i++) {
			cotxes.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}

	public void addCotxe(Avio avio){
		int random;
		if (avio instanceof Caza) {
			random =(int)(Math.random()*3);		

			for (int i = 0; i < random; i++) {
				if (cotxes.size() < MAX_COTXES) {
					Finger finger = getPrimerParkingBuit();
					Cotxe cotxe = new Cotxe(this, "cotxe" + j, carrers.get(0), finger);
					cotxe.start();
					cotxes.add(cotxe);
					j++;
				}
			}
		}else if(avio instanceof Avio || avio instanceof AvioComercial){
			random =(int)(Math.random()*3);		

			for (int i = 0; i < random; i++) {
				if (cotxes.size() < MAX_COTXES) {
					Finger finger = getPrimerParkingBuit();
					Bus bus = new Bus(this, "bus"+j, carrers.get(0), finger);
					bus.start();
					cotxes.add(bus);
					j++;
				}
			}
		}
	}

	public void deleteCotxe(Cotxe cotxe) {
		cotxes.remove(cotxe);
		cotxe.stop();
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
