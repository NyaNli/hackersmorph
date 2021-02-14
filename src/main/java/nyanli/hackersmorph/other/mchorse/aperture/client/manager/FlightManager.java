package nyanli.hackersmorph.other.mchorse.aperture.client.manager;

import java.lang.reflect.Field;
import java.util.WeakHashMap;

import javax.vecmath.Vector3d;

import mchorse.aperture.client.gui.Flight;
import nyanli.hackersmorph.HackersMorph;

public class FlightManager {
	
	private static final WeakHashMap<Flight, Vector3d> lastCenter = new WeakHashMap<>();
	private static final WeakHashMap<Flight, Float> lastDistance = new WeakHashMap<>();
	
	private static boolean supportOrbit = false;
	
	private static Field centerPos;
	private static Field distance;
	private static Field mode;
	
	private static Field vertical; // it used to support 1.4.2
	
	static {
		try {
			centerPos = Flight.class.getDeclaredField("lastPosition");
			centerPos.setAccessible(true);
			distance = Flight.class.getDeclaredField("distance");
			distance.setAccessible(true);
			mode = Flight.class.getDeclaredField("type");
			mode.setAccessible(true);
			supportOrbit = true;
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				vertical = Flight.class.getDeclaredField("vertical");
				supportOrbit = false;
			} catch (NoSuchFieldException | SecurityException e1) {
				HackersMorph.throwCommonException(e1);
			}
		}
	}
	
	public static Mode getModeAndUpdate(Flight flight) {
		try {
			if (supportOrbit) {
				if ("VERTICAL".equals(mode.get(flight).toString()))
					return Mode.V;
				else if ("ORBIT".equals(mode.get(flight).toString())) {
					if (!lastCenter.containsKey(flight))
						lastCenter.put(flight, new Vector3d());
					Vector3d current = (Vector3d) centerPos.get(flight);
					Vector3d last = lastCenter.get(flight);
					if (!last.equals(current)) {
						last.set(current); // Updated
						return Mode.V;
					} else
						return Mode.O;
				} else
					return Mode.H;
			} else {
				return vertical.getBoolean(flight) ? Mode.V : Mode.H;
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			HackersMorph.throwCommonException(e);
			return null; // Dead code
		}
	}
	
	public static float getLastDistanceAndUpdate(Flight flight) {
		try {
			if (supportOrbit) {
				float dist = distance.getFloat(flight);
				float last = lastDistance.getOrDefault(flight, dist);
				lastDistance.put(flight, dist);
				return last;
			} else
				return 0;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			HackersMorph.throwCommonException(e);
			return 0; // Dead code
		}
	}
	
	public static void clearLast(Flight flight) {
		lastDistance.remove(flight);
		lastCenter.remove(flight);
	}
	
	public static enum Mode {
		H, V, O
	}

}
