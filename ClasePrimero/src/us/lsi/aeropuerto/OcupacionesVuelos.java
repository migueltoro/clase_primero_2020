package us.lsi.aeropuerto;

import static us.lsi.tools.StreamTools.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import us.lsi.tools.FileTools;

public class OcupacionesVuelos {

	public static List<OcupacionVuelo> ocupaciones;

	public static void random(Integer numOcupaciones, Integer anyo) {
		OcupacionesVuelos.ocupaciones = toList(
				IntStream.range(0, numOcupaciones).boxed().map(e -> OcupacionVuelo.random(Vuelo.random(), anyo)));
	}

	public static void leeFicheroOcupaciones(String fichero) {
		OcupacionesVuelos.ocupaciones = FileTools.streamFromFile(fichero)
				.map(x -> OcupacionVuelo.parse(x))
				.collect(Collectors.toList());
	}

	// Dada una cadena de caracteres s devuelve el n�mero total de pasajeros a
	// ciudades destino que tienen
	// como prefijo s (esto es, comienzan por s).

	public static Integer numeroDepasajeros(String prefix) {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.vuelo().ciudadDestino().startsWith(prefix))
				.mapToInt(v -> v.numPasajeros())
				.sum();
	}

	// Dado un conjunto de ciudades destino s y una fecha f devuelve cierto si
	// existe un vuelo en la fecha f con destino en s.

	public static Boolean hayDestino(Set<String> destinos, LocalDate f) {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.fecha().toLocalDate().equals(f))
				.anyMatch(ocp -> destinos.contains(ocp.vuelo().ciudadDestino()));
	}

	// Dada una fecha f devuelve el conjunto de ciudades destino diferentes de todos
	// los vuelos de fecha f

	public static Set<String> destinosDiferentes(LocalDate f) {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.fecha().toLocalDate().equals(f))
				.map(ocp -> ocp.vuelo().ciudadDestino())
				.collect(Collectors.toSet());
	}

	// Dado una entero anyo devuelve un SortedMap que relacione cada destino con el
	// total de pasajeros a ese destino en el a�o anyo

	public static SortedMap<String, Integer> totalPasajerosADestino(Integer a) {
		return OcupacionesVuelos.ocupaciones.stream().filter(ocp -> ocp.fecha().getYear() == a)
				.collect(Collectors.groupingBy(ocp -> ocp.vuelo().ciudadDestino(),
						() -> new TreeMap<String, Integer>(Comparator.reverseOrder()),
						Collectors.summingInt(ocp -> ocp.numPasajeros())));
	}

	// Dado un destino devuelve el c�digo del primer vuelo con plazas libres a ese
	// destino

	public static String primerVuelo(String destino) {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.vuelo().ciudadDestino().equals(destino)
						&& ocp.vuelo().numPlazas() > ocp.numPasajeros())
				.filter(ocp -> ocp.fecha().isAfter(LocalDateTime.now()))
				.min(Comparator.comparing(OcupacionVuelo::fecha)).get().vuelo().codigoEmpresa();

	}

	// Devuelve para los vuelos completos un Map que haga corresponder a cada ciudad
	// destino la media de los precios de los vuelos a ese destino.

	public static Map<String, Double> precioMedio() {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.numPasajeros().equals(ocp.vuelo().numPlazas()))
				.collect(Collectors.groupingBy(ocp -> ocp.vuelo().ciudadDestino(),
						Collectors.averagingDouble(ocp -> ocp.vuelo().precio())));
	}

	// Devuelve un Map tal que dado un entero n haga corresponder
	// a cada fecha la lista de los n destinos con los vuelos de mayor duraci�n.

	public static Map<LocalDate, List<String>> destinosConMayorDuracion(Integer n) {
		return OcupacionesVuelos.ocupaciones.stream()
				.collect(Collectors.groupingBy(oc -> oc.fecha().toLocalDate(),
						Collectors.collectingAndThen(Collectors.toList(),
								g -> g.stream().sorted(Comparator.comparing(ocp -> ocp.vuelo().duracion()))
										.map(ocp -> ocp.vuelo().ciudadDestino()).collect(Collectors.toList()))));
	}

	// Dada una fecha f devuelve el precio medio de los vuelos con salida posterior
	// a f. Si no hubiera vuelos devuelve 0.0

	public static Double precioMedio(LocalDateTime f) {
		return OcupacionesVuelos.ocupaciones.stream()
				.filter(ocp -> ocp.fecha().isAfter(f))
				.mapToDouble(ocp -> ocp.vuelo().precio())
				.average()
				.orElse(0.0);
	}

	// Devuelve un Map que haga corresponder a cada destino un conjunto con las
	// fechas de los vuelos a ese destino.

	public static Map<String, Set<LocalDate>> fechasADestino() {
		return OcupacionesVuelos.ocupaciones.stream()
				.collect(Collectors.groupingBy(ocp -> ocp.vuelo().ciudadDestino(),
						Collectors.mapping(OcupacionVuelo::fechaSalida, Collectors.toSet())));
	}

	// 17. Devuelve un Map que haga corresponder a cada destino el n�mero de fechas
	// distintas en las que hay vuelos a ese destino.

	public static Map<String, Integer> fechasDistintas() {
		return OcupacionesVuelos.ocupaciones.stream().collect(
				Collectors.groupingBy(ocp -> ocp.vuelo().ciudadDestino(), Collectors.mapping(OcupacionVuelo::fecha,
						Collectors.collectingAndThen(Collectors.toSet(), s -> s.size()))));
	}

	// Otra forma de resolver el anterior

	public static Map<String, Integer> fechasDistintas2() {
		return OcupacionesVuelos.ocupaciones.stream()
				.collect(Collectors.groupingBy(ocp -> ocp.vuelo().ciudadDestino(), Collectors.collectingAndThen(
						Collectors.toList(), g -> (int) g.stream().map(OcupacionVuelo::fecha).distinct().count())));
	}

}