package Vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle{
	
	public void start(Future<Void> startFuture) {
		vertx.createHttpServer().requestHandler(
				request ->{
					request.response().end("hola ");// gestiona una peticion, enviando un codigo en este caso no es nada 
		}).listen(8082, result->{
			if(result.succeeded()) {
				System.out.println("Todo correcto");
			}else {
				System.out.println(result.cause());
			}
		});
		
		vertx.deployVerticle(ApiRest.class.getName());
	}
}
