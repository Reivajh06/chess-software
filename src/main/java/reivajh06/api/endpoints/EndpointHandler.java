package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import reivajh06.Json;
import reivajh06.api.ApiException;
import reivajh06.api.ports.EndpointOperator;
import reivajh06.api.ports.InputDeserializer;
import reivajh06.api.ports.OutputSerializer;

public class EndpointHandler<I, O> implements Handler {

	protected InputDeserializer<I> inputDeserializer;
	protected OutputSerializer<O> outputSerializer;
	protected EndpointOperator<I, O> endpointOperator;

	public EndpointHandler(InputDeserializer<I> inputDeserializer, OutputSerializer<O> outputSerializer, EndpointOperator<I, O> endpointOperator) {
		this.inputDeserializer = inputDeserializer;
		this.outputSerializer = outputSerializer;
		this.endpointOperator = endpointOperator;
	}

	@Override
	public void handle(Context ctx) {
		String result;

		try {
			I request = inputDeserializer.deserialize(ctx.pathParamMap(), ctx.body());
			O operatorResult = endpointOperator.operate(request);
			result = outputSerializer.serialize(operatorResult);

		} catch(ApiException e) {
			ctx.status(e.errorCode());
			return;
		}

		ctx.result(result);
	}
}
