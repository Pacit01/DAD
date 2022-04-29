package es.us.lsi.dad;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
	
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class RestServerTermometro extends AbstractVerticle {

	private Map<Integer, Termometro> sensors = new HashMap<Integer, Termometro>();
	private Gson gson;

	public void start(Promise<Void> startFuture) {

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router::handle).listen(80, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api*").handler(BodyHandler.create());

		router.get("/api/sensors").handler(this::getTermometro);
		router.get("/api/sensors/:sensorid").handler(this::getTermometroById);
		router.post("/api/sensors").handler(this::addOneSensor);
		router.delete("/api/sensors/:sensorid").handler(this::deleteOneSensor);
		router.put("/api/sensors/:sensorid").handler(this::putOneSensor);

		// Lanzar el nuevo verticle
		// RestServer2 server2 = new RestServer2();
		// server2.setHttpServer(httpServer);
		// vertx.deployVerticle(server2);

	}

	private void getTermometro(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
				.end(gson.toJson(sensors.values()));
	}
	
	private void getTermometroById(RoutingContext routingContext) {
		Termometro sensor = null;
		int param = Integer.parseInt(routingContext.request().getParam("sensorid"));
		if (sensors.containsKey(param)) {
			
				mySqlClient.query("SELECT * FROM dad_database.termometro;").execute(res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<Termometro> result = new ArrayList();
						for (Row elem : resultSet) {
							result.add(new Termometro(elem.getInteger("temperaturaId"),
									elem.getDouble("temperatura"),elem.getDouble("humedad"))));
						}
						routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(result);	
						System.out.println(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
					}
				});
			}
			
			
			
			
			
	
	private void addOneSensor(RoutingContext routingContext) {
		final Termometro sensor = gson.fromJson(routingContext.getBodyAsString(), Termometro.class);
		sensors.put(sensor.getTermometroId(), sensor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(sensor));
	}

	private void deleteOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("sensorid"));
		if (sensors.containsKey(id)) {
			Termometro sensor = sensors.get(id);
			sensors.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(sensor));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("sensorid"));
		Termometro ds = sensors.get(id);
		final Termometro element = gson.fromJson(routingContext.getBodyAsString(), Termometro.class);
		ds.setTermometroId(element.getTermometroId());
		ds.setTemperatura(element.getTemperatura());
		ds.setHumedad(element.getHumedad());
		sensors.put(ds.getTermometroId(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
}