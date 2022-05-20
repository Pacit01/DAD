package vertx;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttClientVerticle extends AbstractVerticle {
	
	Gson gson;

	public void start(Promise<Void> startFuture) {
		gson = new Gson();
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "localhost", s -> {

			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Suscripción " + mqttClient.clientId());
				}
			});

			mqttClient.publishHandler(handler -> {
				System.out.println("Mensaje recibido:");
				System.out.println("    Topic: " + handler.topicName().toString());
				System.out.println("    Id del mensaje: " + handler.messageId());
				System.out.println("    Contenido: " + handler.payload().toString());
				/**
				try {
				Pistas sc = gson.fromJson(handler.payload().toString(), Pistas.class);
				System.out.println("    Pistas: " + sc.toString());
				}catch (JsonSyntaxException e) {
					System.out.println("    No es una SimpleClass. ");
				}  **/
				//Lo sustituyo por otra forma de hacerlo
				if(handler.topicName().equals("topic_2")) {
					//Tipo_sensor sensor = gson.fromJson(message.payload().toString(), Tipo_sensor.class);
					//System.out.println(sensor.toString());
					System.out.println("Aqui deberia de haber un sensor bro");
				}
			});
			mqttClient.publish("topic_1", Buffer.buffer("Ejemplo"), MqttQoS.AT_LEAST_ONCE, false, false);
		}else { 
			System.out.println("Error en la conexión con el broker");
			
		}
	});

	}

}
