package vertx;

import java.util.Calendar;		
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

	private Map<Integer, Pistas> pistas = new HashMap<Integer, Pistas>();
	private Gson gson;

	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		createSomeData(25);

		// Instantiating a Gson serialize object using specific date format
		//gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create(); no hay fecha

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api/pistas*").handler(BodyHandler.create());
		router.get("/api/pistas").handler(this::getAllWithParams);
		router.get("/api/pistas/:pistasId").handler(this::getOne);
		router.post("/api/pistas").handler(this::addOne);
		router.delete("/api/pistas/:pistasId").handler(this::deleteOne);
		router.put("/api/pistas/:pistasId").handler(this::putOne);
	}

	@SuppressWarnings("unused")
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(pistas.values()));
	}

	private void getAllWithParams(RoutingContext routingContext) {
		final String termometroId =routingContext.queryParams().contains("termometroId") ? 
				routingContext.queryParam("termometroId").get(0) : null;
		
		final String nombre = routingContext.queryParams().contains("nombre") ? 
				routingContext.queryParam("nombre").get(0) : null;
		final String longitud = routingContext.queryParams().contains("longitud") ? 
				routingContext.queryParam("longitud").get(0) : null;
		final String fecha = routingContext.queryParams().contains("fecha") ? 
				routingContext.queryParam("fecha").get(0) : null;
		
		final String apertura = routingContext.queryParams().contains("apertura") ? 
				routingContext.queryParam("apertura").get(0) : null;
		
		final String capacidadMax = routingContext.queryParams().contains("capacidadMax") ? 
				routingContext.queryParam("capacidadMax").get(0) : null;
		
		final String dificultad = routingContext.queryParams().contains("dificultad") ? 
				routingContext.queryParam("dificultad").get(0) : null;
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(pistas.values().stream().filter(elem -> {
					boolean res = true;
					res = res && termometroId != null ? elem.getTermometroId().equals(Integer.parseInt(termometroId)) : true;
					res = res && nombre != null ? elem.getNombre().equals(nombre) : true;
					res = res && longitud != null ? elem.getLongitud().equals(Integer.parseInt(longitud)) : true;
					res = res && fecha != null ? elem.getFecha().equals(Long.parseLong(fecha)) : true;
					res = res && apertura != null ? elem.getApertura().equals(Boolean.parseBoolean(apertura)) : true;
					res = res && capacidadMax != null ? elem.getCapacidadMax().equals(Integer.parseInt(capacidadMax)) : true;
					res = res && dificultad != null ? elem.getDificultad().equals(dificultad) : true;
					
					
					
					return res;
				}).collect(Collectors.toList())));
	}

	private void getOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("pistasId"));
		if (pistas.containsKey(id)) {
			Pistas ds = pistas.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOne(RoutingContext routingContext) {
		final Pistas pista = gson.fromJson(routingContext.getBodyAsString(), Pistas.class);
		pistas.put(pista.getPistasId(), pista);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(pista));
	}

	private void deleteOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("pistasId"));
		if (pistas.containsKey(id)) {
			Pistas pista = pistas.get(id);
			pistas.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(pista));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("pistasId"));
		Pistas ds = pistas.get(id);
		final Pistas element = gson.fromJson(routingContext.getBodyAsString(), Pistas.class);
		ds.setTermometroId(element.getTermometroId());
		ds.setNombre(element.getNombre());
		ds.setLongitud(element.getLongitud());
		ds.setFecha(element.getFecha());
		ds.setApertura(element.getApertura());
		pistas.put(ds.getPistasId(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
// Cambiar
	private void createSomeData(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			int term_id= rnd.nextInt();
			Boolean apertura= rnd.nextBoolean();
			pistas.put(id, new Pistas(id, term_id + id, "Rio" + id, 12000 + id,
					 (long) (20 + id), apertura , 1000 + id, "Verde" + id));
		});
	}

}
